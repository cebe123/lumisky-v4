package com.example.lumisky.core

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveWallpaperResumeAnimationContractTest {
    @Test
    fun becomingVisibleStartsTheTwoSecondLiveCatchUpOnTheGlThread() {
        val engine = File("src/main/java/com/example/lumisky/core/LumiskyWallpaperEngine.kt").readText()
        val glThread = File("src/main/java/com/example/lumisky/core/WallpaperGlThread.kt").readText()

        assertTrue(engine.contains("if (visible && !isPreview)"))
        assertTrue(engine.contains("glThread?.triggerLiveCatchUp()"))
        assertTrue(glThread.contains("fun triggerLiveCatchUp()"))
        assertTrue(glThread.contains("renderer.triggerLiveCatchUp(daylightOverride)"))
    }

    @Test
    fun liveCatchUpIsPreparedBeforeTheFirstVisibleFrame() {
        val engine = File("src/main/java/com/example/lumisky/core/LumiskyWallpaperEngine.kt").readText()
        val visibilityBlock = engine
            .substringAfter("private fun applyEffectiveVisibility()")
            .substringBefore("fun onTemporalContextChanged()")

        assertTrue(
            visibilityBlock.indexOf("glThread?.triggerLiveCatchUp()") <
                visibilityBlock.indexOf("glThread?.setVisibility(visible)")
        )
    }
}
