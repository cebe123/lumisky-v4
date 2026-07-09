/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Ui katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Ui katmanı bileşeni.
 */
package com.adnan.lumisky.ui.settings

import com.adnan.lumisky.engine.DaylightOverride

internal data class CelestialTimelineSnapshot(
    val sunriseMinute: Int,
    val sunsetMinute: Int,
    val moonriseMinute: Int,
    val moonsetMinute: Int,
    val sunProgress: Float,
    val moonProgress: Float,
    val sunActive: Boolean,
    val moonActive: Boolean
)

internal fun resolveCelestialTimeline(
    daylight: DaylightOverride,
    currentMinute: Int
): CelestialTimelineSnapshot {
    val sunrise = normalizeMinute(daylight.sunriseMinute)
    val sunset = normalizeMinute(daylight.sunsetMinute)
    val normalizedCurrentMinute = normalizeMinute(currentMinute)
    val moonWindow = deriveMoonWindow(daylight)
    val sunActive = isInWrappedRange(
        minute = normalizedCurrentMinute,
        startMinute = sunrise,
        endMinute = sunset
    )
    val moonActive = !sunActive && isInWrappedRange(
        minute = normalizedCurrentMinute,
        startMinute = moonWindow.startMinute,
        endMinute = moonWindow.endMinute
    )

    return CelestialTimelineSnapshot(
        sunriseMinute = sunrise,
        sunsetMinute = sunset,
        moonriseMinute = moonWindow.startMinute,
        moonsetMinute = moonWindow.endMinute,
        sunProgress = resolveSunProgress(
            currentMinute = normalizedCurrentMinute,
            sunriseMinute = sunrise,
            sunsetMinute = sunset
        ),
        moonProgress = resolveMoonProgress(
            currentMinute = normalizedCurrentMinute,
            moonriseMinute = moonWindow.startMinute,
            moonsetMinute = moonWindow.endMinute,
            isMoonActive = moonActive
        ),
        sunActive = sunActive,
        moonActive = moonActive
    )
}

private data class MoonWindow(
    val startMinute: Int,
    val endMinute: Int
)

private fun deriveMoonWindow(daylight: DaylightOverride): MoonWindow {
    val sunrise = normalizeMinute(daylight.sunriseMinute)
    val sunset = normalizeMinute(daylight.sunsetMinute)
    return MoonWindow(
        startMinute = sunset,
        endMinute = sunrise
    )
}

private fun resolveSunProgress(
    currentMinute: Int,
    sunriseMinute: Int,
    sunsetMinute: Int
): Float {
    if (sunriseMinute == sunsetMinute) return 0f
    if (sunriseMinute < sunsetMinute) {
        if (currentMinute < sunriseMinute) return 0f
        if (currentMinute > sunsetMinute) return 1f
    }
    val duration = minutesForward(
        startMinute = sunriseMinute,
        endMinute = sunsetMinute
    ).coerceAtLeast(1)
    val elapsed = minutesForward(
        startMinute = sunriseMinute,
        endMinute = currentMinute
    )
    if (elapsed > duration) return 1f
    return (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
}

private fun resolveMoonProgress(
    currentMinute: Int,
    moonriseMinute: Int,
    moonsetMinute: Int,
    isMoonActive: Boolean
): Float {
    val duration = minutesForward(
        startMinute = moonriseMinute,
        endMinute = moonsetMinute
    ).coerceAtLeast(1)
    if (!isMoonActive) return 0f
    val elapsed = minutesForward(
        startMinute = moonriseMinute,
        endMinute = currentMinute
    ).coerceAtMost(duration)
    return (1f - (elapsed.toFloat() / duration.toFloat())).coerceIn(0f, 1f)
}

private fun isInWrappedRange(
    minute: Int,
    startMinute: Int,
    endMinute: Int
): Boolean {
    return if (startMinute <= endMinute) {
        minute in startMinute..endMinute
    } else {
        minute >= startMinute || minute <= endMinute
    }
}

private fun minutesForward(
    startMinute: Int,
    endMinute: Int
): Int {
    val normalizedStart = normalizeMinute(startMinute)
    val normalizedEnd = normalizeMinute(endMinute)
    return if (normalizedEnd >= normalizedStart) {
        normalizedEnd - normalizedStart
    } else {
        (MINUTES_PER_DAY - normalizedStart) + normalizedEnd
    }
}

private fun normalizeMinute(minute: Int): Int {
    return ((minute % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY
}

private const val MINUTES_PER_DAY = 24 * 60
