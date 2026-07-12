package com.example.lumisky.core

/** Keeps EGL and GL access confined to the owning render thread. */
class RenderThreadGuard {
    private var owner: Thread? = null

    fun bindCurrentThread() {
        check(owner == null) { "Render thread is already bound" }
        owner = Thread.currentThread()
    }

    fun checkCurrentThread() {
        check(owner === Thread.currentThread()) { "GL/EGL access must run on the render thread" }
    }
}
