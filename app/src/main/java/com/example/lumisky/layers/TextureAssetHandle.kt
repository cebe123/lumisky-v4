package com.example.lumisky.layers

import com.example.lumisky.definition.QualityTier
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.engine.gl.GlResourceManager
import com.example.lumisky.engine.gl.GlTexture

internal class TextureAssetHandle private constructor(
    val path: String
) {
    fun preload(gl: GlResourceManager) {
        gl.textures.preload(paths, QualityTier.BALANCED)
    }

    fun resolve(frame: MutableRenderFrameState): GlTexture =
        frame.gl.textures.get(path, frame.quality)

    companion object {
        fun optional(path: String?): TextureAssetHandle? =
            path?.takeIf { it.isNotBlank() }?.let(::TextureAssetHandle)

        fun required(path: String): TextureAssetHandle = TextureAssetHandle(path)
    }

    private val paths = listOf(path)
}
