import dev.xdark.clientapi.event.render.RenderTickPre
import dev.xdark.clientapi.opengl.GlStateManager
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*
import java.util.*

context(KotlinMod)
class Banners {

    private val banners = mutableMapOf<UUID, Pair<Banner, Context3D>>()
    private val sizes = mutableMapOf<Pair<UUID, Int>, Double>()

    private fun toBlackText(string: String) = "¨222200" + string.replace(Regex("(§[0-9a-fA-F]|¨......)"), "¨222200")

    init {
        fun text(text: String, banner: Banner, rectangle: RectangleElement) {
            text.split("\n").forEachIndexed { index, line ->
                val currentSize = sizes[banner.uuid to index] ?: 1.0
                val v3 = V3(currentSize, currentSize, currentSize)

                rectangle + text {
                    align = TOP
                    origin = TOP
                    content = line
                    size = V3(banner.weight.toDouble(), banner.height.toDouble())
                    color = WHITE
                    offset.z = -0.01
                    offset.y = -(-3 - index * 12) * currentSize
                    scale = v3
                }
                rectangle + text {
                    align = TOP
                    origin = TOP
                    content = toBlackText(line)
                    size = V3(banner.weight.toDouble(), banner.height.toDouble())
                    color = Color(0, 0, 0, 0.82)
                    offset.z = -0.005
                    offset.y = -(-3 - index * 12 - 0.75) * currentSize
                    offset.x += 0.75 * currentSize
                    scale = v3
                }
            }
        }

        mod.registerChannel("banner:new") {
            repeat(readInt()) {
                val uuid = UUID.fromString(NetUtil.readUtf8(this))
                val banner = Banner(
                    uuid,
                    MotionType.values()[readInt()],
                    readBoolean(),
                    mutableMapOf(
                        "yaw" to readDouble(),
                        "pitch" to readDouble(),
                    ), NetUtil.readUtf8(this@registerChannel),
                    readDouble(),
                    readDouble(),
                    readDouble(),
                    readInt(),
                    readInt(),
                    NetUtil.readUtf8(this@registerChannel),
                    readInt(),
                    readInt(),
                    readInt(),
                    readDouble()
                )

                val context = Context3D(V3(banner.x, banner.y, banner.z))
                banners[uuid] = banner to context

                context.addChild(rectangle {
                    if (banner.texture.isNotEmpty()) {
                        val parts = banner.texture.split(":")
                        textureLocation = UIEngine.clientApi.resourceManager().getLocation(parts[0], parts[1])
                    }
                    if (banner.content.isNotEmpty()) {
                        text(banner.content, banner, this)
                    }

                    size = V3(banner.weight.toDouble(), banner.height.toDouble())
                    color = Color(banner.red, banner.green, banner.blue, banner.opacity)
                    context.rotation =
                        Rotation(Math.toRadians(banner.motionSettings["yaw"].toString().toDouble()), 0.0, 1.0, 0.0)
                    rotation =
                        Rotation(Math.toRadians(banner.motionSettings["pitch"].toString().toDouble()), 1.0, 0.0, 0.0)

                    if (banner.motionSettings["xray"].toString().toBoolean()) {
                        beforeRender = {
                            GlStateManager.disableDepth()
                        }
                        afterRender = {
                            GlStateManager.enableDepth()
                        }
                    }
                })
                UIEngine.worldContexts.add(context)
            }
        }

        mod.registerChannel("banner:change-content") {
            val uuid = UUID.fromString(NetUtil.readUtf8(this))
            banners[uuid]?.let { pair ->
                val element = (pair.second.children[0] as RectangleElement)
                element.children.clear()
                text(NetUtil.readUtf8(this), pair.first, element)
            }
        }

        mod.registerChannel("banner:size-text") {
            val uuid = UUID.fromString(NetUtil.readUtf8(this))
            banners[uuid]?.let { pair ->
                repeat(readInt()) {
                    val line = readInt()
                    val newScale = readDouble()

                    sizes[uuid to line] = newScale
                    val element = pair.second.children[0] as RectangleElement
                    element.children[line * 2].animate(0.2) {
                        scale = V3(newScale, newScale, newScale)
                        offset.y = -(-3 - line * 12) * newScale
                    }
                    element.children[line * 2 + 1].animate(0.2) {
                        scale = V3(newScale, newScale, newScale)
                        offset.y = -(-3 - line * 12 - 0.75) * newScale
                    }
                }
            }
        }

        registerHandler<RenderTickPre> {
            val player = UIEngine.clientApi.minecraft().player
            val timer = UIEngine.clientApi.minecraft().timer
            val yaw =
                (player.rotationYaw - player.prevRotationYaw) * timer.renderPartialTicks + player.prevRotationYaw
            val pitch =
                (player.rotationPitch - player.prevRotationPitch) * timer.renderPartialTicks + player.prevRotationPitch

            banners.filter { it.value.first.watchingOnPlayer }.forEach {
                it.value.second.rotation = Rotation(-yaw * Math.PI / 180 + Math.PI, 0.0, 1.0, 0.0)
                it.value.second.children[0].rotation = Rotation(-pitch * Math.PI / 180, 1.0, 0.0, 0.0)
            }
        }
    }
}
