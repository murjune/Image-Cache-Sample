package sample.image.cache

import android.graphics.Bitmap

class ImageLoader(
    private val imageService: ImageService
) {
    private val cachedImages: MutableMap<String, Bitmap> = mutableMapOf<String, Bitmap>()

    suspend fun bitmaps(urls: List<String>): List<Bitmap> {
        if (cachedImages.keys.containsAll(urls.toSet())) {
            return urls.map { requireNotNull(cachedImages[it]) }
        }
        return imageService.bitmaps(urls).also { cacheImages(urls, it) }
    }

    fun clearCache() {
        cachedImages.clear()
    }

    private fun cacheImages(keys: List<String>, images: List<Bitmap>) {
        keys.forEachIndexed { index, key ->
            cachedImages[key] = images[index]
        }
    }
}