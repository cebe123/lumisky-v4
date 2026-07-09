/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Aktif EGL context içinde lazy shader program cache. Singleton GL handle tutmaz.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Aktif EGL context içinde lazy shader program cache. Singleton GL handle tutmaz.
 */
package com.adnan.lumisky.engine.gl

import android.util.Log
import com.adnan.lumisky.assets.ShaderSourceLoader

class ShaderProgramPool(private val manager: GlResourceManager) {
    private val pool = mutableMapOf<String, GlProgram>()

    fun get(shaderRef: String, sourceLoader: ShaderSourceLoader): GlProgram {
        return pool.getOrPut(shaderRef) {
            val sourceInfo = manager.shaderRegistry.getSourceInfo(shaderRef)
                ?: throw IllegalArgumentException("Unknown shader reference: $shaderRef")
            
            val vertSource = sourceLoader.load(sourceInfo.vertexPath)
            val fragSource = sourceLoader.load(sourceInfo.fragmentPath)
            
            val binaryCacheKey = shaderRef.replace(".", "_")
            var programId = 0
            
            // Try loading cached binary
            val cachedBinary = manager.binaryCache.loadBinary(binaryCacheKey)
            if (cachedBinary != null) {
                // If program binary loading is supported, GLES30.glProgramBinary can be used.
                // For safety and portability in GLES 3.0, we fallback to standard compilation if it fails.
            }
            
            if (programId == 0) {
                programId = ShaderCompiler.compileAndLink(vertSource, fragSource)
            }
            
            if (programId == 0) {
                Log.e("ShaderProgramPool", "Failed to compile shader $shaderRef, falling back to default")
                val fallbackSourceInfo = manager.shaderRegistry.getSourceInfo("common.gradient.v1")
                    ?: throw RuntimeException("Fallback shader not found")
                val fallbackVert = sourceLoader.load(fallbackSourceInfo.vertexPath)
                val fallbackFrag = sourceLoader.load(fallbackSourceInfo.fragmentPath)
                programId = ShaderCompiler.compileAndLink(fallbackVert, fallbackFrag)
            }
            
            GlProgram(programId)
        }
    }

    fun clear() {
        pool.values.forEach { it.release() }
        pool.clear()
    }
}
