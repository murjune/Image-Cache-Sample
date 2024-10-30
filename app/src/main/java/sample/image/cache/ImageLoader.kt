package sample.image.cache

import android.graphics.Bitmap

class ImageLoader(
    private val ImageService: PokemonImageService
) {
    suspend fun bitmaps(urls: List<String>): List<Bitmap> {
        return ImageService.bitmaps(urls)
    }
}