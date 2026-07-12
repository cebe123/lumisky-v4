package com.example.lumisky.engine

import com.example.lumisky.layers.LayerFrameMode
import com.example.lumisky.layers.RenderLayer

internal object SceneFramePacingPolicy {
    private const val NANOS_PER_SECOND = 1_000_000_000L
    private const val MINUTE_INTERVAL_NANOS = 60L * NANOS_PER_SECOND

    fun frameIntervalNanos(
        layers: List<RenderLayer>,
        maxFps: Int,
        batterySaver: Boolean,
        forceContinuous: Boolean = false
    ): Long? {
        val cappedMaxFps = maxFps.coerceIn(0, 120)
        if (cappedMaxFps == 0) return null
        if (forceContinuous) return NANOS_PER_SECOND / cappedMaxFps

        var shortestInterval: Long? = null
        layers.forEach { layer ->
            val policy = layer.framePolicy
            val interval = when (layer.frameMode) {
                LayerFrameMode.STATIC,
                LayerFrameMode.ON_DEMAND,
                LayerFrameMode.EVENT_BASED -> null
                LayerFrameMode.MINUTE_TICK -> MINUTE_INTERVAL_NANOS
                LayerFrameMode.ONE_FPS -> NANOS_PER_SECOND
                LayerFrameMode.FIXED_FPS -> {
                    val requestedFps = if (batterySaver && policy.degradeInBatterySaver) {
                        policy.batterySaverFps ?: policy.fps
                    } else {
                        policy.fps
                    }
                    NANOS_PER_SECOND / requestedFps.orDefault(cappedMaxFps).coerceIn(1, cappedMaxFps)
                }
                LayerFrameMode.MATCH_SCENE,
                LayerFrameMode.CONTINUOUS,
                LayerFrameMode.VIDEO_SYNC -> NANOS_PER_SECOND / cappedMaxFps
            }
            if (interval != null && (shortestInterval == null || interval < shortestInterval!!)) {
                shortestInterval = interval
            }
        }
        return shortestInterval
    }

    private fun Int?.orDefault(defaultValue: Int): Int = this ?: defaultValue
}
