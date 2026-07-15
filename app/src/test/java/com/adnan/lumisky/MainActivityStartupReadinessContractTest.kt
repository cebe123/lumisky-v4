package com.example.lumisky

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityStartupReadinessContractTest {
    @Test
    fun splashWaitsForCatalogWarmupAndTwoComposedFrames() {
        val source = File("src/main/java/com/example/lumisky/MainActivity.kt").readText()

        assertTrue(source.contains("splashScreen.setKeepOnScreenCondition { !startupUiReady.value }"))
        assertTrue(source.contains("warmCatalogForLaunch(applicationContext, wallpaperRepository)"))
        assertTrue(source.contains("withFrameNanos { }\n        withFrameNanos { }"))
        assertTrue(source.contains("markStartupUiReady()"))
    }
}
