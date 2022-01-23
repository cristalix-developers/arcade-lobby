package me.func

import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent
import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.func.mod.Banners
import me.func.mod.Banners.location
import me.func.mod.Glow
import me.func.protocol.GlowColor
import me.func.protocol.GlowingPlace
import me.func.protocol.Indicators
import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import java.util.*

object LobbyListener : Listener {

    init {
        val center = app.map.getLabel("center")

        Glow.addPlace(
            GlowingPlace(
                UUID.randomUUID(),
                128,
                0,
                128,
                center.x + 0.5,
                center.y,
                center.z + 0.5,
                2.7,
                20
            )
        ) { it.performCommand("play") }

        Banners.new {
            opacity = 0.0
            content = "§bАркады\nНачать играть"
            watchingOnPlayer = true
            weight = 0
            height = 0
            location(center.clone().add(-0.5, 4.0, 0.5))
        }
    }

    @EventHandler
    fun PlayerInitialSpawnEvent.handle() {
        spawnLocation = app.spawn
    }

    private val compass = item {
        type = Material.COMPASS
        text("§bАркады")
        nbt("click", "play")
    }.build()
    private var cosmeticItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§aПерсонаж")
        nbt("other", "clothes")
        nbt("click", "menu")
    }.build()
    private var backItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§cВыйти")
        nbt("other", "cancel")
        nbt("click", "leave")
    }.build()

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.handle() {
        player.inventory.apply {
            setItem(0, compass)
            setItem(4, cosmeticItem)
            setItem(8, backItem)
        }
        player.teleport(app.spawn)
        player.gameMode = GameMode.ADVENTURE
        joinMessage = null
        MinecraftServer.SERVER.postToNextTick {
            Anime.hideIndicator(player, Indicators.HEALTH, Indicators.EXP, Indicators.HUNGER)
            Anime.topMessage(player, "Загрузка аркадного профиля ${player.playerListName}")
            Glow.showAllPlaces(player)
        }
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (to.clone().subtract(0.0, 1.0, 0.0).block.type == Material.SLIME_BLOCK) {
            player.velocity = player.eyeLocation.direction.multiply(1.3)
            Glow.animate(player, 0.4, GlowColor.BLUE)
        }
    }

    @EventHandler
    fun PlayerInteractEvent.handle() {
        item ?: return

        val nmsItem = CraftItemStack.asNMSCopy(item)
        if (nmsItem.hasTag() && nmsItem.tag.hasKeyOfType("click", 8))
            player.performCommand(nmsItem.tag.getString("click"))
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

    @EventHandler
    fun PlayerDropItemEvent.handle() {
        cancel = true
    }

    @EventHandler
    fun InventoryClickEvent.handle() {
        isCancelled = true
    }
}