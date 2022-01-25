package me.func

import org.bukkit.Location

data class ArcadeNode(
    val type: Arcades,
    val origin: Location,
    var active: Boolean = false,
) {
}