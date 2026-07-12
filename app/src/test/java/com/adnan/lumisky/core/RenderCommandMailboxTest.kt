package com.example.lumisky.core

import com.example.lumisky.definition.QualityTier
import org.junit.Assert.assertEquals
import org.junit.Test

class RenderCommandMailboxTest {
    @Test
    fun preservesFifoCommandsAndCoalescesLatestInputs() {
        val mailbox = RenderCommandMailbox()
        val drained = mutableListOf<RenderCommand>()
        mailbox.offer(RenderCommand.AttachSurface)
        mailbox.offer(RenderCommand.SetParallax(0.1f, 0.2f))
        mailbox.offer(RenderCommand.SetParallax(0.3f, 0.4f))
        mailbox.offer(RenderCommand.DetachSurface)

        mailbox.drainTo(drained)

        assertEquals(
            listOf(
                RenderCommand.AttachSurface,
                RenderCommand.DetachSurface,
                RenderCommand.SetParallax(0.3f, 0.4f)
            ),
            drained
        )
    }

    @Test
    fun coalescesLatestRuntimeAndPowerPolicies() {
        val mailbox = RenderCommandMailbox()
        val drained = mutableListOf<RenderCommand>()
        mailbox.offer(runtimePolicy(maxFps = 30))
        mailbox.offer(RenderCommand.SetPowerPolicy(batterySaver = false, thermalEmergency = false))
        mailbox.offer(runtimePolicy(maxFps = 15))
        mailbox.offer(RenderCommand.SetPowerPolicy(batterySaver = true, thermalEmergency = true))

        mailbox.drainTo(drained)

        assertEquals(
            listOf(
                runtimePolicy(maxFps = 15),
                RenderCommand.SetPowerPolicy(batterySaver = true, thermalEmergency = true)
            ),
            drained
        )
    }

    @Test
    fun highFrequencyInputsHaveBoundedBacklog() {
        val mailbox = RenderCommandMailbox()
        val drained = mutableListOf<RenderCommand>()

        repeat(10_000) { index ->
            mailbox.offer(RenderCommand.SetParallax(index.toFloat(), -index.toFloat()))
            mailbox.offer(RenderCommand.SetTouch(index.toFloat(), index.toFloat(), active = true))
        }

        mailbox.drainTo(drained)

        assertEquals(2, drained.size)
        assertEquals(RenderCommand.SetParallax(9_999f, -9_999f), drained[0])
        assertEquals(RenderCommand.SetTouch(9_999f, 9_999f, active = true), drained[1])
    }

    private fun runtimePolicy(maxFps: Int) = RenderCommand.SetRuntimePolicy(
        qualityTier = QualityTier.BALANCED,
        maxFps = maxFps,
        renderScale = 1f,
        postProcessEnabled = true,
        particleEffectsEnabled = true,
        videoPlaybackEnabled = true,
        sensorParallaxEnabled = true,
        telemetryEnabled = true
    )
}
