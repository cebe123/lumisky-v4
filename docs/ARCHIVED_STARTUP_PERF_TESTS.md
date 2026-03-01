# Archived Startup Perf Tests

This file stores the removed startup benchmark and instrumentation code in one place.
It is intentionally outside active Gradle modules and source sets, so it has no runtime
or build impact unless manually copied back into the project.

## Removed Gradle Wiring

`settings.gradle.kts`

```kotlin
include(":benchmark")
```

`build.gradle.kts`

```kotlin
alias(libs.plugins.android.test) apply false
```

`gradle/libs.versions.toml`

```toml
android-test = { id = "com.android.test", version.ref = "agp" }
```

`app/build.gradle.kts`

```kotlin
testBuildType = "benchmark"

defaultConfig {
    resValue("string", "runtime_build_type", "debug")
}

buildTypes {
    release {
        resValue("string", "runtime_build_type", "release")
    }
    create("benchmark") {
        initWith(getByName("release"))
        signingConfig = signingConfigs.getByName("debug")
        isDebuggable = false
        resValue("string", "runtime_build_type", "benchmark")
        matchingFallbacks += listOf("release")
    }
}

buildFeatures {
    compose = true
    resValues = true
}
```

## Removed File: `app/src/main/java/com/example/lumisky/perf/StartupPerformanceMonitor.kt`

```kotlin
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
```

## Removed Runtime Integration Snippets

`MainActivity.kt`

```kotlin
StartupPerformanceMonitor.beginLaunch()
launchThemeMode = StartupPerformanceMonitor.traceSection("load_launch_theme") {
    appSettingsRepository.getAppThemeMode()
}
val launchLanguageTag = StartupPerformanceMonitor.traceSection("load_launch_language") {
    appSettingsRepository.getLanguageTag()
}
StartupPerformanceMonitor.traceSection("apply_language") {
    applyLanguage(launchLanguageTag)
}
configureDebugStartupGuardrails()

StartupFirstFrameReporter(
    onFirstFrame = { ensureHomeViewModelCreated() }
)

StartupFullyDrawnReporter(
    ready = true,
    onReady = {
        startupAnimationsEnabled = true
        if (getString(R.string.runtime_build_type) != "benchmark") {
            reportFullyDrawn()
        }
    }
)

private fun configureDebugStartupGuardrails() {
    val debuggable =
        (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    if (!debuggable) return

    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .penaltyLog()
            .build()
    )
    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectLeakedClosableObjects()
            .penaltyLog()
            .build()
    )
    Logger.i(TAG, "debug startup guardrails enabled")
}

private fun ensureHomeViewModelCreated(): HomeViewModel {
    homeViewModelOrNull()?.let { return it }
    return StartupPerformanceMonitor.traceSection("home_view_model_create") {
        val initialSettings = appSettingsRepository.snapshot()
        HomeViewModel(
            context = applicationContext,
            settingsRepository = appSettingsRepository,
            initialSettings = initialSettings
        ).also { created ->
            homeViewModelState.value = created
            StartupPerformanceMonitor.mark("home_view_model_ready")
        }
    }
}
```

`HomeViewModel.kt`

```kotlin
private val startupRefreshRunnable = Runnable {
    StartupPerformanceMonitor.traceSection("home_vm.refresh_location_state") {
        refreshLocationState()
    }
    StartupPerformanceMonitor.traceSection("home_vm.refresh_sun_times") {
        refreshSunTimes()
    }
}

init {
    StartupPerformanceMonitor.traceSection("home_vm.seed_initial_catalog") {
        seedInitialCatalog(daylight)
    }
    mainHandler.post(startupRefreshRunnable)
    schedulePeriodicSunTimesRefresh()
    schedulePeriodicBackupCityRefresh()
}
```

## Removed File: `benchmark/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.example.lumisky.benchmark"
    compileSdk {
        version = release(36)
    }
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("benchmark", "release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        animationsDisabled = true
    }
}

androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
        variantBuilder.enable = variantBuilder.buildType == "benchmark"
    }
}

dependencies {
    implementation("androidx.benchmark:benchmark-macro-junit4:1.3.4")
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test:runner:1.6.2")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
```

## Removed File: `benchmark/src/main/java/com/example/lumisky/benchmark/StartupBenchmark.kt`

```kotlin
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
```
