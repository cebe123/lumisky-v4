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

import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

class LocationDaylightController {

    fun resolve(
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        date: LocalDate = LocalDate.now(resolveZone(timeZoneId))
    ): DaylightOverride {
        val zone = resolveZone(timeZoneId)
        val offsetHours = zone.rules.getOffset(date.atStartOfDay(zone).toInstant()).totalSeconds / 3600.0
        val sunrise = calculateLocalHour(
            latitude = latitude.coerceIn(-89.0, 89.0),
            longitude = longitude.coerceIn(-180.0, 180.0),
            dayOfYear = date.dayOfYear,
            offsetHours = offsetHours,
            isSunrise = true
        )
        val sunset = calculateLocalHour(
            latitude = latitude.coerceIn(-89.0, 89.0),
            longitude = longitude.coerceIn(-180.0, 180.0),
            dayOfYear = date.dayOfYear,
            offsetHours = offsetHours,
            isSunrise = false
        )
        if (sunrise == null || sunset == null) {
            val polarDay = isPolarDay(latitude, dayOfYear = date.dayOfYear)
            return DaylightOverride(
                sunriseMinute = if (polarDay) 0 else MINUTES_PER_DAY,
                sunsetMinute = if (polarDay) MINUTES_PER_DAY else 0,
                solarNoonMinute = DEFAULT_SOLAR_NOON,
                timeZoneId = zone.id,
                mode = if (polarDay) DaylightMode.POLAR_DAY else DaylightMode.POLAR_NIGHT
            )
        }

        val sunriseMinute = hourToMinute(sunrise)
        val sunsetMinute = hourToMinute(sunset)
        val solarNoonMinute = resolveSolarNoon(sunriseMinute, sunsetMinute)
        return DaylightOverride(
            sunriseMinute = sunriseMinute,
            sunsetMinute = sunsetMinute,
            solarNoonMinute = solarNoonMinute,
            timeZoneId = zone.id
        )
    }

    private fun calculateLocalHour(
        latitude: Double,
        longitude: Double,
        dayOfYear: Int,
        offsetHours: Double,
        isSunrise: Boolean
    ): Double? {
        val lngHour = longitude / 15.0
        val approximateTime = dayOfYear + (((if (isSunrise) 6.0 else 18.0) - lngHour) / 24.0)
        val meanAnomaly = (0.9856 * approximateTime) - 3.289
        val trueLongitude = normalizeDegrees(
            meanAnomaly +
                (1.916 * sinDeg(meanAnomaly)) +
                (0.020 * sinDeg(2.0 * meanAnomaly)) +
                282.634
        )
        var rightAscension = normalizeDegrees(radToDeg(atan(0.91764 * tanDeg(trueLongitude))))
        val longitudeQuadrant = floor(trueLongitude / 90.0) * 90.0
        val ascensionQuadrant = floor(rightAscension / 90.0) * 90.0
        rightAscension = (rightAscension + (longitudeQuadrant - ascensionQuadrant)) / 15.0

        val sinDeclination = 0.39782 * sinDeg(trueLongitude)
        val cosDeclination = cos(asin(sinDeclination))
        val cosHour = (cosDeg(ZENITH_DEGREES) - (sinDeclination * sinDeg(latitude))) /
            (cosDeclination * cosDeg(latitude))
        if (cosHour > 1.0 || cosHour < -1.0) return null

        val hourAngle = if (isSunrise) {
            360.0 - radToDeg(acos(cosHour))
        } else {
            radToDeg(acos(cosHour))
        } / 15.0

        val localMeanTime = hourAngle + rightAscension - (0.06571 * approximateTime) - 6.622
        val utcHour = normalizeHours(localMeanTime - lngHour)
        return normalizeHours(utcHour + offsetHours)
    }

    private fun isPolarDay(latitude: Double, dayOfYear: Int): Boolean {
        return (latitude >= 0.0 && dayOfYear in 80..263) ||
            (latitude < 0.0 && (dayOfYear < 80 || dayOfYear > 263))
    }

    private fun hourToMinute(hour: Double): Int {
        return ((normalizeHours(hour) * 60.0).toInt()).coerceIn(0, MINUTES_PER_DAY)
    }

    private fun resolveSolarNoon(sunriseMinute: Int, sunsetMinute: Int): Int {
        return if (sunsetMinute >= sunriseMinute) {
            sunriseMinute + ((sunsetMinute - sunriseMinute) / 2)
        } else {
            ((sunriseMinute + (((MINUTES_PER_DAY - sunriseMinute) + sunsetMinute) / 2)) % MINUTES_PER_DAY)
        }
    }

    private fun resolveZone(timeZoneId: String): ZoneId {
        return try {
            ZoneId.of(timeZoneId.ifBlank { DEFAULT_TIME_ZONE })
        } catch (e: Throwable) {
            ZoneId.of(DEFAULT_TIME_ZONE)
        }
    }

    private fun normalizeDegrees(value: Double): Double {
        val normalized = value % 360.0
        return if (normalized < 0.0) normalized + 360.0 else normalized
    }

    private fun normalizeHours(value: Double): Double {
        val normalized = value % 24.0
        return if (normalized < 0.0) normalized + 24.0 else normalized
    }

    private fun sinDeg(value: Double): Double = sin(degToRad(value))

    private fun cosDeg(value: Double): Double = cos(degToRad(value))

    private fun tanDeg(value: Double): Double = tan(degToRad(value))

    private fun degToRad(value: Double): Double = value * PI / 180.0

    private fun radToDeg(value: Double): Double = value * 180.0 / PI

    private companion object {
        const val MINUTES_PER_DAY = 1440
        const val DEFAULT_SUNRISE = 360
        const val DEFAULT_SUNSET = 1080
        const val DEFAULT_SOLAR_NOON = 720
        const val DEFAULT_TIME_ZONE = "Europe/Istanbul"
        const val ZENITH_DEGREES = 90.833
    }
}
