package com.example.demoapp

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache


object CachedImage {

    private var memoryCache: LruCache<String, Bitmap>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    fun getBitmapFromMemCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }

    fun addImageToMemCache(key: String, value: Bitmap) {
        if (memoryCache.get(key) == null) {
            memoryCache.put(key, value)
        }
    }

    fun clearCache() {
        memoryCache.evictAll()
    }

}