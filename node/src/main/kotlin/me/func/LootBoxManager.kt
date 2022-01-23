package me.func

import dev.implario.bukkit.entity.StandHelper
import dev.implario.bukkit.item.item
import implario.humanize.Humanize
import me.func.mod.Banners
import me.func.protocol.element.Banner
import net.minecraft.server.v1_12_R1.EnumItemSlot
import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.server.TabCompleteEvent

object LootBoxManager : Listener {

    private val chest = item {
        type = Material.CHEST
    }.build()

    private val lootbox = app.map.getLabels("lootbox").map { LootBox(it.clone().add(1.0, -0.6, 1.0)) }

    init {
        Bukkit.getScheduler().runTaskTimer(app, {
            lootbox.forEach { lootbox ->
                Bukkit.getOnlinePlayers().minByOrNull { it.location.distanceSquared(lootbox.origin) }?.let {
                    lootbox.stand.headPose =
                        lootbox.stand.headPose.setX(Math.toRadians(it.location.pitch.toDouble() - 15))
                            .setY(Math.toRadians(it.location.yaw.toDouble() + 180))
                }
            }
        }, 10, 1)
    }

    @EventHandler
    fun TabCompleteEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerArmorStandManipulateEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        MinecraftServer.SERVER.postToNextTick {
            Arcade.get(player)?.let { data ->
                lootbox.forEach {
                    it.banner.content = "§bЛутбокс\n§fДоступно ${data.crates} ${
                        Humanize.plurals(
                            "штука",
                            "штуки",
                            "штук",
                            data.crates
                        )
                    }\n"
                    Banners.show(player, it.banner)
                }
            }
        }
    }

    data class LootBox(
        val origin: Location,
        val stand: ArmorStand = StandHelper(origin)
            .gravity(false)
            .invisible(false)
            .name("§e§lКЛИК")
            .build()
            .apply {
                (this as CraftArmorStand).handle
                    .setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(chest))
            },
        val banner: Banner = Banner.Builder()
            .x(origin.x)
            .y(origin.y + 3.6)
            .z(origin.z + 2.0)
            .yaw(-90f)
            .pitch(10f)
            .weight(65)
            .height(14)
            .resizeLine(0, 0.5)
            .resizeLine(1, 0.5)
            .build()
    )
}