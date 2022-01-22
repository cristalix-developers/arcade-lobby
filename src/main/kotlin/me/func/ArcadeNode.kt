package me.func

import me.func.mod.conversation.ModTransfer
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

data class ArcadeNode(
    val type: Arcades,
    val origin: Location,
    var active: Boolean = false,
) {

    fun update(player: Player) {
        ModTransfer()
            .integer(app.client.queueOnline.getOrDefault(UUID.fromString(type.queue), null) ?: 0)
            .boolean(active)
            .send("arcade-lobby:update", player)
    }

}