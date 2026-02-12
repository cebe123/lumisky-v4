package com.example.core.api

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZonedDateTime

data class SunDaylight(
	val sunriseMinute: Int,
	val sunsetMinute: Int
) {
	companion object {
		fun fallback(): SunDaylight {
			return SunDaylight(
				sunriseMinute = 6 * 60,
				sunsetMinute = 18 * 60
			)
		}
	}
}

class SunTimesApiClient {

	fun fetchDaylight(
		latitude: Double,
		longitude: Double
	): SunDaylight? {
		val endpoint = buildEndpoint(latitude, longitude)
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
			parseDaylight(payload)
		} catch (_: Throwable) {
			null
		} finally {
			connection.disconnect()
		}
	}

	private fun buildEndpoint(latitude: Double, longitude: Double): String {
		return "$BASE_URL?lat=$latitude&lng=$longitude&formatted=0"
	}

	private fun parseDaylight(rawJson: String): SunDaylight? {
		val root = JSONObject(rawJson)
		if (root.optString("status") != "OK") return null
		val results = root.optJSONObject("results") ?: return null
		val sunriseIso = results.optString("sunrise")
		val sunsetIso = results.optString("sunset")
		if (sunriseIso.isBlank() || sunsetIso.isBlank()) return null

		val sunrise = ZonedDateTime.parse(sunriseIso)
		val sunset = ZonedDateTime.parse(sunsetIso)
		val sunriseMinute = sunrise.hour * 60 + sunrise.minute
		val sunsetMinute = sunset.hour * 60 + sunset.minute

		return SunDaylight(
			sunriseMinute = sunriseMinute.coerceIn(0, 24 * 60),
			sunsetMinute = sunsetMinute.coerceIn(0, 24 * 60)
		)
	}

	companion object {
		private const val BASE_URL = "https://api.sunrise-sunset.org/json"
		private const val CONNECT_TIMEOUT_MS = 4_000
		private const val READ_TIMEOUT_MS = 4_000
	}
}
