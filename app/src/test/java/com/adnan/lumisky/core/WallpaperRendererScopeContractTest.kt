package com.example.lumisky.core

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class WallpaperRendererScopeContractTest {
    @Test
    fun liveWallpaperRendererIsNotSingletonScoped() {
        val source = File("src/main/java/com/adnan/lumisky/engine/LumiskyRenderer.kt").readText()

        assertFalse(source.contains("@Singleton"))
        assertFalse(source.contains("import javax.inject.Singleton"))
    }

    @Test
    fun liveWallpaperEngineDoesNotTriggerPreviewFastForward() {
        val source = File("src/main/java/com/adnan/lumisky/core/LumiskyWallpaperEngine.kt").readText()

        assertFalse(source.contains("triggerPreviewAnimation()"))
    }
}
