package me.func

import dev.implario.games5e.QueueProperties
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.NoopGameNode
import dev.implario.games5e.packets.PacketOk
import dev.implario.games5e.packets.PacketQueueEnter
import dev.implario.games5e.packets.PacketQueueState
import dev.xdark.feder.NetUtil
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import me.func.battlepass.quest.ArcadeType
import me.func.mod.Anime
import me.func.mod.conversation.ModTransfer
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.lib.Futures
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmInfo
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import java.util.*
import java.util.concurrent.TimeUnit

object Games5e {

    val client = CoordinatorClient(NoopGameNode())
    private val online: MutableMap<UUID, Int> = HashMap()

    init {
        client.listenQueues()
        client.enable()

        Bukkit.getScheduler().runTaskTimer(app, {
            Bukkit.getOnlinePlayers().forEach { player ->
                ArcadeType.values().filter { it.queue.isNotEmpty() }.forEach {
                    val count = client.queueOnline[UUID.fromString(it.queue)] ?: -1
                    ModTransfer().string(it.address).integer(count).send("queue:online", player)
                }
            }

            for (queues in client.allQueues) {
                online[queues.properties.queueId] = IRealmService.get().realms
                    .filter { it.realmId.typeName == queues.properties.tags["realm_type"] }
                    .sumOf { it.currentPlayers }
                if (queues.properties.strategy == "noop") {
                    client.queueOnline[queues.properties.queueId] = -1
                }
            }
            val buffer: ByteBuf = Unpooled.buffer()
            NetUtil.writeVarInt(online.size, buffer)
            online.forEach { (u: UUID?, i: Int?) ->
                NetUtil.writeId(u, buffer)
                NetUtil.writeVarInt(i, buffer)
                NetUtil.writeVarInt(client.queueOnline[u] ?: 0, buffer)
            }
            Bukkit.getOnlinePlayers().forEach {
                (it as CraftPlayer).handle.playerConnection.sendPacket(
                    PacketPlayOutCustomPayload(
                        "g5e:q",
                        PacketDataSerializer(buffer.retainedSlice())
                    )
                )
            }
        }, 10, 10)

        app.getCommand("queue").setExecutor(CommandExecutor { sender, _, _, args ->
            if (sender is Player) {
                val queueOnline = client.queueOnline
                val queueId = UUID.fromString(args[0])
                if (!queueOnline.containsKey(queueId)) {
                    Anime.killboardMessage(sender, "§cНет такой очереди.")
                    return@CommandExecutor false
                }
                Futures.timeout(IPartyService.get().getPartyByMember(sender.uniqueId), 1, TimeUnit.SECONDS)
                    .whenComplete { party, err ->
                        err?.printStackTrace()
                        if (party.isPresent && !party.get().leader.equals(sender.uniqueId)) {
                            val buffer: ByteBuf = Unpooled.buffer()
                            NetUtil.writeUtf8("Вы не лидер пати", buffer)
                            (sender as CraftPlayer).handle.playerConnection.sendPacket(
                                PacketPlayOutCustomPayload("g5e:qerror", PacketDataSerializer(buffer))
                            )
                            return@whenComplete
                        }
                        val players: List<UUID> = if (party.isPresent)
                            party.get().members.toMutableList()
                        else Collections.singletonList(sender.uniqueId)
                        val opt: Optional<PacketQueueState> = client.allQueues.stream()
                            .filter { it.properties.queueId == queueId }.findFirst()
                        if (!opt.isPresent) return@whenComplete
                        val properties: QueueProperties = opt.get().properties
                        if (properties.strategy == "noop") {
                            val realmType = properties.tags["realm_type"]
                            val ri: Optional<RealmInfo> =
                                IRealmService.get().getStreamRealmsOfType(realmType)
                                    .filter { s: RealmInfo ->
                                        s.status == RealmStatus.WAITING_FOR_PLAYERS || s.status == RealmStatus.STARTING_GAME || s.status == RealmStatus.GAME_STARTED_CAN_JOIN
                                    }
                                    .filter { s: RealmInfo -> s.currentPlayers + players.size <= s.maxPlayers }
                                    .max(Comparator.comparingInt(RealmInfo::getMaxPlayers))
                            if (ri.isPresent) {
                                ITransferService.get().transferBatch(players, ri.get().realmId)
                            } else {
                                sender.sendMessage("§cНе удалось найти ни одного свободного сервера $realmType")
                            }
                        } else {
                            Futures.timeout(
                                client.client.send(
                                    PacketQueueEnter(
                                        queueId,
                                        players, false, true,
                                        HashMap()
                                    )
                                ).awaitFuture(PacketOk::class.java), 1, TimeUnit.SECONDS
                            ).whenComplete { _, err1 ->
                                if (err1 != null) {
                                    err1.printStackTrace()
                                    sender.sendMessage("§cОшибка: " + err1::class.java.simpleName)
                                } else {
                                    players.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
                                        Anime.killboardMessage(player, Formatting.fine("Вы добавлены в очередь!"))
                                        ArcadeType.values().find { it.queue == queueId.toString() }?.let {
                                            ModTransfer()
                                                .string(it.address)
                                                .string(it.title)
                                                .integer(it.slots)
                                                .send("queue:show", player)
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
            return@CommandExecutor true
        })
    }
}