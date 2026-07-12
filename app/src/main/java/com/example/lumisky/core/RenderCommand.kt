package com.example.lumisky.core

import com.example.lumisky.engine.DaylightOverride
import com.example.lumisky.definition.QualityTier

sealed interface RenderCommand {
    data object Stop : RenderCommand
    data object AttachSurface : RenderCommand
    data object DetachSurface : RenderCommand
    data class ResizeSurface(val width: Int, val height: Int) : RenderCommand
    data class SetVisibility(val visible: Boolean) : RenderCommand
    data class SetParallax(val x: Float, val y: Float) : RenderCommand
    data class SetTouch(val x: Float, val y: Float, val active: Boolean) : RenderCommand
    data class SetRuntimePolicy(
        val qualityTier: QualityTier,
        val maxFps: Int,
        val renderScale: Float,
        val postProcessEnabled: Boolean,
        val particleEffectsEnabled: Boolean,
        val videoPlaybackEnabled: Boolean,
        val sensorParallaxEnabled: Boolean,
        val telemetryEnabled: Boolean
    ) : RenderCommand
    data class SetPowerPolicy(val batterySaver: Boolean, val thermalEmergency: Boolean) : RenderCommand
    data class SetDaylight(val daylight: DaylightOverride) : RenderCommand
}
