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

import com.example.lumisky.definition.CelestialOrbitDefinition
import com.example.lumisky.definition.WallpaperDefinition
import kotlin.math.PI
import kotlin.math.sin

data class CelestialUniformState(
    val sunX: Float,
    val sunY: Float,
    val moonX: Float,
    val moonY: Float,
    val drawSun: Boolean,
    val isNight: Boolean,
    val minute: Float,
    val sunriseMinute: Int,
    val sunsetMinute: Int,
    val solarNoonMinute: Int,
    val nightAmount: Float,
    val horizonY: Float,
    val sunColorR: Float,
    val sunColorG: Float,
    val sunColorB: Float
)

class CelestialMotionController {
    private val sunOrbit = ResolvedOrbit()
    private val moonOrbit = ResolvedOrbit()

    fun resolve(
        definition: WallpaperDefinition?,
        dayProgress: Float,
        daylightOverride: DaylightOverride? = null
    ): CelestialUniformState {
        resolveValues(definition, dayProgress, daylightOverride)
        return CelestialUniformState(
            sunX = resolvedState.sunX,
            sunY = resolvedState.sunY,
            moonX = resolvedState.moonX,
            moonY = resolvedState.moonY,
            drawSun = resolvedState.drawSun,
            isNight = resolvedState.isNight,
            minute = resolvedState.minute,
            sunriseMinute = resolvedState.sunriseMinute,
            sunsetMinute = resolvedState.sunsetMinute,
            solarNoonMinute = resolvedState.solarNoonMinute,
            nightAmount = resolvedState.nightAmount,
            horizonY = resolvedState.horizonY,
            sunColorR = resolvedState.sunColorR,
            sunColorG = resolvedState.sunColorG,
            sunColorB = resolvedState.sunColorB
        )
    }

    fun resolveInto(
        definition: WallpaperDefinition?,
        dayProgress: Float,
        daylightOverride: DaylightOverride? = null,
        output: MutableRenderFrameState
    ) {
        resolveValues(definition, dayProgress, daylightOverride)
        output.sunX = resolvedState.sunX
        output.sunY = resolvedState.sunY
        output.moonX = resolvedState.moonX
        output.moonY = resolvedState.moonY
        output.drawSun = resolvedState.drawSun
        output.isNight = resolvedState.isNight
        output.minute = resolvedState.minute
        output.sunriseMinute = resolvedState.sunriseMinute
        output.sunsetMinute = resolvedState.sunsetMinute
        output.solarNoonMinute = resolvedState.solarNoonMinute
        output.nightAmount = resolvedState.nightAmount
        output.horizonY = resolvedState.horizonY
        output.sunColorR = resolvedState.sunColorR
        output.sunColorG = resolvedState.sunColorG
        output.sunColorB = resolvedState.sunColorB
    }

    private fun resolveValues(
        definition: WallpaperDefinition?,
        dayProgress: Float,
        daylightOverride: DaylightOverride?
    ) {
        val output = resolvedState
        val resolvedDefinition = definition
        val minute = minuteOfDay(dayProgress)
        val sunrise = daylightOverride?.sunriseMinute?.coerceIn(0, MINUTES_PER_DAY)
            ?: resolvedDefinition?.daylight?.sunriseMinute?.coerceIn(0, MINUTES_PER_DAY)
            ?: 360
        val sunset = daylightOverride?.sunsetMinute?.coerceIn(0, MINUTES_PER_DAY)
            ?: resolvedDefinition?.daylight?.sunsetMinute?.coerceIn(0, MINUTES_PER_DAY)
            ?: 1080
        val solarNoon = daylightOverride?.solarNoonMinute?.coerceIn(0, MINUTES_PER_DAY)
            ?: resolveSolarNoon(resolvedDefinition, sunrise, sunset)
        val horizon = (resolvedDefinition?.horizon?.offset ?: DEFAULT_HORIZON_Y).coerceIn(0f, 1f)
        val isDaytime = isDaytime(minute, sunrise.toFloat(), sunset.toFloat())

        resolveOrbit(
            target = sunOrbit,
            explicit = resolvedDefinition?.celestial?.sunOrbit,
            pathType = resolvedDefinition?.celestial?.sunPathType,
            fallbackPeakY = resolvedDefinition?.peakY ?: DEFAULT_PEAK_Y
        )
        resolveOrbit(
            target = moonOrbit,
            explicit = resolvedDefinition?.celestial?.moonOrbit,
            pathType = resolvedDefinition?.celestial?.moonPathType,
            fallbackPeakY = resolvedDefinition?.peakY ?: DEFAULT_PEAK_Y
        )

        val sunPhase = if (isDaytime) {
            val phase = resolvePeakAlignedPhaseProgress(
                currentMinute = minute,
                startMinute = sunrise.toFloat(),
                peakMinute = solarNoon.toFloat(),
                endMinute = sunset.toFloat()
            )
            phase
        } else {
            -1f
        }

        val moonPhase = if (isDaytime) {
            -1f
        } else {
            resolvePeakAlignedPhaseProgress(
                currentMinute = minute,
                startMinute = sunset.toFloat(),
                peakMinute = ((solarNoon + HALF_DAY_MINUTES) % MINUTES_PER_DAY).toFloat(),
                endMinute = sunrise.toFloat()
            )
        }

        val sunX = if (isDaytime) resolveVisibleX(sunOrbit, sunPhase) else resolveHiddenX(sunOrbit)
        val sunY = if (isDaytime) resolveVisibleY(sunOrbit, horizon, sunPhase) else resolveHiddenY(sunOrbit, horizon)
        val moonX = if (isDaytime) resolveHiddenX(moonOrbit) else resolveVisibleX(moonOrbit, moonPhase)
        val moonY = if (isDaytime) resolveHiddenY(moonOrbit, horizon) else resolveVisibleY(moonOrbit, horizon, moonPhase)
        val colorPhase = resolveSunColorPhase(
            wallpaperId = resolvedDefinition?.id,
            minute = minute,
            sunrise = sunrise.toFloat(),
            sunset = sunset.toFloat()
        )

        output.sunX = sunX.coerceIn(0f, 1f)
        output.sunY = mapToLegacyShaderY(sunY)
        output.moonX = moonX.coerceIn(0f, 1f)
        output.moonY = mapToLegacyShaderY(moonY)
        output.drawSun = isDaytime
        output.isNight = !isDaytime
        output.minute = minute
        output.sunriseMinute = sunrise
        output.sunsetMinute = sunset
        output.solarNoonMinute = solarNoon
        output.nightAmount = calculateNightAmount(minute, sunrise.toFloat(), sunset.toFloat())
        output.horizonY = mapToLegacyShaderY(horizon)
        output.sunColorR = 1f
        output.sunColorG = 0.9f - (0.4f * colorPhase)
        output.sunColorB = 0.8f - (0.6f * colorPhase)
    }

    private fun resolveOrbit(
        target: ResolvedOrbit,
        explicit: CelestialOrbitDefinition?,
        pathType: String?,
        fallbackPeakY: Float
    ) {
        target.isVertical = (explicit?.pathType ?: pathType).equals("VERTICAL", ignoreCase = true)
        target.startX = explicit?.startX ?: Float.NaN
        target.endX = explicit?.endX ?: Float.NaN
        target.peakY = explicit?.peakY ?: fallbackPeakY
        target.hiddenY = explicit?.hiddenY ?: Float.NaN
        target.easeInOut = (explicit?.curve ?: "LINEAR").equals("EASE_IN_OUT", ignoreCase = true)
    }

    private fun resolveVisibleY(orbit: ResolvedOrbit, horizonY: Float, phaseProgress: Float): Float {
        val shapedProgress = applyCurve(phaseProgress, orbit.easeInOut)
        val peakY = resolvePeakY(horizonY, orbit.peakY)
        return horizonY + (sin(shapedProgress.coerceIn(0f, 1f) * PI.toFloat()) * (peakY - horizonY).coerceAtLeast(0f))
    }

    private fun resolveHiddenY(orbit: ResolvedOrbit, horizonY: Float): Float {
        return if (orbit.hiddenY.isNaN()) (horizonY - HIDDEN_DEPTH).coerceAtMost(HIDDEN_Y_MAX) else orbit.hiddenY
    }

    private fun resolveVisibleX(orbit: ResolvedOrbit, phaseProgress: Float): Float {
        return if (orbit.isVertical) {
            firstDefined(orbit.startX, orbit.endX, VERTICAL_PATH_X).coerceIn(0f, 1f)
        } else {
            lerp(
                start = firstDefined(orbit.startX, ARC_PATH_START_X),
                end = firstDefined(orbit.endX, ARC_PATH_END_X),
                t = phaseProgress
            ).coerceIn(0f, 1f)
        }
    }

    private fun resolveHiddenX(orbit: ResolvedOrbit): Float {
        return if (orbit.isVertical) {
            firstDefined(orbit.startX, orbit.endX, VERTICAL_PATH_X).coerceIn(0f, 1f)
        } else {
            val start = firstDefined(orbit.startX, ARC_PATH_START_X)
            val end = firstDefined(orbit.endX, ARC_PATH_END_X)
            ((start + end) * 0.5f).coerceIn(0f, 1f)
        }
    }

    private fun resolvePeakY(horizonY: Float, peakY: Float): Float {
        val minimumPeakY = (horizonY + MIN_PEAK_DELTA).coerceAtMost(1f)
        return peakY.coerceIn(minimumPeakY, 1f)
    }

    private fun applyCurve(phaseProgress: Float, easeInOut: Boolean): Float {
        val clamped = phaseProgress.coerceIn(0f, 1f)
        return if (easeInOut) clamped * clamped * (3f - (2f * clamped)) else clamped
    }

    private fun resolveSolarNoon(
        definition: WallpaperDefinition?,
        sunriseMinute: Int,
        sunsetMinute: Int
    ): Int {
        val configured = definition?.daylight?.solarNoonMinute?.coerceIn(0, MINUTES_PER_DAY)
            ?: (sunriseMinute + ((sunsetMinute - sunriseMinute) / 2))
        if (sunriseMinute == sunsetMinute) return configured
        return if (configured in sunriseMinute..sunsetMinute) {
            configured
        } else {
            sunriseMinute + ((sunsetMinute - sunriseMinute).coerceAtLeast(1) / 2)
        }
    }

    private fun resolvePeakAlignedPhaseProgress(
        currentMinute: Float,
        startMinute: Float,
        peakMinute: Float,
        endMinute: Float
    ): Float {
        val start = normalizeMinute(startMinute)
        val peak = normalizeMinuteForward(peakMinute, anchorMinute = start)
        val normalizedEnd = normalizeMinute(endMinute)
        val end = if (normalizedEnd == start) {
            start + MINUTES_PER_DAY.toFloat()
        } else {
            normalizeMinuteForward(endMinute, anchorMinute = start)
        }
        val current = normalizeMinuteForward(currentMinute, anchorMinute = start)
        if (current <= peak) {
            val firstHalfDuration = (peak - start).coerceAtLeast(1f)
            return (((current - start) / firstHalfDuration) * 0.5f).coerceIn(0f, 0.5f)
        }
        val secondHalfDuration = (end - peak).coerceAtLeast(1f)
        return (0.5f + (((current - peak) / secondHalfDuration) * 0.5f)).coerceIn(0.5f, 1f)
    }

    private fun isDaytime(minute: Float, sunrise: Float, sunset: Float): Boolean {
        return if (sunset == sunrise) {
            true
        } else if (sunset > sunrise) {
            minute >= sunrise && minute <= sunset
        } else {
            minute >= sunrise || minute <= sunset
        }
    }

    private fun calculateNightAmount(minute: Float, sunrise: Float, sunset: Float): Float {
        val relMinute = (minute - sunset + MINUTES_PER_DAY) % MINUTES_PER_DAY
        val relSunrise = (sunrise - sunset + MINUTES_PER_DAY) % MINUTES_PER_DAY

        if (relMinute < NIGHT_TRANSITION_AFTER_SUNSET_MIN) {
            return smoothstep(0f, NIGHT_TRANSITION_AFTER_SUNSET_MIN, relMinute)
        }

        val sunriseStart = relSunrise - NIGHT_TRANSITION_BEFORE_SUNRISE_WIDE_MIN
        val sunriseEnd = relSunrise + NIGHT_TRANSITION_AFTER_SUNRISE_MIN
        if (relMinute >= sunriseStart && relMinute <= sunriseEnd) {
            return 1f - smoothstep(sunriseStart, sunriseEnd, relMinute)
        }

        if (relMinute > NIGHT_TRANSITION_AFTER_SUNSET_MIN && relMinute < sunriseStart) {
            return 1f
        }

        return 0f
    }

    private fun resolveSunColorPhase(
        wallpaperId: String?,
        minute: Float,
        sunrise: Float,
        sunset: Float
    ): Float {
        if (sunset <= sunrise || minute < sunrise || minute > sunset) {
            return 0f
        }
        val dayPhase = ((minute - sunrise) / (sunset - sunrise)).coerceIn(0f, 1f)
        var colorPhase = if (dayPhase < 0.5f) {
            dayPhase * 2f
        } else {
            2f - (dayPhase * 2f)
        }
        if (wallpaperId?.contains("pixel_forest", ignoreCase = true) == true) {
            colorPhase = 1f - colorPhase
        }
        return colorPhase
    }

    private fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
        if (edge0 == edge1) return if (value < edge0) 0f else 1f
        val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - (2f * t))
    }

    private fun minuteOfDay(progress: Float): Float {
        if (!progress.isFiniteValue()) return 0f
        val wrapped = ((progress % 1f) + 1f) % 1f
        if (!wrapped.isFiniteValue()) return 0f
        val minute = wrapped * MINUTES_PER_DAY.toFloat()
        return if (minute >= MINUTES_PER_DAY.toFloat()) 0f else minute.coerceAtLeast(0f)
    }

    private fun normalizeMinute(minute: Float): Float {
        if (!minute.isFiniteValue()) return 0f
        return minute.coerceIn(0f, MINUTES_PER_DAY.toFloat())
    }

    private fun normalizeMinuteForward(minute: Float, anchorMinute: Float): Float {
        var normalized = normalizeMinute(minute)
        val anchor = normalizeMinute(anchorMinute)
        var iterations = 0
        while (normalized < anchor && iterations < MAX_MINUTE_WRAP_ITERATIONS) {
            normalized += MINUTES_PER_DAY.toFloat()
            iterations++
        }
        return normalized
    }

    private fun mapToLegacyShaderY(engineY: Float): Float {
        return 1f - engineY
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + ((end - start) * t.coerceIn(0f, 1f))
    }

    private fun Float.isFiniteValue(): Boolean {
        return !isNaN() && this != Float.POSITIVE_INFINITY && this != Float.NEGATIVE_INFINITY
    }

    private fun firstDefined(primary: Float, fallback: Float): Float =
        if (primary.isNaN()) fallback else primary

    private fun firstDefined(primary: Float, secondary: Float, fallback: Float): Float =
        if (!primary.isNaN()) primary else firstDefined(secondary, fallback)

    private class ResolvedOrbit {
        var isVertical = false
        var startX = Float.NaN
        var endX = Float.NaN
        var peakY = DEFAULT_PEAK_Y
        var hiddenY = Float.NaN
        var easeInOut = false
    }

    private class CelestialScratch {
        var sunX = 0f
        var sunY = 0f
        var moonX = 0f
        var moonY = 0f
        var drawSun = false
        var isNight = false
        var minute = 0f
        var sunriseMinute = 0
        var sunsetMinute = 0
        var solarNoonMinute = 0
        var nightAmount = 0f
        var horizonY = 0f
        var sunColorR = 0f
        var sunColorG = 0f
        var sunColorB = 0f
    }

    private val resolvedState = CelestialScratch()

    private companion object {
        const val MINUTES_PER_DAY = 1440
        const val HALF_DAY_MINUTES = 720
        const val DEFAULT_HORIZON_Y = 0.2f
        const val DEFAULT_PEAK_Y = 0.9f
        const val VERTICAL_PATH_X = 0.5f
        const val ARC_PATH_START_X = 0f
        const val ARC_PATH_END_X = 1f
        const val MIN_PEAK_DELTA = 0.05f
        const val HIDDEN_DEPTH = 0.75f
        const val HIDDEN_Y_MAX = -0.15f
        const val MAX_MINUTE_WRAP_ITERATIONS = 3
        const val NIGHT_TRANSITION_AFTER_SUNSET_MIN = 20f
        const val NIGHT_TRANSITION_BEFORE_SUNRISE_WIDE_MIN = 30f
        const val NIGHT_TRANSITION_AFTER_SUNRISE_MIN = 10f
    }
}
