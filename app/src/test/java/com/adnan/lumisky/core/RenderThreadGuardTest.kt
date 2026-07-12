package com.example.lumisky.core

import org.junit.Assert.assertThrows
import org.junit.Test

class RenderThreadGuardTest {
    @Test
    fun rejectsAccessFromAnotherThread() {
        val guard = RenderThreadGuard()
        guard.bindCurrentThread()
        var failure: Throwable? = null
        val thread = Thread {
            failure = assertThrows(IllegalStateException::class.java) {
                guard.checkCurrentThread()
            }
        }

        thread.start()
        thread.join()

        check(failure != null)
    }
}
