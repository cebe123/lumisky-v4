package com.example.lumisky.ui.settings

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.api.SunDaylight
import com.example.core.settings.AppSettingsDefaults
import com.example.core.settings.AppThemeMode
import com.example.core.settings.CityGroup
import com.example.core.settings.LocationMode
import com.example.core.settings.ManualCity
import com.example.core.settings.PerformanceMode
import com.example.lumisky.R
import com.example.lumisky.report.ErrorReporter
import com.example.lumisky.ui.components.BottomNavBar
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	onNavigateHome: () -> Unit,
	appThemeMode: AppThemeMode,
	onThemeModeSelected: (AppThemeMode) -> Unit,
	highRefreshEnabled: Boolean,
	onHighRefreshChanged: (Boolean) -> Unit,
	performanceMode: PerformanceMode,
	onPerformanceModeChanged: (PerformanceMode) -> Unit,
	locationMode: LocationMode,
	locationLabel: String,
	daylight: SunDaylight,
	gpsLocationAvailable: Boolean,
	systemLocationEnabled: Boolean,
	locationRefreshInProgress: Boolean,
	onLocationModeChanged: (LocationMode) -> Unit,
	onRefreshLocation: () -> Unit,
	onRequestEnableSystemLocation: () -> Unit,
	manualCity: ManualCity,
	lastKnownCity: ManualCity?,
	onManualCitySelected: (ManualCity) -> Unit,
	languageTag: String,
	onLanguageSelected: (String) -> Unit
) {
	val context = LocalContext.current
	var showLanguageDialog by remember { mutableStateOf(false) }
	var showCityDialog by remember { mutableStateOf(false) }
	val locationSummary = when {
		locationMode != LocationMode.GPS -> manualCity.name
		!systemLocationEnabled -> stringResource(R.string.location_service_disabled)
		gpsLocationAvailable -> locationLabel
		else -> stringResource(R.string.location_unknown)
	}
	val timelineTimeZoneId = when (locationMode) {
		LocationMode.GPS -> daylight.timeZoneId
		LocationMode.MANUAL -> manualCity.timeZoneId
	}
	val currentMinute = rememberCurrentMinute(timeZoneId = timelineTimeZoneId)
	val celestialTimeline = remember(
		daylight.sunriseMinute,
		daylight.sunsetMinute,
		daylight.solarNoonMinute,
		currentMinute
	) {
		resolveCelestialTimeline(
			daylight = daylight,
			currentMinute = currentMinute
		)
	}
	val citySelectionEnabled = locationMode != LocationMode.GPS
	val appVersionName = remember(context) { resolveAppVersionName(context) }
	val lastKnownCityId = lastKnownCity?.id
	val lastKnownLocationName = lastKnownCity?.name?.takeIf { it.isNotBlank() }
	val selectedLastKnownLocationText = lastKnownLocationName
		?.takeIf { manualCity.id == lastKnownCityId }
		?.let { name -> stringResource(R.string.location_last_known_format, name) }

	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestMultiplePermissions()
	) { result ->
		val granted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
			result[Manifest.permission.ACCESS_FINE_LOCATION] == true
		if (granted) {
			onLocationModeChanged(LocationMode.GPS)
			if (!systemLocationEnabled) {
				onRequestEnableSystemLocation()
			}
		} else {
			onLocationModeChanged(LocationMode.MANUAL)
		}
	}

	Scaffold(
		containerColor = SettingsBackground,
		contentWindowInsets = WindowInsets(0, 0, 0, 0)
	) { innerPadding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(SettingsBackground)
				.padding(innerPadding)
		) {
			SettingsBackdrop()

			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = 20.dp),
				verticalArrangement = Arrangement.spacedBy(18.dp)
			) {
				SettingsTopBar(onNavigateHome = onNavigateHome)
				SettingsHeader()
				AppearanceSection(
					appThemeMode = appThemeMode,
					onThemeModeSelected = onThemeModeSelected,
					languageTag = languageTag,
					onShowLanguageDialog = { showLanguageDialog = true }
				)
				LocationSection(
					locationMode = locationMode,
					locationSummary = locationSummary,
					manualCity = manualCity,
					citySelectionEnabled = citySelectionEnabled,
					selectedLastKnownLocationText = selectedLastKnownLocationText,
					locationRefreshInProgress = locationRefreshInProgress,
					celestialTimeline = celestialTimeline,
					onToggleGps = { enabled ->
						if (!enabled) {
							onLocationModeChanged(LocationMode.MANUAL)
							return@LocationSection
						}

						val hasPermission = ContextCompat.checkSelfPermission(
							context,
							Manifest.permission.ACCESS_COARSE_LOCATION
						) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
							context,
							Manifest.permission.ACCESS_FINE_LOCATION
						) == PackageManager.PERMISSION_GRANTED
						if (hasPermission) {
							onLocationModeChanged(LocationMode.GPS)
							if (!systemLocationEnabled) {
								onRequestEnableSystemLocation()
							}
						} else {
							permissionLauncher.launch(
								arrayOf(
									Manifest.permission.ACCESS_COARSE_LOCATION,
									Manifest.permission.ACCESS_FINE_LOCATION
								)
							)
						}
					},
					onRefreshLocation = {
						val hasPermission = ContextCompat.checkSelfPermission(
							context,
							Manifest.permission.ACCESS_COARSE_LOCATION
						) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
							context,
							Manifest.permission.ACCESS_FINE_LOCATION
						) == PackageManager.PERMISSION_GRANTED
						if (hasPermission) {
							if (systemLocationEnabled) onRefreshLocation() else onRequestEnableSystemLocation()
						} else {
							permissionLauncher.launch(
								arrayOf(
									Manifest.permission.ACCESS_COARSE_LOCATION,
									Manifest.permission.ACCESS_FINE_LOCATION
								)
							)
						}
					},
					onSelectCity = { showCityDialog = true }
				)
				WallpaperSection(
					highRefreshEnabled = highRefreshEnabled,
					onHighRefreshChanged = onHighRefreshChanged,
					performanceMode = performanceMode,
					onPerformanceModeChanged = onPerformanceModeChanged
				)
				SupportSection(
					onReportError = { ErrorReporter.sendErrorReport(context) }
				)
				AboutSection(appVersionName = appVersionName)
				Spacer(modifier = Modifier.height(118.dp))
			}

			Box(
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.fillMaxWidth()
			) {
				BottomNavBar(
					selectedItem = 1,
					onItemSelected = { item ->
						if (item == 0) onNavigateHome()
					}
				)
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

	if (showCityDialog && citySelectionEnabled) {
		val lastKnownLocationTitle = stringResource(R.string.location_last_known)
		CountryCityDialog(
			title = stringResource(R.string.location_select_city),
			groups = remember(languageTag, lastKnownCity, lastKnownLocationTitle) {
				val baseGroups = cityGroups(languageTag)
				if (lastKnownCity != null) {
					listOf(
						CityGroup(
							countryCode = "GPS",
							countryName = lastKnownLocationTitle,
							cities = listOf(lastKnownCity)
						)
					) + baseGroups
				} else {
					baseGroups
				}
			},
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
private fun SettingsBackdrop() {
	val backdropGradient = if (SettingsIsDark) {
		listOf(Color(0xFF080A10), SettingsBackground, Color(0xFF0A0F18))
	} else {
		listOf(Color(0xFFF8FAFF), SettingsBackground, Color(0xFFEFF4FA))
	}
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Brush.verticalGradient(
					colors = backdropGradient
				)
			)
	) {
		Box(
			modifier = Modifier
				.align(Alignment.TopStart)
				.offset(x = (-120).dp, y = (-90).dp)
				.size(300.dp)
				.clip(CircleShape)
				.background(Brush.radialGradient(listOf(SettingsPrimary.copy(alpha = 0.20f), Color.Transparent)))
		)
		Box(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.offset(x = 100.dp, y = 90.dp)
				.size(250.dp)
				.clip(CircleShape)
				.background(Brush.radialGradient(listOf(SettingsSecondary.copy(alpha = 0.14f), Color.Transparent)))
		)
	}
}

@Composable
private fun SettingsTopBar(
	onNavigateHome: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.statusBarsPadding()
			.padding(top = 12.dp, bottom = 8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(10.dp)
		) {
			Box(
				modifier = Modifier
					.size(30.dp)
					.clip(CircleShape)
					.background(SettingsPrimary.copy(alpha = 0.14f)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.Filled.FilterHdr,
					contentDescription = null,
					tint = SettingsPrimary,
					modifier = Modifier.size(18.dp)
				)
			}
			Text(
				text = stringResource(R.string.app_name),
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
				color = SettingsOnSurface
			)
		}
		Spacer(modifier = Modifier.weight(1f))
		IconButton(
			onClick = onNavigateHome,
			modifier = Modifier
				.size(42.dp)
				.clip(CircleShape)
				.background(SettingsSurfaceHighest.copy(alpha = 0.45f))
				.border(1.dp, SettingsGhostBorder, CircleShape)
		) {
			Icon(
				imageVector = Icons.AutoMirrored.Filled.ArrowBack,
				contentDescription = stringResource(R.string.nav_home),
				tint = SettingsOnSurfaceVariant
			)
		}
	}
}

@Composable
private fun SettingsHeader() {
	Column {
		Text(
			text = stringResource(R.string.settings_title),
			style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
			color = SettingsOnSurface
		)
	}
}

@Composable
private fun AppearanceSection(
	appThemeMode: AppThemeMode,
	onThemeModeSelected: (AppThemeMode) -> Unit,
	languageTag: String,
	onShowLanguageDialog: () -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
		GlassCard(kicker = stringResource(R.string.section_appearance)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(14.dp)
			) {
				Text(
					text = stringResource(R.string.theme_title),
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
					color = SettingsOnSurface,
					modifier = Modifier.weight(1f)
				)
				ThemeSelector(
					selectedMode = appThemeMode,
					onModeSelected = onThemeModeSelected
				)
			}
		}
		GlassCard {
			InfoActionRow(
				title = stringResource(R.string.language_title),
				value = languageLabel(languageTag),
				leadingIcon = Icons.Filled.Language,
				onClick = onShowLanguageDialog
			)
		}
	}
}

@Composable
private fun LocationSection(
	locationMode: LocationMode,
	locationSummary: String,
	manualCity: ManualCity,
	citySelectionEnabled: Boolean,
	selectedLastKnownLocationText: String?,
	locationRefreshInProgress: Boolean,
	celestialTimeline: CelestialTimelineSnapshot,
	onToggleGps: (Boolean) -> Unit,
	onRefreshLocation: () -> Unit,
	onSelectCity: () -> Unit
) {
	GlassCard(kicker = stringResource(R.string.section_location_time)) {
		LocationToggleRow(
			locationMode = locationMode,
			locationSummary = locationSummary,
			locationRefreshInProgress = locationRefreshInProgress,
			onRefreshLocation = onRefreshLocation,
			onToggleGps = onToggleGps
		)
		Spacer(modifier = Modifier.height(14.dp))
		CitySelectionField(
			label = stringResource(R.string.location_select_city),
			value = manualCity.name,
			supportingText = selectedLastKnownLocationText,
			enabled = citySelectionEnabled,
			onClick = onSelectCity
		)
		Spacer(modifier = Modifier.height(16.dp))
		CelestialCyclePanel(
			title = stringResource(R.string.location_current_celestial_cycle),
			timelineLabel = stringResource(R.string.location_current_timeline),
			celestialTimeline = celestialTimeline
		)
	}
}

@Composable
private fun WallpaperSection(
	highRefreshEnabled: Boolean,
	onHighRefreshChanged: (Boolean) -> Unit,
	performanceMode: PerformanceMode,
	onPerformanceModeChanged: (PerformanceMode) -> Unit
) {
	GlassCard(kicker = stringResource(R.string.section_wallpaper_settings)) {
		SectionTitle(text = stringResource(R.string.quality_title))
		Spacer(modifier = Modifier.height(10.dp))
		BooleanSelector(
			enabled = highRefreshEnabled,
			enabledLabel = stringResource(R.string.quality_high),
			disabledLabel = stringResource(R.string.quality_low),
			onValueChanged = onHighRefreshChanged
		)
		Spacer(modifier = Modifier.height(16.dp))
		SectionDivider()
		Spacer(modifier = Modifier.height(16.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Box(
				modifier = Modifier
					.size(40.dp)
					.clip(RoundedCornerShape(14.dp))
					.background(SettingsPrimary.copy(alpha = 0.12f)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.Filled.Tune,
					contentDescription = null,
					tint = SettingsPrimary
				)
			}
			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(2.dp)
			) {
				Text(
					text = stringResource(R.string.performance_mode_title),
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
					color = SettingsOnSurface
				)
				Text(
					text = stringResource(R.string.wallpaper_description),
					style = MaterialTheme.typography.bodySmall,
					color = SettingsOnSurfaceVariant,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
		Spacer(modifier = Modifier.height(14.dp))
		SelectableOptionRow(
			title = stringResource(R.string.performance_mode_battery),
			icon = Icons.Filled.BatterySaver,
			selected = performanceMode == PerformanceMode.BATTERY,
			onClick = { onPerformanceModeChanged(PerformanceMode.BATTERY) }
		)
		Spacer(modifier = Modifier.height(10.dp))
		SelectableOptionRow(
			title = stringResource(R.string.performance_mode_auto),
			icon = Icons.Filled.Tune,
			selected = performanceMode == PerformanceMode.AUTO,
			onClick = { onPerformanceModeChanged(PerformanceMode.AUTO) }
		)
		Spacer(modifier = Modifier.height(10.dp))
		SelectableOptionRow(
			title = stringResource(R.string.performance_mode_smooth),
			icon = Icons.Filled.Speed,
			selected = performanceMode == PerformanceMode.SMOOTH,
			onClick = { onPerformanceModeChanged(PerformanceMode.SMOOTH) }
		)
	}
}

@Composable
private fun AboutSection(
	appVersionName: String
) {
	GlassCard(kicker = stringResource(R.string.section_about)) {
		InfoActionRow(
			title = stringResource(R.string.app_version),
			value = appVersionName,
			leadingIcon = null,
			onClick = {},
			enabled = false
		)
	}
}

@Composable
private fun SupportSection(
	onReportError: () -> Unit
) {
	GlassCard(kicker = stringResource(R.string.section_support)) {
		InfoActionRow(
			title = stringResource(R.string.report_issue_title),
			value = stringResource(R.string.report_issue_action),
			leadingIcon = Icons.Filled.BugReport,
			onClick = onReportError
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = stringResource(R.string.report_issue_summary),
			style = MaterialTheme.typography.bodySmall,
			color = SettingsOnSurfaceVariant,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis
		)
	}
}

@Composable
private fun GlassCard(
	kicker: String? = null,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
		if (!kicker.isNullOrBlank()) {
			Text(
				text = kicker.uppercase(Locale.ROOT),
				style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
				color = SettingsPrimary.copy(alpha = 0.70f)
			)
		}
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(28.dp))
				.background(SettingsGlassBrush)
				.border(1.dp, SettingsGhostBorder, RoundedCornerShape(28.dp))
				.padding(20.dp),
			content = content
		)
	}
}

@Composable
private fun ThemeSelector(
	selectedMode: AppThemeMode,
	onModeSelected: (AppThemeMode) -> Unit
) {
	Row(
		modifier = Modifier
			.clip(RoundedCornerShape(16.dp))
			.background(SettingsSurfaceHighest.copy(alpha = 0.70f))
			.border(1.dp, SettingsGhostBorder, RoundedCornerShape(16.dp))
			.padding(4.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp)
	) {
		listOf(AppThemeMode.SYSTEM, AppThemeMode.LIGHT, AppThemeMode.DARK).forEach { mode ->
			SelectorChip(
				label = themeLabel(mode),
				selected = selectedMode == mode,
				onClick = { onModeSelected(mode) }
			)
		}
	}
}

@Composable
private fun BooleanSelector(
	enabled: Boolean,
	enabledLabel: String,
	disabledLabel: String,
	onValueChanged: (Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(18.dp))
			.background(SettingsSurfaceLow.copy(alpha = 0.92f))
			.border(1.dp, SettingsGhostBorder, RoundedCornerShape(18.dp))
			.padding(4.dp),
		horizontalArrangement = Arrangement.spacedBy(6.dp)
	) {
		SelectorChip(
			label = disabledLabel,
			selected = !enabled,
			modifier = Modifier.weight(1f),
			onClick = { onValueChanged(false) }
		)
		SelectorChip(
			label = enabledLabel,
			selected = enabled,
			modifier = Modifier.weight(1f),
			onClick = { onValueChanged(true) }
		)
	}
}

@Composable
private fun SelectorChip(
	label: String,
	selected: Boolean,
	modifier: Modifier = Modifier,
	onClick: () -> Unit
) {
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.background(if (selected) SettingsPrimary.copy(alpha = 0.18f) else Color.Transparent)
			.clickable(onClick = onClick)
			.padding(horizontal = 12.dp, vertical = 9.dp),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
			color = if (selected) SettingsPrimary else SettingsOnSurfaceVariant,
			maxLines = 1
		)
	}
}

@Composable
private fun InfoActionRow(
	title: String,
	value: String,
	leadingIcon: androidx.compose.ui.graphics.vector.ImageVector?,
	onClick: () -> Unit,
	enabled: Boolean = true
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(18.dp))
			.clickable(enabled = enabled, onClick = onClick)
			.padding(vertical = 2.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (leadingIcon != null) {
			Icon(
				imageVector = leadingIcon,
				contentDescription = null,
				tint = SettingsOnSurfaceVariant,
				modifier = Modifier.size(18.dp)
			)
			Spacer(modifier = Modifier.width(10.dp))
		}
		Text(
			text = title,
			style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
			color = if (enabled) SettingsOnSurface else SettingsOnSurfaceVariant,
			modifier = Modifier.weight(1f)
		)
		Text(
			text = value,
			style = MaterialTheme.typography.bodyMedium,
			color = SettingsOnSurfaceVariant
		)
		if (enabled) {
			Spacer(modifier = Modifier.width(8.dp))
			Icon(
				imageVector = Icons.Filled.ChevronRight,
				contentDescription = null,
				tint = SettingsOnSurfaceVariant,
				modifier = Modifier.size(18.dp)
			)
		}
	}
}

@Composable
private fun LocationToggleRow(
	locationMode: LocationMode,
	locationSummary: String,
	locationRefreshInProgress: Boolean,
	onRefreshLocation: () -> Unit,
	onToggleGps: (Boolean) -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Text(
				text = stringResource(R.string.location_enable),
				style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
				color = SettingsOnSurface
			)
			Text(
				text = locationSummary,
				style = MaterialTheme.typography.bodySmall,
				color = SettingsOnSurfaceVariant,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			RefreshLocationButton(
				enabled = locationMode == LocationMode.GPS && !locationRefreshInProgress,
				inProgress = locationRefreshInProgress,
				onClick = onRefreshLocation
			)
			Switch(
				checked = locationMode == LocationMode.GPS,
				onCheckedChange = onToggleGps,
				colors = SwitchDefaults.colors(
					checkedThumbColor = SettingsOnPrimaryFixed,
					checkedTrackColor = SettingsPrimary,
					uncheckedThumbColor = SettingsOnSurfaceVariant,
					uncheckedTrackColor = SettingsSurfaceHighest,
					uncheckedBorderColor = SettingsGhostBorder,
					checkedBorderColor = Color.Transparent
				)
			)
		}
	}
}

@Composable
private fun RefreshLocationButton(
	enabled: Boolean,
	inProgress: Boolean,
	onClick: () -> Unit
) {
	val shape = RoundedCornerShape(16.dp)
	val containerBrush = if (enabled) {
		Brush.linearGradient(
			listOf(
				SettingsPrimary.copy(alpha = 0.18f),
				SettingsSurfaceHigh.copy(alpha = 0.94f)
			)
		)
	} else {
		Brush.linearGradient(
			listOf(
				SettingsSurfaceLow.copy(alpha = 0.90f),
				SettingsSurfaceHighest.copy(alpha = 0.86f)
			)
		)
	}
	val borderColor = if (enabled) {
		SettingsPrimary.copy(alpha = 0.26f)
	} else {
		SettingsGhostBorder
	}
	Box(
		modifier = Modifier
			.size(44.dp)
			.clip(shape)
			.background(containerBrush)
			.border(1.dp, borderColor, shape)
			.clickable(enabled = enabled, onClick = onClick),
		contentAlignment = Alignment.Center
	) {
		if (inProgress) {
			CircularProgressIndicator(
				modifier = Modifier.size(18.dp),
				color = SettingsPrimary,
				strokeWidth = 2.dp
			)
		} else {
			Icon(
				imageVector = Icons.Filled.Refresh,
				contentDescription = stringResource(R.string.location_refresh),
				tint = if (enabled) SettingsPrimary else SettingsOnSurfaceVariant.copy(alpha = 0.45f),
				modifier = Modifier.size(18.dp)
			)
		}
	}
}

@Composable
private fun CitySelectionField(
	label: String,
	value: String,
	supportingText: String? = null,
	enabled: Boolean,
	onClick: () -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Text(
			text = label.uppercase(Locale.ROOT),
			style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
			color = SettingsOnSurfaceVariant
		)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(18.dp))
				.background(SettingsSurfaceLow.copy(alpha = 0.78f))
				.border(1.dp, SettingsGhostBorder, RoundedCornerShape(18.dp))
				.clickable(enabled = enabled, onClick = onClick)
				.padding(horizontal = 16.dp, vertical = 14.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = value,
				style = MaterialTheme.typography.bodyMedium,
				color = if (enabled) SettingsOnSurface else SettingsOnSurfaceVariant.copy(alpha = 0.55f),
				modifier = Modifier.weight(1f)
			)
			Icon(
				imageVector = Icons.Filled.LocationOn,
				contentDescription = null,
				tint = if (enabled) SettingsOnSurfaceVariant else SettingsOnSurfaceVariant.copy(alpha = 0.35f),
				modifier = Modifier.size(18.dp)
			)
		}
		if (!supportingText.isNullOrBlank()) {
			Text(
				text = supportingText,
				style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
				color = SettingsOnSurfaceVariant,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

@Composable
private fun CelestialCyclePanel(
	title: String,
	timelineLabel: String,
	celestialTimeline: CelestialTimelineSnapshot
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(24.dp))
			.background(SettingsSurfaceLow.copy(alpha = 0.94f))
			.border(1.dp, SettingsGhostBorder, RoundedCornerShape(24.dp))
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Icon(
					imageVector = Icons.Filled.FilterHdr,
					contentDescription = null,
					tint = SettingsPrimary,
					modifier = Modifier.size(16.dp)
				)
				Text(
					text = title.uppercase(Locale.ROOT),
					style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
					color = SettingsOnSurface
				)
			}
			Box(
				modifier = Modifier
					.clip(CircleShape)
					.background(SettingsSurfaceHighest.copy(alpha = 0.82f))
					.border(1.dp, SettingsGhostBorder, CircleShape)
					.padding(horizontal = 10.dp, vertical = 5.dp)
			) {
				Text(
					text = timelineLabel.uppercase(Locale.ROOT),
					style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
					color = SettingsOnSurfaceVariant
				)
			}
		}
		CelestialTimelineTrack(celestialTimeline = celestialTimeline)
		Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(24.dp)
			) {
				CelestialEventCell(
					icon = Icons.Filled.LightMode,
					label = stringResource(R.string.location_current_sunrise),
					value = formatMinuteLabel(celestialTimeline.sunriseMinute),
					accent = SettingsTertiary,
					modifier = Modifier.weight(1f)
				)
				CelestialEventCell(
					icon = Icons.Filled.LightMode,
					label = stringResource(R.string.location_current_sunset),
					value = formatMinuteLabel(celestialTimeline.sunsetMinute),
					accent = SettingsSecondary,
					modifier = Modifier.weight(1f),
					trailing = true
				)
			}
			HorizontalDivider(color = SettingsGhostBorder.copy(alpha = 0.24f))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(24.dp)
			) {
				CelestialEventCell(
					icon = Icons.Filled.DarkMode,
					label = stringResource(R.string.location_current_moonrise),
					value = formatMinuteLabel(celestialTimeline.moonriseMinute),
					accent = SettingsPrimary.copy(alpha = 0.88f),
					modifier = Modifier.weight(1f)
				)
				CelestialEventCell(
					icon = Icons.Filled.DarkMode,
					label = stringResource(R.string.location_current_moonset),
					value = formatMinuteLabel(celestialTimeline.moonsetMinute),
					accent = SettingsOnSurfaceVariant.copy(alpha = 0.84f),
					modifier = Modifier.weight(1f),
					trailing = true
				)
			}
		}
	}
}

@Composable
private fun CelestialTimelineTrack(
	celestialTimeline: CelestialTimelineSnapshot
) {
	BoxWithConstraints(
		modifier = Modifier
			.fillMaxWidth()
			.height(30.dp)
	) {
		val markerSize = 18.dp
		val markerTravel = (maxWidth - markerSize).coerceAtLeast(0.dp)
		Box(
			modifier = Modifier
				.align(Alignment.CenterStart)
				.fillMaxWidth()
				.height(6.dp)
				.clip(CircleShape)
				.background(CelestialTimelineBrush)
				.border(1.dp, SettingsGhostBorder.copy(alpha = 0.34f), CircleShape)
		)
		if (celestialTimeline.sunActive) {
			CelestialMarker(
				icon = Icons.Filled.LightMode,
				fillColor = Color(0xFFFFC14D),
				iconTint = Color(0xFF6F3B00),
				glowColor = Color(0xFFFFC14D),
				modifier = Modifier
					.align(Alignment.CenterStart)
					.offset(x = markerTravel * celestialTimeline.sunProgress)
			)
		}
		if (celestialTimeline.moonActive) {
			CelestialMarker(
				icon = Icons.Filled.DarkMode,
				fillColor = Color(0xFFE9EEF8),
				iconTint = Color(0xFF384467),
				glowColor = SettingsSecondary,
				modifier = Modifier
					.align(Alignment.CenterStart)
					.offset(x = markerTravel * celestialTimeline.moonProgress)
			)
		}
	}
}

@Composable
private fun CelestialMarker(
	icon: ImageVector,
	fillColor: Color,
	iconTint: Color,
	glowColor: Color,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier,
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = Modifier
				.size(26.dp)
				.clip(CircleShape)
				.background(glowColor.copy(alpha = 0.22f))
		)
		Box(
			modifier = Modifier
				.size(18.dp)
				.clip(CircleShape)
				.background(fillColor)
				.border(1.dp, SettingsOnSurface.copy(alpha = 0.14f), CircleShape),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = iconTint,
				modifier = Modifier.size(10.dp)
			)
		}
	}
}

@Composable
private fun CelestialEventCell(
	icon: ImageVector,
	label: String,
	value: String,
	accent: Color,
	modifier: Modifier = Modifier,
	trailing: Boolean = false
) {
	Column(
		modifier = modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(6.dp),
		horizontalAlignment = if (trailing) Alignment.End else Alignment.Start
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(6.dp)
		) {
			if (trailing) {
				Text(
					text = label.uppercase(Locale.ROOT),
					style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
					color = SettingsOnSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = accent,
				modifier = Modifier.size(14.dp)
			)
			if (!trailing) {
				Text(
					text = label.uppercase(Locale.ROOT),
					style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
					color = SettingsOnSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
		Text(
			text = value,
			style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
			color = SettingsOnSurface,
			textAlign = if (trailing) TextAlign.End else TextAlign.Start
		)
	}
}

@Composable
private fun SectionTitle(
	text: String
) {
	Text(
		text = text.uppercase(Locale.ROOT),
		style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
		color = SettingsOnSurfaceVariant
	)
}

@Composable
private fun SelectableOptionRow(
	title: String,
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	selected: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(18.dp))
			.background(if (selected) SettingsPrimary.copy(alpha = 0.10f) else SettingsSurfaceLow.copy(alpha = 0.58f))
			.border(
				1.dp,
				if (selected) SettingsPrimary.copy(alpha = 0.26f) else SettingsGhostBorder,
				RoundedCornerShape(18.dp)
			)
			.clickable(onClick = onClick)
			.padding(horizontal = 14.dp, vertical = 14.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = if (selected) SettingsPrimary else SettingsOnSurfaceVariant,
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.width(12.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
			color = SettingsOnSurface,
			modifier = Modifier.weight(1f)
		)
		RadioButton(
			selected = selected,
			onClick = null
		)
	}
}

@Composable
private fun SectionDivider() {
	HorizontalDivider(color = SettingsGhostBorder)
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
		containerColor = SettingsSurfaceHigh,
		title = {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
				color = SettingsOnSurface
			)
		},
		text = {
			Column(
				modifier = Modifier.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				options.forEach { (value, label) ->
					DialogOptionRow(
						label = label,
						selected = selectedValue == value,
						onClick = { onSelect(value) }
					)
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(
					text = stringResource(R.string.btn_cancel),
					color = SettingsPrimary
				)
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
		containerColor = SettingsSurfaceHigh,
		title = {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
				color = SettingsOnSurface
			)
		},
		text = {
			Column(
				modifier = Modifier.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(10.dp)
			) {
				groups.forEach { group ->
					Text(
						text = group.countryName.uppercase(Locale.ROOT),
						style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
						color = SettingsPrimary.copy(alpha = 0.72f)
					)
					group.cities.forEach { city ->
						DialogOptionRow(
							label = city.name,
							selected = selectedCityId == city.id,
							onClick = { onSelect(city) }
						)
					}
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(
					text = stringResource(R.string.btn_cancel),
					color = SettingsPrimary
				)
			}
		}
	)
}

@Composable
private fun DialogOptionRow(
	label: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.background(if (selected) SettingsPrimary.copy(alpha = 0.12f) else SettingsSurfaceLow.copy(alpha = 0.52f))
			.border(
				1.dp,
				if (selected) SettingsPrimary.copy(alpha = 0.22f) else SettingsGhostBorder,
				RoundedCornerShape(16.dp)
			)
			.clickable(onClick = onClick)
			.padding(horizontal = 14.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyMedium,
			color = SettingsOnSurface,
			modifier = Modifier.weight(1f)
		)
		RadioButton(
			selected = selected,
			onClick = null
		)
	}
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

private fun cityGroups(languageTag: String): List<CityGroup> {
	return AppSettingsDefaults.supportedCityGroups(languageTag)
}

private fun resolveAppVersionName(context: android.content.Context): String {
	return runCatching {
		val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
		packageInfo.versionName ?: "1.0"
	}.getOrDefault("1.0")
}

@Composable
private fun rememberCurrentMinute(timeZoneId: String?): Int {
	val context = LocalContext.current
	val appContext = remember(context) { context.applicationContext }
	val zoneId = remember(timeZoneId) { resolveTimelineZoneId(timeZoneId) }
	var currentMinute by remember(appContext, zoneId) {
		mutableStateOf(currentMinuteOfDay(zoneId))
	}
	DisposableEffect(appContext, zoneId) {
		val receiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				currentMinute = currentMinuteOfDay(zoneId)
			}
		}
		val filter = IntentFilter().apply {
			addAction(Intent.ACTION_TIME_TICK)
			addAction(Intent.ACTION_TIME_CHANGED)
			addAction(Intent.ACTION_TIMEZONE_CHANGED)
			addAction(Intent.ACTION_DATE_CHANGED)
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
		} else {
			@Suppress("DEPRECATION")
			appContext.registerReceiver(receiver, filter)
		}
		onDispose {
			runCatching { appContext.unregisterReceiver(receiver) }
		}
	}
	LaunchedEffect(zoneId) {
		while (true) {
			val now = ZonedDateTime.now(zoneId)
			currentMinute = now.hour * 60 + now.minute
			delay(delayUntilNextMinute(now))
		}
	}
	return currentMinute
}

private fun resolveTimelineZoneId(timeZoneId: String?): ZoneId {
	val normalized = timeZoneId?.trim().orEmpty()
	if (normalized.isBlank()) return ZoneId.systemDefault()
	return runCatching { ZoneId.of(normalized) }.getOrElse { ZoneId.systemDefault() }
}

private fun currentMinuteOfDay(zoneId: ZoneId): Int {
	val now = ZonedDateTime.now(zoneId)
	return now.hour * 60 + now.minute
}

private fun delayUntilNextMinute(now: ZonedDateTime): Long {
	val millisUntilNextMinute = ((60 - now.second).coerceAtLeast(1) * 1000L) - (now.nano / 1_000_000L)
	return millisUntilNextMinute.coerceAtLeast(250L)
}

private fun formatMinuteLabel(minute: Int): String {
	val normalized = minute.coerceIn(0, (24 * 60) - 1)
	return String.format(Locale.US, "%02d:%02d", normalized / 60, normalized % 60)
}

private val SettingsIsDark: Boolean
	@Composable
	get() = MaterialTheme.colorScheme.background.luminance() < 0.5f

private val SettingsBackground: Color
	@Composable
	get() = MaterialTheme.colorScheme.background

private val SettingsSurfaceLow: Color
	@Composable
	get() = if (SettingsIsDark) {
		MaterialTheme.colorScheme.surface
	} else {
		MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
	}

private val SettingsSurfaceHigh: Color
	@Composable
	get() = if (SettingsIsDark) {
		MaterialTheme.colorScheme.surfaceVariant
	} else {
		MaterialTheme.colorScheme.surface
	}

private val SettingsSurfaceHighest: Color
	@Composable
	get() = if (SettingsIsDark) {
		MaterialTheme.colorScheme.surfaceTint
	} else {
		MaterialTheme.colorScheme.surfaceVariant
	}

private val SettingsPrimary: Color
	@Composable
	get() = MaterialTheme.colorScheme.primary

private val SettingsSecondary: Color
	@Composable
	get() = MaterialTheme.colorScheme.secondary

private val SettingsTertiary: Color
	@Composable
	get() = MaterialTheme.colorScheme.tertiary

private val SettingsOnPrimaryFixed: Color
	@Composable
	get() = MaterialTheme.colorScheme.onPrimary

private val SettingsOnSurface: Color
	@Composable
	get() = MaterialTheme.colorScheme.onSurface

private val SettingsOnSurfaceVariant: Color
	@Composable
	get() = MaterialTheme.colorScheme.onSurfaceVariant

private val SettingsGhostBorder: Color
	@Composable
	get() = MaterialTheme.colorScheme.outline.copy(alpha = if (SettingsIsDark) 0.42f else 0.28f)

private val SettingsGlassBrush: Brush
	@Composable
	get() = Brush.verticalGradient(
		colors = if (SettingsIsDark) {
			listOf(
				SettingsSurfaceHighest.copy(alpha = 0.84f),
				SettingsSurfaceHigh.copy(alpha = 0.96f)
			)
		} else {
			listOf(
				MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
				MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f)
			)
		}
	)

private val CelestialTimelineBrush: Brush
	@Composable
	get() = Brush.horizontalGradient(
		colorStops = arrayOf(
			0.00f to Color(0xFFD05434),
			0.46f to Color(0xFFF3C64E),
			1.00f to if (SettingsIsDark) Color(0xFF07090D) else MaterialTheme.colorScheme.primary
		)
	)
