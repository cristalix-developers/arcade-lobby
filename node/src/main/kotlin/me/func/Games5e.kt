package me.func

import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.NoopGameNode
import dev.xdark.feder.NetUtil
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import ru.cristalix.core.realm.IRealmService
import java.util.*

object Games5e {

    val client = CoordinatorClient(NoopGameNode())
    private val online: MutableMap<UUID, Int> = HashMap()

    init {
        client.listenQueues()
        client.enable()
        Bukkit.getScheduler().runTaskTimer(app, {
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
    }
}