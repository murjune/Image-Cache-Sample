package sample.image.cache

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.collection.lruCache

class ImageLoader(
    private val imageService: ImageService,
    private val imageSaver: ImageSaver
) {
    private val cachedImages: LruCache<String, Bitmap> =
        lruCache(cacheSize(), sizeOf = { _, value -> value.byteCount / 1024 })

    suspend fun bitmaps(urls: List<String>): List<Bitmap> {
        if (isMemoryCached(urls)) {
            return urls.map { requireNotNull(cachedImages[it]) }
        }
        if (isDiskCached(urls)) {
            return imageSaver.bitmaps(urls).also { cacheImages(urls, it) }
        }

        return imageService.bitmaps(urls)
            .also { bitmap ->
                urls.zip(bitmap).forEach { (url, bitmap) ->
                    imageSaver.saveImage(url, bitmap)
                }
            }
            .also { bitmap ->
                cacheImages(urls, bitmap)
            }
    }

    private fun isMemoryCached(urls: List<String>): Boolean {
        return urls.all { cachedImages[it] != null }
    }

    private suspend fun isDiskCached(urls: List<String>): Boolean {
        return urls.all { imageSaver.hasImage(it) }
    }

    suspend fun clearCache() {
        cachedImages.evictAll()
        imageSaver.clear()
    }

    private fun cacheImages(keys: List<String>, images: List<Bitmap>) {
        keys.forEachIndexed { index, key ->
            cachedImages.put(key, images[index])
        }
    }

    private fun cacheSize(): Int {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        return maxMemory / 8
    }
}