package com.example.engine.shader

import android.opengl.GLES20

internal data class ResolvedPreviewTextures(
	val background: Int,
	val sun: Int,
	val moon: Int,
	val flare: Int,
	val texture1: Int,
	val texture2: Int
)

internal class PreviewTextureBinder {

	fun resolve(
		swapForegroundPair: Boolean,
		backgroundTextureHandle: Int,
		sunTextureHandle: Int,
		moonTextureHandle: Int,
		flareTextureHandle: Int,
		fallbackSolidTextureHandle: Int,
		fallbackTransparentTextureHandle: Int
	): ResolvedPreviewTextures {
		val background = textureOrFallback(backgroundTextureHandle, fallbackTransparentTextureHandle)
		val sunTexture = textureOrFallback(sunTextureHandle, fallbackSolidTextureHandle)
		val moonTexture = textureOrFallback(moonTextureHandle, fallbackSolidTextureHandle)
		val flareTexture = textureOrFallback(flareTextureHandle, fallbackTransparentTextureHandle)
		val texture1 = if (swapForegroundPair) background else flareTexture
		val texture2 = if (swapForegroundPair) {
			textureOrFallback(flareTextureHandle, background)
		} else {
			background
		}
		return ResolvedPreviewTextures(
			background = background,
			sun = sunTexture,
			moon = moonTexture,
			flare = flareTexture,
			texture1 = texture1,
			texture2 = texture2
		)
	}

	fun bindTextureUnit(
		uniformHandle: Int,
		textureUnit: Int,
		textureHandle: Int
	) {
		if (uniformHandle < 0 || textureHandle == 0) return
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureUnit)
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
		GLES20.glUniform1i(uniformHandle, textureUnit)
	}

	private fun textureOrFallback(
		handle: Int,
		fallback: Int
	): Int {
		return if (handle != 0) handle else fallback
	}
}
