package me.func

import dev.implario.bukkit.entity.StandHelper
import me.func.mod.conversation.ModTransfer
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import java.util.*

data class ArcadeNode(
    val type: Arcades,
    val origin: Location,
    var active: Boolean = false,
    val onlineStand: ArmorStand = StandHelper(origin.clone().apply {
        x += 0.5
        y += 1.7
        z += 0.5
        yaw = getYaw()
    }).name("Загрузка...")
        .marker(true)
        .gravity(false)
        .invisible(false)
        .build()
) {

    fun getYaw() = Math.toDegrees(
        -kotlin.math.atan2(
            app.spawn.x - origin.x,
            app.spawn.z - origin.z
        )
    ).toFloat()

    fun sendCreation(player: Player) {
        ModTransfer()
            .double(origin.x)
            .double(origin.y)
            .double(origin.z)
            .double(getYaw().toDouble())
            .string(type.title)
            .boolean(active)
            .send("arcade-lobby:plot", player)
    }

    fun update(player: Player) {
        ModTransfer()
            .integer(app.client.queueOnline.getOrDefault(UUID.fromString(type.queue), null) ?: 0)
            .boolean(active)
            .send("arcade-lobby:update", player)
    }

}