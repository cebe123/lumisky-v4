package com.example.lumisky.core

import android.os.Handler
import android.os.HandlerThread
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RenderLifecycleInstrumentationTest {
    @Test
    fun visibleLiveWorkDoesNotWakeInvisiblePreviewWork() {
        val liveThread = HandlerThread("live-lifecycle-test").apply { start() }
        val previewThread = HandlerThread("preview-lifecycle-test").apply { start() }
        try {
            val completed = CountDownLatch(2)
            val liveFrames = AtomicInteger()
            val previewFrames = AtomicInteger()
            val previewSensors = AtomicInteger()
            val previewVideoFrames = AtomicInteger()
            val previewCallbacks = AtomicInteger()

            Handler(liveThread.looper).post {
                if (RenderLifecycleGate.canRender(visible = true, hasSurface = true)) {
                    liveFrames.incrementAndGet()
                }
                completed.countDown()
            }
            Handler(previewThread.looper).post {
                if (RenderLifecycleGate.canRender(visible = false, hasSurface = true)) {
                    previewFrames.incrementAndGet()
                }
                if (RenderLifecycleGate.canRunSensor(visible = false)) previewSensors.incrementAndGet()
                if (RenderLifecycleGate.canRunVideo(visible = false, playbackEnabled = true)) previewVideoFrames.incrementAndGet()
                if (RenderLifecycleGate.canPublishCallback(visible = false)) previewCallbacks.incrementAndGet()
                completed.countDown()
            }

            assertTrue(completed.await(2, TimeUnit.SECONDS))
            assertEquals(1, liveFrames.get())
            assertEquals(0, previewFrames.get())
            assertEquals(0, previewSensors.get())
            assertEquals(0, previewVideoFrames.get())
            assertEquals(0, previewCallbacks.get())
        } finally {
            liveThread.quitSafely()
            previewThread.quitSafely()
            liveThread.join(2_000L)
            previewThread.join(2_000L)
        }
    }
}
