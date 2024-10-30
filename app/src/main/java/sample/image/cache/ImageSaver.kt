package sample.image.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ImageSaver(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(32)
) {
    private val cacheFolder: File = File(context.cacheDir, "pokemon")
        get() {
            if (!field.exists()) {
                field.mkdir()
            }
            return field
        }
    private val cacheMutex = Mutex()

    suspend fun bitmaps(urls: List<String>): List<Bitmap> = withContext(dispatcher) {
        cacheMutex.withLock {
            urls.mapNotNull { url ->
                val file = photoCacheFile(url)
                if (file.exists()) {
                    updateFileAccessTime(file)
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    null
                }
            }
        }
    }

    suspend fun hasImage(url: String): Boolean = withContext(dispatcher) {
        cacheMutex.withLock {
            photoCacheFile(url).let { file ->
                if (file.exists()) {
                    updateFileAccessTime(file)
                    true
                } else {
                    false
                }
            }
        }
    }

    suspend fun saveImage(url: String, bitmap: Bitmap) = withContext(dispatcher) {
        cacheMutex.withLock {
            val file = photoCacheFile(url)
            file.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
            updateFileAccessTime(file)
            manageDiskCacheSize()
        }
    }

    suspend fun clear() = withContext(dispatcher) {
        cacheMutex.withLock {
            cacheFolder.deleteRecursively()
        }
    }

    private fun updateFileAccessTime(file: File) {
        file.setLastModified(System.currentTimeMillis())
    }

    private fun manageDiskCacheSize() {
        val files = cacheFolder.listFiles() ?: return
        var totalSize = files.sumOf { it.length() }
        val maxSize = MAX_DISK_CACHE_SIZE

        if (totalSize > maxSize) {
            val sortedFiles = files.sortedBy { it.lastModified() }
            for (file in sortedFiles) {
                if (totalSize <= maxSize) break
                totalSize -= file.length()
                file.delete()
            }
        }
    }

    private fun photoCacheFile(url: String) = File(cacheFolder, formatImageName(url))

    private fun formatImageName(path: String): String {
        return path.substringAfterLast("/").substringBefore(".") + EXTENSION
    }

    private companion object {
        const val EXTENSION = ".png"
        private const val MAX_DISK_CACHE_SIZE = 10 * 1024 * 1024 // 10MB
    }
}