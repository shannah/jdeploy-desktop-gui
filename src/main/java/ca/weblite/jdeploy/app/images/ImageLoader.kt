package ca.weblite.jdeploy.app.images

import ca.weblite.jdeploy.app.cache.FileSystemCache
import ca.weblite.swinky.coroutines.SwingDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Component
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoader() {
    private lateinit var fileSystemCache: FileSystemCache

    @Inject
    constructor(fileSystemCache: FileSystemCache) : this() {
        this.fileSystemCache = fileSystemCache
    }

    suspend fun loadImage(url: URL, targetWidth: Int, component: Component): BufferedImage {
        val scaledWidth = scaleWidth(component, targetWidth)
        val cacheDir = getImageCacheDir()
        val cacheKey = getCacheKey(url, scaledWidth)
        val cacheFile = File(cacheDir, "$cacheKey.png")

        if (cacheFile.exists()) {
            return loadImageFromFile(cacheFile)
        }

        val image = loadImageFromUrl(url)
        val resizedImage = scaleImageIfNeeded(image, scaledWidth)
        withContext(Dispatchers.IO) {
            cacheFile.parentFile.mkdirs()
            ImageIO.write(resizedImage, "png", cacheFile)
        }

        return resizedImage
    }

    private suspend fun scaleImageIfNeeded(image: BufferedImage, targetWidth: Int): BufferedImage {
        if (targetWidth >= image.width) {
            return image;
        }

        // Perform high-quality scaling
        val aspectRatio = image.height.toDouble() / image.width
        val newHeight = (targetWidth * aspectRatio).toInt()

        val scaledImage = BufferedImage(targetWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
        withContext(Dispatchers.IO){
            val g2d = scaledImage.createGraphics()
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2d.drawImage(image, 0, 0, targetWidth, newHeight, null)
            g2d.dispose()
        }

        return scaledImage
    }
    private fun getCacheKey(url: URL, targetWidth: Int): String {
        return fileSystemCache.getCacheKey(url, targetWidth.toString())
    }

    private fun getImageCacheDir(appName: String = "jdeploy"): File {
        return fileSystemCache.getCacheDir()
    }

    private suspend fun scaleWidth(component: Component, width: Int): Int {
        return withContext(SwingDispatcher) {
            val graphics = component.graphicsConfiguration
            val transform = graphics.defaultTransform
            Math.round(transform.scaleX * width).toInt()
        }
    }

    private suspend fun loadImageFromUrl(url: URL): BufferedImage {
        return withContext(Dispatchers.IO) {
            ImageIO.read(url)
        }
    }

    private suspend fun loadImageFromFile(file: File): BufferedImage {
        return withContext(Dispatchers.IO) {
            ImageIO.read(file)
        }
    }

}