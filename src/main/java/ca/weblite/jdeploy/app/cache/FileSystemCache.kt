package ca.weblite.jdeploy.app.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.URL
import javax.inject.Singleton

@Singleton
class FileSystemCache {

    suspend fun purge(url: URL, variant: String) {
        val cacheFile = getCacheFile(url, variant)
        if (cacheFile.exists()) {
            withContext(Dispatchers.IO) {
                cacheFile.delete()
            }
        }
    }

    suspend fun load(url: URL, variant: String): InputStream {
        val cacheFile = getCacheFile(url, variant)
        if (cacheFile.exists()) {
            return cacheFile.inputStream()
        }

        // Load from the URL and save to cache
        withContext(Dispatchers.IO) {
            val inputStream = url.openStream()
            cacheFile.parentFile.mkdirs()
            inputStream.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        if (!cacheFile.exists()) {
            throw Exception("Failed to load file from URL: $url")
        }

        return cacheFile.inputStream()
    }

    fun getCacheFile(url: URL, variant: String): File {
        val cacheDir = getCacheDir()
        val cacheKey = getCacheKey(url, variant)
        return File(cacheDir, cacheKey)
    }

    fun getCacheDir(appName: String = "jdeploy"): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        val cacheDir = when {
            os.contains("mac") -> File(userHome, "Library/Caches/$appName")
            os.contains("win") -> File(System.getenv("LOCALAPPDATA") ?: "$userHome/AppData/Local", "$appName/cache")
            else -> File(System.getenv("XDG_CACHE_HOME") ?: "$userHome/.cache", appName)
        }

        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        return cacheDir
    }

    fun getCacheKey(url: URL, variant: String): String {
        // generate a unique key based on the URL and target width that is file-name friendly
        val key =  "${url.toString().replace(":", "_").replace("/", "_")}_$variant"

        // generate md5 hash of the key
        val md = java.security.MessageDigest.getInstance("MD5")
        val hash = md.digest(key.toByteArray()).joinToString("") { "%02x".format(it) }

        return hash
    }
}