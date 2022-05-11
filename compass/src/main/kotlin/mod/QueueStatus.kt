package mod

import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.resource.ResourceLocation
import dev.xdark.feder.NetUtil
import io.netty.buffer.Unpooled
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*
import sun.security.jgss.GSSToken.readInt

private const val margin = 3
private const val width = 140.0

context(KotlinMod)
class QueueStatus {
    private var counter = 0
    private var total = 0
    private var need = 80

    private lateinit var icon: RectangleElement
    private lateinit var title: TextElement
    private lateinit var online: TextElement
    private lateinit var time: TextElement
    private lateinit var background: RectangleElement
    private lateinit var cancel: RectangleElement

    private val box = rectangle {
        scale = V3(1.5, 1.5)
        enabled = false

        align = TOP
        origin = TOP
        offset.y += -width + 15

        size = V3(width, width / 4.0 * 1.2857142857)

        icon = +rectangle {
            size = V3(width / 4.0, width / 4.0 * 1.2857142857)
            color = WHITE
            align = TOP_LEFT
            origin = TOP_LEFT

            time = +text {
                align = TOP
                origin = TOP
                color = WHITE
                shadow = true
                scale = V3(0.9, 0.9)
                offset.y += margin
            }
        }

        background = +rectangle {
            size = V3(width - width / 4.0, width / 4.0)
            color = Color(0, 0, 0, 0.62)
            align = TOP_RIGHT
            origin = TOP_RIGHT
            title = +text {
                align = TOP_LEFT
                origin = TOP_LEFT
                offset.x += margin + 0.2
                offset.y += margin
                scale = V3(0.9, 0.9)
                content = "Загрузка..."
                color = WHITE
                shadow = true
            }
            online = +text {
                align = TOP_LEFT
                origin = TOP_LEFT
                offset.x += margin + 0.2
                offset.y += margin + 13
                scale = V3(0.9, 0.9)
                content = "§b0 из $need"
                color = WHITE
                shadow = true
            }
        }

        cancel = +rectangle {
            align = BOTTOM_RIGHT
            origin = BOTTOM_RIGHT
            size = V3(width / 4 * 3, width / 4.0 * 1.2857142857 - width / 4)
            color = Color(255, 0, 0, 0.62)
            offset.y -= 1

            +text {
                align = LEFT
                origin = LEFT
                color = WHITE
                scale = V3(0.9, 0.9)
                offset.x += margin + 0.2
                content = "Покинуть очередь"
            }

            +text {
                align = RIGHT
                origin = RIGHT
                color = WHITE
                scale = V3(0.9, 0.9)
                offset.x -= margin
                content = ">"
            }

            onClick {
                UIEngine.clientApi.clientConnection().sendPayload("queue:leave", Unpooled.buffer())
            }
        }
    }

    init {
        UIEngine.overlayContext + box

        registerChannel("queue:show") {
            if (!box.enabled) {
                box.animate(0.4, Easings.BACK_OUT) {
                    offset.y = 15.0
                }
            }

            before = System.currentTimeMillis()

            val address = NetUtil.readUtf8(this)
            val name = NetUtil.readUtf8(this)
            need = readInt()

            icon.textureLocation = ResourceLocation.of("games5e", address)
            title.content = name
            online.content = "§7$total из $need"

            box.enabled = true
        }

        registerChannel("queue:online") {
            val address = NetUtil.readUtf8(this)
            val currentTotal = readInt()

            if (address == icon.textureLocation?.path) {
                total = currentTotal
                println("update online")
            }

            if (counter >= 300) {
                UIEngine.clientApi.clientConnection().sendPayload("queue:leave", Unpooled.buffer())
            }
        }

        registerChannel("queue:hide") {
            box.animate(0.25, Easings.QUART_IN) {
                offset.y = -width + 15
            }
            UIEngine.schedule(0.26) {
                counter = 0
                box.enabled = false
            }
        }

        var before = System.currentTimeMillis()

        registerHandler<GameLoop> {
            if (!box.enabled)
                return@registerHandler
            val now = System.currentTimeMillis()

            if (now - before > 1000) {
                before = now
                counter++
                time.content = "⏰ ${counter / 60}:${(counter % 60).toString().padStart(2, '0')}"
                online.content = "§7$total из $need"
            }
        }
    }
}
