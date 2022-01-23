package me.func

import dev.implario.bukkit.platform.Platforms
import dev.implario.bukkit.world.Label
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.NoopGameNode
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.misc.PersonalizationMenu
import me.func.mod.Anime
import me.func.mod.Npc
import me.func.mod.Npc.onClick
import me.func.protocol.dialog.*
import me.func.protocol.npc.NpcBehaviour
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import java.util.*

lateinit var app: App

class App : JavaPlugin() {

    val map = WorldMeta(MapLoader.load("func", "basic"))
    val client = CoordinatorClient(NoopGameNode())
    private var arcades: List<ArcadeNode> = mutableListOf()
    val spawn: Label = map.getLabel("spawn").apply {
        x += 0.5
        z += 0.5
        yaw = -90f
    }

    override fun onEnable() {
        app = this

        CoreApi.get().registerService(IInventoryService::class.java, InventoryService())
        CoreApi.get().registerService(ITransferService::class.java, TransferService(CoreApi.get().socketClient))
        Platforms.set(PlatformDarkPaper())
        Arcade.start()

        Bukkit.getScheduler().runTaskTimer(app, {
            arcades.forEach { arcade ->
                if (arcade.type.queue.isEmpty())
                    return@forEach

                Bukkit.getOnlinePlayers().forEach { arcade.update(it) }
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

        lobbyNpc(
            Triple(-17.5, 88.0, -13.5),
            Pair(-115.0f, 0f),
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
            Pair(-75.0f, 0f),
            "Faelan_",
            UUID.fromString("6f3f4a2e-7f84-11e9-8374-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -17.5),
            Pair(135.0f, 0f),
            "rigb0s",
            UUID.fromString("c155c00c-e4c0-11eb-acca-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-17.5, 88.0, -18.5),
            Pair(-45.0f, 0f),
            "ZentoFX",
            UUID.fromString("307b1c52-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-16.5, 88.0, -18.5),
            Pair(-45.0f, 0f),
            "Fiwka1338",
            UUID.fromString("845e92f3-7006-11ea-acca-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Fiwka1338",
                    "Fiwka1338",
                    Screen(
                        "ФАААНК! МНЕ НРАВИТСЯ КАК СТОЯТ НПС!",
                        "Я ТАК РЕШИЛ! ТВОЁ МНЕНИЕ НИЧТОЖНО!"
                    ).buttons(
                        Button("Согласиться").actions(Action(Actions.CLOSE)),
                        Button("Согласиться").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -18.5),
            Pair(45.0f, 0f),
            "_Demaster_",
            UUID.fromString("303c31eb-2c69-11e8-b5ea-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "_Demaster_",
                    "_Demaster_",
                    Screen(
                        "Эх, вот если бы не было читеров, было бы 10к онлайна",
                    ).buttons(
                        Button("Поставить античит").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, 21.5),
            Pair(140.0f, 0f),
            "DiamondDen",
            UUID.fromString("ee476051-dc55-11e8-8374-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-13.8, 88.75, -18.5),
            Pair(0.0f, 0f),
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
            Pair(-105.0f, 0f),
            "Sworroo",
            UUID.fromString("ae7abc6b-d142-11e8-8374-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Sworroo",
                    "Sworroo",
                    Screen(
                        "Уже завтра новый лаунчер!",
                    ).buttons(
                        Button("Поверить").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-10.5, 88.0, -20.5),
            Pair(45.0f, 0f),
            "kasdo",
            UUID.fromString("303dc644-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -15.5),
            Pair(70.0f, 0f),
            "Zabelov",
            UUID.fromString("308380a9-2c69-11e8-b5ea-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Zabelov",
                    "Zabelov",
                    Screen(
                        "Раст кринж! Котлин кринж! Жава мощь!11",
                    ).buttons(
                        Button("Отправить в дурку").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -14.5),
            Pair(120.0f, 0f),
            "WhiteNights",
            UUID.fromString("3089411e-2c69-11e8-b5ea-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "WhiteNights",
                    "WhiteNights",
                    Screen(
                        "Сегодня вечером сделаю обнову",
                        "Вечером: Ayaka Shizumy был в сети сегодня в 11:54",
                    ).buttons(
                        Button("Напомнить завтра").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -13.5),
            Pair(-180.0f, 0f),
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
            Pair(-130.0f, 0f),
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
            Pair(40.0f, 0f),
            "Master_chan",
            UUID.fromString("3044712b-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-15.5, 88.0, 21.5),
            Pair(180.0f, 0f),
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
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-16.5, 88.0, 21.5),
            Pair(20.0f, 0f),
            "Псина_",
            UUID.fromString("f12a63a0-ca64-11e9-80c4-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-13.5, 88.0, 21.5),
            Pair(0.0f, 0f),
            "Mr_Zlodey_5",
            UUID.fromString("30581daf-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-12.5, 88.0, 21.5),
            Pair(35.0f, 0f),
            "Pony",
            UUID.fromString("306f45f5-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-11.5, 88.0, 17.5),
            Pair(160.0f, 0f),
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
            Pair(20.0f, 0f),
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
            Pair(-60.0f, 0f),
            "ONE1SIDE",
            UUID.fromString("7f3fea26-be9f-11e9-80c4-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-17.5, 88.0, 17.5),
            Pair(-60.0f, 0f),
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
    }

    fun lobbyNpc(blockPos: Triple<Double, Double, Double>, view: Pair<Float, Float>, name: String?, uuid: UUID, dialog: Dialog?, sitting: Boolean = true, sleeping: Boolean = false) {
        Npc.npc {
            x = blockPos.first
            y = blockPos.second
            z = blockPos.third

            yaw = view.first
            pitch = view.second

            this.sitting = sitting
            this.sleeping = sleeping
            behaviour = NpcBehaviour.NONE

            skinUrl = "https://webdata.c7x.dev/textures/skin/$uuid"
            skinDigest = uuid.toString() + "1"

            onClick {
                dialog ?: return@onClick

                if (it.hand == EquipmentSlot.OFF_HAND)
                    return@onClick
                Anime.dialog(it.player, dialog, name ?: "")
            }
        }
    }

}