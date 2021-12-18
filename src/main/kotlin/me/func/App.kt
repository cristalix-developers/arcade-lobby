package me.func

import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.NoopGameNode
import dev.implario.games5e.packets.PacketOk
import dev.implario.games5e.packets.PacketQueueEnter
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import dev.xdark.feder.collection.DiscardingCollections.queue
import io.netty.buffer.Unpooled
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.mod.conversation.ModTransfer
import me.func.protocol.GlowColor
import me.func.protocol.GlowingPlace
import me.func.protocol.Marker
import me.func.protocol.MarkerSign
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.EntityType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.lib.Futures
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs
import java.util.*
import java.util.concurrent.TimeUnit

lateinit var app: App

class App: JavaPlugin() {

    val client = CoordinatorClient(NoopGameNode())
    val map = WorldMeta(MapLoader.load("MISC", "nendosa"))
    lateinit var arcades: List<ArcadeNode>
    lateinit var marker: Marker
    val spawn = map.getLabel("spawn").apply {
        x += 0.5
        z += 0.5
        yaw = -180f
    }

    override fun onEnable() {
        app = this

        Platforms.set(PlatformDarkPaper())
        Arcade.start()
        Npcs.init(this)

        arcades = map.getLabels("arcade").map { ArcadeNode(Arcades.valueOf(it.tag.uppercase()), it.clone().subtract(0.5, 0.0, 0.5)) }

        map.getLabel("portal").also { label ->
            GlowingPlace(UUID.randomUUID(), GlowColor.BLUE, label.x + 0.5, label.y, label.z + 0.5, 2.5, 24).also { place ->
                Glow.addPlace(place) { it.teleport(spawn) }
            }
            marker = Marker(label.x + 0.5, label.y + 3.0, label.z + 0.5, MarkerSign.ARROW_DOWN)
        }

        var counter = 0

        Bukkit.getScheduler().runTaskTimer(app, {
            counter++
            marker.y += if (counter % 2 == 0) 0.5 else -0.5
            Bukkit.getOnlinePlayers().forEach {
                Anime.moveMarker(it, marker, 0.5)
            }

            arcades.forEach { arcade ->
                if (arcade.type.queue.isEmpty())
                    return@forEach

                Bukkit.getOnlinePlayers().forEach { arcade.update(it) }
            }
        }, 5, 10)

        Bukkit.getPluginManager().registerEvents(LobbyListener, this)
    }

}