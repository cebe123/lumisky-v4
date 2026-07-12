/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - EGL display/context/surface lifecycle yöneticisi. GLThread dışında kullanılmaz.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: EGL display/context/surface lifecycle yöneticisi. GLThread dışında kullanılmaz.
 */
package com.example.lumisky.engine.gl

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.view.SurfaceHolder

enum class EglSwapResult { SUCCESS, SURFACE_LOST, CONTEXT_LOST }

class EglManager {
    private val lifecycle = EglLifecycleState()
    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var eglConfig: EGLConfig? = null

    val hasContext: Boolean
        get() = lifecycle.hasContext
    val hasSurface: Boolean
        get() = lifecycle.hasSurface

    fun initialize() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("eglGetDisplay failed")
        }

        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw RuntimeException("eglInitialize failed")
        }

        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT or 0x0040, // GL ES3 bit
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfigs, 0)) {
            throw RuntimeException("eglChooseConfig failed")
        }
        eglConfig = configs[0] ?: throw RuntimeException("No suitable EGLConfig found")

        val contextAttribs = intArrayOf(
            0x3098, 3, // EGL_CONTEXT_CLIENT_VERSION = 3
            EGL14.EGL_NONE
        )
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw RuntimeException("eglCreateContext failed")
        }
        lifecycle.onContextCreated()
    }

    fun createSurface(surfaceHolder: SurfaceHolder) {
        destroySurface()
        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceHolder.surface, surfaceAttribs, 0)
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw RuntimeException("eglCreateWindowSurface failed")
        }
        lifecycle.onSurfaceCreated()
    }

    fun createSurface(surface: android.view.Surface) {
        destroySurface()
        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0)
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw RuntimeException("eglCreateWindowSurface failed")
        }
        lifecycle.onSurfaceCreated()
    }

    fun createOffscreenSurface(width: Int, height: Int) {
        destroySurface()
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE
        )
        eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0)
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw RuntimeException("eglCreatePbufferSurface failed")
        }
        lifecycle.onSurfaceCreated()
    }

    fun makeCurrent() {
        check(hasContext && hasSurface) { "EGL current requires a context and surface" }
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw RuntimeException("eglMakeCurrent failed")
        }
    }

    fun makeUncurrent() {
        EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
    }

    fun swapBuffers(): EglSwapResult {
        if (!hasContext || !hasSurface) return EglSwapResult.SURFACE_LOST
        val result = EglSwapErrorPolicy.classify(
            success = EGL14.eglSwapBuffers(eglDisplay, eglSurface),
            errorCode = EGL14.eglGetError(),
            contextLostErrorCode = EGL14.EGL_CONTEXT_LOST
        )
        if (result == EglSwapResult.CONTEXT_LOST) {
            lifecycle.onContextLost()
        }
        return result
    }

    fun destroySurface() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            eglSurface = EGL14.EGL_NO_SURFACE
        }
        lifecycle.onSurfaceDestroyed()
    }

    fun release() {
        makeUncurrent()
        destroySurface()
        if (eglContext != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            eglContext = EGL14.EGL_NO_CONTEXT
        }
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglTerminate(eglDisplay)
            eglDisplay = EGL14.EGL_NO_DISPLAY
        }
        lifecycle.onContextLost()
    }
}
