package sample.image.cache

import androidx.collection.lruCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LRUCacheStudyTest {
    data class Foo(val name: String)

    @Test
    fun test_size_of_cache() {
        // given
        val maxCacheSize = 200
        val data = Foo("odoong")
        val sizeBuilder = { key: String, value: Foo -> key.length * value.name.length }
        val cache = lruCache<String, Foo>(maxCacheSize, sizeOf = sizeBuilder)
        // when
        cache.put("lee", data)
        // then
        assertEquals(cache.size(), 18)
    }

    @Test
    fun test_cache_replace() {
        // given
        val maxCacheSize = 30
        val data = Foo("odoong")
        val data2 = Foo("odoong2")
        val sizeBuilder = { key: String, value: Foo -> key.length * value.name.length }
        val cache = lruCache<String, Foo>(maxCacheSize, sizeOf = sizeBuilder)
        // when
        cache.put("lee", data)
        cache.put("lee2", data2) // lee to odoong 은 삭제된다
        // then
        assertFalse(cache.snapshot().contains("lee"))
        assertEquals(cache.size(), 28)
    }
}