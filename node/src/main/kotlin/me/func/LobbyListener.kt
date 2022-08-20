package me.func

import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import dev.implario.bukkit.item.item
import dev.implario.games5e.packets.PacketQueueLeave
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import me.func.Games5e.client
import me.func.battlepass.quest.ArcadeType
import me.func.mod.Anime
import me.func.mod.Banners
import me.func.mod.Banners.location
import me.func.mod.Glow
import me.func.mod.Npc
import me.func.mod.conversation.ModTransfer
import me.func.mod.util.after
import me.func.protocol.GlowColor
import me.func.protocol.GlowingPlace
import me.func.protocol.Indicators
import me.func.protocol.Tricolor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedSoundEffect
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
import java.util.*

object LobbyListener : Listener {

    private val godSet = hashSetOf(
        "307264a1-2c69-11e8-b5ea-1cb72caa35fd",
        "e7c13d3d-ac38-11e8-8374-1cb72caa35fd",
        "6f3f4a2e-7f84-11e9-8374-1cb72caa35fd",
        "bf30a1df-85de-11e8-a6de-1cb72caa35fd",
        "303dc644-2c69-11e8-b5ea-1cb72caa35fd",
        "a45d2a88-7efe-11e9-8374-1cb72caa35fd"
    )

    init {
        val center = app.map.getLabel("center")

        Glow.addPlace(
            GlowingPlace(
                UUID.randomUUID(),
                Tricolor(128, 0, 128),
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
    fun PlayerQuitEvent.handle() = leave(player)

    private val compass = item {
        type = Material.COMPASS
        text("§bИграть")
        nbt("p13nModelId", "1a4caaf5-77bc-4d7f-9302-6b2fcb510a6a")
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
        nbt("p13nModelId", "e28387d9-c465-41a5-871b-7f27fd26076d")
        nbt("click", "battlepass")
    }.build()

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.apply {
            inventory.apply {
                setItem(0, compass)
                setItem(2, battlepassItem)
                setItem(4, cosmeticItem)
                setItem(8, backItem)
            }
            teleport(app.spawn)
            gameMode = GameMode.ADVENTURE
            isOp = godSet.contains(player.uniqueId.toString())
            setResourcePack("", "")
        }

        joinMessage = null

        after(5) {
            if (app.userManager.getUser(player.uniqueId)?.stat?.enabledResourcePack == Tristate.UNKNOWN) {
                player.performCommand("rp")
            } else {
                player.sendMessage(Formatting.fine("Чтобы включить или выключить в лобби ресурс-пак пишите /rp"))
            }

            Anime.hideIndicator(player, Indicators.HEALTH, Indicators.EXP, Indicators.ARMOR, Indicators.HUNGER)
            var famous = Arcade.getFamousArcade(player)?.arcadeType ?: ArcadeType.values().random()

            if (famous == ArcadeType.TEST || famous == ArcadeType.DBD || famous == ArcadeType.ARC)
                famous = ArcadeType.PILL

            if (Math.random() < 0.7) {
                player.spigot().sendMessage(
                    *ComponentBuilder("\n§7Заходи играть - §b${famous.title}§7, нажми §e§lСЮДА§7 чтобы играть!\n")
                        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/queue ${famous.queue}"))
                        .create()
                )
            } else {
                player.performCommand("bot")
            }
        }

        after(10) {
            Glow.showAllPlaces(player)
            Npc.npcs.forEach { (_, value) -> value.spawn(player) }

            Arcade.getArcadeData(player).mask.setMask(player)

            ModTransfer()
                .json(client.allQueues.map { it.properties })
                .send("queues:data", player)
        }

        after(40) {
            val targetServer = player.playerProfile.gameProfile.getProperties()["hc:targetServer"]

            println("Joined ${player.displayName}, target ${targetServer?.lastOrNull()?.value ?: "null"}")

            if (!targetServer.isNullOrEmpty()) {
                val address = targetServer.last().value
                player.performCommand("queue ${ArcadeType.valueOf(address.uppercase()).queue}")
            }
        }

        (player as CraftPlayer).handle.playerConnection.networkManager.channel.pipeline()
            .addBefore("packet_handler", player.customName, object : ChannelDuplexHandler() {
                override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
                    if (msg is PacketPlayOutNamedSoundEffect) msg.f = 0f
                    super.write(ctx, msg, promise)
                }
            })
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

    @EventHandler
    fun PlayerUseUnknownEntityEvent.handle() {
        Npc.npcs[entityId]?.click?.accept(this)
    }
}
