package com.example.lumisky.ui.preview

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lumisky.engine.RuntimeProfile
import com.example.lumisky.ui.components.LumiskyWallpaperPreviewView
import com.example.lumisky.ui.wallpaper.LiveWallpaperSetLauncher
import kotlinx.coroutines.launch

@Composable
fun WallpaperPreviewScreen(
    wallpaperId: String,
    viewModel: WallpaperPreviewViewModel,
    onBackClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)
    val definition by viewModel.definition.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(wallpaperId) { viewModel.loadWallpaper(wallpaperId) }

    Box(Modifier.fillMaxSize()) {
        LumiskyWallpaperPreviewView(
            wallpaperId = wallpaperId,
            modifier = Modifier.fillMaxSize(),
            playPlayback = true,
            runtimeProfile = RuntimeProfile.fullscreenPreview()
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))))
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = definition?.name ?: wallpaperId.replace('_', ' ').replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    scope.launch {
                        viewModel.setWallpaperNow(wallpaperId)
                        if (!LiveWallpaperSetLauncher.open(context)) {
                            Toast.makeText(context, "Live wallpaper picker unavailable", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Set as Live Wallpaper")
            }
        }
    }
}
