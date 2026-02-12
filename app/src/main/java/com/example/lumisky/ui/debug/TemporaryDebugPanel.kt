package com.example.lumisky.ui.debug

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.core.perf.DebugMetricLine
import com.example.core.perf.TemporaryDebugMetrics
import kotlinx.coroutines.delay

@Composable
fun TemporaryDebugPanel(
	modifier: Modifier = Modifier,
	enabled: Boolean = isDebuggableApp()
) {
	if (!enabled) return

	var lines by remember { mutableStateOf<List<DebugMetricLine>>(emptyList()) }
	LaunchedEffect(Unit) {
		while (true) {
			lines = TemporaryDebugMetrics.snapshot()
			delay(1_000L)
		}
	}

	Column(
		modifier = modifier
			.background(Color(0xAA101418))
			.padding(horizontal = 10.dp, vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Text("TEMP DEBUG HUD", color = Color(0xFF9CE8B0))
		if (lines.isEmpty()) {
			Text("No metrics yet", color = Color.White)
		} else {
			lines.take(6).forEach { line ->
				Text("${line.tag}: ${line.summary}", color = Color.White)
			}
		}
	}
}

@Composable
private fun isDebuggableApp(): Boolean {
	val context = LocalContext.current
	return remember(context) {
		(context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
	}
}
