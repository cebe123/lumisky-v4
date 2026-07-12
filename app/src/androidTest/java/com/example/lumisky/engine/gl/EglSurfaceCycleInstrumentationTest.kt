package com.example.lumisky.engine.gl

import android.opengl.GLES30
import android.os.Handler
import android.os.HandlerThread
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EglSurfaceCycleInstrumentationTest {
    @Test
    fun oneHundredSurfaceCyclesKeepContextValidAndReleaseCleanly() {
        val renderThread = HandlerThread("egl-surface-cycle-test").apply { start() }
        val completed = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>(null)
        try {
            Handler(renderThread.looper).post {
                val egl = EglManager()
                try {
                    egl.initialize()
                    repeat(100) {
                        egl.createOffscreenSurface(width = 4, height = 4)
                        egl.makeCurrent()
                        GLES30.glClearColor(0f, 0f, 0f, 1f)
                        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
                        assertEquals(GLES30.GL_NO_ERROR, GLES30.glGetError())
                        egl.destroySurface()
                        assertTrue(egl.hasContext)
                        assertFalse(egl.hasSurface)
                    }
                    egl.release()
                    assertFalse(egl.hasContext)
                    assertFalse(egl.hasSurface)
                } catch (throwable: Throwable) {
                    failure.set(throwable)
                    runCatching { egl.release() }
                } finally {
                    completed.countDown()
                }
            }

            assertTrue(completed.await(20, TimeUnit.SECONDS))
            failure.get()?.let { throw it }
        } finally {
            renderThread.quitSafely()
            renderThread.join(2_000L)
        }
    }
}
