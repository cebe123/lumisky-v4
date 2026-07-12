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
import com.example.lumisky.R
import com.example.lumisky.data.SettingsRepository
import com.example.lumisky.ui.catalog.WallpaperCatalogScreen
import com.example.lumisky.ui.catalog.WallpaperCatalogViewModel
import com.example.lumisky.ui.preview.WallpaperPreviewScreen
import com.example.lumisky.ui.preview.WallpaperPreviewViewModel
import com.example.lumisky.ui.settings.SettingsScreen
import com.example.lumisky.ui.settings.SettingsViewModel
import com.example.lumisky.ui.theme.LumiskyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var isWarmedUp = false
        splashScreen.setKeepOnScreenCondition { !isWarmedUp }
        configureEdgeToEdge()
        setContent {
            val themeMode by settingsRepository.appThemeMode.collectAsState(initial = SettingsRepository.THEME_SYSTEM)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                SettingsRepository.THEME_LIGHT -> false
                SettingsRepository.THEME_DARK -> true
                else -> systemDark
            }
            LumiskyTheme(darkTheme = darkTheme) {
                var startupReady by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    isWarmedUp = true
                    startupReady = true
                }
                if (startupReady) {
                    LumiskyMainScreen()
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

}

private const val SCREEN_HOME = "home"
private const val SCREEN_SETTINGS = "settings"
private const val SCREEN_PREVIEW_PREFIX = "preview:"
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
    var startLocationWork by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        withFrameNanos { }
        (context as? MainActivity)?.reportFullyDrawn()
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
                val viewModel: WallpaperCatalogViewModel = hiltViewModel()
                WallpaperCatalogScreen(
                    viewModel = viewModel,
                    onItemClick = { id ->
                        currentScreen = "$SCREEN_PREVIEW_PREFIX$id"
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
            screen.startsWith(SCREEN_PREVIEW_PREFIX) -> {
                val previewViewModel: WallpaperPreviewViewModel = hiltViewModel()
                WallpaperPreviewScreen(
                    wallpaperId = screen.removePrefix(SCREEN_PREVIEW_PREFIX),
                    viewModel = previewViewModel,
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
