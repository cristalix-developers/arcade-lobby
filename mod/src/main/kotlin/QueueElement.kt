import dev.implario.games5e.QueueProperties
import dev.xdark.clientapi.opengl.GlStateManager
import dev.xdark.clientapi.resource.ResourceLocation
import org.lwjgl.opengl.GL11
import ru.cristalix.clientapi.JavaMod
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.onMouseDown
import ru.cristalix.uiengine.utility.*

inline fun queue(info: QueueProperties, builder: QueueElement.() -> Unit) =
    QueueElement(info).also(builder)

val namePattern = Regex("^[a-z0-9A-Z_-]+$")

class QueueElement(val info: QueueProperties) : RectangleElement() {

    private val address = info.tags?.get("address")
    private val iconUrl = info.tags?.get("icon_url")
    private val iconHash = info.tags?.get("icon_hash")
    private val queueMask = +rectangle {
        offset.z = 5.0
        size.x = 36.0
        size.y = 14.0
        origin = TOP_RIGHT
        align = TOP_RIGHT
        color = WHITE
        color.alpha = 0.00005
        beforeRender {
            // GlStateManager.blendFunc(GL11.GL_ZERO, GL11.GL_ZERO)
            GlStateManager.disableAlpha()
            GlStateManager.depthMask(true)
            GlStateManager.enableDepth()
        }
    }

    val icon = +rectangle {
        size = V3(72.0, 72.0 + 14.0)
        offset.z = 1.0
        color = WHITE

        if (address != null && iconUrl != null && iconHash != null) {
            if (!address.matches(namePattern)) {
                println("Invalid address for queue ${info.queueId}: $address")
            } else if (!iconHash.matches(namePattern)) {
                println("Invalid icon hash for queue ${info.queueId}: $iconHash")
            } else {
                val location = ResourceLocation.of("games5e", address)
                loadTextures(RemoteTexture(location, iconUrl, iconHash)).thenAccept {
                    println("hello")
                    textureLocation = location
                }
            }
        }

        val rect = this
        val overlay = +rectangle {
            size = rect.size
            color = BLACK
            color.alpha = 0.0
            +text {
                color.alpha = 0.0
                offset.y = 7.0
                content = "Играть"
                align = CENTER
                origin = CENTER
                beforeTransform {
                    val factor = if (JavaMod.clientApi.fontRenderer().unicodeFlag) 2.0 else 1.5
                    scale.x = factor
                    scale.y = factor
                }
            }
            onMouseDown {
                JavaMod.clientApi.chat().sendChatMessage("/queue " + info.queueId)
            }
        }

        onHover {
            animate(0.2, Easings.CUBIC_OUT) {
                overlay.color.alpha = if (hovered) 0.5 else 0.0
                overlay.children[0].color.alpha = if (hovered) 1.0 else 0.0
            }
        }

        beforeRender {
            GlStateManager.depthFunc(GL11.GL_LEQUAL)
            GlStateManager.enableDepth()
        }
        afterRender {
            GlStateManager.disableDepth()
        }


    }

    private val queueFlex = queueMask + flex {
        flexSpacing = 3.0
        align = CENTER
        origin = CENTER
        flexDirection = FlexDirection.RIGHT
    }

    init {
        queueFlex + rectangle {
            size = V3(8.0, 8.0)
            beforeRender {
                val unicode = JavaMod.clientApi.fontRenderer().unicodeFlag
                val texturePath = if (unicode) "clockthin" else "clock"
                if (texturePath != textureLocation?.path) {
                    textureLocation = ResourceLocation.of("games5e", texturePath)
                }
            }
            color = WHITE
            color.alpha = 0.62
        }
        size = V3(72.0, 72.0 + 18.0)
    }


    private val queueText = queueFlex + text {
        color.alpha = 0.62
        shadow = true
    }

    var queued: Int = -1
    set(value) {
        field = value
        if (value < 0) {
            queueMask.enabled = false
        } else {
            queueMask.enabled = true
            // ToDo: Short form for values beyond 1k
            queueText.content = "$value"
        }
    }

    private val onlineText = icon + text {
        offset.y = 2.5
        shadow = true
        origin.x = 0.5
        offset.x = 72.0 / 4
        beforeTransform {
            val unicode = JavaMod.clientApi.fontRenderer().unicodeFlag
            scale.x = if (unicode) 2.0 else 1.5
            scale.y = if (unicode) 2.0 else 1.5
        }
    }

    var onlineEnabled = false

    var online: Int = 0
        set(value) {
            field = value

            val prevOnlineEnabled = onlineEnabled

            if (value < 0) {
                onlineText.content = "§7OFF"
                onlineEnabled = false
            } else {
                // ToDo: Short form for values beyond 1k
                onlineText.content = "$value"
                onlineEnabled = true
            }
            if (prevOnlineEnabled != onlineEnabled) icon.animate(0.3) {
                color.alpha = if (onlineEnabled) 1.0 else 0.5
            }
        }

    val title = icon + text {
        origin = TOP
        align = BOTTOM
        offset.y = 3.0
        content = info.tags?.get("name_ru") ?: info.imageId ?: "???"
    }


}