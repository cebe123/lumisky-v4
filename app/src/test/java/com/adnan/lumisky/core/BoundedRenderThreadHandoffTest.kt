package com.example.lumisky.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoundedRenderThreadHandoffTest {
    @Test
    fun ownerThreadRunsImmediately() {
        var executed = false

        val completed = BoundedRenderThreadHandoff(timeoutMillis = 10L).run(
            isOwnerThread = true,
            post = { error("owner path must not post") }
        ) {
            executed = true
        }

        assertTrue(completed)
        assertTrue(executed)
    }

    @Test
    fun missingOwnerResponseTimesOut() {
        val completed = BoundedRenderThreadHandoff(timeoutMillis = 10L).run(
            isOwnerThread = false,
            post = { }
        ) { }

        assertFalse(completed)
    }
}
