package me.func

import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import dev.implario.bukkit.platform.Platforms
import dev.implario.bukkit.world.Label
import dev.implario.games5e.QueueProperties
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.NoopGameNode
import dev.implario.games5e.packets.PacketOk
import dev.implario.games5e.packets.PacketQueueEnter
import dev.implario.games5e.packets.PacketQueueState
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import dev.xdark.feder.NetUtil
import dev.xdark.feder.collection.DiscardingCollections.queue
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.unix.NativeInetAddress.address
import me.func.battlepass.quest.ArcadeType
import me.func.misc.PersonalizationMenu
import me.func.mod.Anime
import me.func.mod.Anime.title
import me.func.mod.Npc
import me.func.mod.Npc.npc
import me.func.mod.Npc.onClick
import me.func.mod.conversation.ModTransfer
import me.func.protocol.dialog.*
import me.func.protocol.npc.NpcBehaviour
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.craftbukkit.v1_12_R1.CraftEquipmentSlot.slots
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.lib.Futures
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmInfo
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import java.util.*
import java.util.concurrent.TimeUnit

lateinit var app: App

class App : JavaPlugin() {

    val map = WorldMeta(MapLoader.load("func", "basic"))
    val client = CoordinatorClient(NoopGameNode())
    val spawn: Label = map.getLabel("spawn").apply {
        x += 0.5
        z += 0.5
        yaw = -90f
    }

    override fun onEnable() {
        app = this

        CoreApi.get().registerService(IInventoryService::class.java, InventoryService())
        CoreApi.get().registerService(ITransferService::class.java, TransferService(CoreApi.get().socketClient))
        CoreApi.get().registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        val realm = IRealmService.get().currentRealmInfo

        realm.status = RealmStatus.WAITING_FOR_PLAYERS
        realm.isLobbyServer = true
        realm.readableName = "Аркадное Лобби"
        realm.groupName = "Аркады"
        realm.servicedServers = arrayOf("MURP", *ArcadeType.values().map { it.name }.toTypedArray())

        Platforms.set(PlatformDarkPaper())
        Arcade.start(realm.realmId.realmName)

        Bukkit.getScheduler().runTaskTimer(app, {
            Bukkit.getOnlinePlayers().forEach { player ->
                ArcadeType.values().filter { it.queue.isNotEmpty() }.forEach {
                    val count = Games5e.client.queueOnline[UUID.fromString(it.queue)] ?: -1
                    ModTransfer().string(it.address).integer(count).send("queue:online", player)
                }
            }
        }, 5, 10)

        Bukkit.getPluginManager().registerEvents(LobbyListener, this)
        Bukkit.getPluginManager().registerEvents(LoadNpc, this)
        Bukkit.getPluginManager().registerEvents(LootBoxManager, this)

        getCommand("menu").setExecutor(CommandExecutor { sender, _, _, _ ->
            if (sender is Player)
                PersonalizationMenu.open(sender)
            return@CommandExecutor true
        })

        val hub = RealmId.of("HUB")

        getCommand("leave").setExecutor(CommandExecutor { sender, _, _, _ ->
            if (sender is Player)
                ITransferService.get().transfer(sender.uniqueId, hub)
            return@CommandExecutor true
        })

        getCommand("play").setExecutor(CommandExecutor { sender, _, _, _ ->
            if (sender is Player)
                Anime.sendEmptyBuffer("g5e:open", sender)
            return@CommandExecutor true
        })

        getCommand("battlepass").setExecutor(CommandExecutor { sender, _, _, _ ->
            if (sender is Player)
                BattlePassManager.show(sender)
            return@CommandExecutor true
        })

        Games5e

        getCommand("queue").setExecutor(CommandExecutor { sender, _, _, args ->
            if (sender is Player) {
                val queueOnline = Games5e.client.queueOnline
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
                        val opt: Optional<PacketQueueState> = Games5e.client.allQueues.stream()
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
                                Games5e.client.client.send(
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

        lobbyNpc(
            Triple(-17.5, 88.0, -13.5),
            -115.0f,
            "func",
            UUID.fromString("307264a1-2c69-11e8-b5ea-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "func",
                    "func",
                    Screen(
                        "Коцепной комплекс внешних",
                        "дифференциальных форм на гладком",
                        "многообразии."
                    ).buttons(
                        Button("Нинавижу когомологии").actions(Action(Actions.CLOSE)),
                        Button("Сложно...").actions(Action(Actions.CLOSE)),
                        Button("Ок").actions(Action(Actions.CLOSE)),
                    )
                )
            ),
        )

        lobbyNpc(
            Triple(-17.5, 88.0, -14.5),
            -75.0f,
            "Faelan_",
            UUID.fromString("6f3f4a2e-7f84-11e9-8374-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Faelan_",
                    "Faelan_",
                    Screen(
                        "Привет, я куратор билдеров,",
                        "если хочешь начать строить у нас - напиши",
                        "заявку на билдера на форуме. Ждем",
                        "милых ребят :3"
                    ).buttons(
                        Button("Не умею строить :(").actions(Action(Actions.CLOSE)),
                        Button("Сейчас напишу!").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -17.5),
            135.0f,
            "rigb0s",
            UUID.fromString("c155c00c-e4c0-11eb-acca-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-17.5, 88.0, -18.5),
            -45.0f,
            "ZentoFX",
            UUID.fromString("307b1c52-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-16.5, 88.0, -18.5),
            -45.0f,
            "Fiwka1338",
            UUID.fromString("845e92f3-7006-11ea-acca-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Fiwka1338",
                    "Fiwka1338",
                    Screen(
                        "Сломался AmongUs? Пиши мне!",
                        "§bvk.com/kostyan_konovalov"
                    ).buttons(
                        Button("Пока").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -18.5),
            45.0f,
            "_Demaster_",
            UUID.fromString("303c31eb-2c69-11e8-b5ea-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "_Demaster_",
                    "_Demaster_",
                    Screen("Эх, вот если бы не было читеров, было бы 10к онлайна")
                        .buttons(Button("Поставить античит").actions(Action(Actions.CLOSE)))
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, 21.5),
            140.0f,
            "DiamondDen",
            UUID.fromString("ee476051-dc55-11e8-8374-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-14.8, 88.75, -19.5),
            0.0f,
            "nurtalshok",
            UUID.fromString("ef2fb6fb-a6b5-11e8-8374-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "nurtalshok",
                    "nurtalshok",
                    Screen(
                        "Вообще-то мой ник пишется nutrolshok!",
                    ).buttons(
                        Button("Не верить").actions(Action(Actions.CLOSE)),
                    )
                )
            ),
            sitting = false,
            sleeping = true
        )

        lobbyNpc(
            Triple(-17.5, 88.0, -17.5),
            -105f,
            "Sworroo",
            UUID.fromString("ae7abc6b-d142-11e8-8374-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Sworroo",
                    "Sworroo",
                    Screen(
                        "Уже завтра новый лаунчер!",
                    ).buttons(
                        Button("Поверить").actions(
                            Action.command("/msg sworroo Я тебе верю! Но когда лаунчер"),
                            Action(Actions.CLOSE)
                        ),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-10.5, 88.0, -20.5),
            45.0f,
            "kasdo",
            UUID.fromString("303dc644-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -15.5),
            70.0f,
            "Zabelov",
            UUID.fromString("308380a9-2c69-11e8-b5ea-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Zabelov",
                    "Zabelov",
                    Screen(
                        "Ребят, забеликс будет через пол",
                        "года, ждемс."
                    ).buttons(
                        Button("Ок").actions(Action(Actions.CLOSE)),
                        Button("забеликс?").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -14.5),
            120.0f,
            "WhiteNights",
            UUID.fromString("3089411e-2c69-11e8-b5ea-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "WhiteNights",
                    "WhiteNights",
                    Screen(
                        "Как дела? Сломался Tom & Jerry",
                        "или может быть Sheep Wars? В любом",
                        "случае пиши мне, я помогу."
                    ).buttons(
                        Button("Написать").actions(
                            Action.command("/msg WhiteNights Что-то сломалось!"),
                            Action(Actions.CLOSE)
                        ),
                        Button("Пока").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -13.5),
            -180.0f,
            "ItsPVX",
            UUID.fromString("2bd88cc8-603c-11ec-acca-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "ItsPVX",
                    "ItsPVX",
                    Screen(
                        "Винда @$#! Линукс топ!",
                    ).buttons(
                        Button("Поставить винду").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-17.5, 88.0, 21.5),
            -130.0f,
            "Sefeo",
            UUID.fromString("30a1bff7-2c69-11e8-b5ea-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Sefeo",
                    "Sefeo",
                    Screen(
                        "О, заработало! А нет, показалось",
                    ).buttons(
                        Button("Панимаю").actions(Action(Actions.CLOSE))
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, 20.5),
            40.0f,
            "Master_chan",
            UUID.fromString("3044712b-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-15.5, 88.0, 21.5),
            180.0f,
            "СразуЛегенд",
            UUID.fromString("f03c2e10-f6ac-11eb-acca-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "СразуЛегенд",
                    "СразуЛегенд",
                    Screen(
                        "ААААА! Не бейте!!",
                    ).buttons(
                        Button("Побить").actions(Action(Actions.CLOSE)),
                        Button("Замахнуться").actions(Action(Actions.CLOSE)),
                        Button("Оставить").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-16.5, 88.0, 21.5),
            20.0f,
            "Псина_",
            UUID.fromString("f12a63a0-ca64-11e9-80c4-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-13.5, 88.0, 21.5),
            0.0f,
            "Mr_Zlodey_5",
            UUID.fromString("30581daf-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-12.5, 88.0, 21.5),
            35.0f,
            "Pony",
            UUID.fromString("306f45f5-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-11.5, 88.0, 17.5),
            160.0f,
            "Zenk__",
            UUID.fromString("573f139e-57f5-11eb-acca-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Zenk__",
                    "Zenk__",
                    Screen(
                        "Лисов мой раб!",
                        "...",
                        "§9Dev ¨36d87aWhiteNights ¨36d87a» §aНе верю"
                    ).buttons(
                        Button("Да").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, 16.5),
            20.0f,
            "iLisov",
            UUID.fromString("94964b0d-f545-11e8-8374-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "iLisov",
                    "iLisov",
                    Screen(
                        "Я не раб!",
                    ).buttons(
                        Button("Не поверить").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-17.5, 88.0, 18.5),
            -60.0f,
            "ONE1SIDE",
            UUID.fromString("7f3fea26-be9f-11e9-80c4-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-17.5, 88.0, 17.5),
            -60.0f,
            "BaggiYT",
            UUID.fromString("64c67d57-a461-11e8-8374-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "BaggiYT",
                    "BaggiYT",
                    Screen(
                        "Я не ютубер!",
                    ).buttons(
                        Button("Подписаться на BaggiYT").actions(Action(Actions.CLOSE)),
                        Button("Поставить лайк").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-12.5, 88.0, -18.5),
            120f,
            "delfikpro",
            UUID.fromString("e7c13d3d-ac38-11e8-8374-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "delfikpro",
                    "delfikpro",
                    Screen(
                        "Говорю мужу",
                        "§a§l$ sudo docker stop cristalix-core",
                        "§fОтвет убил:",
                        "§cADM ¨c90000DiamondDen » §aты чё сделал %$#!",
                    ).buttons(
                        Button("Вернуть как было").actions(Action(Actions.CLOSE)),
                        Button("/ignore DiamondDen").actions(
                            Action.command("/ignore DiamondDen"),
                            Action(Actions.CLOSE)
                        )
                    )
                )
            )
        )

        val dialog = Dialog(
            Entrypoint(
                "cristalix",
                "Команда Cristalix",
                Screen(
                    "Привет. Это будущее.",
                    "2022 обещает быть волшебным.",
                    "Это место является экспериментальным и активно дорабатывается.",
                    "Спасибо вам, игроки <3"
                ).buttons(
                    Button("Пока").actions(Action(Actions.CLOSE)),
                    Button("Спасибо!").actions(Action(Actions.CLOSE)),
                )
            )
        )

        npc {
            x = -12.0
            y = 87.0
            z = 3.0

            yaw = 115f
            behaviour = NpcBehaviour.STARE_AT_PLAYER

            name = "Команда Cristalix"

            skinUrl = "https://implario.dev/Builder.png"
            skinDigest = "JHIhuhgyushgsufsghoyufsgfsussf"

            onClick {
                if (it.hand == EquipmentSlot.OFF_HAND)
                    return@onClick
                Anime.dialog(it.player, dialog, "cristalix")
            }
        }

    }

    fun lobbyNpc(
        blockPos: Triple<Double, Double, Double>,
        view: Float,
        name: String?,
        uuid: UUID,
        dialog: Dialog?,
        sitting: Boolean = true,
        sleeping: Boolean = false
    ) {
        Npc.npc {
            this.x = blockPos.first
            this.y = blockPos.second
            this.z = blockPos.third

            this.yaw = 0f
            this.pitch = view

            this.sitting = sitting
            this.sleeping = sleeping
            this.behaviour = NpcBehaviour.NONE

            this.skinUrl = "https://webdata.c7x.dev/textures/skin/$uuid"
            this.skinDigest = uuid.toString() + "1"

            onClick {
                dialog ?: return@onClick

                if (it.hand == EquipmentSlot.OFF_HAND)
                    return@onClick
                Anime.dialog(it.player, dialog, name ?: "")
            }
        }
    }

}