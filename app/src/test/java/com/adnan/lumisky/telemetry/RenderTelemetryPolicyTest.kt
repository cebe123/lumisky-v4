package com.example.lumisky.telemetry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderTelemetryPolicyTest {

    @Test
    fun sameEventIsReportedOnlyOncePerSession() {
        val limiter = TelemetryRateLimiter()
        val event = RenderTelemetryEvent.AssetMissing(
            wallpaperId = "pixel_forest",
            pathHash = "asset-hash",
            assetPack = null
        )

        assertTrue(limiter.shouldReport(event))
        assertFalse(limiter.shouldReport(event))
    }

    @Test
    fun differentFallbackKeysAreReportedIndependently() {
        val limiter = TelemetryRateLimiter()

        assertTrue(
            limiter.shouldReport(
                RenderTelemetryEvent.FallbackActivated(
                    wallpaperId = "pixel_forest",
                    fallbackType = "starter_gradient",
                    reason = "shader"
                )
            )
        )
        assertTrue(
            limiter.shouldReport(
                RenderTelemetryEvent.FallbackActivated(
                    wallpaperId = "sky",
                    fallbackType = "starter_gradient",
                    reason = "shader"
                )
            )
        )
    }
}
