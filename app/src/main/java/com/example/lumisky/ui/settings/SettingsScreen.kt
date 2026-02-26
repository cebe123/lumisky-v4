package com.example.lumisky.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppThemeMode
import com.example.core.settings.CityGroup
import com.example.core.settings.LocationMode
import com.example.core.settings.ManualCity
import com.example.core.settings.PerformanceMode
import com.example.lumisky.R
import com.example.lumisky.ui.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	onNavigateHome: () -> Unit,
	appThemeMode: AppThemeMode,
	onCycleTheme: () -> Unit,
	highRefreshEnabled: Boolean,
	onHighRefreshChanged: (Boolean) -> Unit,
	performanceMode: PerformanceMode,
	onPerformanceModeChanged: (PerformanceMode) -> Unit,
	locationMode: LocationMode,
	locationLabel: String,
	gpsLocationAvailable: Boolean,
	systemLocationEnabled: Boolean,
	onLocationModeChanged: (LocationMode) -> Unit,
	onRequestEnableSystemLocation: () -> Unit,
	manualCity: ManualCity,
	onManualCitySelected: (ManualCity) -> Unit,
	languageTag: String,
	onLanguageSelected: (String) -> Unit
) {
	val context = LocalContext.current
	var showLanguageDialog by remember { mutableStateOf(false) }
	var showCityDialog by remember { mutableStateOf(false) }
	val sectionAppearance = stringResource(R.string.section_appearance)
	val sectionLocationTime = stringResource(R.string.section_location_time)
	val sectionWallpaper = stringResource(R.string.section_wallpaper_settings)
	val sectionAbout = stringResource(R.string.section_about)
	val sectionOrder = remember(sectionAppearance, sectionLocationTime, sectionWallpaper, sectionAbout) {
		listOf(sectionAppearance, sectionLocationTime, sectionWallpaper, sectionAbout)
	}
	val appVersionName = remember(context) { resolveAppVersionName(context) }

	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission()
	) { granted ->
		if (granted) {
			onLocationModeChanged(LocationMode.GPS)
			if (!systemLocationEnabled) {
				onRequestEnableSystemLocation()
			}
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(
						text = stringResource(R.string.settings_title),
						style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
					)
				},
				navigationIcon = {
					IconButton(onClick = onNavigateHome) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = stringResource(R.string.settings_title)
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					scrolledContainerColor = MaterialTheme.colorScheme.background
				)
			)
		},
		bottomBar = {
			BottomNavBar(
				selectedItem = 1,
				onItemSelected = { item ->
					if (item == 0) onNavigateHome()
				}
			)
		}
	) { innerPadding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding),
			contentPadding = PaddingValues(16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
				items(sectionOrder) { section ->
					when (section) {
						sectionAppearance -> {
						SectionCard(title = section) {
							SettingActionRow(
								icon = Icons.Filled.Palette,
								title = stringResource(R.string.theme_title),
								value = themeLabel(appThemeMode),
								onClick = onCycleTheme
							)
							HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
							SettingActionRow(
								icon = Icons.Filled.Language,
								title = stringResource(R.string.language_title),
								value = languageLabel(languageTag),
								onClick = { showLanguageDialog = true }
							)
						}
					}

						sectionLocationTime -> {
						SectionCard(title = section) {
							SettingSwitchRow(
								icon = Icons.Filled.LocationOn,
								title = stringResource(R.string.location_enable),
								checked = locationMode == LocationMode.GPS,
								onCheckedChange = { enabled ->
									if (!enabled) {
										onLocationModeChanged(LocationMode.MANUAL)
										return@SettingSwitchRow
									}

									val hasPermission = ContextCompat.checkSelfPermission(
										context,
										Manifest.permission.ACCESS_FINE_LOCATION
									) == PackageManager.PERMISSION_GRANTED
									if (hasPermission) {
										onLocationModeChanged(LocationMode.GPS)
										if (!systemLocationEnabled) {
											onRequestEnableSystemLocation()
										}
									} else {
										permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
									}
								}
							)
							HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
							SettingActionRow(
								icon = Icons.Filled.LocationCity,
								title = stringResource(R.string.location_select_city),
								value = if (locationMode == LocationMode.GPS) {
									if (gpsLocationAvailable && systemLocationEnabled) {
										locationLabel
									} else {
										"$locationLabel / GPS unavailable"
									}
								} else {
									locationLabel
								},
								onClick = {
									showCityDialog = true
								},
								enabled = true
							)
						}
					}

						sectionWallpaper -> {
						SectionCard(title = section) {
							SettingActionRow(
								icon = Icons.Filled.Tune,
								title = stringResource(R.string.performance_mode_title),
								value = performanceModeLabel(performanceMode),
								onClick = { onPerformanceModeChanged(nextPerformanceMode(performanceMode)) }
							)
						}
					}

						else -> {
							SectionCard(title = section) {
									SettingActionRow(
										icon = Icons.Filled.Tune,
										title = stringResource(R.string.app_version),
										value = appVersionName,
										onClick = {},
										enabled = false
									)
							}
						}
					}
				}
			}
		}

	if (showLanguageDialog) {
		ChoiceDialog(
			title = stringResource(R.string.language_select),
			options = languageOptions(),
			selectedValue = languageTag,
			onDismiss = { showLanguageDialog = false },
			onSelect = { selected ->
				onLanguageSelected(selected)
				showLanguageDialog = false
			}
		)
	}

	if (showCityDialog) {
		CountryCityDialog(
			title = stringResource(R.string.location_select_city),
			groups = cityGroups(languageTag),
			selectedCityId = manualCity.id,
			onDismiss = { showCityDialog = false },
			onSelect = { city ->
				onManualCitySelected(city)
				showCityDialog = false
			}
		)
	}
}

@Composable
private fun SectionCard(
	title: String,
	content: @Composable () -> Unit
) {
	Column(
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.labelLarge.copy(
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.Bold
			)
		)
		Card(
			modifier = Modifier.fillMaxWidth(),
			shape = RoundedCornerShape(16.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer
			)
		) {
			content()
		}
	}
}

@Composable
private fun SettingActionRow(
	icon: ImageVector,
	title: String,
	value: String,
	onClick: () -> Unit,
	enabled: Boolean = true
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(enabled = enabled, onClick = onClick)
			.padding(horizontal = 16.dp, vertical = 14.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge,
				color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
			)
		}
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(
				text = value,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(modifier = Modifier.width(8.dp))
			Icon(
				imageVector = Icons.Filled.ChevronRight,
				contentDescription = null,
				tint = if (enabled) MaterialTheme.colorScheme.outline else Color.Transparent
			)
		}
	}
}

@Composable
private fun SettingSwitchRow(
	icon: ImageVector,
	title: String,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Box(
				modifier = Modifier
					.size(34.dp)
					.background(
						color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
						shape = RoundedCornerShape(10.dp)
					),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary
				)
			}
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge
			)
		}
		Switch(
			checked = checked,
			onCheckedChange = onCheckedChange
		)
	}
}

@Composable
private fun ChoiceDialog(
	title: String,
	options: Map<String, String>,
	selectedValue: String,
	onDismiss: () -> Unit,
	onSelect: (String) -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium
			)
		},
		text = {
			Column(
				modifier = Modifier.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				options.forEach { (value, label) ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable { onSelect(value) }
							.padding(vertical = 10.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						Text(
							text = label,
							style = MaterialTheme.typography.bodyLarge
						)
						if (selectedValue == value) {
							Text(
								text = "•",
								style = MaterialTheme.typography.titleMedium,
								color = MaterialTheme.colorScheme.primary
							)
						}
					}
					HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.btn_cancel))
			}
		}
	)
}

@Composable
private fun CountryCityDialog(
	title: String,
	groups: List<CityGroup>,
	selectedCityId: String,
	onDismiss: () -> Unit,
	onSelect: (ManualCity) -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium
			)
		},
		text = {
			Column(
				modifier = Modifier.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				groups.forEach { group ->
					Text(
						text = group.countryName,
						style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
					)
					group.cities.forEach { city ->
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clickable { onSelect(city) }
								.padding(vertical = 8.dp),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							Text(
								text = city.name,
								style = MaterialTheme.typography.bodyLarge,
								modifier = Modifier.padding(start = 10.dp)
							)
							if (selectedCityId == city.id) {
								Text(
									text = "•",
									style = MaterialTheme.typography.titleMedium,
									color = MaterialTheme.colorScheme.primary
								)
							}
						}
					}
					HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.btn_cancel))
			}
		}
	)
}

@Composable
private fun themeLabel(mode: AppThemeMode): String {
	return when (mode) {
		AppThemeMode.SYSTEM -> stringResource(R.string.theme_system)
		AppThemeMode.LIGHT -> stringResource(R.string.theme_light)
		AppThemeMode.DARK -> stringResource(R.string.theme_dark)
	}
}

private fun languageLabel(tag: String): String {
	return languageOptions()[tag] ?: languageOptions()[AppSettingsLanguage.SYSTEM] ?: "System"
}

@Composable
private fun performanceModeLabel(mode: PerformanceMode): String {
	return when (mode) {
		PerformanceMode.AUTO -> stringResource(R.string.performance_mode_auto)
		PerformanceMode.SMOOTH -> stringResource(R.string.performance_mode_smooth)
		PerformanceMode.BATTERY -> stringResource(R.string.performance_mode_battery)
	}
}

private fun nextPerformanceMode(mode: PerformanceMode): PerformanceMode {
	return when (mode) {
		PerformanceMode.AUTO -> PerformanceMode.SMOOTH
		PerformanceMode.SMOOTH -> PerformanceMode.BATTERY
		PerformanceMode.BATTERY -> PerformanceMode.AUTO
	}
}

private object AppSettingsLanguage {
	const val SYSTEM = "system"
	const val EN = "en"
	const val TR = "tr"
	const val ES = "es"
	const val FR = "fr"
	const val DE = "de"
	const val IT = "it"
	const val PT = "pt"
	const val RU = "ru"
	const val JA = "ja"
	const val ZH_CN = "zh-CN"
	const val HI = "hi"
	const val AR = "ar"
}

private fun languageOptions(): Map<String, String> {
	return linkedMapOf(
		AppSettingsLanguage.SYSTEM to "System",
		AppSettingsLanguage.EN to "English",
		AppSettingsLanguage.TR to "Turkce",
		AppSettingsLanguage.ES to "Espanol",
		AppSettingsLanguage.FR to "Francais",
		AppSettingsLanguage.DE to "Deutsch",
		AppSettingsLanguage.IT to "Italiano",
		AppSettingsLanguage.PT to "Portugues",
		AppSettingsLanguage.RU to "Russkiy",
		AppSettingsLanguage.JA to "Nihongo",
		AppSettingsLanguage.ZH_CN to "JianTi ZhongWen",
		AppSettingsLanguage.HI to "Hindi",
		AppSettingsLanguage.AR to "Arabic"
	)
}

private fun cityOptions(): List<ManualCity> {
	return AppSettingsDefaults.SUPPORTED_CITIES
}

private fun cityGroups(languageTag: String): List<CityGroup> {
	return AppSettingsDefaults.supportedCityGroups(languageTag)
}

private fun resolveAppVersionName(context: android.content.Context): String {
	return runCatching {
		val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
		packageInfo.versionName ?: "1.0"
	}.getOrDefault("1.0")
}
