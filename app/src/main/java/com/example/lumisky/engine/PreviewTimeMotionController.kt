/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Engine katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Engine katmanı bileşeni.
 */
package com.example.lumisky.engine

import com.example.lumisky.definition.WallpaperDefinition
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
    private var window = PreviewFocusCatchUpWindow(0f, 0f)
    private var completedProgress: Float? = null
    private var animationActive = false

    val isAnimating: Boolean
        get() = animationActive

    fun reset(wallpaperId: String?) {
        activeWallpaperId = wallpaperId
        animationElapsedSeconds = 0f
        completedProgress = null
        animationActive = false
        window = PreviewFocusCatchUpWindow(0f, 0f)
    }

    fun startFocusAnimation(definition: WallpaperDefinition?) {
        val wallpaperId = definition?.id
        if (wallpaperId != activeWallpaperId) reset(wallpaperId)
        window = resolveFocusCatchUpWindow(
            nowProgress = currentDayProgress(definition),
            sunriseMinute = definition?.daylight?.sunriseMinute ?: DEFAULT_SUNRISE_MINUTE,
            sunsetMinute = definition?.daylight?.sunsetMinute ?: DEFAULT_SUNSET_MINUTE
        )
        animationElapsedSeconds = 0f
        completedProgress = null
        animationActive = true
    }

    fun resolveDayProgress(
        definition: WallpaperDefinition?,
        deltaTimeSeconds: Float,
        durationSeconds: Float = DEFAULT_FOCUS_CATCH_UP_SECONDS
    ): Float {
        val wallpaperId = definition?.id
        if (wallpaperId != activeWallpaperId) {
            reset(wallpaperId)
        }
        completedProgress?.let { return it }
        if (!animationActive) return currentDayProgress(definition)
        animationElapsedSeconds += deltaTimeSeconds.coerceAtLeast(0f)
        val progress = wrapDayProgress(
            lerp(window.startProgress, window.targetProgress, animationElapsedSeconds / durationSeconds)
        )
        if (animationElapsedSeconds >= durationSeconds) {
            completedProgress = wrapDayProgress(window.targetProgress)
            animationActive = false
            return completedProgress!!
        }
        return progress
    }

    fun resolveFocusCatchUpWindow(
        nowProgress: Float,
        sunriseMinute: Int,
        sunsetMinute: Int
    ): PreviewFocusCatchUpWindow {
        val normalizedNow = nowProgress.coerceIn(0f, 1f)
        val startProgress = NOON_PROGRESS
        val targetProgress = 1f + normalizedNow
        return PreviewFocusCatchUpWindow(
            startProgress = startProgress,
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
        private const val DEFAULT_FOCUS_CATCH_UP_SECONDS = 2f
        private const val MIN_FOCUS_CATCH_UP_SECONDS = 0.3f
        private const val MINUTES_PER_DAY = 24 * 60
        private const val DEFAULT_SUNRISE_MINUTE = 360
        private const val DEFAULT_SUNSET_MINUTE = 1080
        private const val NOON_PROGRESS = 0.5f
        private const val NANOS_PER_DAY = 24L * 60L * 60L * 1_000_000_000L
    }
}
