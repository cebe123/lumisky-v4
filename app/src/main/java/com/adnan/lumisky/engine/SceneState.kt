/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Sahnenin mantıksal durumu: zaman, atmosfer, parallax, battery, visibility, quality.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Sahnenin mantıksal durumu: zaman, atmosfer, parallax, battery, visibility, quality.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.definition.QualityTier
import java.time.Instant
import java.time.ZoneId
import kotlin.math.floor

class SceneState {
    var wallpaperId: String = ""
    var quality: QualityTier = QualityTier.BALANCED
    var isVisible: Boolean = false
    var batterySaver: Boolean = false
    var timeSeconds: Float = 0.0f
    
    var dayProgress: Float = 0.0f
    var fastForwardTimeRemaining: Float = 0f
    var isFastForwarding: Boolean = false
    var catchUpTimeRemaining: Float = 0f
        private set
    var isCatchUpAnimating: Boolean = false
        private set
    private var catchUpDurationSeconds: Float = 0f
    private var catchUpElapsedSeconds: Float = 0f
    private var catchUpStartProgress: Float = 0f
    private var catchUpTargetProgress: Float = 0f

    fun triggerFastForward() {
        fastForwardTimeRemaining = 8.0f
        isFastForwarding = true
    }

    fun triggerDayProgressCatchUp(
        startProgress: Float,
        targetProgress: Float,
        durationSeconds: Float = 1f
    ) {
        catchUpDurationSeconds = durationSeconds.coerceAtLeast(0.3f)
        catchUpElapsedSeconds = 0f
        catchUpTimeRemaining = catchUpDurationSeconds
        catchUpStartProgress = startProgress
        catchUpTargetProgress = targetProgress
        isCatchUpAnimating = true
        isFastForwarding = false
        fastForwardTimeRemaining = 0f
    }

    fun update(deltaTime: Float, timeZoneId: String = "") {
        timeSeconds += deltaTime

        if (isCatchUpAnimating) {
            catchUpElapsedSeconds += deltaTime.coerceAtLeast(0f)
            catchUpTimeRemaining = (catchUpDurationSeconds - catchUpElapsedSeconds).coerceAtLeast(0f)
            val t = (catchUpElapsedSeconds / catchUpDurationSeconds.coerceAtLeast(0.3f))
                .coerceIn(0f, 1f)
            dayProgress = wrapProgress(lerp(catchUpStartProgress, catchUpTargetProgress, smoothstep(t)))
            if (t >= 1f) {
                isCatchUpAnimating = false
            }
            return
        }
        
        if (isFastForwarding) {
            fastForwardTimeRemaining -= deltaTime
            if (fastForwardTimeRemaining <= 0f) {
                fastForwardTimeRemaining = 0f
                isFastForwarding = false
            } else {
                dayProgress = (8.0f - fastForwardTimeRemaining) / 8.0f
            }
        }
        
        if (!isFastForwarding) {
            dayProgress = currentDayProgress(timeZoneId)
        }
    }

    private fun currentDayProgress(timeZoneId: String): Float {
        val zone = runCatching {
            ZoneId.of(timeZoneId.takeIf { it.isNotBlank() } ?: ZoneId.systemDefault().id)
        }.getOrElse { ZoneId.systemDefault() }
        val time = Instant.ofEpochMilli(System.currentTimeMillis())
            .atZone(zone)
            .toLocalTime()
        return (time.toNanoOfDay().toDouble() / NANOS_PER_DAY.toDouble())
            .toFloat()
            .coerceIn(0f, 1f)
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + ((end - start) * t.coerceIn(0f, 1f))
    }

    private fun smoothstep(value: Float): Float {
        val t = value.coerceIn(0f, 1f)
        return t * t * (3f - (2f * t))
    }

    private fun wrapProgress(value: Float): Float {
        val wrapped = value - floor(value)
        return wrapped.coerceIn(0f, 1f)
    }

    private companion object {
        const val NANOS_PER_DAY = 24L * 60L * 60L * 1_000_000_000L
    }
}
