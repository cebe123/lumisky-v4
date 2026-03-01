package com.example.lumisky.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

	@get:Rule
	val benchmarkRule = MacrobenchmarkRule()

	@Test
	fun coldStartupNoCompilation() {
		measureStartup(
			startupMode = StartupMode.COLD,
			compilationMode = CompilationMode.None(),
			iterations = COLD_START_ITERATIONS,
			setupBlock = {
				pressHome()
			}
		)
	}

	@Test
	fun coldStartupBaselineProfile() {
		measureStartup(
			startupMode = StartupMode.COLD,
			compilationMode = CompilationMode.Partial(
				baselineProfileMode = BaselineProfileMode.Require
			),
			iterations = COLD_START_ITERATIONS,
			setupBlock = {
				pressHome()
			}
		)
	}

	@Test
	fun warmStartupNoCompilation() {
		measureStartup(
			startupMode = StartupMode.WARM,
			compilationMode = CompilationMode.None(),
			iterations = WARM_START_ITERATIONS,
			setupBlock = {
				prepareWarmStart()
			}
		)
	}

	@Test
	fun warmStartupBaselineProfile() {
		measureStartup(
			startupMode = StartupMode.WARM,
			compilationMode = CompilationMode.Partial(
				baselineProfileMode = BaselineProfileMode.Require
			),
			iterations = WARM_START_ITERATIONS,
			setupBlock = {
				prepareWarmStart()
			}
		)
	}

	private fun measureStartup(
		startupMode: StartupMode,
		compilationMode: CompilationMode,
		iterations: Int,
		setupBlock: MacrobenchmarkScope.() -> Unit
	) {
		benchmarkRule.measureRepeated(
			packageName = TARGET_PACKAGE,
			metrics = listOf(
				StartupTimingMetric(),
				FrameTimingMetric()
			),
			compilationMode = compilationMode,
			startupMode = startupMode,
			iterations = iterations,
			setupBlock = setupBlock
		) {
			if (startupMode == StartupMode.WARM) {
				launchTargetActivityWithShell()
			} else {
				startActivityAndWait()
			}
		}
	}

	private fun MacrobenchmarkScope.prepareWarmStart() {
		launchTargetActivityWithShell()
		pressHome()
		device.waitForIdle()
	}

	private fun MacrobenchmarkScope.launchTargetActivityWithShell() {
		device.executeShellCommand(
			"am start -W -n $TARGET_ACTIVITY"
		)
		device.waitForIdle()
	}

	private companion object {
		private const val TARGET_PACKAGE = "com.example.lumisky"
		private const val TARGET_ACTIVITY = "com.example.lumisky/com.example.lumisky.MainActivity"
		private const val COLD_START_ITERATIONS = 5
		private const val WARM_START_ITERATIONS = 8
	}
}
