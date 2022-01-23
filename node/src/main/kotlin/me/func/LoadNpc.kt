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
    private val markerHelp = Marker(costume.x, costume.y + 3.4, costume.z, MarkerSign.ARROW_DOWN)
    private val achievement: Label = app.map.getLabel("achievement")
    private val newAchievement = Marker(achievement.x, achievement.y + 3.0, achievement.z, MarkerSign.QUESTION_WARNING)

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
                ).buttons(Button("Пока").actions(Action(Actions.CLOSE)),)
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
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        Bukkit.getScheduler().runTaskLater(app, {
            Anime.markers(player, markerHelp, newAchievement)
        }, 5)
    }
}