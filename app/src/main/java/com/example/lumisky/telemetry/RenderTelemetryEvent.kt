/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Telemetry katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Telemetry katmanı bileşeni.
 */
package com.example.lumisky.telemetry

import com.example.lumisky.definition.QualityTier

sealed interface RenderTelemetryEvent {
    data class ShaderCompileFailed(
        val shaderRef: String,
        val glRenderer: String,
        val logHash: String
    ) : RenderTelemetryEvent

    data class AssetMissing(
        val wallpaperId: String,
        val pathHash: String,
        val assetPack: String?
    ) : RenderTelemetryEvent

    data class FallbackActivated(
        val wallpaperId: String,
        val fallbackType: String,
        val reason: String
    ) : RenderTelemetryEvent

    data class ContextRestoreFailed(
        val wallpaperId: String,
        val glRenderer: String
    ) : RenderTelemetryEvent

    data class FrameBudgetExceeded(
        val wallpaperId: String,
        val p95FrameMs: Float,
        val qualityTier: QualityTier
    ) : RenderTelemetryEvent

    data class ThermalEmergencyDegrade(
        val wallpaperId: String,
        val thermalStatus: Int,
        val appliedSceneMaxFps: Int
    ) : RenderTelemetryEvent
}

fun RenderTelemetryEvent.dedupeKey(): String {
    return when (this) {
        is RenderTelemetryEvent.ShaderCompileFailed -> "shader:$shaderRef:$glRenderer:$logHash"
        is RenderTelemetryEvent.AssetMissing -> "asset:$wallpaperId:$pathHash:${assetPack.orEmpty()}"
        is RenderTelemetryEvent.FallbackActivated -> "fallback:$wallpaperId:$fallbackType:$reason"
        is RenderTelemetryEvent.ContextRestoreFailed -> "context:$wallpaperId:$glRenderer"
        is RenderTelemetryEvent.FrameBudgetExceeded -> "frame:$wallpaperId:$qualityTier"
        is RenderTelemetryEvent.ThermalEmergencyDegrade -> "thermal:$wallpaperId:$thermalStatus:$appliedSceneMaxFps"
    }
}
