package com.example.lumisky.engine.gl

internal class EglLifecycleState {
    var hasContext = false
        private set
    var hasSurface = false
        private set

    fun onContextCreated() {
        hasContext = true
    }

    fun onSurfaceCreated() {
        check(hasContext) { "EGL surface requires a context" }
        hasSurface = true
    }

    fun onSurfaceDestroyed() {
        hasSurface = false
    }

    fun onContextLost() {
        hasSurface = false
        hasContext = false
    }
}
