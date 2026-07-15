package com.example.lumisky.preview

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewShutdownContractTest {
    @Test
    fun composeDisposalNeverWaitsForTheGlThread() {
        val view = File("src/main/java/com/example/lumisky/ui/components/LumiskyWallpaperPreviewView.kt").readText()
        val thread = File("src/main/java/com/example/lumisky/preview/PreviewGlThread.kt").readText()

        assertTrue(view.contains("glThread.shutdownAsync()"))
        assertTrue(thread.contains("fun shutdownAsync()"))
        assertTrue(thread.contains("handler.postDelayed(::finishAsyncShutdown, ASYNC_SHUTDOWN_DELAY_MILLIS)"))
    }
}
