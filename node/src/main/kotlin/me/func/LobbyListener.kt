package me.func

import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent
import dev.implario.bukkit.item.item
import dev.implario.games5e.packets.PacketQueueLeave
import me.func.Games5e.client
import me.func.battlepass.quest.ArcadeType
import me.func.mod.Anime
import me.func.mod.Banners
import me.func.mod.Banners.location
import me.func.mod.Glow
import me.func.mod.conversation.ModLoader
import me.func.mod.conversation.ModTransfer
import me.func.protocol.GlowColor
import me.func.protocol.GlowingPlace
import me.func.protocol.Indicators
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import java.util.*

object LobbyListener : Listener {

    private val godSet = hashSetOf(
        "307264a1-2c69-11e8-b5ea-1cb72caa35fd",
        "e7c13d3d-ac38-11e8-8374-1cb72caa35fd",
        "6f3f4a2e-7f84-11e9-8374-1cb72caa35fd",
        "bf30a1df-85de-11e8-a6de-1cb72caa35fd"
    )

    init {
        ModLoader.loadAll("mods")
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
        ) {
            it.performCommand("play")
            it.teleport(app.spawn)
        }

        Banners.new {
            opacity = 0.0
            content = "§bАркады\nНачать играть"
            watchingOnPlayer = true
            weight = 0
            height = 0
            location(center.clone().add(-0.5, 4.0, 0.5))
        }

        Anime.createReader("queue:leave") { player, _ ->
            leave(player)
            Anime.sendEmptyBuffer("queue:hide", player)
        }
    }

    fun leave(player: Player) {
        client.client.send(PacketQueueLeave(Collections.singletonList(player.uniqueId)))
    }

    @EventHandler
    fun PlayerInitialSpawnEvent.handle() {
        spawnLocation = app.spawn
    }

    @EventHandler
    fun PlayerQuitEvent.handle()  = leave(player)

    private val compass = item {
        type = Material.COMPASS
        text("§bАркады")
        nbt("click", "play")
    }.build()
    private val cosmeticItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§aПерсонаж")
        nbt("other", "clothes")
        nbt("click", "menu")
    }.build()
    private val backItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§cВыйти")
        nbt("other", "cancel")
        nbt("click", "leave")
    }.build()
    private val battlepassItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§6BattlePass")
        nbt("other", "quest_day_booster")
        nbt("click", "battlepass")
    }.build()

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.handle() {
        player.inventory.apply {
            setItem(0, compass)
            setItem(2, battlepassItem)
            setItem(4, cosmeticItem)
            setItem(8, backItem)
        }
        player.teleport(app.spawn)
        player.gameMode = GameMode.ADVENTURE
        player.isOp = godSet.contains(player.uniqueId.toString())
        ModLoader.manyToOne(player)
        joinMessage = null
        MinecraftServer.SERVER.postToNextTick {
            Anime.hideIndicator(player, Indicators.HEALTH, Indicators.EXP, Indicators.HUNGER)
            Anime.topMessage(player, "Загрузка аркадного профиля ${player.playerListName}")
            Glow.showAllPlaces(player)
            ModTransfer()
                .json(client.allQueues.map { it.properties })
                .send("queues:data", player)

            player!!.spigot().sendMessage(
                *ComponentBuilder("\n§7Новая аркада - §6Землекопы§7, нажми §e§lСЮДА§7 чтобы играть!\n")
                    .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/queue ${ArcadeType.DOUT.queue}"))
                    .create()
            )
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