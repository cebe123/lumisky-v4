/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Aktif scene için shader, texture, FBO, mesh kaynaklarını context scope içinde yönetir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Aktif scene için shader, texture, FBO, mesh kaynaklarını context scope içinde yönetir.
 */
package com.example.lumisky.engine.gl

import android.content.Context
import com.example.lumisky.registry.ShaderRegistry

class GlResourceManager(
    val context: Context,
    val shaderRegistry: ShaderRegistry,
    val releaseQueue: GlReleaseQueue = GlReleaseQueue()
) {
    val programs = ShaderProgramPool(this)
    val textures = TexturePool(this)
    val framebuffers = FramebufferPool(this)
    val meshes = MeshRegistry(this)
    val bitmapPool = BitmapPool()
    val binaryCache = ShaderBinaryCache(context)

    fun onContextLost() {
        programs.invalidate()
        textures.invalidate()
        framebuffers.invalidate()
        meshes.invalidate()
        bitmapPool.clear()
        releaseQueue.discard()
    }

    fun release() {
        onContextLost()
        textures.release()
        releaseQueue.drain()
    }
}
