package com.example.lumisky.engine.gl

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ShaderProgramFallbackPolicyTest {
    @Test
    fun keepsPrimaryProgramWhenItLinked() {
        assertEquals(7, ShaderProgramFallbackPolicy.requireUsable(primaryProgramId = 7, fallbackProgramId = 9))
    }

    @Test
    fun usesSafeFallbackWhenPrimaryFailed() {
        assertEquals(9, ShaderProgramFallbackPolicy.requireUsable(primaryProgramId = 0, fallbackProgramId = 9))
    }

    @Test
    fun rejectsInvalidProgramWhenFallbackAlsoFailed() {
        try {
            ShaderProgramFallbackPolicy.requireUsable(primaryProgramId = 0)
            fail("Expected an invalid shader program to be rejected")
        } catch (_: IllegalStateException) {
        }
    }
}
