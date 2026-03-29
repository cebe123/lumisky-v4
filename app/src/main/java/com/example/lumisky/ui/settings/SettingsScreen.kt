package com.example.lumisky.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.lumisky.ui.components.BottomNavBar
import java.util.Locale

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
	val moonZenithMinute = remember(daylight.solarNoonMinute) {
		(daylight.solarNoonMinute + (12 * 60)) % (24 * 60)
	}
	val citySelectionEnabled = locationMode != LocationMode.GPS
	val appVersionName = remember(context) { resolveAppVersionName(context) }

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

	Scaffold(containerColor = SettingsBackground) { innerPadding ->
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
					locationRefreshInProgress = locationRefreshInProgress,
					daylight = daylight,
					moonZenithMinute = moonZenithMinute,
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
						if (systemLocationEnabled) onRefreshLocation() else onRequestEnableSystemLocation()
					},
					onSelectCity = { showCityDialog = true }
				)
				WallpaperSection(
					highRefreshEnabled = highRefreshEnabled,
					onHighRefreshChanged = onHighRefreshChanged,
					performanceMode = performanceMode,
					onPerformanceModeChanged = onPerformanceModeChanged
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
private fun SettingsBackdrop() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(
				brush = Brush.verticalGradient(
					colors = listOf(Color(0xFF080A10), SettingsBackground, Color(0xFF0A0F18))
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
	locationRefreshInProgress: Boolean,
	daylight: SunDaylight,
	moonZenithMinute: Int,
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
			enabled = citySelectionEnabled,
			onClick = onSelectCity
		)
		Spacer(modifier = Modifier.height(16.dp))
		SunTimesPanel(
			title = stringResource(R.string.location_current_sun_times),
			subtitle = locationSummary,
			entries = listOf(
				SunTimeEntry(
					label = stringResource(R.string.location_current_sunrise),
					value = formatMinuteLabel(daylight.sunriseMinute),
					accent = SettingsSecondary
				),
				SunTimeEntry(
					label = stringResource(R.string.location_current_solar_noon),
					value = formatMinuteLabel(daylight.solarNoonMinute),
					accent = SettingsPrimary
				),
				SunTimeEntry(
					label = stringResource(R.string.location_current_sunset),
					value = formatMinuteLabel(daylight.sunsetMinute),
					accent = SettingsTertiary
				),
				SunTimeEntry(
					label = stringResource(R.string.location_current_moon_zenith),
					value = formatMinuteLabel(moonZenithMinute),
					accent = SettingsSecondary.copy(alpha = 0.82f)
				)
			)
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
			Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
				Text(
					text = stringResource(R.string.performance_mode_title),
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
					color = SettingsOnSurface
				)
				Text(
					text = stringResource(R.string.wallpaper_description),
					style = MaterialTheme.typography.bodySmall,
					color = SettingsOnSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
		Spacer(modifier = Modifier.height(14.dp))
		PerformanceOptionRow(
			title = stringResource(R.string.performance_mode_battery),
			icon = Icons.Filled.BatterySaver,
			selected = performanceMode == PerformanceMode.BATTERY,
			onClick = { onPerformanceModeChanged(PerformanceMode.BATTERY) }
		)
		Spacer(modifier = Modifier.height(10.dp))
		PerformanceOptionRow(
			title = stringResource(R.string.performance_mode_auto),
			icon = Icons.Filled.Tune,
			selected = performanceMode == PerformanceMode.AUTO,
			onClick = { onPerformanceModeChanged(PerformanceMode.AUTO) }
		)
		Spacer(modifier = Modifier.height(10.dp))
		PerformanceOptionRow(
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
	}
}

@Composable
private fun SunTimesPanel(
	title: String,
	subtitle: String,
	entries: List<SunTimeEntry>
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(24.dp))
			.background(SettingsSurfaceLow.copy(alpha = 0.94f))
			.border(1.dp, SettingsGhostBorder, RoundedCornerShape(24.dp))
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(14.dp)
	) {
		Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
			Text(
				text = title,
				style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
				color = SettingsOnSurface
			)
			Text(
				text = subtitle,
				style = MaterialTheme.typography.bodySmall,
				color = SettingsOnSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
		entries.chunked(2).forEach { rowEntries ->
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(10.dp)
			) {
				rowEntries.forEach { entry ->
					SunTimeCell(
						entry = entry,
						modifier = Modifier.weight(1f)
					)
				}
				if (rowEntries.size == 1) {
					Spacer(modifier = Modifier.weight(1f))
				}
			}
		}
	}
}

@Composable
private fun SunTimeCell(
	entry: SunTimeEntry,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.clip(RoundedCornerShape(18.dp))
			.background(SettingsSurfaceHighest.copy(alpha = 0.55f))
			.padding(horizontal = 12.dp, vertical = 12.dp),
		verticalArrangement = Arrangement.spacedBy(6.dp)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Box(
				modifier = Modifier
					.size(8.dp)
					.clip(CircleShape)
					.background(entry.accent)
			)
			Text(
				text = entry.label.uppercase(Locale.ROOT),
				style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
				color = SettingsOnSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
		Text(
			text = entry.value,
			style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
			color = SettingsOnSurface
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
private fun PerformanceOptionRow(
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

private fun formatMinuteLabel(minute: Int): String {
	val normalized = minute.coerceIn(0, (24 * 60) - 1)
	return String.format(Locale.US, "%02d:%02d", normalized / 60, normalized % 60)
}

private data class SunTimeEntry(
	val label: String,
	val value: String,
	val accent: Color
)

private val SettingsBackground = Color(0xFF0D0E13)
private val SettingsSurfaceLow = Color(0xFF121319)
private val SettingsSurfaceHigh = Color(0xFF1E1F26)
private val SettingsSurfaceHighest = Color(0xFF24252D)
private val SettingsPrimary = Color(0xFF81ECFF)
private val SettingsSecondary = Color(0xFFB884FF)
private val SettingsTertiary = Color(0xFFFF84AA)
private val SettingsOnPrimaryFixed = Color(0xFF003840)
private val SettingsOnSurface = Color(0xFFF7F5FD)
private val SettingsOnSurfaceVariant = Color(0xFFABAAB1)
private val SettingsGhostBorder = Color(0xFF47474E).copy(alpha = 0.42f)
private val SettingsGlassBrush = Brush.verticalGradient(
	colors = listOf(
		SettingsSurfaceHighest.copy(alpha = 0.84f),
		SettingsSurfaceHigh.copy(alpha = 0.96f)
	)
)
