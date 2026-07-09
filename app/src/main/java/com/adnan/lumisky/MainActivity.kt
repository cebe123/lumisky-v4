/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 uygulama bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 uygulama bileşeni.
 */
package com.adnan.lumisky

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.adnan.lumisky.R
import com.adnan.lumisky.data.SettingsRepository
import com.adnan.lumisky.data.WallpaperRepository
import com.adnan.lumisky.ui.catalog.WallpaperCatalogScreen
import com.adnan.lumisky.ui.catalog.WallpaperCatalogViewModel
import com.adnan.lumisky.ui.preview.WallpaperPreviewScreen
import com.adnan.lumisky.ui.preview.WallpaperPreviewViewModel
import com.adnan.lumisky.ui.settings.SettingsScreen
import com.adnan.lumisky.ui.settings.SettingsViewModel
import com.adnan.lumisky.ui.theme.LumiskyTheme
import com.adnan.lumisky.ui.wallpaper.LiveWallpaperSetLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var wallpaperRepository: WallpaperRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    warmHomeStartupThumbnails()
                    startupReady = true
                }
                if (startupReady) {
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

    private suspend fun warmHomeStartupThumbnails() {
        withContext(Dispatchers.IO) {
            runCatching {
                wallpaperRepository.getCatalog()
                    .wallpapers
                    .take(STARTUP_THUMBNAIL_WARM_LIMIT)
                    .forEach { item ->
                        runCatching {
                            assets.open(item.thumbnail).use { stream ->
                                BitmapFactory.decodeStream(stream)
                            }
                        }
                    }
            }
        }
    }

    private companion object {
        const val STARTUP_THUMBNAIL_WARM_LIMIT = 3
    }
}

private const val SCREEN_HOME = "home"
private const val SCREEN_SETTINGS = "settings"
private const val SCREEN_PREVIEW_PREFIX = "preview:"

@Composable
private fun LaunchSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                android.widget.ImageView(context).apply {
                    setImageResource(R.mipmap.ic_launcher)
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                }
            },
            modifier = Modifier.size(112.dp)
        )
    }
}

@Composable
private fun LumiskyMainScreen() {
    var currentScreen by rememberSaveable { mutableStateOf(SCREEN_HOME) }

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
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                WallpaperCatalogScreen(
                    viewModel = viewModel,
                    onItemClick = { id ->
                        coroutineScope.launch {
                            viewModel.selectWallpaperForSet(id)
                            LiveWallpaperSetLauncher.open(context)
                        }
                    },
                    onSettingsClick = { currentScreen = SCREEN_SETTINGS }
                )
            }
            screen == SCREEN_SETTINGS -> {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { currentScreen = SCREEN_HOME }
                )
            }
            screen.startsWith(SCREEN_PREVIEW_PREFIX) -> {
                val viewModel: WallpaperPreviewViewModel = hiltViewModel()
                WallpaperPreviewScreen(
                    wallpaperId = screen.removePrefix(SCREEN_PREVIEW_PREFIX),
                    viewModel = viewModel,
                    onBackClick = { currentScreen = SCREEN_HOME }
                )
            }
        }
    }
}
