package com.example.engine.manifest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WallpaperManifest(
	val id: String,
	val title: String,
	val category: String,
	val variants: List<WallpaperVariant> = emptyList()
)

@Serializable
data class WallpaperVariant(
	val id: String,
	val name: String,
	val thumbnail: String? = null,
	val shader: ShaderConfig = ShaderConfig(),
	val assets: WallpaperAssets = WallpaperAssets(),
	val effects: EffectsConfig = EffectsConfig(),
	val scene: SceneConfig = SceneConfig(),
	val render: RenderConfig = RenderConfig()
)

@Serializable
data class ShaderConfig(
	val mode: ShaderMode = ShaderMode.SHARED,
	val shaderId: String? = null,
	val vertex: String? = null,
	val fragment: String? = null,
	val includes: List<String> = emptyList(),
	val fallbackShaderId: String? = null
)

@Serializable
enum class ShaderMode {
	@SerialName("shared")
	SHARED,

	@SerialName("custom")
	CUSTOM
}

@Serializable
data class SelectableAssetOption(
	val id: String,
	val name: String,
	val file: String,
	val default: Boolean = false
)

@Serializable
data class WallpaperAssets(
	val background: String? = null,
	val foreground: String? = null,
	val stars: String? = null,
	val sunOptions: List<SelectableAssetOption> = emptyList(),
	val moonOptions: List<SelectableAssetOption> = emptyList(),
	val cloudOptions: List<SelectableAssetOption> = emptyList()
)

@Serializable
data class EffectOption(
	val id: String,
	val name: String,
	val shaderInclude: String? = null,
	val enabledByDefault: Boolean = false,
	val userSelectable: Boolean = true,
	val params: Map<String, Float> = emptyMap()
)

@Serializable
data class EffectsConfig(
	val available: List<EffectOption> = emptyList()
)

@Serializable
data class SceneConfig(
	val horizonY: Float = 0.6f,
	val sunPathType: String = "arc",
	val moonPathType: String = "arc",
	val sunSize: Float = 0.12f,
	val moonSize: Float = 0.1f
)

@Serializable
data class RenderConfig(
	val mode: RenderMode = RenderMode.MINUTE_TICK,
	val previewFps: Int = 60,
	val liveWallpaperFps: Int = 1,
	val continuousLiveWallpaperFps: Int = 15,
	val previewDayCycleSeconds: Int = 8,
	val pauseWhenNotVisible: Boolean = true,
	val renderImmediatelyOnVisible: Boolean = true,
	val batterySaverFps: Int = 5,
	val lowPowerModeSupported: Boolean = true,
	val allowUserFpsOverride: Boolean = false,
	val minUserFps: Int = 1,
	val maxUserFps: Int = 30,
	val quality: String = "medium"
)

@Serializable
enum class RenderMode {
	STATIC_ON_CHANGE,
	MINUTE_TICK,
	CONTINUOUS
}

enum class RendererRuntimeMode {
	APP_PREVIEW,
	LIVE_WALLPAPER
}

@Serializable
data class UserVariantSelection(
	val variantId: String,
	val selectedSunId: String? = null,
	val selectedMoonId: String? = null,
	val selectedCloudId: String? = null,
	val enabledEffectIds: Set<String> = emptySet(),
	val effectParams: Map<String, Map<String, Float>> = emptyMap(),
	val userLiveWallpaperFps: Int? = null
)
