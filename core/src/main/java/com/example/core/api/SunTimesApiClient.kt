package com.example.core.api

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.ZoneId
import java.time.ZonedDateTime

data class SunDaylight(
	val sunriseMinute: Int,
	val sunsetMinute: Int,
	val solarNoonMinute: Int = 12 * 60,
	val timeZoneId: String? = null
) {
	companion object {
		fun fallback(): SunDaylight {
			return SunDaylight(
				sunriseMinute = 6 * 60,
				sunsetMinute = 18 * 60,
				solarNoonMinute = 12 * 60,
				timeZoneId = null
			)
		}
	}
}

class SunTimesApiClient {

	fun fetchDaylight(
		latitude: Double,
		longitude: Double,
		timeZoneId: String? = null
	): SunDaylight? {
		val endpoint = buildEndpoint(latitude, longitude, timeZoneId)
		val connection = (URL(endpoint).openConnection() as? HttpURLConnection) ?: return null
		return try {
			connection.requestMethod = "GET"
			connection.connectTimeout = CONNECT_TIMEOUT_MS
			connection.readTimeout = READ_TIMEOUT_MS
			connection.instanceFollowRedirects = true
			connection.connect()

			if (connection.responseCode !in 200..299) return null
			val payload = connection.inputStream.use { stream ->
				BufferedReader(InputStreamReader(stream)).use { it.readText() }
			}
			parseDaylight(payload, timeZoneId)
		} catch (_: Throwable) {
			null
		} finally {
			connection.disconnect()
		}
	}

	private fun buildEndpoint(
		latitude: Double,
		longitude: Double,
		timeZoneId: String?
	): String {
		val normalizedTimeZone = timeZoneId?.trim().orEmpty()
		if (normalizedTimeZone.isBlank()) {
			return "$BASE_URL?lat=$latitude&lng=$longitude&formatted=0"
		}
		val encodedTimeZone = URLEncoder.encode(
			normalizedTimeZone,
			StandardCharsets.UTF_8.name()
		)
		return "$BASE_URL?lat=$latitude&lng=$longitude&formatted=0&tzid=$encodedTimeZone"
	}

	private fun parseDaylight(
		rawJson: String,
		timeZoneId: String?
	): SunDaylight? {
		val root = JSONObject(rawJson)
		val status = root.optString("status")
		if (status != STATUS_OK && status != STATUS_INVALID_TZID) return null
		val results = root.optJSONObject("results") ?: return null
		val sunriseIso = results.optString("sunrise")
		val sunsetIso = results.optString("sunset")
		val solarNoonIso = results.optString("solar_noon")
		if (sunriseIso.isBlank() || sunsetIso.isBlank()) return null

		val targetZone = timeZoneId
			?.trim()
			?.takeIf { it.isNotBlank() }
			?.let { requested ->
				runCatching { ZoneId.of(requested) }.getOrNull()
			}
		val resolvedTimeZoneId = targetZone?.id ?: root.optString("tzid").takeIf { it.isNotBlank() }
		val sunrise = ZonedDateTime.parse(sunriseIso).let { value ->
			targetZone?.let(value::withZoneSameInstant) ?: value
		}
		val sunset = ZonedDateTime.parse(sunsetIso).let { value ->
			targetZone?.let(value::withZoneSameInstant) ?: value
		}
		val solarNoon = solarNoonIso
			.takeIf { it.isNotBlank() }
			?.let { raw ->
				runCatching { ZonedDateTime.parse(raw) }.getOrNull()
			}
			?.let { value ->
				targetZone?.let(value::withZoneSameInstant) ?: value
			}
		val sunriseMinute = sunrise.hour * 60 + sunrise.minute
		val sunsetMinute = sunset.hour * 60 + sunset.minute
		val solarNoonMinute = solarNoon?.let { it.hour * 60 + it.minute }
			?: deriveSolarNoonMinute(sunriseMinute = sunriseMinute, sunsetMinute = sunsetMinute)

		return SunDaylight(
			sunriseMinute = sunriseMinute.coerceIn(0, 24 * 60),
			sunsetMinute = sunsetMinute.coerceIn(0, 24 * 60),
			solarNoonMinute = solarNoonMinute.coerceIn(0, 24 * 60),
			timeZoneId = resolvedTimeZoneId
		)
	}

	private fun deriveSolarNoonMinute(
		sunriseMinute: Int,
		sunsetMinute: Int
	): Int {
		val sunrise = sunriseMinute.coerceIn(0, 24 * 60)
		val sunset = sunsetMinute.coerceIn(0, 24 * 60)
		val duration = (sunset - sunrise).coerceAtLeast(1)
		return (sunrise + (duration / 2)).coerceIn(0, 24 * 60)
	}

	companion object {
		private const val BASE_URL = "https://api.sunrise-sunset.org/json"
		private const val CONNECT_TIMEOUT_MS = 4_000
		private const val READ_TIMEOUT_MS = 4_000
		private const val STATUS_OK = "OK"
		private const val STATUS_INVALID_TZID = "INVALID_TZID"
	}
}
