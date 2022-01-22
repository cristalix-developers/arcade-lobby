import dev.implario.games5e.QueueProperties
import ru.cristalix.clientapi.JavaMod
import ru.cristalix.uiengine.element.ContextGui
import ru.cristalix.uiengine.utility.*

class QueuesScreen: ContextGui() {

    val queuesContainer = +flex {
        flexSpacing = 8.0

        align = CENTER
        origin = CENTER
    }

    init {
        color.alpha = 0.82
        beforeTransform {
            val factor = JavaMod.clientApi.resolution().scaleFactor
            queuesContainer.offset.y = -(size.y % factor) / factor
        }
    }

    fun init(queues: Array<QueueProperties>) {
        queuesContainer.children.clear()
        for (queue in queues) {
            queuesContainer + queue(queue) {}
        }
        queuesContainer.update()
    }

}
