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

    fun resolve(
        definition: WallpaperDefinition?,
        dayProgress: Float,
        daylightOverride: DaylightOverride? = null
    ): CelestialUniformState {
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

        val sunOrbit = resolveOrbit(
            explicit = resolvedDefinition?.celestial?.sunOrbit,
            pathType = resolvedDefinition?.celestial?.sunPathType,
            fallbackPeakY = resolvedDefinition?.peakY ?: DEFAULT_PEAK_Y
        )
        val moonOrbit = resolveOrbit(
            explicit = resolvedDefinition?.celestial?.moonOrbit,
            pathType = resolvedDefinition?.celestial?.moonPathType,
            fallbackPeakY = resolvedDefinition?.peakY ?: DEFAULT_PEAK_Y
        )

        val sun = if (isDaytime) {
            val phase = resolvePeakAlignedPhaseProgress(
                currentMinute = minute,
                startMinute = sunrise.toFloat(),
                peakMinute = solarNoon.toFloat(),
                endMinute = sunset.toFloat()
            )
            resolveVisiblePosition(sunOrbit, horizon, phase)
        } else {
            resolveHiddenPosition(sunOrbit, horizon)
        }

        val moon = if (isDaytime) {
            resolveHiddenPosition(moonOrbit, horizon)
        } else {
            val phase = resolvePeakAlignedPhaseProgress(
                currentMinute = minute,
                startMinute = sunset.toFloat(),
                peakMinute = ((solarNoon + HALF_DAY_MINUTES) % MINUTES_PER_DAY).toFloat(),
                endMinute = sunrise.toFloat()
            )
            resolveVisiblePosition(moonOrbit, horizon, phase)
        }

        val sunColor = resolveSunColor(
            wallpaperId = resolvedDefinition?.id.orEmpty(),
            minute = minute,
            sunrise = sunrise.toFloat(),
            sunset = sunset.toFloat()
        )

        return CelestialUniformState(
            sunX = sun.x.coerceIn(0f, 1f),
            sunY = mapToLegacyShaderY(sun.y),
            moonX = moon.x.coerceIn(0f, 1f),
            moonY = mapToLegacyShaderY(moon.y),
            drawSun = isDaytime,
            isNight = !isDaytime,
            minute = minute,
            sunriseMinute = sunrise,
            sunsetMinute = sunset,
            solarNoonMinute = solarNoon,
            nightAmount = calculateNightAmount(minute, sunrise.toFloat(), sunset.toFloat()),
            horizonY = mapToLegacyShaderY(horizon),
            sunColorR = sunColor.r,
            sunColorG = sunColor.g,
            sunColorB = sunColor.b
        )
    }

    private fun resolveOrbit(
        explicit: CelestialOrbitDefinition?,
        pathType: String?,
        fallbackPeakY: Float
    ): ResolvedOrbit {
        val normalizedPathType = (explicit?.pathType ?: pathType ?: "ARC").uppercase()
        return ResolvedOrbit(
            pathType = if (normalizedPathType == "VERTICAL") PathType.VERTICAL else PathType.ARC,
            startX = explicit?.startX,
            endX = explicit?.endX,
            peakY = explicit?.peakY ?: fallbackPeakY,
            hiddenY = explicit?.hiddenY,
            curve = if ((explicit?.curve ?: "LINEAR").uppercase() == "EASE_IN_OUT") {
                OrbitCurve.EASE_IN_OUT
            } else {
                OrbitCurve.LINEAR
            }
        )
    }

    private fun resolveVisiblePosition(
        orbit: ResolvedOrbit,
        horizonY: Float,
        phaseProgress: Float
    ): Vec2 {
        val shapedProgress = applyCurve(phaseProgress, orbit.curve)
        val peakY = resolvePeakY(horizonY, orbit.peakY)
        return Vec2(
            x = resolveVisibleX(orbit, shapedProgress),
            y = horizonY + (sin(shapedProgress.coerceIn(0f, 1f) * PI.toFloat()) * (peakY - horizonY).coerceAtLeast(0f))
        )
    }

    private fun resolveHiddenPosition(orbit: ResolvedOrbit, horizonY: Float): Vec2 {
        return Vec2(
            x = resolveHiddenX(orbit),
            y = orbit.hiddenY ?: ((horizonY - HIDDEN_DEPTH).coerceAtMost(HIDDEN_Y_MAX))
        )
    }

    private fun resolveVisibleX(orbit: ResolvedOrbit, phaseProgress: Float): Float {
        return when (orbit.pathType) {
            PathType.VERTICAL -> (orbit.startX ?: orbit.endX ?: VERTICAL_PATH_X).coerceIn(0f, 1f)
            PathType.ARC -> lerp(
                start = orbit.startX ?: ARC_PATH_START_X,
                end = orbit.endX ?: ARC_PATH_END_X,
                t = phaseProgress
            ).coerceIn(0f, 1f)
        }
    }

    private fun resolveHiddenX(orbit: ResolvedOrbit): Float {
        return when (orbit.pathType) {
            PathType.VERTICAL -> (orbit.startX ?: orbit.endX ?: VERTICAL_PATH_X).coerceIn(0f, 1f)
            PathType.ARC -> {
                val start = orbit.startX ?: ARC_PATH_START_X
                val end = orbit.endX ?: ARC_PATH_END_X
                ((start + end) * 0.5f).coerceIn(0f, 1f)
            }
        }
    }

    private fun resolvePeakY(horizonY: Float, peakY: Float): Float {
        val minimumPeakY = (horizonY + MIN_PEAK_DELTA).coerceAtMost(1f)
        return peakY.coerceIn(minimumPeakY, 1f)
    }

    private fun applyCurve(phaseProgress: Float, curve: OrbitCurve): Float {
        val clamped = phaseProgress.coerceIn(0f, 1f)
        return when (curve) {
            OrbitCurve.LINEAR -> clamped
            OrbitCurve.EASE_IN_OUT -> clamped * clamped * (3f - (2f * clamped))
        }
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

    private fun resolveSunColor(
        wallpaperId: String,
        minute: Float,
        sunrise: Float,
        sunset: Float
    ): Vec3 {
        if (sunset <= sunrise || minute < sunrise || minute > sunset) {
            return Vec3(1.0f, 0.9f, 0.8f)
        }
        val dayPhase = ((minute - sunrise) / (sunset - sunrise)).coerceIn(0f, 1f)
        var colorPhase = if (dayPhase < 0.5f) {
            dayPhase * 2f
        } else {
            2f - (dayPhase * 2f)
        }
        if (wallpaperId.lowercase().contains("pixel_forest")) {
            colorPhase = 1f - colorPhase
        }
        return Vec3(
            r = 1.0f,
            g = 0.9f - (0.4f * colorPhase),
            b = 0.8f - (0.6f * colorPhase)
        )
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

    private data class ResolvedOrbit(
        val pathType: PathType,
        val startX: Float?,
        val endX: Float?,
        val peakY: Float,
        val hiddenY: Float?,
        val curve: OrbitCurve
    )

    private data class Vec2(val x: Float, val y: Float)

    private data class Vec3(val r: Float, val g: Float, val b: Float)

    private enum class PathType {
        VERTICAL,
        ARC
    }

    private enum class OrbitCurve {
        LINEAR,
        EASE_IN_OUT
    }

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
