package me.func.compass

import dev.xdark.clientapi.resource.ResourceLocation
import ru.cristalix.uiengine.UIEngine.clientApi
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
                    Files.newInputStream(path).use {
                        ImageIO.read(it)
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    val url = URL(photo.url)
                    val bytes = url.openStream().readBytes()
                    Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                    val i = ImageIO.read(ByteArrayInputStream(bytes))
                    i
                }
                val mc = clientApi.minecraft()
                val renderEngine = clientApi.renderEngine()
                mc.execute {
                    renderEngine.loadTexture(photo.location, renderEngine.newImageTexture(image, false, false))
                    future.complete(null)
                }
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
    }
    return future
}
