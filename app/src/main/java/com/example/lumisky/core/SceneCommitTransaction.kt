package com.example.lumisky.core

/** Holds a candidate scene until its first frame has reached the surface. */
internal class SceneCommitTransaction<T : Any> {
    private var pending: T? = null

    fun stage(candidate: T) {
        pending = candidate
    }

    fun takeAfterSwap(succeeded: Boolean): T? {
        if (!succeeded) return null
        return pending.also { pending = null }
    }
}
