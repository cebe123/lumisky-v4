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

data class LiveWallpaperCatchUpWindow(
    val startProgress: Float,
    val targetProgress: Float
)

class LiveWallpaperCatchUpController(
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {
    fun resolveWindow(
        definition: WallpaperDefinition?,
        daylightOverride: DaylightOverride?
    ): LiveWallpaperCatchUpWindow {
        val sunriseMinute = (daylightOverride?.sunriseMinute ?: definition?.daylight?.sunriseMinute ?: DEFAULT_SUNRISE_MINUTE)
            .coerceIn(0, MINUTES_PER_DAY)
        val sunsetMinute = (daylightOverride?.sunsetMinute ?: definition?.daylight?.sunsetMinute ?: DEFAULT_SUNSET_MINUTE)
            .coerceIn(0, MINUTES_PER_DAY)
        return resolveWindow(
            nowProgress = currentDayProgress(resolveZoneId(definition, daylightOverride)),
            sunriseMinute = sunriseMinute,
            sunsetMinute = sunsetMinute
        )
    }

    fun resolveWindow(
        nowProgress: Float,
        sunriseMinute: Int,
        sunsetMinute: Int
    ): LiveWallpaperCatchUpWindow {
        val now = nowProgress.coerceIn(0f, 1f)
        val nowMinute = (now * MINUTES_PER_DAY).toInt().coerceIn(0, MINUTES_PER_DAY - 1)
        val isDaytime = isDaytime(nowMinute, sunriseMinute, sunsetMinute)
        val modeStart = (if (isDaytime) sunriseMinute else sunsetMinute) / MINUTES_PER_DAY.toFloat()
        val target = if (now >= modeStart) now else now + 1f
        val start = modeStart + ((target - modeStart) * MODE_INTERIOR_START_FRACTION)
        return LiveWallpaperCatchUpWindow(
            startProgress = start,
            targetProgress = target
        )
    }

    private fun currentDayProgress(zoneId: ZoneId): Float {
        val localTime = Instant.ofEpochMilli(nowProvider())
            .atZone(zoneId)
            .toLocalTime()
        return (localTime.toNanoOfDay().toDouble() / NANOS_PER_DAY.toDouble())
            .toFloat()
            .coerceIn(0f, 1f)
    }

    private fun resolveZoneId(
        definition: WallpaperDefinition?,
        daylightOverride: DaylightOverride?
    ): ZoneId {
        val id = daylightOverride?.timeZoneId
            ?.takeIf { it.isNotBlank() }
            ?: definition?.daylight?.timeZoneId
            ?: DEFAULT_TIME_ZONE
        return runCatching { ZoneId.of(id) }.getOrElse { ZoneId.systemDefault() }
    }

    private fun isDaytime(nowMinute: Int, sunriseMinute: Int, sunsetMinute: Int): Boolean {
        return if (sunsetMinute == sunriseMinute) {
            true
        } else if (sunsetMinute > sunriseMinute) {
            nowMinute in sunriseMinute..sunsetMinute
        } else {
            nowMinute >= sunriseMinute || nowMinute <= sunsetMinute
        }
    }

    private companion object {
        const val MINUTES_PER_DAY = 24 * 60
        const val DEFAULT_SUNRISE_MINUTE = 360
        const val DEFAULT_SUNSET_MINUTE = 1080
        const val DEFAULT_TIME_ZONE = "Europe/Istanbul"
        const val NANOS_PER_DAY = 24L * 60L * 60L * 1_000_000_000L
        const val MODE_INTERIOR_START_FRACTION = 0.5f
    }
}
