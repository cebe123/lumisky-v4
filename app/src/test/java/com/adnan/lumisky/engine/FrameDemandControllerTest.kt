package com.example.lumisky.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FrameDemandControllerTest {
    @Test
    fun dirtyReasonRequestsOneFrameUntilPresented() {
        val controller = FrameDemandController()
        controller.request(FrameDemandReason.PARALLAX_CHANGED)
        assertTrue(controller.hasDemand(0L))
        controller.onFramePresented()
        assertFalse(controller.hasDemand(0L))
    }

    @Test
    fun aNewReasonAfterPresentationRequestsAnotherFrame() {
        val controller = FrameDemandController()
        controller.request(FrameDemandReason.INITIAL_FRAME)
        controller.onFramePresented()

        controller.request(FrameDemandReason.SCENE_SWITCH)

        assertTrue(controller.hasDemand(0L))
    }
}
