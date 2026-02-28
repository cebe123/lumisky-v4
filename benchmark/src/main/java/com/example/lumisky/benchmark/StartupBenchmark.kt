package com.example.lumisky.benchmark

import androidx.benchmark.macro.FrameTimingMetric
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
	fun coldStartup() {
		benchmarkRule.measureRepeated(
			packageName = TARGET_PACKAGE,
			metrics = listOf(
				StartupTimingMetric(),
				FrameTimingMetric()
			),
			iterations = 5,
			startupMode = StartupMode.COLD,
			setupBlock = {
				pressHome()
			}
		) {
			startActivityAndWait()
		}
	}

	@Test
	fun warmStartup() {
		benchmarkRule.measureRepeated(
			packageName = TARGET_PACKAGE,
			metrics = listOf(
				StartupTimingMetric(),
				FrameTimingMetric()
			),
			iterations = 8,
			startupMode = StartupMode.WARM,
			setupBlock = {
				startActivityAndWait()
				device.waitForIdle()
				pressHome()
				device.waitForIdle()
			}
		) {
			startActivityAndWait()
		}
	}

	private companion object {
		private const val TARGET_PACKAGE = "com.example.lumisky"
	}
}
