package com.example.lumisky.preview

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewAnimationStartContractTest {
    @Test
    fun firstVisiblePreviewFrameStartsAtAnimationOriginWithoutCarriedDelta() {
        val source = File("src/main/java/com/example/lumisky/preview/PreviewGlThread.kt").readText()
        assertTrue(
            source.contains(
                "previewAnimationPending && previewWarmupFrameRendered && renderer.activeScene != null && !renderer.hasPendingTextureWork"
            )
        )
        assertTrue(source.contains("previewWarmupFrameRendered = true"))
        assertTrue(
            source.contains(
                "renderer.activeScene != null && !renderer.hasPendingTextureWork && !previewAnimationPending"
            )
        )
        assertTrue(source.contains("renderer.shouldContinueRendering || previewAnimationPending"))
        val resetDelta = source.indexOf("renderContext.deltaTimeSeconds = 0f")
        val startAnimation = source.indexOf("renderer.triggerPreviewAnimation()")
        val renderFrame = source.indexOf("renderer.renderFrame(renderContext, inputSnapshot)")

        assertTrue(resetDelta >= 0)
        assertTrue(resetDelta < startAnimation)
        assertTrue(startAnimation < renderFrame)
    }
}
