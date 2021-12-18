package me.func

import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.protocol.GlowColor
import me.func.protocol.Indicators
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent

object LobbyListener : Listener {

    @EventHandler
    fun PlayerInitialSpawnEvent.handle() {
        spawnLocation = app.spawn
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.teleport(app.spawn)
        player.gameMode = GameMode.ADVENTURE
        joinMessage = null
        Bukkit.getScheduler().runTaskLater(app, {
            Glow.showAllPlaces(player)
            Anime.hideIndicator(player, Indicators.HEALTH, Indicators.EXP, Indicators.HUNGER)
            Anime.marker(player, app.marker)

            app.arcades.forEach { it.sendCreation(player) }
        }, 5)
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (to.clone().subtract(0.0, 1.0, 0.0).block.type == Material.SLIME_BLOCK) {
            player.velocity = player.eyeLocation.direction.multiply(1.3)
            Glow.animate(player, 0.4, GlowColor.BLUE)
        }
    }

    @EventHandler
    fun EntityDamageEvent.handle() {
        cancelled = true
    }

    @EventHandler
    fun BlockPhysicsEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun FoodLevelChangeEvent.handle() {
        foodLevel = 20
    }

}