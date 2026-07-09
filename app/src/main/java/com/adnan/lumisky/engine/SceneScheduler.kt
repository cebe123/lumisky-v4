/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - LayerFramePolicy’ye göre update, cache refresh ve render kararlarını verir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: LayerFramePolicy’ye göre update, cache refresh ve render kararlarını verir.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.layers.LayerFrameMode
import com.adnan.lumisky.layers.RenderLayer

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneScheduler @Inject constructor() {
    private val lastUpdateTimes = mutableMapOf<String, Long>()

    fun shouldUpdate(
        layer: RenderLayer,
        frameTimeNanos: Long,
        batterySaver: Boolean = false,
        idle: Boolean = false
    ): Boolean {
        val policy = layer.framePolicy
        val mode = try { LayerFrameMode.valueOf(policy.mode) } catch (e: Throwable) { LayerFrameMode.MATCH_SCENE }
        
        return when (mode) {
            LayerFrameMode.STATIC -> false
            LayerFrameMode.MATCH_SCENE, LayerFrameMode.CONTINUOUS -> true
            LayerFrameMode.FIXED_FPS -> {
                val fps = when {
                    idle && policy.idleFps != null -> policy.idleFps
                    batterySaver && policy.degradeInBatterySaver && policy.batterySaverFps != null -> policy.batterySaverFps
                    else -> policy.fps
                } ?: 30
                val frameDurationNanos = 1_000_000_000L / fps
                val lastTime = lastUpdateTimes[layer.id] ?: 0L
                if (frameTimeNanos - lastTime >= frameDurationNanos) {
                    lastUpdateTimes[layer.id] = frameTimeNanos
                    true
                } else {
                    false
                }
            }
            else -> true
        }
    }

    fun shouldRefreshCache(
        layer: RenderLayer,
        frameTimeNanos: Long,
        batterySaver: Boolean = false,
        idle: Boolean = false
    ): Boolean {
        return shouldUpdate(layer, frameTimeNanos, batterySaver, idle)
    }
}
