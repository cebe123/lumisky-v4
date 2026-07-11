/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Fallback, shader fail, missing asset, GL init fail gibi non-fatal olayları rate-limited loglar.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Fallback, shader fail, missing asset, GL init fail gibi non-fatal olayları rate-limited loglar.
 */
package com.example.lumisky.telemetry

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RenderTelemetryLogger @Inject constructor() {
    private val rateLimiter = TelemetryRateLimiter()

    fun logFallback(event: RenderFallbackEvent) {
        log(toTelemetryEvent(event))
    }

    fun log(event: RenderTelemetryEvent) {
        if (rateLimiter.shouldReport(event)) {
            Log.w("RenderTelemetry", "Render event recorded: ${event.dedupeKey()}")
        }
    }

    fun clear() {
        rateLimiter.clear()
    }

    private fun toTelemetryEvent(event: RenderFallbackEvent): RenderTelemetryEvent {
        return when (event) {
            is RenderFallbackEvent.ShaderFallback -> RenderTelemetryEvent.ShaderCompileFailed(
                shaderRef = event.layerId,
                glRenderer = "unknown",
                logHash = event.error.hashCode().toString()
            )
            is RenderFallbackEvent.TextureFallback -> RenderTelemetryEvent.AssetMissing(
                wallpaperId = "unknown",
                pathHash = event.path.hashCode().toString(),
                assetPack = null
            )
            is RenderFallbackEvent.GlInitFallback -> RenderTelemetryEvent.ContextRestoreFailed(
                wallpaperId = "unknown",
                glRenderer = event.error.hashCode().toString()
            )
        }
    }
}
