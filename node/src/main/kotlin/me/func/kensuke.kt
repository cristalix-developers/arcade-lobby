package me.func

import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.IBukkitKensukeUser
import me.func.mod.util.unit
import org.bukkit.entity.Player
import kotlin.properties.Delegates.notNull

data class ArcadeLobbyUserData(
    var enabledResourcePack: Tristate,
)

class ArcadeLobbyUser(
    private val session: KensukeSession,
    stat: ArcadeLobbyUserData?,
) : IBukkitKensukeUser {
    private var player: Player? = null
    var stat: ArcadeLobbyUserData by notNull()

    init {
        this.stat = stat ?: ArcadeLobbyUserData(Tristate.UNKNOWN)
    }

    override fun getSession(): KensukeSession = session
    override fun getPlayer(): Player? = player
    override fun setPlayer(player: Player?) = unit { this.player = player }
}
