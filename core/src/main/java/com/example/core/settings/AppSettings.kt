package com.example.core.settings

import com.example.core.location.LocationSnapshot
import java.util.Locale
import kotlin.math.abs

enum class AppThemeMode {
	SYSTEM,
	LIGHT,
	DARK
}

enum class LocationMode {
	GPS,
	MANUAL
}

enum class PerformanceMode {
	AUTO,
	SMOOTH,
	BATTERY
}

data class ManualCity(
	val id: String,
	val name: String,
	val countryCode: String,
	val latitude: Double,
	val longitude: Double,
	val timeZoneId: String
)

data class CityGroup(
	val countryCode: String,
	val countryName: String,
	val cities: List<ManualCity>
)

data class AppSettingsSnapshot(
	val appThemeMode: AppThemeMode,
	val languageTag: String,
	val highRefreshEnabled: Boolean,
	val performanceMode: PerformanceMode,
	val locationMode: LocationMode,
	val manualCity: ManualCity,
	val automaticLocation: LocationSnapshot?
)

private data class CityDefinition(
	val id: String,
	val countryCode: String,
	val latitude: Double,
	val longitude: Double,
	val timeZoneId: String,
	val names: Map<String, String>
) {
	fun toManualCity(languageTag: String): ManualCity {
		return ManualCity(
			id = id,
			name = resolveLocalizedName(names, languageTag),
			countryCode = countryCode,
			latitude = latitude,
			longitude = longitude,
			timeZoneId = timeZoneId
		)
	}
}

private data class CountryDefinition(
	val code: String,
	val names: Map<String, String>,
	val cityIds: List<String>
) {
	fun displayName(languageTag: String): String {
		return resolveLocalizedName(names, languageTag)
	}
}

object AppSettingsDefaults {
	const val LANGUAGE_SYSTEM = "system"
	const val DEFAULT_CITY_ID = "tr_istanbul"

	val DEFAULT_CITY: ManualCity
		get() = defaultCity(LANGUAGE_SYSTEM)

	val SUPPORTED_CITIES: List<ManualCity>
		get() = supportedCities(LANGUAGE_SYSTEM)

	fun defaultCity(languageTag: String): ManualCity {
		return cityDefinitionById[DEFAULT_CITY_ID]?.toManualCity(languageTag)
			?: cityDefinitions.first().toManualCity(languageTag)
	}

	fun supportedCities(languageTag: String): List<ManualCity> {
		return countryDefinitions.flatMap { country ->
			country.cityIds.mapNotNull { cityId ->
				cityDefinitionById[cityId]?.toManualCity(languageTag)
			}
		}
	}

	fun supportedCityGroups(languageTag: String): List<CityGroup> {
		return countryDefinitions.mapNotNull { country ->
			val cities = country.cityIds.mapNotNull { cityId ->
				cityDefinitionById[cityId]?.toManualCity(languageTag)
			}
			if (cities.isEmpty()) {
				null
			} else {
				CityGroup(
					countryCode = country.code,
					countryName = country.displayName(languageTag),
					cities = cities
				)
			}
		}
	}

	fun resolveCityById(cityId: String?, languageTag: String): ManualCity {
		if (cityId.isNullOrBlank()) return defaultCity(languageTag)
		return cityDefinitionById[cityId]?.toManualCity(languageTag)
			?: defaultCity(languageTag)
	}

	fun resolveCityByLegacy(
		legacyName: String?,
		latitude: Double,
		longitude: Double,
		languageTag: String
	): ManualCity {
		val byCoordinates = cityDefinitions.firstOrNull { city ->
			abs(city.latitude - latitude) <= LEGACY_COORDINATE_THRESHOLD &&
				abs(city.longitude - longitude) <= LEGACY_COORDINATE_THRESHOLD
		}
		if (byCoordinates != null) return byCoordinates.toManualCity(languageTag)

		val normalizedName = legacyName.orEmpty().trim().lowercase(Locale.ROOT)
		if (normalizedName.isNotBlank()) {
			val byName = cityDefinitions.firstOrNull { city ->
				city.names.values.any { name ->
					name.trim().lowercase(Locale.ROOT) == normalizedName
				}
			}
			if (byName != null) return byName.toManualCity(languageTag)
		}

		return defaultCity(languageTag)
	}

	private const val LEGACY_COORDINATE_THRESHOLD = 0.08

	private val cityDefinitions = listOf(
		CityDefinition(
			id = "tr_istanbul",
			countryCode = "TR",
			latitude = 41.0082,
			longitude = 28.9784,
			timeZoneId = "Europe/Istanbul",
			names = mapOf("en" to "Istanbul", "tr" to "Istanbul")
		),
		CityDefinition(
			id = "tr_ankara",
			countryCode = "TR",
			latitude = 39.9334,
			longitude = 32.8597,
			timeZoneId = "Europe/Istanbul",
			names = mapOf("en" to "Ankara", "tr" to "Ankara")
		),
		CityDefinition(
			id = "tr_izmir",
			countryCode = "TR",
			latitude = 38.4192,
			longitude = 27.1287,
			timeZoneId = "Europe/Istanbul",
			names = mapOf("en" to "Izmir", "tr" to "Izmir")
		),
		CityDefinition(
			id = "tr_bursa",
			countryCode = "TR",
			latitude = 40.1885,
			longitude = 29.0610,
			timeZoneId = "Europe/Istanbul",
			names = mapOf("en" to "Bursa", "tr" to "Bursa")
		),
		CityDefinition(
			id = "tr_antalya",
			countryCode = "TR",
			latitude = 36.8969,
			longitude = 30.7133,
			timeZoneId = "Europe/Istanbul",
			names = mapOf("en" to "Antalya", "tr" to "Antalya")
		),
		CityDefinition(
			id = "tr_adana",
			countryCode = "TR",
			latitude = 37.0000,
			longitude = 35.3213,
			timeZoneId = "Europe/Istanbul",
			names = mapOf("en" to "Adana", "tr" to "Adana")
		),
		CityDefinition(
			id = "us_new_york",
			countryCode = "US",
			latitude = 40.7128,
			longitude = -74.0060,
			timeZoneId = "America/New_York",
			names = mapOf("en" to "New York", "tr" to "New York")
		),
		CityDefinition(
			id = "us_los_angeles",
			countryCode = "US",
			latitude = 34.0522,
			longitude = -118.2437,
			timeZoneId = "America/Los_Angeles",
			names = mapOf("en" to "Los Angeles", "tr" to "Los Angeles")
		),
		CityDefinition(
			id = "us_chicago",
			countryCode = "US",
			latitude = 41.8781,
			longitude = -87.6298,
			timeZoneId = "America/Chicago",
			names = mapOf("en" to "Chicago", "tr" to "Chicago")
		),
		CityDefinition(
			id = "us_san_francisco",
			countryCode = "US",
			latitude = 37.7749,
			longitude = -122.4194,
			timeZoneId = "America/Los_Angeles",
			names = mapOf("en" to "San Francisco", "tr" to "San Francisco")
		),
		CityDefinition(
			id = "gb_london",
			countryCode = "GB",
			latitude = 51.5074,
			longitude = -0.1278,
			timeZoneId = "Europe/London",
			names = mapOf("en" to "London", "tr" to "Londra")
		),
		CityDefinition(
			id = "gb_manchester",
			countryCode = "GB",
			latitude = 53.4808,
			longitude = -2.2426,
			timeZoneId = "Europe/London",
			names = mapOf("en" to "Manchester", "tr" to "Manchester")
		),
		CityDefinition(
			id = "fr_paris",
			countryCode = "FR",
			latitude = 48.8566,
			longitude = 2.3522,
			timeZoneId = "Europe/Paris",
			names = mapOf("en" to "Paris", "tr" to "Paris")
		),
		CityDefinition(
			id = "fr_lyon",
			countryCode = "FR",
			latitude = 45.7640,
			longitude = 4.8357,
			timeZoneId = "Europe/Paris",
			names = mapOf("en" to "Lyon", "tr" to "Lyon")
		),
		CityDefinition(
			id = "fr_marseille",
			countryCode = "FR",
			latitude = 43.2965,
			longitude = 5.3698,
			timeZoneId = "Europe/Paris",
			names = mapOf("en" to "Marseille", "tr" to "Marsilya")
		),
		CityDefinition(
			id = "de_berlin",
			countryCode = "DE",
			latitude = 52.5200,
			longitude = 13.4050,
			timeZoneId = "Europe/Berlin",
			names = mapOf("en" to "Berlin", "tr" to "Berlin")
		),
		CityDefinition(
			id = "de_munich",
			countryCode = "DE",
			latitude = 48.1351,
			longitude = 11.5820,
			timeZoneId = "Europe/Berlin",
			names = mapOf("en" to "Munich", "tr" to "Munih")
		),
		CityDefinition(
			id = "de_hamburg",
			countryCode = "DE",
			latitude = 53.5511,
			longitude = 9.9937,
			timeZoneId = "Europe/Berlin",
			names = mapOf("en" to "Hamburg", "tr" to "Hamburg")
		),
		CityDefinition(
			id = "jp_tokyo",
			countryCode = "JP",
			latitude = 35.6762,
			longitude = 139.6503,
			timeZoneId = "Asia/Tokyo",
			names = mapOf("en" to "Tokyo", "tr" to "Tokyo")
		),
		CityDefinition(
			id = "jp_osaka",
			countryCode = "JP",
			latitude = 34.6937,
			longitude = 135.5023,
			timeZoneId = "Asia/Tokyo",
			names = mapOf("en" to "Osaka", "tr" to "Osaka")
		),
		CityDefinition(
			id = "jp_kyoto",
			countryCode = "JP",
			latitude = 35.0116,
			longitude = 135.7681,
			timeZoneId = "Asia/Tokyo",
			names = mapOf("en" to "Kyoto", "tr" to "Kyoto")
		),
		CityDefinition(
			id = "es_madrid",
			countryCode = "ES",
			latitude = 40.4168,
			longitude = -3.7038,
			timeZoneId = "Europe/Madrid",
			names = mapOf("en" to "Madrid", "tr" to "Madrid")
		),
		CityDefinition(
			id = "es_barcelona",
			countryCode = "ES",
			latitude = 41.3874,
			longitude = 2.1686,
			timeZoneId = "Europe/Madrid",
			names = mapOf("en" to "Barcelona", "tr" to "Barselona")
		),
		CityDefinition(
			id = "it_rome",
			countryCode = "IT",
			latitude = 41.9028,
			longitude = 12.4964,
			timeZoneId = "Europe/Rome",
			names = mapOf("en" to "Rome", "tr" to "Roma")
		),
		CityDefinition(
			id = "it_milan",
			countryCode = "IT",
			latitude = 45.4642,
			longitude = 9.1900,
			timeZoneId = "Europe/Rome",
			names = mapOf("en" to "Milan", "tr" to "Milano")
		)
	)

	private val cityDefinitionById = cityDefinitions.associateBy { it.id }

	private val countryDefinitions = listOf(
		CountryDefinition(
			code = "TR",
			names = mapOf("en" to "Turkey", "tr" to "Turkiye"),
			cityIds = listOf(
				"tr_istanbul",
				"tr_ankara",
				"tr_izmir",
				"tr_bursa",
				"tr_antalya",
				"tr_adana"
			)
		),
		CountryDefinition(
			code = "US",
			names = mapOf("en" to "United States", "tr" to "Amerika Birlesik Devletleri"),
			cityIds = listOf("us_new_york", "us_los_angeles", "us_chicago", "us_san_francisco")
		),
		CountryDefinition(
			code = "GB",
			names = mapOf("en" to "United Kingdom", "tr" to "Birlesik Krallik"),
			cityIds = listOf("gb_london", "gb_manchester")
		),
		CountryDefinition(
			code = "FR",
			names = mapOf("en" to "France", "tr" to "Fransa"),
			cityIds = listOf("fr_paris", "fr_lyon", "fr_marseille")
		),
		CountryDefinition(
			code = "DE",
			names = mapOf("en" to "Germany", "tr" to "Almanya"),
			cityIds = listOf("de_berlin", "de_munich", "de_hamburg")
		),
		CountryDefinition(
			code = "JP",
			names = mapOf("en" to "Japan", "tr" to "Japonya"),
			cityIds = listOf("jp_tokyo", "jp_osaka", "jp_kyoto")
		),
		CountryDefinition(
			code = "ES",
			names = mapOf("en" to "Spain", "tr" to "Ispanya"),
			cityIds = listOf("es_madrid", "es_barcelona")
		),
		CountryDefinition(
			code = "IT",
			names = mapOf("en" to "Italy", "tr" to "Italya"),
			cityIds = listOf("it_rome", "it_milan")
		)
	)
}

private fun resolveLocalizedName(
	names: Map<String, String>,
	languageTag: String
): String {
	val normalized = normalizeLanguageTag(languageTag)
	val baseLanguage = normalized.substringBefore('-')
	return names[normalized]
		?: names[baseLanguage]
		?: names["en"]
		?: names.values.firstOrNull()
		?: "Unknown"
}

private fun normalizeLanguageTag(languageTag: String): String {
	val resolved = if (languageTag == AppSettingsDefaults.LANGUAGE_SYSTEM) {
		Locale.getDefault().toLanguageTag()
	} else {
		languageTag
	}
	return resolved.trim().lowercase(Locale.ROOT)
}
