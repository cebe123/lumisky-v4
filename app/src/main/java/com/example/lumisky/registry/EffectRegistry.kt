/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Effect/layer variant kayıtları için stateless registry.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Effect/layer variant kayıtları için stateless registry.
 */
package com.example.lumisky.registry

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EffectRegistry @Inject constructor() {
    private val effects = mutableMapOf<String, EffectConfig>()

    init {
        register("fog", EffectConfig(name = "fog", type = "FogLayer", defaultUniforms = mapOf("density" to 0.12f)))
        register("parallax", EffectConfig(name = "parallax", type = "ParallaxController", defaultUniforms = mapOf("strength" to 0.035f)))
        register("color_grade", EffectConfig(name = "color_grade", type = "ShaderLayer", defaultUniforms = mapOf("intensity" to 1.0f)))
        register("stars", EffectConfig(name = "stars", type = "StarsLayer", defaultUniforms = mapOf("opacity" to 0.8f)))
        register("rain", EffectConfig(name = "rain", type = "RainLayer", defaultUniforms = mapOf("speed" to 0.5f)))
    }

    fun register(name: String, config: EffectConfig) {
        effects[name] = config
    }

    fun get(name: String): EffectConfig? {
        return effects[name]
    }
}

data class EffectConfig(
    val name: String,
    val type: String,
    val defaultUniforms: Map<String, Float> = emptyMap()
)
