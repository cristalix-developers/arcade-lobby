import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.inventory.EntityEquipmentSlot
import dev.xdark.clientapi.item.ItemTools
import dev.xdark.clientapi.math.BlockPos
import dev.xdark.clientapi.util.EnumFacing
import dev.xdark.clientapi.util.EnumHand
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine.clientApi
import java.util.*
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt

object Npc {

    init {
        // Утилита для работы с NPC
        NpcManager

        // Чтение NPC
        mod.registerChannel("npc:spawn") {
            val data = NpcData(
                readInt(),
                UUID.fromString(NetUtil.readUtf8(this)),
                readDouble(),
                readDouble(),
                readDouble(),
                readInt(),
                NetUtil.readUtf8(this),
                NpcBehaviour.values()[readInt()],
                readDouble().toFloat(),
                readDouble().toFloat(),
                NetUtil.readUtf8(this),
                NetUtil.readUtf8(this),
                readBoolean(),
                readBoolean(),
                readBoolean(),
                readBoolean()
            )
            NpcManager.spawn(data)
            NpcManager.show(data.uuid)
        }

        // Показать NPC
        mod.registerChannel("npc:show") {
            NpcManager.show(UUID.fromString(NetUtil.readUtf8(this)))
        }

        registerHandler<GameLoop> {
            val player = clientApi.minecraft().player

            NpcManager.each { _, data ->
                data.entity?.let { entity ->
                    if (data.data.behaviour == NpcBehaviour.NONE)
                        return@let
                    val dx: Double = player.x - entity.x
                    var dy: Double = player.y - entity.y
                    val dz: Double = player.z - entity.z

                    val active = dx * dx + dy * dy + dz * dz < 196

                    dy /= sqrt(dx * dx + dz * dz)
                    val yaw = if (active) (atan2(-dx, dz) / Math.PI * 180).toFloat() else data.data.yaw

                    entity.apply {
                        rotationYawHead = yaw
                        setYaw(yaw)
                        setPitch((atan(-dy) / Math.PI * 180).toFloat())
                    }
                }
            }
        }
    }
}