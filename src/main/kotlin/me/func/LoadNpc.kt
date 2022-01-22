package me.func

import dev.implario.bukkit.world.Label
import me.func.mod.Anime
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

object LoadNpc : Listener {

    private val costume: Label = app.map.getLabel("costume")
    private val marker = Marker(costume.x, costume.y + 3.4, costume.z, MarkerSign.ARROW_DOWN)

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
                    Button("§bПоиск игры §l§eNEW!").actions(Action.command("/start")),
                    Button("§bBattlePass §l§eNEW!").actions(Action.command("/battlepass")),
                    Button("§bПерсонализация").actions(Action.command("/menu")),
                )
            )
        )

        var counter = 0

        Bukkit.getScheduler().runTaskTimer(app, {
            counter++
            marker.y += if (counter % 2 == 0) 0.5 else -0.5
            Bukkit.getOnlinePlayers().forEach {
                Anime.moveMarker(it, marker, 0.5)
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

        val funcDialog = Dialog(
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
                    Button("/ignore DiamondDen").actions(Action.command("/ignore DiamondDen"), Action(Actions.CLOSE))
                )
            )
        )

        npc {
            x = -13.5
            y = 88.0
            z = -18.5

            sitting = true
            behaviour = NpcBehaviour.STARE_AT_PLAYER
            name = "delfikpro"

            skinUrl = "https://webdata.c7x.dev/textures/skin/e7c13d3d-ac38-11e8-8374-1cb72caa35fd"
            skinDigest = "e7c13d3d-ac38-11e8-8374-1cb72caa35fd"

            onClick {
                if (it.hand == EquipmentSlot.OFF_HAND)
                    return@onClick
                Anime.dialog(it.player, funcDialog, "delfikpro")
            }
        }
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        Bukkit.getScheduler().runTaskLater(app, {
            Anime.marker(player, marker)
        }, 5)
    }
}