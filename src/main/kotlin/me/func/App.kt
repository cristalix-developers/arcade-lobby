package me.func

import dev.implario.bukkit.platform.Platforms
import dev.implario.bukkit.world.Label
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.NoopGameNode
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.mod.Glow
import me.func.protocol.GlowingPlace
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color.red
import java.util.*

lateinit var app: App

class App: JavaPlugin() {

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
    }

}