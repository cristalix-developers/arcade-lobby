package me.func

import dev.implario.bukkit.platform.Platforms
import dev.implario.bukkit.world.Label
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.NoopGameNode
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.misc.PersonalizationMenu
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService

lateinit var app: App

class App : JavaPlugin() {

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


        CoreApi.get().registerService(IInventoryService::class.java, InventoryService())
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
        getCommand("menu").setExecutor(CommandExecutor { sender, _, _, _ ->
            if (sender is Player)
                PersonalizationMenu.open(sender)
            return@CommandExecutor true
        })
    }

}