package me.func

import dev.implario.bukkit.platform.Platforms
import dev.implario.bukkit.world.Label
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.NoopGameNode
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.battlepass.quest.ArcadeType
import me.func.mod.Anime
import me.func.mod.conversation.ModLoader
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import kotlin.properties.Delegates.notNull

lateinit var app: App

class App : JavaPlugin() {

    val map = WorldMeta(MapLoader.load("func", "basic"))
    val client = CoordinatorClient(NoopGameNode())
    val spawn: Label = map.getLabel("spawn").apply {
        x += 0.5
        z += 0.5
        yaw = -90f
    }

    private var kensuke: Kensuke by notNull()
    private val statScope = Scope("arcade-lobby", ArcadeLobbyUserData::class.java)
    val userManager = BukkitUserManager(
        listOf(statScope),
        { session, context -> ArcadeLobbyUser(session, context.getData(statScope)) },
        { user, context -> context.store(statScope, user.stat) }
    ).apply { isOptional = true }

    override fun onEnable() {
        app = this

        CoreApi.get().apply {
            registerService(IInventoryService::class.java, InventoryService())
            registerService(ITransferService::class.java, TransferService(this.socketClient))
            registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        }

        Platforms.set(PlatformDarkPaper())

        IRealmService.get().currentRealmInfo.apply {
            status = RealmStatus.WAITING_FOR_PLAYERS
            isLobbyServer = true
            readableName = "Аркадное Лобби"
            groupName = "Аркады"
            servicedServers =
                arrayOf("MURP", *ArcadeType.values().map { it.name }.minus("BP").minus("TEST").toTypedArray())
        }

        kensuke = BukkitKensuke.setup(this).apply {
            addGlobalUserManager(userManager)
            globalRealm = "MURP-0"
        }

        Arcade.start(ArcadeType.TEST)
        Arcade.enableStepParticles()

        Bukkit.getPluginManager().apply {
            registerEvents(LobbyListener, this@App)
            registerEvents(LoadNpc, this@App)
            registerEvents(LootBoxManager, this@App)
        }

        ModLoader.loadAll("mods")
        ModLoader.onJoining("mod.jar")

        UserCommands
        Games5e
        AdminCommands
        Anime
    }
}
