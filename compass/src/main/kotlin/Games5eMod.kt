import com.google.gson.Gson
import dev.implario.games5e.QueueProperties
import dev.xdark.clientapi.event.lifecycle.GameLoop
import implario.humanize.Humanize
import io.netty.buffer.Unpooled
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.clientapi.readId
import ru.cristalix.clientapi.readUtf8
import ru.cristalix.clientapi.readVarInt
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.element.debug
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

lateinit var mod: Games5eMod

class Games5eMod : KotlinMod() {

    lateinit var money: TextElement
    
    override fun onEnable() {
        mod = this
        UIEngine.initialize(this)

        val queuesScreen = QueuesScreen()
        val gson = Gson()

        QueueStatus

        registerChannel("queues:data") {
            queuesScreen.init(gson.fromJson(readUtf8(), Array<QueueProperties>::class.java))
        }

        registerChannel("g5e:open") {
            queuesScreen.open()
        }

        registerChannel("g5e:q") {
            repeat(readVarInt()) {
                val id = readId()
                val online = readVarInt()
                val queue = readVarInt()
                for (child in queuesScreen.queuesContainer.children) {
                    child as QueueElement
                    if (child.info.queueId == id) {
                        child.online = online
                        child.queued = queue
                    }
                }
            }
        }

        registerHandler<GameLoop> { debug = Keyboard.isKeyDown(Keyboard.KEY_F12) }

        loadTextureFromJar("games5e", "clock", "clock.png")
        loadTextureFromJar("games5e", "face", "face.png")
        loadTextureFromJar("games5e", "clockthin", "clockthin.png")
        loadTextureFromJar("games5e", "facethin", "facethin.png")

        clientApi.clientConnection().sendPayload("g5e:loaded", Unpooled.EMPTY_BUFFER)

        money = UIEngine.overlayContext + text {
            align = BOTTOM
            origin = BOTTOM
            shadow = true
            color = Color(238, 130, 238)
            offset.y -= 30
        }

        registerChannel("arcade:money") {
            val value = readInt()
            val new = "$value ${
                Humanize.plurals(
                    "жетон",
                    "жетона",
                    "жетонов",
                    value
                )
            } 㥘"

            if (new == money.content)
                return@registerChannel

            money.content = new
            money.animate(0.2) {
                color = WHITE
                offset.y -= 8
            }
            UIEngine.schedule(0.2) {
                money.animate(0.45) {
                    color = Color(238, 130, 238)
                    offset.y += 8
                }
            }
        }
    }

}