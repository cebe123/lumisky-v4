package com.example.lumisky.ui.catalog

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.lumisky.data.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class CatalogThumbnailTargetSize(val widthPx: Int, val heightPx: Int)

object CatalogStartupWarmupPolicy {
    fun resolveTargetSize(screenWidthDp: Int, screenHeightDp: Int, density: Float): CatalogThumbnailTargetSize {
        val shortEdge = min(screenWidthDp, screenHeightDp).coerceAtLeast(1)
        val longEdge = max(screenWidthDp, screenHeightDp).coerceAtLeast(shortEdge)
        val aspect = (longEdge.toFloat() / shortEdge.toFloat()).coerceIn(1.65f, 2.25f) * 0.9f
        val widthPx = (CARD_WIDTH_DP * density).roundToInt().coerceAtLeast(1)
        return CatalogThumbnailTargetSize(widthPx, (CARD_WIDTH_DP * aspect * density).roundToInt().coerceAtLeast(1))
    }

    fun uniqueThumbnailPaths(paths: List<String>): List<String> = paths.filter(String::isNotBlank).distinct()

    fun thumbnailBatches(paths: List<String>, maxParallelism: Int): List<List<String>> {
        return uniqueThumbnailPaths(paths).chunked(maxParallelism.coerceAtLeast(1))
    }

    private const val CARD_WIDTH_DP = 276f
}

object CatalogThumbnailLoader {
    suspend fun load(context: Context, path: String, targetWidthPx: Int, targetHeightPx: Int): Bitmap? {
        val key = CatalogThumbnailMemoryCache.key(path, targetWidthPx, targetHeightPx)
        CatalogThumbnailMemoryCache.get(key)?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
                val options = BitmapFactory.Options().apply {
                    inSampleSize = CatalogThumbnailDecodePolicy.calculateInSampleSize(
                        bounds.outWidth,
                        bounds.outHeight,
                        targetWidthPx,
                        targetHeightPx
                    )
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
                    ?.also { CatalogThumbnailMemoryCache.put(key, it) }
            }.getOrNull()
        }
    }
}

suspend fun warmCatalogForLaunch(context: Context, repository: WallpaperRepository) {
    val catalog = repository.getCatalog()
    val configuration = context.resources.configuration
    val size = CatalogStartupWarmupPolicy.resolveTargetSize(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        context.resources.displayMetrics.density
    )
    coroutineScope {
        CatalogStartupWarmupPolicy.thumbnailBatches(
            paths = catalog.wallpapers.map { it.thumbnail },
            maxParallelism = MAX_PARALLEL_THUMBNAIL_WARMUPS
        ).forEach { batch ->
            batch.map { path ->
                async {
                    CatalogThumbnailLoader.load(context, path, size.widthPx, size.heightPx)
                }
            }.awaitAll()
        }
    }
}

private const val MAX_PARALLEL_THUMBNAIL_WARMUPS = 4
