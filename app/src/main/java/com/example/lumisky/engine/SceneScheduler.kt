/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - LayerFramePolicy’ye göre update, cache refresh ve render kararlarını verir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: LayerFramePolicy’ye göre update, cache refresh ve render kararlarını verir.
 */
package com.example.lumisky.engine

import com.example.lumisky.layers.LayerFrameMode
import com.example.lumisky.layers.RenderLayer

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneScheduler @Inject constructor() {
    private val lastUpdateTimes = mutableMapOf<String, Long>()
    private val lastCacheRefreshTimes = mutableMapOf<String, Long>()

    fun shouldUpdate(
        layer: RenderLayer,
        frameTimeNanos: Long,
        batterySaver: Boolean = false,
        idle: Boolean = false
    ): Boolean {
        return shouldRun(layer, frameTimeNanos, batterySaver, idle, lastUpdateTimes)
    }

    fun shouldRefreshCache(
        layer: RenderLayer,
        frameTimeNanos: Long,
        batterySaver: Boolean = false,
        idle: Boolean = false
    ): Boolean {
        return shouldRun(layer, frameTimeNanos, batterySaver, idle, lastCacheRefreshTimes)
    }

    private fun shouldRun(
        layer: RenderLayer,
        frameTimeNanos: Long,
        batterySaver: Boolean,
        idle: Boolean,
        lastRunTimes: MutableMap<String, Long>
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
                val frameDurationNanos = 1_000_000_000L / fps.coerceAtLeast(1)
                val lastTime = lastRunTimes[layer.id] ?: 0L
                if (frameTimeNanos - lastTime >= frameDurationNanos) {
                    lastRunTimes[layer.id] = frameTimeNanos
                    true
                } else {
                    false
                }
            }
            else -> true
        }
    }

}
