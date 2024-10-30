package sample.image.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ImageSaver(context: Context) {
    private val cacheFolder: File = File(context.cacheDir, "pokemon")
        get() {
            if (!field.exists()) {
                field.mkdir()
            }
            return field
        }

    suspend fun bitmaps(urls: List<String>): List<Bitmap> = withContext(Dispatchers.IO) {
        urls.mapNotNull { url ->
            val file = photoCacheFile(url)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        }
    }

    suspend fun hasImage(url: String): Boolean = withContext(Dispatchers.IO) {
        photoCacheFile(url).exists()
    }

    suspend fun saveImage(url: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        photoCacheFile(url).outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        cacheFolder.deleteRecursively()
    }

    private fun photoCacheFile(url: String) = File(cacheFolder, formatImageName(url))

    private fun formatImageName(path: String): String {
        return path.substringAfterLast("/").substringBefore(".") + EXTENSION
    }

    private companion object {
        const val EXTENSION = ".jpg"
    }
}