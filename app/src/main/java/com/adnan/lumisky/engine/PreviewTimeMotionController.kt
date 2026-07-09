/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Engine katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Engine katmanı bileşeni.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.definition.WallpaperDefinition
import java.time.Instant
import java.time.ZoneId
import kotlin.math.floor

data class PreviewFocusCatchUpWindow(
    val startProgress: Float,
    val targetProgress: Float
)

class PreviewTimeMotionController(
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {
    private var activeWallpaperId: String? = null
    private var animationElapsedSeconds: Float = 0f
    private var window: PreviewFocusCatchUpWindow = PreviewFocusCatchUpWindow(0f, 0f)
    private var completedProgress: Float? = null

    fun reset(wallpaperId: String?) {
        activeWallpaperId = wallpaperId
        animationElapsedSeconds = 0f
        completedProgress = null
        window = PreviewFocusCatchUpWindow(0f, 0f)
    }

    fun resolveDayProgress(
        definition: WallpaperDefinition?,
        deltaTimeSeconds: Float,
        durationSeconds: Float = 8f
    ): Float {
        val wallpaperId = definition?.id
        if (wallpaperId != activeWallpaperId) {
            reset(wallpaperId)
        }
        animationElapsedSeconds += deltaTimeSeconds.coerceAtLeast(0f)
        val loopProgress = animationElapsedSeconds / durationSeconds
        return wrapDayProgress(loopProgress)
    }

    fun resolveFocusCatchUpWindow(
        nowProgress: Float,
        sunriseMinute: Int,
        sunsetMinute: Int
    ): PreviewFocusCatchUpWindow {
        val normalizedNow = nowProgress.coerceIn(0f, 1f)
        val normalizedSunrise = sunriseMinute.coerceIn(0, MINUTES_PER_DAY)
        val normalizedSunset = sunsetMinute.coerceIn(0, MINUTES_PER_DAY)
        val sunriseProgress = (normalizedSunrise / MINUTES_PER_DAY.toFloat()).coerceIn(0f, 1f)
        val nowMinute = (normalizedNow * MINUTES_PER_DAY).toInt().coerceIn(0, MINUTES_PER_DAY)
        val isDaytime = if (normalizedSunset >= normalizedSunrise) {
            nowMinute in normalizedSunrise..normalizedSunset
        } else {
            nowMinute >= normalizedSunrise || nowMinute <= normalizedSunset
        }
        val targetProgress = when {
            isDaytime -> 1f + normalizedNow
            normalizedNow >= sunriseProgress -> normalizedNow
            else -> normalizedNow + 1f
        }
        return PreviewFocusCatchUpWindow(
            startProgress = sunriseProgress,
            targetProgress = targetProgress
        )
    }

    private fun currentDayProgress(definition: WallpaperDefinition?): Float {
        val zone = definition?.daylight?.timeZoneId
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { ZoneId.of(it) }.getOrNull() }
            ?: ZoneId.systemDefault()
        val localTime = Instant.ofEpochMilli(nowProvider())
            .atZone(zone)
            .toLocalTime()
        val progress = localTime.toNanoOfDay().toDouble() / NANOS_PER_DAY.toDouble()
        return progress.toFloat().coerceIn(0f, 1f)
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + ((end - start) * t.coerceIn(0f, 1f))
    }

    private fun wrapDayProgress(value: Float): Float {
        val wrapped = value - floor(value)
        return wrapped.coerceIn(0f, 1f)
    }

    companion object {
        private const val DEFAULT_FOCUS_CATCH_UP_SECONDS = 4f
        private const val MIN_FOCUS_CATCH_UP_SECONDS = 0.3f
        private const val MINUTES_PER_DAY = 24 * 60
        private const val DEFAULT_SUNRISE_MINUTE = 360
        private const val DEFAULT_SUNSET_MINUTE = 1080
        private const val NANOS_PER_DAY = 24L * 60L * 60L * 1_000_000_000L
    }
}
