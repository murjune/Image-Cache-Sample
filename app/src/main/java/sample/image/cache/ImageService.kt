package sample.image.cache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ImageService {
    suspend fun bitmaps(urls: List<String>): List<Bitmap> = withContext(Dispatchers.IO) {
        urls.map { url ->
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.run {
                doInput = true
                connect()
                inputStream.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }
        }
    }
}