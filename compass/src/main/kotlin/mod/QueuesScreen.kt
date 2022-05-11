package mod

import dev.implario.games5e.QueueProperties
import ru.cristalix.clientapi.JavaMod
import ru.cristalix.uiengine.UIEngine.clientApi
import ru.cristalix.uiengine.element.ContextGui
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

class QueuesScreen: ContextGui() {

    val queuesContainer = +flex {
        flexSpacing = 8.0

        align = CENTER
        origin = CENTER

        scale = V3(0.7, 0.7, 0.7)

        overflowWrap = true
        beforeTransform {
            var p = ((this@QueuesScreen.size.x - 40.0 - 72.0) / (flexSpacing + 72.0)).toInt() + 1
            if (p > children.size) p = children.size
            size.x = p * 80.0 - 8.0
        }
    }

    init {
        color.alpha = 0.86
        beforeTransform {
            val factor = clientApi.resolution().scaleFactor
            queuesContainer.offset.y = -(size.y % factor) / factor
        }
    }

    fun init(queues: Array<QueueProperties>) {
        fun queue(info: QueueProperties, builder: QueueElement.() -> Unit) =
            QueueElement(info).apply(builder)

        queuesContainer.children.clear()
        for (queue in queues) {
            queuesContainer + queue(queue) {}
        }
        queuesContainer.update()
    }
}
