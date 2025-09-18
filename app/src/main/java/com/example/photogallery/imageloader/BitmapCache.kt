package com.example.photogallery.imageloader

import android.graphics.Bitmap
import android.util.LruCache

//memory cache for bitmaps
object BitmapCache {
    private val cacheSize = (Runtime.getRuntime().maxMemory() / 8).toInt()
    private val lru = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
    fun get(key: String): Bitmap? = lru.get(key)
    fun put(key: String, bmp: Bitmap) { lru.put(key, bmp) }
}
