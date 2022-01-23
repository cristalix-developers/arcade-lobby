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

        lobbyNpc(
            Triple(-17.5, 88.0, -13.5),
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
            )
        )

        lobbyNpc(
            Triple(-17.5, 88.0, -14.5),
            "Faelan_",
            UUID.fromString("6f3f4a2e-7f84-11e9-8374-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-11.75, 88.0, -18.15),
            "rigb0s",
            UUID.fromString("c155c00c-e4c0-11eb-acca-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-17.5, 88.0, -18.5),
            "ZentoFX",
            UUID.fromString("307b1c52-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-16.5, 88.0, -18.5),
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
            "DiamondDen",
            UUID.fromString("ee476051-dc55-11e8-8374-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-10.5, 89.0, -12.5),
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
            "kasdo",
            UUID.fromString("303dc644-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-11.5, 88.0, -15.5),
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
            "Sefeo",
            UUID.fromString("30a1bff7-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-16.5, 88.0, 21.5),
            "Master_chan",
            UUID.fromString("3044712b-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-15.5, 88.0, 21.5),
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
            Triple(-14.5, 88.0, 21.5),
            "Псина_",
            UUID.fromString("f12a63a0-ca64-11e9-80c4-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-13.5, 88.0, 21.5),
            "Mr_Zlodey_5",
            UUID.fromString("30581daf-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-12.5, 88.0, 21.5),
            "Pony",
            UUID.fromString("306f45f5-2c69-11e8-b5ea-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-17.5, 88.0, 20.5),
            "Zenk__",
            UUID.fromString("573f139e-57f5-11eb-acca-1cb72caa35fd"),
            Dialog(
                Entrypoint(
                    "Zenk__",
                    "Zenk__",
                    Screen(
                        "Лисов мой раб!",
                    ).buttons(
                        Button("Да").actions(Action(Actions.CLOSE)),
                    )
                )
            )
        )

        lobbyNpc(
            Triple(-17.5, 88.0, 19.5),
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
            "ONE1SIDE",
            UUID.fromString("7f3fea26-be9f-11e9-80c4-1cb72caa35fd"),
            null
        )

        lobbyNpc(
            Triple(-17.5, 88.0, 17.5),
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
            Triple(-14.5, 87.5, -15.5),
            null,
            UUID.fromString("ef2fb6fb-a6b5-11e8-8374-1cb72caa35fd"),
            null,
            sleeping = true,
            sitting = false
        )
    }

    fun lobbyNpc(blockPos: Triple<Double, Double, Double>, name: String?, uuid: UUID, dialog: Dialog?, sitting: Boolean = true, sleeping: Boolean = false) {
        Npc.npc {
            x = blockPos.first
            y = blockPos.second
            z = blockPos.third

            this.sitting = sitting
            this.sleeping = sleeping
            behaviour = NpcBehaviour.STARE_AT_PLAYER

            if (name != null)
                this.name = name

            skinUrl = "https://webdata.c7x.dev/textures/skin/$uuid"
            skinDigest = uuid.toString()

            onClick {
                dialog ?: return@onClick

                if (it.hand == EquipmentSlot.OFF_HAND)
                    return@onClick
                Anime.dialog(it.player, dialog, name ?: "")
            }
        }
    }

}