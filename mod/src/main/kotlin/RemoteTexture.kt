import dev.xdark.clientapi.resource.ResourceLocation
import ru.cristalix.uiengine.UIEngine
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

data class RemoteTexture(val location: ResourceLocation, val url: String, val sha1: String)

const val NAMESPACE = "squidgame"

private val cacheDir = Paths.get("$NAMESPACE/")

fun loadTextures(vararg info: RemoteTexture): CompletableFuture<Nothing> {
    val future = CompletableFuture<Nothing>()
    CompletableFuture.runAsync {
        for (photo in info) {
            try {
                val cacheDir = cacheDir
                Files.createDirectories(cacheDir)
                val path = cacheDir.resolve(photo.sha1)

                val image = try {
                    println("trying use cached")
                    Files.newInputStream(path).use {
                        println("success cached")
                        ImageIO.read(it)
                    }
                } catch (ex: IOException) {
                    println("error")
                    ex.printStackTrace()
                    println("downloading")
                    val url = URL(photo.url)
                    println("url parsed")
                    val bytes = url.openStream().readBytes()
                    println("bytes read")
                    Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                    println("file written")
                    val i = ImageIO.read(ByteArrayInputStream(bytes))
                    println("image read")
                    i
                }
                val api = UIEngine.clientApi
                val mc = api.minecraft()
                val renderEngine = api.renderEngine()
                println("mc execute call")
                mc.execute {
                    println("mc execute body")
                    renderEngine.loadTexture(photo.location, renderEngine.newImageTexture(image, false, false))
                    println("texture loaded")
                    future.complete(null)
                    println("future completed")
                }
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
    }
    return future
}