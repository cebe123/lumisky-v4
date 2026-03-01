package com.example.lumisky.perf

import android.os.SystemClock
import android.os.Trace
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import com.example.core.Logger

object StartupPerformanceMonitor {
	private const val TAG = "StartupPerf"

	@Volatile
	private var launchStartUptimeMs: Long = 0L
	@Volatile
	private var firstFrameLogged: Boolean = false
	@Volatile
	private var fullyDrawnLogged: Boolean = false

	fun beginLaunch() {
		launchStartUptimeMs = SystemClock.uptimeMillis()
		firstFrameLogged = false
		fullyDrawnLogged = false
		Logger.i(TAG, "launch_started")
	}

	fun <T> traceSection(
		name: String,
		block: () -> T
	): T {
		val startMs = SystemClock.uptimeMillis()
		beginTraceSection(name)
		return try {
			block()
		} finally {
			endTraceSection(name, startMs)
		}
	}

	fun mark(name: String) {
		Logger.i(TAG, "$name sinceLaunch=${sinceLaunchLabel()}")
	}

	fun markFirstFrame() {
		if (firstFrameLogged) return
		firstFrameLogged = true
		Logger.i(TAG, "first_frame sinceLaunch=${sinceLaunchLabel()}")
	}

	fun markFullyDrawn() {
		if (fullyDrawnLogged) return
		fullyDrawnLogged = true
		Logger.i(TAG, "fully_drawn sinceLaunch=${sinceLaunchLabel()}")
	}

	private fun sectionLabel(name: String): String {
		return "Lumisky:$name".take(MAX_TRACE_SECTION_LENGTH)
	}

	private fun sinceLaunchLabel(): String {
		if (launchStartUptimeMs <= 0L) return "n/a"
		return "${(SystemClock.uptimeMillis() - launchStartUptimeMs).coerceAtLeast(0L)}ms"
	}

	internal fun beginTraceSection(name: String) {
		Trace.beginSection(sectionLabel(name))
	}

	internal fun endTraceSection(
		name: String,
		startMs: Long
	) {
		Trace.endSection()
		val durationMs = (SystemClock.uptimeMillis() - startMs).coerceAtLeast(0L)
		Logger.d(TAG, "$name duration=${durationMs}ms sinceLaunch=${sinceLaunchLabel()}")
	}

	private const val MAX_TRACE_SECTION_LENGTH = 127
}

private class StartupCompositionTraceGate(
	var armed: Boolean = true
)

@Composable
fun StartupTraceComposableOnce(
	name: String,
	content: @Composable () -> Unit
) {
	val gate = remember(name) { StartupCompositionTraceGate() }
	if (!gate.armed) {
		content()
		return
	}

	val startMs = SystemClock.uptimeMillis()
	StartupPerformanceMonitor.beginTraceSection(name)
	content()
	gate.armed = false
	StartupPerformanceMonitor.endTraceSection(name, startMs)
}

@Composable
fun StartupFirstFrameReporter(
	onFirstFrame: (() -> Unit)? = null
) {
	LaunchedEffect(Unit) {
		withFrameNanos { }
		StartupPerformanceMonitor.markFirstFrame()
		onFirstFrame?.invoke()
	}
}

@Composable
fun StartupFullyDrawnReporter(
	ready: Boolean,
	onReady: () -> Unit
) {
	LaunchedEffect(ready) {
		if (!ready) return@LaunchedEffect
		withFrameNanos { }
		onReady()
		StartupPerformanceMonitor.markFullyDrawn()
	}
}
