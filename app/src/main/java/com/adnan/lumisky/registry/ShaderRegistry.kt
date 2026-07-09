/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - shaderRef -> shader source/path/family bilgisi. GL program ID tutmaz.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: shaderRef -> shader source/path/family bilgisi. GL program ID tutmaz.
 */
package com.adnan.lumisky.registry

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShaderRegistry @Inject constructor() {
    private val shaders = mutableMapOf<String, ShaderSourceInfo>()

    init {
        // Register default built-in shaders
        register("common.gradient.v1", "", "shaders/common/gradient.frag")
        register("common.video.oes", "", "shaders/common/video_oes.frag")
        register("common.texture2d", "", "shaders/common/texture2d.frag")
    }

    fun register(ref: String, vertexPath: String, fragmentPath: String) {
        shaders[ref] = ShaderSourceInfo(vertexPath, fragmentPath)
    }

    fun getSourceInfo(ref: String): ShaderSourceInfo? {
        if (!shaders.containsKey(ref) && (ref.contains("/") || ref.contains("."))) {
            register(ref, "", ref)
        }
        return shaders[ref]
    }
}

data class ShaderSourceInfo(
    val vertexPath: String,
    val fragmentPath: String
)
