package com.example.lumisky.core

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class BoundedRenderThreadHandoff(
    private val timeoutMillis: Long
) {
    fun run(
        isOwnerThread: Boolean,
        post: (() -> Unit) -> Unit,
        action: () -> Unit
    ): Boolean {
        if (isOwnerThread) {
            action()
            return true
        }
        val latch = CountDownLatch(1)
        post {
            try {
                action()
            } finally {
                latch.countDown()
            }
        }
        return latch.await(timeoutMillis, TimeUnit.MILLISECONDS)
    }
}
