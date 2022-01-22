import com.google.gson.Gson
import dev.implario.games5e.QueueProperties
import io.netty.buffer.Unpooled
import org.lwjgl.opengl.GL11.*
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.clientapi.readId
import ru.cristalix.clientapi.readUtf8
import ru.cristalix.clientapi.readVarInt
import ru.cristalix.uiengine.UIEngine

lateinit var mod: Games5eMod

class Games5eMod : KotlinMod() {

    override fun onEnable() {
        mod = this
        UIEngine.initialize(this)

        val queuesScreen = QueuesScreen()
        val gson = Gson()

        registerChannel("queues:data") {
            queuesScreen.init(gson.fromJson(readUtf8(), Array<QueueProperties>::class.java))
        }

        registerChannel("g5e:open") {
            queuesScreen.open()
        }

        registerChannel("g5e:q") {
            for (i in 0..readVarInt()) {
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

        loadTextureFromJar("games5e", "clock", "clock.png")
        loadTextureFromJar("games5e", "face", "face.png")
        loadTextureFromJar("games5e", "clockthin", "clockthin.png")
        loadTextureFromJar("games5e", "facethin", "facethin.png")

        clientApi.clientConnection().sendPayload("g5e:loaded", Unpooled.EMPTY_BUFFER)
    }

}