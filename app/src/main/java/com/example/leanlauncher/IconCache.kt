package com.example.leanlauncher
import android.graphics.drawable.Drawable
import android.util.LruCache
object IconCache {
    private val cache = object : LruCache<String, Drawable>(96) {
        override fun sizeOf(key: String, value: Drawable): Int = 1
    }
    fun get(pkg: String): Drawable? = cache.get(pkg)
    fun put(pkg: String, d: Drawable) { cache.put(pkg, d) }
    fun clear() = cache.evictAll()
}
