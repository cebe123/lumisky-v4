/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 uygulama bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 uygulama bileşeni.
 */
package com.example.lumisky

import android.app.Activity
import android.os.Build
import android.os.Bundle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.widget.Toast
import android.content.ActivityNotFoundException
import com.google.android.play.core.review.ReviewManagerFactory
import android.net.Uri
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.example.lumisky.R
import com.example.lumisky.data.SettingsRepository
import com.example.lumisky.data.WallpaperRepository
import com.example.lumisky.ui.catalog.warmCatalogForLaunch
import com.example.lumisky.ui.catalog.WallpaperCatalogScreen
import com.example.lumisky.ui.catalog.WallpaperCatalogViewModel
import com.example.lumisky.ui.settings.SettingsScreen
import com.example.lumisky.ui.settings.SettingsViewModel
import com.example.lumisky.ui.theme.LumiskyTheme
import com.example.lumisky.ui.wallpaper.LiveWallpaperSetLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var wallpaperRepository: WallpaperRepository
    private val startupWarmupFinished = mutableStateOf(false)
    private val startupUiReady = mutableStateOf(false)
    private var wallpaperPickerFlowActive = false
    private var wallpaperChangedReceiverRegistered = false
    private val wallpaperChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_WALLPAPER_CHANGED || !wallpaperPickerFlowActive) return
            wallpaperPickerFlowActive = false
            lifecycleScope.launch {
                settingsRepository.promotePreviewWallpaper()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        ContextCompat.registerReceiver(
            this,
            wallpaperChangedReceiver,
            IntentFilter(Intent.ACTION_WALLPAPER_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )
        wallpaperChangedReceiverRegistered = true
        splashScreen.setKeepOnScreenCondition { !startupUiReady.value }
        configureEdgeToEdge()
        lifecycleScope.launch {
            runCatching {
                warmCatalogForLaunch(applicationContext, wallpaperRepository)
            }.onFailure { error ->
                Log.w("LumiskyStartup", "Catalog warmup failed; continuing with on-demand loading", error)
            }
            startupWarmupFinished.value = true
        }
        setContent {
            val themeMode by settingsRepository.appThemeMode.collectAsState(initial = SettingsRepository.THEME_SYSTEM)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                SettingsRepository.THEME_LIGHT -> false
                SettingsRepository.THEME_DARK -> true
                else -> systemDark
            }
            LumiskyTheme(darkTheme = darkTheme) {
                if (startupWarmupFinished.value) {
                    LumiskyMainScreen()
                } else {
                    LaunchSkeleton()
                }
            }
        }
    }

    private fun configureEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val attributes = window.attributes
            attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            window.attributes = attributes
        }
    }

    override fun onResume() {
        super.onResume()
        if (cancelWallpaperPickerFlowIfActive()) {
            lifecycleScope.launch {
                settingsRepository.clearPreviewWallpaper()
            }
        }
    }

    override fun onDestroy() {
        if (wallpaperChangedReceiverRegistered) {
            unregisterReceiver(wallpaperChangedReceiver)
            wallpaperChangedReceiverRegistered = false
        }
        super.onDestroy()
    }

    fun beginWallpaperPickerFlow() {
        wallpaperPickerFlowActive = true
    }

    fun cancelWallpaperPickerFlowIfActive(): Boolean {
        if (!wallpaperPickerFlowActive) return false
        wallpaperPickerFlowActive = false
        return true
    }

    fun markStartupUiReady() {
        if (startupUiReady.value) return
        startupUiReady.value = true
        reportFullyDrawn()
    }

}

private const val SCREEN_HOME = "home"
private const val SCREEN_SETTINGS = "settings"
private const val STARTUP_PERMISSION_IDLE_DELAY_MILLIS = 350L

@Composable
fun LaunchSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_splash_logo),
            contentDescription = "App Logo",
            tint = Color(0xFF81ECFF),
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun LumiskyMainScreen() {
    var currentScreen by rememberSaveable { mutableStateOf(SCREEN_HOME) }
    val context = LocalContext.current
    val activity = context as? MainActivity
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val catalogViewModel: WallpaperCatalogViewModel = hiltViewModel()
    val catalogItems by catalogViewModel.items.collectAsState()
    val wallpaperPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK && activity?.cancelWallpaperPickerFlowIfActive() == true) {
            scope.launch {
                catalogViewModel.completeWallpaperSet(applied = false)
            }
        }
    }
    var startLocationWork by remember { mutableStateOf(false) }

    LaunchedEffect(catalogItems.isNotEmpty()) {
        if (catalogItems.isEmpty()) return@LaunchedEffect
        withFrameNanos { }
        withFrameNanos { }
        activity?.markStartupUiReady()
        delay(STARTUP_PERMISSION_IDLE_DELAY_MILLIS)
        startLocationWork = true
    }

    if (startLocationWork) {
        val startupSettingsViewModel: SettingsViewModel = hiltViewModel()
        StartupLocationEffects(startupSettingsViewModel)
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            val direction = if (targetState == SCREEN_SETTINGS) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            (
                slideIntoContainer(
                    towards = direction,
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                    initialOffset = { offset -> offset / 3 }
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 220, delayMillis = 60)
                )
            ).togetherWith(
                slideOutOfContainer(
                    towards = direction,
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    targetOffset = { offset -> offset / 4 }
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 180)
                )
            ).using(SizeTransform(clip = false))
        },
        label = "main_screen_transition"
    ) { screen ->
        when {
            screen == SCREEN_HOME -> {
                WallpaperCatalogScreen(
                    viewModel = catalogViewModel,
                    onItemClick = { id ->
                        scope.launch {
                            catalogViewModel.prepareWallpaperForSet(id)
                            activity?.beginWallpaperPickerFlow()
                            if (!LiveWallpaperSetLauncher.open(context, wallpaperPickerLauncher::launch)) {
                                if (activity?.cancelWallpaperPickerFlowIfActive() != false) {
                                    catalogViewModel.completeWallpaperSet(applied = false)
                                }
                                Toast.makeText(context, "Live wallpaper picker is unavailable", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onSettingsClick = { currentScreen = SCREEN_SETTINGS }
                )
            }
            screen == SCREEN_SETTINGS -> {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { currentScreen = SCREEN_HOME }
                )
            }
        }
    }
}

@Composable
private fun StartupLocationEffects(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val startupLocationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) viewModel.refreshDeviceLocation()
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            startupLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            // Already have permission, attempt to refresh on startup to ensure latest info
            viewModel.refreshDeviceLocation()
        }
    }

    androidx.compose.runtime.DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    viewModel.refreshDeviceLocation()
                }
            }
        }
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}
