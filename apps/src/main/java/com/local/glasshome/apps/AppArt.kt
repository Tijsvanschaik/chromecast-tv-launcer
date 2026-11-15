package com.local.glasshome.apps

import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

class AppArt(val bitmap: ImageBitmap, val isBanner: Boolean)

/**
 * Leanback banner (16:9) when the app has one, else its icon. Rasterized once at TV banner
 * size into a byte-bounded cache, two decodes at a time so cold start stays responsive on HD.
 */
object AppArtCache {
    private const val BANNER_W = 320
    private const val BANNER_H = 180
    private const val ICON_PX = 96
    private const val MAX_BYTES = 12 * 1024 * 1024

    private val cache = object : LruCache<String, AppArt>(MAX_BYTES) {
        override fun sizeOf(key: String, value: AppArt): Int = value.bitmap.width * value.bitmap.height * 4
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val decodeDispatcher = Dispatchers.IO.limitedParallelism(2)

    fun peek(packageName: String): AppArt? = cache.get(packageName)

    fun evict(packageName: String) {
        cache.remove(packageName)
    }

    suspend fun load(packageManager: PackageManager, app: AppEntry): AppArt? {
        cache.get(app.packageName)?.let { return it }
        return withContext(decodeDispatcher) {
            runCatching { decode(packageManager, app) }.getOrNull()?.also { cache.put(app.packageName, it) }
        }
    }

    private fun decode(packageManager: PackageManager, app: AppEntry): AppArt? {
        val component = ComponentName(app.packageName, app.activityName)
        val banner = runCatching { packageManager.getActivityBanner(component) }.getOrNull()
            ?: runCatching { packageManager.getApplicationBanner(app.packageName) }.getOrNull()
        if (banner != null) {
            return AppArt(banner.toBitmap(BANNER_W, BANNER_H, Bitmap.Config.ARGB_8888).asImageBitmap(), isBanner = true)
        }
        val icon = runCatching { packageManager.getActivityIcon(component) }.getOrNull() ?: return null
        return AppArt(icon.toBitmap(ICON_PX, ICON_PX, Bitmap.Config.ARGB_8888).asImageBitmap(), isBanner = false)
    }
}
