/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - JSON wallpaper tanımının latest schema data modeli.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: JSON wallpaper tanımının latest schema data modeli.
 */
package com.example.lumisky.definition

import kotlinx.serialization.Serializable

@Serializable
enum class WallpaperSourceKind {
    LAYERED_IMAGE,
    HYBRID,
    VIDEO,
    PROCEDURAL
}

@Serializable
data class WallpaperDefinition(
    val schemaVersion: Int = 5,
    val id: String,
    val name: String,
    val category: String,
    val sourceKind: WallpaperSourceKind = WallpaperSourceKind.PROCEDURAL,
    val contentLicense: ContentLicenseInfo? = null,
    val assetPack: String? = null,
    val colorHints: ColorHintsDefinition? = null,
    val capabilities: CapabilitiesDefinition = CapabilitiesDefinition(),
    val displayPolicy: DisplayPolicyDefinition? = null,
    val ambientPolicy: AmbientPolicyDefinition? = null,
    val renderScheduler: RenderSchedulerDefinition? = null,
    val qualityPolicy: QualityPolicyDefinition? = null,
    val parallax: ParallaxDefinition? = null,
    val horizon: HorizonDefinition = HorizonDefinition(),
    val celestial: CelestialDefinition = CelestialDefinition(),
    val daylight: DaylightDefinition = DaylightDefinition(),
    val peakY: Float = 0.9f,
    val belowHorizonOffset: Float = 0.1f,
    val layers: List<LayerDefinition> = emptyList(),
    val events: List<EventDefinition> = emptyList(),
    val preview: PreviewDefinition = PreviewDefinition()
)

@Serializable
data class ContentLicenseInfo(
    val source: String,
    val license: String,
    val approvedForDistribution: Boolean
)

@Serializable
data class ColorHintsDefinition(
    val primary: String? = null,
    val secondary: String? = null,
    val tertiary: String? = null,
    val supportsDarkText: Boolean = false,
    val supportsDarkTheme: Boolean = true,
    val source: String = "metadata"
)

@Serializable
data class CapabilitiesDefinition(
    val parallax: Boolean = false,
    val touch: Boolean = false,
    val locationAware: Boolean = false,
    val supportsPreviewTimeSimulation: Boolean = true
)

@Serializable
data class DisplayPolicyDefinition(
    val foldableAware: Boolean = true,
    val baseAspectRatio: Float = 0.5625f,
    val fovPhone: Float = 45.0f,
    val fovExpanded: Float = 38.0f,
    val maxOffsetXPhone: Float = 0.035f,
    val maxOffsetXExpanded: Float = 0.022f,
    val maxOffsetY: Float = 0.018f
)

@Serializable
data class AmbientPolicyDefinition(
    val enabled: Boolean = true,
    val mode: String = "BLACK_CLEAR_OR_LOW_LUMINANCE_FRAME",
    val burnInProtectionPx: Int = 3,
    val refreshSeconds: Int = 60
)

@Serializable
data class RenderSchedulerDefinition(
    val mode: String = "LAYER_SCHEDULED",
    val sceneMaxFps: Int = 30,
    val batterySaverSceneMaxFps: Int = 15,
    val idleSceneMaxFps: Int = 8
)

@Serializable
data class QualityPolicyDefinition(
    val defaultTier: String = "BALANCED",
    val allowAdaptiveQuality: Boolean = true,
    val maxTextureSizeLow: Int = 1024,
    val maxTextureSizeBalanced: Int = 2048,
    val maxTextureSizeHigh: Int = 4096
)

@Serializable
data class ParallaxDefinition(
    val enabled: Boolean = true,
    val maxOffsetX: Float = 0.035f,
    val maxOffsetY: Float = 0.018f,
    val smoothing: Float = 0.12f
)

@Serializable
data class HorizonDefinition(
    val offset: Float = 0.2f
)

@Serializable
data class CelestialDefinition(
    val sunPathType: String = "VERTICAL",
    val moonPathType: String = "VERTICAL",
    val sunOrbit: CelestialOrbitDefinition? = null,
    val moonOrbit: CelestialOrbitDefinition? = null
)

@Serializable
data class CelestialOrbitDefinition(
    val pathType: String = "ARC",
    val startX: Float? = null,
    val endX: Float? = null,
    val peakY: Float? = null,
    val hiddenY: Float? = null,
    val curve: String = "LINEAR"
)

@Serializable
data class DaylightDefinition(
    val sunriseMinute: Int = 360,
    val sunsetMinute: Int = 1080,
    val solarNoonMinute: Int = 720,
    val timeZoneId: String? = null
)
