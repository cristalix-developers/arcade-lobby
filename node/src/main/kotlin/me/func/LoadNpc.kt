package me.func

import dev.implario.bukkit.world.Label
import me.func.battlepass.quest.ArcadeType
import me.func.mod.Anime
import me.func.mod.Npc
import me.func.mod.Npc.location
import me.func.mod.Npc.npc
import me.func.mod.Npc.onClick
import me.func.protocol.Marker
import me.func.protocol.MarkerSign
import me.func.protocol.dialog.*
import me.func.protocol.npc.NpcBehaviour
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

object LoadNpc : Listener {

    private val costume: Label = app.map.getLabel("costume")
    private val markerHelp = Marker(costume.x, costume.y + 3.4, costume.z, MarkerSign.ARROW_DOWN)
    private val achievement: Label = app.map.getLabel("achievement")
    private val newAchievement = Marker(achievement.x, achievement.y + 3.0, achievement.z, MarkerSign.QUESTION_WARNING)

    @EventHandler
    fun PlayerJoinEvent.handle() {
        Bukkit.getScheduler().runTaskLater(app, {
            Anime.markers(player, markerHelp, newAchievement)
        }, 5)
    }

    init {
        val npcDialog = Dialog(
            Entrypoint(
                "npc",
                "NPC",
                Screen(
                    "Привет, я помогу тебе попасть в мир",
                    "аркад, покажу настоящее веселье, так же могу",
                    "показать баттлпасс и твою персонализацию"
                ).buttons(
                    Button("§bПоиск игры §l§eNEW!").actions(Action.command("/play")),
                    Button("§bBattlePass §l§eNEW!").actions(Action.command("/battlepass")),
                    Button("§bПерсонализация").actions(Action.command("/menu")),
                )
            )
        )

        var counter = 0

        Bukkit.getScheduler().runTaskTimer(app, {
            counter++
            markerHelp.y += if (counter % 2 == 0) 0.5 else -0.5
            Bukkit.getOnlinePlayers().forEach {
                Anime.moveMarker(it, markerHelp, 0.5)
            }
        }, 10, 10)

        npc {
            behaviour = NpcBehaviour.STARE_AT_PLAYER
            name = "§lNPC"

            skinUrl = "https://webdata.c7x.dev/textures/skin/861768ab-c14e-11eb-acca-1cb72caa35fd"
            skinDigest = "861768ab-c14e-11eb-acca-1cb72caa35fd"

            onClick {
                if (it.hand == EquipmentSlot.OFF_HAND)
                    return@onClick
                Anime.dialog(it.player, npcDialog, "npc")
            }
            location(costume)
        }

        val achievementDialog = Dialog(
            Entrypoint(
                "achievement",
                "Достижения",
                Screen(
                    "Привет, сейчас у меня нет списка",
                    "достижений для тебя, как только они",
                    "появятся - я дам знать!",
                ).buttons(Button("Пока").actions(Action(Actions.CLOSE)))
            )
        )

        npc {
            behaviour = NpcBehaviour.STARE_AT_PLAYER
            name = "§lДостижения"

            skinUrl = "https://webdata.c7x.dev/textures/skin/dc890a09-9962-11e9-80c4-1cb72caa35fd"
            skinDigest = "dc890a09-9962-11e9-80c4-1cb72caa35fd"
            slimArms = true

            location(achievement)
            onClick {
                if (it.hand == EquipmentSlot.OFF_HAND)
                    return@onClick
                Anime.dialog(it.player, achievementDialog, "achievement")
            }
        }

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
                      "Делаю парад аркад...",
                      "Осталось еще 93 игры,",
                      "как же я устал"
                  ).buttons(
                      Button("Когомологии нулевые").actions(Action(Actions.CLOSE)),
                      Button("Цепи сверкают").actions(Action(Actions.CLOSE)),
                      Button("Семи масен").actions(Action(Actions.CLOSE)),
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
                "Аркадный Бот",
                Screen(
                    "Привет. Нам надоели баги и ошибки!",
                    "Поэтому сделали нового бота, вы",
                    "можете напрямую нам писать их через",
                    "/bug или в Discord."
                ).buttons(
                    Button("Ок").actions(Action(Actions.CLOSE)),
                    Button("Случайная игра").actions(
                        Action.command("/queue ${ArcadeType.values().random().queue}"),
                        Action(Actions.CLOSE)
                    ),
                    Button("BattlePass").actions(Action.command("/battlepass"), Action(Actions.CLOSE)),
                    Button("Хочу в Discord").actions(Action.command("/discord"), Action(Actions.CLOSE))
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

            skinUrl = "https://webdata.c7x.dev/textures/skin/d074b53b-929a-11eb-acca-1cb72caa35fd"
            skinDigest = "JHIhuhgyusgfsudgdgfgdgdgussf"

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