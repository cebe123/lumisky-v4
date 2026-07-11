/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Kalite, parallax, battery behavior gibi kullanıcı ayarları.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Kalite, parallax, battery behavior gibi kullanıcı ayarları.
 */
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.example.lumisky.data.LocationLightingMode
import com.example.lumisky.data.SettingsLocationPlanner
import com.example.lumisky.data.SettingsRepository
import com.example.lumisky.engine.LocationDaylightController
import com.example.lumisky.ui.components.BottomNavBar
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.common.api.ResolvableApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val selectedWallpaperId by viewModel.selectedWallpaperId.collectAsState()
    val qualityTier by viewModel.qualityTier.collectAsState()
    val locationMode by viewModel.locationMode.collectAsState()
    val manualLatitude by viewModel.manualLatitude.collectAsState()
    val manualLongitude by viewModel.manualLongitude.collectAsState()
    val manualTimeZone by viewModel.manualTimeZone.collectAsState()
    val deviceSnapshot by viewModel.deviceLocationSnapshot.collectAsState()
    val previewTimeSimulation by viewModel.previewTimeSimulation.collectAsState()
    val highRefreshEnabled by viewModel.highRefreshEnabled.collectAsState()
    val performanceMode by viewModel.performanceMode.collectAsState()
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val languageTag by viewModel.languageTag.collectAsState()

    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            viewModel.refreshDeviceLocation()
        } else {
            viewModel.setLocationMode(SettingsRepository.LOCATION_MODE_MANUAL)
        }
    }

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.setLocationMode(SettingsRepository.LOCATION_MODE_DEVICE)
        } else {
            viewModel.setLocationMode(SettingsRepository.LOCATION_MODE_MANUAL)
        }
    }

    fun promptEnableGps() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)
        client.checkLocationSettings(builder.build()).addOnSuccessListener {
            viewModel.setLocationMode(SettingsRepository.LOCATION_MODE_DEVICE)
        }.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    locationSettingsLauncher.launch(intentSenderRequest)
                } catch (sendEx: Exception) {
                    viewModel.setLocationMode(SettingsRepository.LOCATION_MODE_MANUAL)
                }
            } else {
                viewModel.setLocationMode(SettingsRepository.LOCATION_MODE_MANUAL)
            }
        }
    }

    val manualLocation = remember(manualLatitude, manualLongitude, manualTimeZone) {
        SettingsLocationPlanner.resolveManualLocation(manualLatitude, manualLongitude, manualTimeZone)
    }
    val resolvedLocation = remember(locationMode, manualLocation, deviceSnapshot) {
        SettingsLocationPlanner.resolve(
            mode = SettingsLocationPlanner.modeFromStorage(locationMode),
            manualLocation = manualLocation,
            deviceSnapshot = deviceSnapshot,
            nowEpochMs = System.currentTimeMillis()
        )
    }
    val daylight = remember(resolvedLocation) {
        LocationDaylightController().resolve(
            latitude = resolvedLocation.latitude,
            longitude = resolvedLocation.longitude,
            timeZoneId = resolvedLocation.timeZoneId
        )
    }
    val currentMinute = rememberCurrentMinute(timeZoneId = resolvedLocation.timeZoneId)
    val celestialTimeline = remember(daylight, currentMinute) {
        resolveCelestialTimeline(daylight, currentMinute)
    }

    val bgColor = MaterialTheme.colorScheme.background
    val isDark = bgColor.luminance() < 0.5f

    Scaffold(
        containerColor = bgColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 126.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsTopBar(onBackClick)
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                AppearanceSection(
                    isDark = isDark,
                    appThemeMode = appThemeMode,
                    languageTag = languageTag,
                    onThemeModeSelected = viewModel::setAppThemeMode,
                    onLanguageSelected = viewModel::setLanguageTag
                )
                LocationSection(
                    isDark = isDark,
                    locationMode = SettingsLocationPlanner.modeFromStorage(locationMode),
                    resolvedLabel = resolvedLocation.label,
                    usesDeviceLocation = resolvedLocation.usesDeviceLocation,
                    deviceSnapshotLabel = deviceSnapshot?.label,
                    celestialTimeline = celestialTimeline,
                    onToggleDevice = { enabled ->
                        if (!enabled) {
                            viewModel.setLocationMode(SettingsRepository.LOCATION_MODE_MANUAL)
                            return@LocationSection
                        }
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                            val isGpsEnabled = locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true ||
                                               locationManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true
                            if (isGpsEnabled) {
                                viewModel.setLocationMode(SettingsRepository.LOCATION_MODE_DEVICE)
                            } else {
                                promptEnableGps()
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        }
                    },
                    onRefreshDevice = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                            val isGpsEnabled = locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true ||
                                               locationManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true
                            if (isGpsEnabled) {
                                viewModel.setLocationMode(SettingsRepository.LOCATION_MODE_DEVICE)
                            } else {
                                promptEnableGps()
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        }
                    },
                    onManualLocationSelected = { preset ->
                        viewModel.setManualLocation(preset.latitude, preset.longitude, preset.timeZoneId)
                    },
                    manualLocation = manualLocation
                )
                WallpaperSection(
                    isDark = isDark,
                    selectedWallpaperId = selectedWallpaperId,
                    qualityTier = qualityTier,
                    highRefreshEnabled = highRefreshEnabled,
                    performanceMode = performanceMode,
                    onQualitySelected = viewModel::setQualityTier,
                    onHighRefreshChanged = viewModel::setHighRefreshEnabled,
                    onPerformanceModeSelected = viewModel::setPerformanceMode
                )
                RuntimeEffectsSection(
                    isDark = isDark,
                    previewTimeSimulation = previewTimeSimulation,
                    onPreviewTimeSimulationChanged = viewModel::setPreviewTimeSimulation
                )
                SupportAndAboutSection(isDark)
                Spacer(modifier = Modifier.height(10.dp))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                BottomNavBar(
                    selectedItem = 1,
                    animationsEnabled = false,
                    onItemSelected = { index ->
                        if (index == 0) onBackClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 4.dp),
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterHdr,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Lumisky",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Home",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppearanceSection(
    isDark: Boolean,
    appThemeMode: String,
    languageTag: String,
    onThemeModeSelected: (String) -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    var languageExpanded by rememberSaveable { mutableStateOf(false) }
    SettingsCard(isDark = isDark, kicker = "Appearance") {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        listOf(
            SettingsRepository.THEME_SYSTEM to "System",
            SettingsRepository.THEME_LIGHT to "Light",
            SettingsRepository.THEME_DARK to "Dark"
        ).forEach { (value, title) ->
            SelectableOptionRow(
                title = title,
                icon = if (value == SettingsRepository.THEME_DARK) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                selected = appThemeMode == value,
                onClick = { onThemeModeSelected(value) }
            )
        }
        SectionDivider()
        InfoActionRow(
            title = "Language",
            value = languageLabel(languageTag),
            leadingIcon = Icons.Filled.Language
        )
        SelectableOptionRow(
            title = languageLabel(languageTag),
            subtitle = "Tap to ${if (languageExpanded) "close" else "change"} language list",
            icon = if (languageExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            selected = true,
            onClick = { languageExpanded = !languageExpanded }
        )
        if (languageExpanded) {
            languageOptions().forEach { (tag, label) ->
                SelectableOptionRow(
                    title = label,
                    icon = Icons.Filled.Language,
                    selected = languageTag == tag,
                    onClick = {
                        onLanguageSelected(tag)
                        languageExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LocationSection(
    isDark: Boolean,
    locationMode: LocationLightingMode,
    resolvedLabel: String,
    usesDeviceLocation: Boolean,
    deviceSnapshotLabel: String?,
    celestialTimeline: CelestialTimelineSnapshot,
    onToggleDevice: (Boolean) -> Unit,
    onRefreshDevice: () -> Unit,
    onManualLocationSelected: (com.example.lumisky.data.ManualLocationPreset) -> Unit,
    manualLocation: com.example.lumisky.data.ManualLocationPreset
) {
    var manualCityExpanded by rememberSaveable { mutableStateOf(false) }
    SettingsCard(isDark = isDark, kicker = "Location & Time") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Location lighting",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = resolvedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(
                checked = locationMode == LocationLightingMode.DEVICE,
                onCheckedChange = onToggleDevice
            )
            IconButton(
                onClick = onRefreshDevice,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        deviceSnapshotLabel?.takeIf { it.isNotBlank() }?.let { label ->
            Text(
                text = "Device location: $label",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (locationMode != LocationLightingMode.DEVICE) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Manual city",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            SelectableOptionRow(
                title = "${manualLocation.label}, ${manualLocation.country}",
                subtitle = "Tap to ${if (manualCityExpanded) "close" else "change"} city list",
                icon = if (manualCityExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                selected = true,
                onClick = { manualCityExpanded = !manualCityExpanded }
            )
            if (manualCityExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsLocationPlanner.supportedManualLocations().forEach { preset ->
                    SelectableOptionRow(
                        title = "${preset.label}, ${preset.country}",
                        icon = Icons.Filled.LocationOn,
                        selected = preset.id == manualLocation.id,
                        onClick = {
                            onManualLocationSelected(preset)
                            manualCityExpanded = false
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        CelestialCyclePanel(celestialTimeline)
    }
}

@Composable
private fun WallpaperSection(
    isDark: Boolean,
    selectedWallpaperId: String,
    qualityTier: String,
    highRefreshEnabled: Boolean,
    performanceMode: String,
    onQualitySelected: (String) -> Unit,
    onHighRefreshChanged: (Boolean) -> Unit,
    onPerformanceModeSelected: (String) -> Unit
) {
    SettingsCard(isDark = isDark, kicker = "Wallpaper Settings") {
        InfoActionRow(
            title = "Active wallpaper",
            value = selectedWallpaperId,
            leadingIcon = Icons.Filled.LightMode
        )
        SectionDivider()
        Text(
            text = "Rendering quality",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        listOf("LOW", "BALANCED", "HIGH").forEach { tier ->
            SelectableOptionRow(
                title = tier.lowercase(Locale.US).replaceFirstChar { it.uppercase() },
                icon = Icons.Filled.Tune,
                selected = qualityTier == tier,
                onClick = { onQualitySelected(tier) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        SectionDivider()
        SwitchRow(
            icon = Icons.Filled.Speed,
            title = "High refresh preview",
            subtitle = if (highRefreshEnabled) "Smooth app previews" else "Lower app preview power use",
            checked = highRefreshEnabled,
            onCheckedChange = onHighRefreshChanged
        )
        SectionDivider()
        Text(
            text = "Performance mode",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        listOf(
            Triple(SettingsRepository.PERFORMANCE_MODE_BATTERY, "Battery", "15 FPS, low quality, optional effects paused"),
            Triple(SettingsRepository.PERFORMANCE_MODE_AUTO, "Auto", "Uses selected quality and adapts under OS pressure"),
            Triple(SettingsRepository.PERFORMANCE_MODE_SMOOTH, "Smooth", "High quality, 60 FPS when high refresh is enabled")
        ).forEach { (mode, title, subtitle) ->
            SelectableOptionRow(
                title = title,
                subtitle = subtitle,
                icon = if (mode == SettingsRepository.PERFORMANCE_MODE_BATTERY) Icons.Filled.BatterySaver else Icons.Filled.Speed,
                selected = performanceMode == mode,
                onClick = { onPerformanceModeSelected(mode) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RuntimeEffectsSection(
    isDark: Boolean,
    previewTimeSimulation: Boolean,
    onPreviewTimeSimulationChanged: (Boolean) -> Unit
) {
    SettingsCard(isDark = isDark, kicker = "Effects & Runtime") {
        SwitchRow(
            icon = Icons.Filled.LightMode,
            title = "Preview time simulation",
            subtitle = if (previewTimeSimulation) "Sun and moon animate in previews" else "Preview uses real time",
            checked = previewTimeSimulation,
            onCheckedChange = onPreviewTimeSimulationChanged
        )
        SectionDivider()
        InfoActionRow(
            title = "Effect pipeline",
            value = "Layer policies, shader fallback and telemetry are enabled by the v5 engine.",
            leadingIcon = Icons.Filled.Tune
        )
    }
}

@Composable
private fun SupportAndAboutSection(isDark: Boolean) {
    SettingsCard(isDark = isDark, kicker = "Support") {
        InfoActionRow(
            title = "Report issue",
            value = "Collect logs after a render fallback or black screen.",
            leadingIcon = Icons.Filled.BugReport
        )
        SectionDivider()
        InfoActionRow(
            title = "Version",
            value = "1.0",
            leadingIcon = null
        )
    }
}

@Composable
private fun CelestialCyclePanel(
    celestialTimeline: CelestialTimelineSnapshot
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.62f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Current celestial cycle",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Timeline",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        CelestialTimelineTrack(celestialTimeline = celestialTimeline)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TimelineCell(
                icon = Icons.Filled.LightMode,
                label = "Sunrise",
                value = formatMinuteLabel(celestialTimeline.sunriseMinute),
                modifier = Modifier.weight(1f)
            )
            TimelineCell(
                icon = Icons.Filled.LightMode,
                label = "Sunset",
                value = formatMinuteLabel(celestialTimeline.sunsetMinute),
                modifier = Modifier.weight(1f)
            )
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
        
        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        val timelineBrush = Brush.horizontalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFD05434),
                0.46f to Color(0xFFF3C64E),
                1.00f to if (isDark) Color(0xFF07090D) else MaterialTheme.colorScheme.primary
            )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(timelineBrush)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.34f), CircleShape)
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
                glowColor = MaterialTheme.colorScheme.secondary,
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
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f), CircleShape),
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
private fun TimelineCell(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label.uppercase(Locale.US),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
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

@Composable
private fun SettingsCard(
    isDark: Boolean,
    kicker: String,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val gradient = if (isDark) {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
            )
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(gradient)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.34f else 0.24f), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = kicker.uppercase(Locale.US),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun InfoActionRow(
    title: String,
    value: String,
    leadingIcon: ImageVector?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SelectableOptionRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.52f)
            )
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
}

private fun languageLabel(tag: String): String {
    return languageOptions()[tag] ?: languageOptions()[SettingsRepository.LANGUAGE_SYSTEM] ?: "System"
}

private fun languageOptions(): Map<String, String> {
    return linkedMapOf(
        SettingsRepository.LANGUAGE_SYSTEM to "System",
        SettingsRepository.LANGUAGE_EN to "English",
        SettingsRepository.LANGUAGE_TR to "Turkce",
        SettingsRepository.LANGUAGE_ES to "Espanol",
        SettingsRepository.LANGUAGE_FR to "Francais",
        SettingsRepository.LANGUAGE_DE to "Deutsch",
        SettingsRepository.LANGUAGE_IT to "Italiano",
        SettingsRepository.LANGUAGE_PT to "Portugues",
        SettingsRepository.LANGUAGE_RU to "Russkiy",
        SettingsRepository.LANGUAGE_JA to "Nihongo",
        SettingsRepository.LANGUAGE_ZH_CN to "JianTi ZhongWen",
        SettingsRepository.LANGUAGE_HI to "Hindi",
        SettingsRepository.LANGUAGE_AR to "Arabic"
    )
}
