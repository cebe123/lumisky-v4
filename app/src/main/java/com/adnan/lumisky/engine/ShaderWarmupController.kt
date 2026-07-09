/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Shader compile/binary load işlerini throttled şekilde hazırlar.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Shader compile/binary load işlerini throttled şekilde hazırlar.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.engine.gl.GlResourceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShaderWarmupController @Inject constructor(
    private val shaderSourceLoader: ShaderSourceLoader
) {
    fun warmup(gl: GlResourceManager) {
        try {
            gl.programs.get("common.gradient.v1", shaderSourceLoader)
            gl.programs.get("common.texture2d", shaderSourceLoader)
        } catch (e: Throwable) {
            // Log warmup exceptions silently
        }
    }
}
