package com.example.lumisky.data

import com.example.lumisky.definition.QualityTier
import com.example.lumisky.engine.RenderFeatureFlags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeSettingsPolicyTest {

    @Test
    fun batteryModeForcesLowQualityAndConservativeFps() {
        val policy = RuntimeSettingsPolicy.resolve(
            qualityTier = "HIGH",
            performanceMode = SettingsRepository.PERFORMANCE_MODE_BATTERY,
            highRefreshEnabled = true
        )

        assertEquals(QualityTier.LOW, policy.qualityTier)
        assertEquals(15, policy.maxFps)
        assertEquals(0.5f, policy.renderScale, 0.001f)
        assertFalse(policy.postProcessEnabled)
    }

    @Test
    fun smoothModeAllowsHighRefreshWhenEnabled() {
        val policy = RuntimeSettingsPolicy.resolve(
            qualityTier = "BALANCED",
            performanceMode = SettingsRepository.PERFORMANCE_MODE_SMOOTH,
            highRefreshEnabled = true,
            sceneMaxFps = 60
        )

        assertEquals(QualityTier.HIGH, policy.qualityTier)
        assertEquals(60, policy.maxFps)
    }

    @Test
    fun autoModeHonorsExplicitQualityButCapsFpsWhenHighRefreshDisabled() {
        val policy = RuntimeSettingsPolicy.resolve(
            qualityTier = "HIGH",
            performanceMode = SettingsRepository.PERFORMANCE_MODE_AUTO,
            highRefreshEnabled = false
        )

        assertEquals(QualityTier.HIGH, policy.qualityTier)
        assertEquals(30, policy.maxFps)
    }

    @Test
    fun thermalSevereForcesEmergencyDegradation() {
        val policy = RuntimeSettingsPolicy.resolve(
            qualityTier = "ULTRA",
            performanceMode = SettingsRepository.PERFORMANCE_MODE_SMOOTH,
            highRefreshEnabled = true,
            thermalStatus = RuntimeSettingsPolicy.THERMAL_STATUS_SEVERE
        )

        assertEquals(QualityTier.LOW, policy.qualityTier)
        assertEquals(15, policy.maxFps)
        assertEquals(0.35f, policy.renderScale, 0.001f)
        assertTrue(policy.thermalEmergency)
        assertFalse(policy.postProcessEnabled)
        assertFalse(policy.particleEffectsEnabled)
        assertFalse(policy.videoPlaybackEnabled)
    }

    @Test
    fun ambientModeStopsContinuousRenderingAndSensors() {
        val policy = RuntimeSettingsPolicy.resolve(
            qualityTier = "HIGH",
            performanceMode = SettingsRepository.PERFORMANCE_MODE_AUTO,
            highRefreshEnabled = true,
            ambientMode = true
        )

        assertEquals(QualityTier.LOW, policy.qualityTier)
        assertEquals(0, policy.maxFps)
        assertFalse(policy.sensorParallaxEnabled)
        assertFalse(policy.videoPlaybackEnabled)
        assertFalse(policy.postProcessEnabled)
    }

    @Test
    fun featureFlagsDisableRiskyRuntimeCapabilities() {
        val policy = RuntimeSettingsPolicy.resolve(
            qualityTier = "HIGH",
            performanceMode = SettingsRepository.PERFORMANCE_MODE_AUTO,
            highRefreshEnabled = true,
            featureFlags = RenderFeatureFlags(
                shaderBinaryCacheEnabled = true,
                videoOesEnabled = false,
                postProcessEnabled = false,
                fboCacheEnabled = true,
                astcEnabled = false,
                sensorParallaxEnabled = false,
                bitmapPoolEnabled = true,
                renderTelemetryEnabled = false
            )
        )

        assertFalse(policy.videoPlaybackEnabled)
        assertFalse(policy.postProcessEnabled)
        assertFalse(policy.sensorParallaxEnabled)
        assertFalse(policy.telemetryEnabled)
    }
}
