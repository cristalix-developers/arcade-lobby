import java.util.*

data class GlowingPlace(
    val uuid: UUID = UUID.randomUUID(),
    val red: Int,
    val blue: Int,
    val green: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val radius: Double = 1.3,
    val angles: Int = 12
)
