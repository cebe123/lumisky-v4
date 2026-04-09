package com.example.engine.shader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLUtils
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderFrameState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

class PreviewSkyProgram {

	private val vertexBuffer: FloatBuffer =
		ByteBuffer.allocateDirect(FULLSCREEN_VERTICES.size * FLOAT_SIZE_BYTES)
			.order(ByteOrder.nativeOrder())
			.asFloatBuffer()
			.apply {
				put(FULLSCREEN_VERTICES)
				position(0)
			}

	private var programHandle: Int = 0
	private var positionHandle: Int = -1

	private var skyColorHandle: Int = -1
	private var sunHandle: Int = -1
	private var moonHandle: Int = -1
	private var sunAlphaHandle: Int = -1
	private var moonAlphaHandle: Int = -1

	private var uSunPosHandle: Int = -1
	private var uSunColorHandle: Int = -1
	private var uAspectRatioHandle: Int = -1
	private var uDrawSunHandle: Int = -1
	private var uMoonPosHandle: Int = -1
	private var uIsNightHandle: Int = -1
	private var uMinuteHandle: Int = -1
	private var uCloudOffsetHandle: Int = -1
	private var uCloudAlphaHandle: Int = -1
	private var uSunsetHandle: Int = -1
	private var uSunriseHandle: Int = -1
	private var uSolarNoonHandle: Int = -1
	private var uNightAmountHandle: Int = -1
	private var uHorizonYHandle: Int = -1
	private var uHasAtmosphereHandle: Int = -1
	private var uHasFlareHandle: Int = -1
	private var uHasStarsHandle: Int = -1
	private var uFlareIntensityHandle: Int = -1
	private var uResolutionHandle: Int = -1
	private var uTimeHandle: Int = -1
	private var uTimeOfDayHandle: Int = -1
	private var uTextureHandle: Int = -1
	private var uTexture1Handle: Int = -1
	private var uTexture2Handle: Int = -1
	private var uTouchPositionHandle: Int = -1
	private var uTouchTimeHandle: Int = -1
	private var uWindSpeedHandle: Int = -1
	private var uCityZoomHandle: Int = -1
	private var uCityVerticalOffsetHandle: Int = -1
	private var uCityHorizontalOffsetHandle: Int = -1

	private var uSunTextureHandle: Int = -1
	private var uMoonTextureHandle: Int = -1
	private var uForegroundTextureHandle: Int = -1
	private var uCloudTextureHandle: Int = -1

	private var viewportWidth: Int = 1
	private var viewportHeight: Int = 1
	private var fallbackSolidTextureHandle: Int = 0
	private var fallbackTransparentTextureHandle: Int = 0
	private var config: WallpaperConfig = WallpaperConfig.default()
	private var textureBytesLoader: ((String) -> ByteArray?)? = null
	private var qualityScale: Float = 1f
	private var backgroundTextureHandle: Int = 0
	private var sunTextureHandle: Int = 0
	private var moonTextureHandle: Int = 0
	private var flareTextureHandle: Int = 0
	private val legacyThemeAdapter = LegacyThemeAdapter()
	private val effectAdapter = PreviewEffectAdapter()
	private val textureBinder = PreviewTextureBinder()

	fun configure(
		value: WallpaperConfig,
		textureLoader: ((String) -> ByteArray?)? = null,
		renderQualityScale: Float = 1f
	) {
		config = value
		textureBytesLoader = textureLoader
		qualityScale = renderQualityScale.coerceIn(0.3f, 1f)
	}

	fun setRenderQuality(value: Float) {
		qualityScale = value.coerceIn(0.3f, 1f)
	}

	fun init(fragmentShaderOverride: String? = null) {
		if (programHandle != 0) return
		val fragmentShader = fragmentShaderOverride ?: BUILTIN_FRAGMENT_SHADER
		programHandle = buildProgram(VERTEX_SHADER, fragmentShader)
		if (programHandle == 0) {
			// Fallback to built-in shader if override fails.
			programHandle = buildProgram(VERTEX_SHADER, BUILTIN_FRAGMENT_SHADER)
		}
		if (programHandle == 0) return

		positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
		skyColorHandle = GLES20.glGetUniformLocation(programHandle, "uSkyColor")
		sunHandle = GLES20.glGetUniformLocation(programHandle, "uSun")
		moonHandle = GLES20.glGetUniformLocation(programHandle, "uMoon")
		sunAlphaHandle = GLES20.glGetUniformLocation(programHandle, "uSunAlpha")
		moonAlphaHandle = GLES20.glGetUniformLocation(programHandle, "uMoonAlpha")

		uSunPosHandle = GLES20.glGetUniformLocation(programHandle, "u_SunPos")
		uSunColorHandle = GLES20.glGetUniformLocation(programHandle, "u_SunColor")
		uAspectRatioHandle = GLES20.glGetUniformLocation(programHandle, "u_AspectRatio")
		uDrawSunHandle = GLES20.glGetUniformLocation(programHandle, "u_DrawSun")
		uMoonPosHandle = GLES20.glGetUniformLocation(programHandle, "u_MoonPos")
		uIsNightHandle = GLES20.glGetUniformLocation(programHandle, "u_IsNight")
		uMinuteHandle = GLES20.glGetUniformLocation(programHandle, "u_Minute")
		uCloudOffsetHandle = GLES20.glGetUniformLocation(programHandle, "u_CloudOffset")
		uCloudAlphaHandle = GLES20.glGetUniformLocation(programHandle, "u_CloudAlpha")
		uSunsetHandle = GLES20.glGetUniformLocation(programHandle, "u_Sunset")
		uSunriseHandle = GLES20.glGetUniformLocation(programHandle, "u_Sunrise")
		uSolarNoonHandle = GLES20.glGetUniformLocation(programHandle, "u_SolarNoon")
		uNightAmountHandle = GLES20.glGetUniformLocation(programHandle, "u_NightAmount")
		uHorizonYHandle = GLES20.glGetUniformLocation(programHandle, "u_HorizonY")
		uHasAtmosphereHandle = GLES20.glGetUniformLocation(programHandle, "u_HasAtmosphere")
		uHasFlareHandle = GLES20.glGetUniformLocation(programHandle, "u_HasFlare")
		uHasStarsHandle = GLES20.glGetUniformLocation(programHandle, "u_HasStars")
		uFlareIntensityHandle = GLES20.glGetUniformLocation(programHandle, "u_FlareIntensity")
		uResolutionHandle = GLES20.glGetUniformLocation(programHandle, "u_Resolution")
		uTimeHandle = GLES20.glGetUniformLocation(programHandle, "u_Time")
		uTimeOfDayHandle = GLES20.glGetUniformLocation(programHandle, "u_TimeOfDay")
		uTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture")
		uTexture1Handle = GLES20.glGetUniformLocation(programHandle, "u_Texture1")
		uTexture2Handle = GLES20.glGetUniformLocation(programHandle, "u_Texture2")
		uTouchPositionHandle = GLES20.glGetUniformLocation(programHandle, "u_TouchPosition")
		uTouchTimeHandle = GLES20.glGetUniformLocation(programHandle, "u_TouchTime")
		uWindSpeedHandle = GLES20.glGetUniformLocation(programHandle, "u_WindSpeed")
		uCityZoomHandle = GLES20.glGetUniformLocation(programHandle, "u_CityZoom")
		uCityVerticalOffsetHandle = GLES20.glGetUniformLocation(programHandle, "u_CityVerticalOffset")
		uCityHorizontalOffsetHandle = GLES20.glGetUniformLocation(programHandle, "u_CityHorizontalOffset")

		uSunTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_SunTexture")
		uMoonTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_MoonTexture")
		uForegroundTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_ForegroundTexture")
		uCloudTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_CloudTexture")
		loadConfiguredTextures()
	}

	fun setViewport(width: Int, height: Int) {
		viewportWidth = width.coerceAtLeast(1)
		viewportHeight = height.coerceAtLeast(1)
	}

	fun draw(state: RenderFrameState) {
		if (programHandle == 0) return
		ensureFallbackTextures()

		GLES20.glUseProgram(programHandle)
		vertexBuffer.position(0)
		GLES20.glEnableVertexAttribArray(positionHandle)
		GLES20.glVertexAttribPointer(
			positionHandle,
			2,
			GLES20.GL_FLOAT,
			false,
			0,
			vertexBuffer
		)

		val skyR = ((state.skyColor shr 16) and 0xFF) / 255f
		val skyG = ((state.skyColor shr 8) and 0xFF) / 255f
		val skyB = (state.skyColor and 0xFF) / 255f
		val sunAlpha = sin((state.dayProgress.coerceIn(0f, 1f)) * PI.toFloat()).coerceAtLeast(0f)
		val minute = (state.dayProgress * MINUTES_PER_DAY).coerceIn(0f, MINUTES_PER_DAY.toFloat())
		val aspect = viewportWidth.toFloat() / viewportHeight.toFloat()
		val legacyThemeState = legacyThemeAdapter.resolve(config = config, state = state)
		val effectState = effectAdapter.resolve(config = config, state = state)
		val shaderSunX = state.sun.x.coerceIn(0f, 1f)
		val shaderSunY = mapToLegacyShaderY(state.sun.y)
		val shaderMoonX = state.moon.x.coerceIn(0f, 1f)
		val shaderMoonY = mapToLegacyShaderY(state.moon.y)
		val shaderHorizonY = mapToLegacyShaderY(config.horizon.offset.coerceIn(0f, 1f))
		val drawSun = if (minute >= state.sunriseMinute && minute <= state.sunsetMinute) 1f else 0f
		val windSpeed = 1.0f + kotlin.math.sin(legacyThemeState.timeSeconds * 0.1f) * 0.5f
		val effectiveAtmosphere = state.atmosphereEnabled && qualityScale >= QUALITY_ATMOSPHERE_THRESHOLD
		val effectiveFlare = state.lensFlareEnabled && qualityScale >= QUALITY_FLARE_THRESHOLD
		val effectiveStars = effectState.starsEnabled && qualityScale >= QUALITY_STARS_THRESHOLD
		val qualityScaledFlare = if (effectiveFlare) {
			(state.flareIntensity * qualityScale.coerceAtLeast(0.35f)).coerceIn(0f, 1f)
		} else {
			0f
		}

		setVec3(skyColorHandle, skyR, skyG, skyB)
		setVec2(sunHandle, shaderSunX, shaderSunY)
		setVec2(moonHandle, shaderMoonX, shaderMoonY)
		setFloat(sunAlphaHandle, sunAlpha)
		setFloat(moonAlphaHandle, if (state.isNight) 1f else 0f)

		setVec2(uSunPosHandle, shaderSunX, shaderSunY)
		setVec3(
			uSunColorHandle,
			legacyThemeState.sunColor.red,
			legacyThemeState.sunColor.green,
			legacyThemeState.sunColor.blue
		)
		setFloat(uAspectRatioHandle, aspect)
		setFloat(uDrawSunHandle, drawSun)
		setVec2(uMoonPosHandle, shaderMoonX, shaderMoonY)
		setFloat(uIsNightHandle, if (state.isNight) 1f else 0f)
		setFloat(uMinuteHandle, minute)
		setFloat(uCloudOffsetHandle, effectState.cloudOffset)
		setFloat(uCloudAlphaHandle, effectState.cloudAlpha)
		setFloat(uSunriseHandle, state.sunriseMinute.toFloat())
		setFloat(uSunsetHandle, state.sunsetMinute.toFloat())
		setFloat(uSolarNoonHandle, legacyThemeState.solarNoonMinute)
		setFloat(uNightAmountHandle, legacyThemeState.nightAmount)
		setFloat(uHorizonYHandle, shaderHorizonY)
		setFloat(uHasAtmosphereHandle, if (effectiveAtmosphere) 1f else 0f)
		setFloat(uHasFlareHandle, if (effectiveFlare) 1f else 0f)
		setFloat(uHasStarsHandle, if (effectiveStars) 1f else 0f)
		setFloat(uFlareIntensityHandle, qualityScaledFlare)
		setVec2(uResolutionHandle, viewportWidth.toFloat(), viewportHeight.toFloat())
		setFloat(uTimeHandle, legacyThemeState.timeSeconds)
		setFloat(uTimeOfDayHandle, legacyThemeState.timeOfDay)
		setVec2(uTouchPositionHandle, 0f, 0f)
		setFloat(uTouchTimeHandle, 0f)
		setFloat(uWindSpeedHandle, windSpeed)
		setFloat(uCityZoomHandle, legacyThemeState.cityTuning.zoom)
		setFloat(uCityVerticalOffsetHandle, legacyThemeState.cityTuning.verticalOffset)
		setFloat(uCityHorizontalOffsetHandle, legacyThemeState.cityTuning.horizontalOffset)

		val textures = textureBinder.resolve(
			swapForegroundPair = legacyThemeState.swapForegroundPair,
			backgroundTextureHandle = backgroundTextureHandle,
			sunTextureHandle = sunTextureHandle,
			moonTextureHandle = moonTextureHandle,
			flareTextureHandle = flareTextureHandle,
			fallbackSolidTextureHandle = fallbackSolidTextureHandle,
			fallbackTransparentTextureHandle = fallbackTransparentTextureHandle
		)

		textureBinder.bindTextureUnit(uSunTextureHandle, 0, textures.sun)
		textureBinder.bindTextureUnit(uForegroundTextureHandle, 1, textures.background)
		textureBinder.bindTextureUnit(uMoonTextureHandle, 2, textures.moon)
		textureBinder.bindTextureUnit(uCloudTextureHandle, 3, textures.flare)
		textureBinder.bindTextureUnit(uTextureHandle, 0, textures.background)
		textureBinder.bindTextureUnit(uTexture1Handle, 0, textures.texture1)
		textureBinder.bindTextureUnit(uTexture2Handle, 1, textures.texture2)

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT)
		GLES20.glDisableVertexAttribArray(positionHandle)
	}

	fun release() {
		if (!hasCurrentGlContext()) {
			programHandle = 0
			fallbackSolidTextureHandle = 0
			fallbackTransparentTextureHandle = 0
			backgroundTextureHandle = 0
			sunTextureHandle = 0
			moonTextureHandle = 0
			flareTextureHandle = 0
			return
		}
		if (programHandle != 0) {
			GLES20.glDeleteProgram(programHandle)
			programHandle = 0
		}
		if (fallbackSolidTextureHandle != 0) {
			GLES20.glDeleteTextures(1, intArrayOf(fallbackSolidTextureHandle), 0)
			fallbackSolidTextureHandle = 0
		}
		if (fallbackTransparentTextureHandle != 0) {
			GLES20.glDeleteTextures(1, intArrayOf(fallbackTransparentTextureHandle), 0)
			fallbackTransparentTextureHandle = 0
		}
		deleteConfiguredTextures()
	}

	private fun hasCurrentGlContext(): Boolean {
		val context = EGL14.eglGetCurrentContext()
		return context != null && context != EGL14.EGL_NO_CONTEXT
	}

	private fun ensureFallbackTextures() {
		if (fallbackSolidTextureHandle != 0 && fallbackTransparentTextureHandle != 0) return
		val handles = IntArray(1)

		GLES20.glGenTextures(1, handles, 0)
		fallbackSolidTextureHandle = handles[0]
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fallbackSolidTextureHandle)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

		val solidBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
		solidBitmap.eraseColor(0xFFFFFFFF.toInt())
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, solidBitmap, 0)
		solidBitmap.recycle()

		GLES20.glGenTextures(1, handles, 0)
		fallbackTransparentTextureHandle = handles[0]
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fallbackTransparentTextureHandle)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

		val transparentBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
		transparentBitmap.eraseColor(0x00000000)
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, transparentBitmap, 0)
		transparentBitmap.recycle()
	}

	private fun loadConfiguredTextures() {
		deleteConfiguredTextures()

		backgroundTextureHandle = loadTexture(config.textures.backgroundTexture)
		sunTextureHandle = loadTexture(config.textures.sunTexture)
		moonTextureHandle = loadTexture(config.textures.moonTexture)
		flareTextureHandle = loadTexture(config.textures.flareTexture)
	}

	private fun deleteConfiguredTextures() {
		val handles = intArrayOf(
			backgroundTextureHandle,
			sunTextureHandle,
			moonTextureHandle,
			flareTextureHandle
		).filter { it != 0 }
		if (handles.isNotEmpty()) {
			GLES20.glDeleteTextures(handles.size, handles.toIntArray(), 0)
		}
		backgroundTextureHandle = 0
		sunTextureHandle = 0
		moonTextureHandle = 0
		flareTextureHandle = 0
	}

	private fun loadTexture(path: String?): Int {
		val normalized = path?.takeIf { it.isNotBlank() } ?: return 0
		val loader = textureBytesLoader ?: return 0
		val resolvedTexture = resolveTextureBytes(
			originalPath = normalized,
			loader = loader
		) ?: return 0
		val resolvedPath = resolvedTexture.path
		val bytes = resolvedTexture.bytes

		val decodedBitmap = BitmapFactory.decodeByteArray(
			bytes,
			0,
			bytes.size,
			BitmapFactory.Options().apply {
				inPreferredConfig = Bitmap.Config.ARGB_8888
				inDither = true
				inSampleSize = resolveTextureDecodeSampleSize(resolvedPath)
			}
		) ?: return 0
		val bitmap = preprocessTextureBitmap(
			assetPath = resolvedPath,
			bitmap = decodedBitmap
		)
		return try {
			val handles = IntArray(1)
			GLES20.glGenTextures(1, handles, 0)
			val handle = handles[0]
			if (handle == 0) return 0

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle)
			val isPixelArt = isPixelArtTexture(resolvedPath)
			val isPot = isPowerOfTwo(bitmap.width) && isPowerOfTwo(bitmap.height)
			val minFilter = if (isPixelArt) {
				GLES20.GL_NEAREST
			} else if (isPot) {
				GLES20.GL_LINEAR_MIPMAP_LINEAR
			} else {
				GLES20.GL_LINEAR
			}
			val magFilter = if (isPixelArt) GLES20.GL_NEAREST else GLES20.GL_LINEAR
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, minFilter)
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, magFilter)
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
			if (!isPixelArt && isPot) {
				GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
			}
			handle
		} finally {
			if (bitmap !== decodedBitmap) {
				bitmap.recycle()
			}
			decodedBitmap.recycle()
		}
	}

	private fun preprocessTextureBitmap(
		assetPath: String,
		bitmap: Bitmap
	): Bitmap {
		var processed = bitmap
		if (shouldTrimTransparentTop(assetPath)) {
			val trimmed = trimTransparentTop(
				bitmap = processed,
				minAlpha = ALPHA_TRIM_MIN_VISIBLE_ALPHA
			)
			if (trimmed != null) {
				processed = trimmed
			}
		}
		if (shouldBleedTransparentEdgeColors(assetPath)) {
			val bled = bleedTransparentEdgeColors(
				bitmap = processed,
				minOpaqueAlpha = ALPHA_BLEED_MIN_OPAQUE_ALPHA,
				passes = ALPHA_BLEED_PASSES
			)
			if (bled != null) {
				if (processed !== bitmap) {
					processed.recycle()
				}
				processed = bled
			}
		}
		if (shouldFeatherTransparentTopBoundary(assetPath)) {
			val feathered = featherTransparentTopBoundary(
				bitmap = processed,
				minVisibleAlpha = ALPHA_TRIM_MIN_VISIBLE_ALPHA,
				colorCarryPixels = TEEMO_TOP_BOUNDARY_COLOR_CARRY_PIXELS,
				fadePixels = TEEMO_TOP_BOUNDARY_ALPHA_FADE_PIXELS
			)
			if (feathered != null) {
				if (processed !== bitmap) {
					processed.recycle()
				}
				processed = feathered
			}
		}
		return processed
	}

	private fun resolveTextureDecodeSampleSize(assetPath: String): Int {
		if (qualityScale >= PREVIEW_TEXTURE_FULL_QUALITY_THRESHOLD) {
			return 1
		}
		return if (shouldUsePreviewTextureSampling(assetPath)) {
			PREVIEW_TEXTURE_SAMPLE_SIZE
		} else {
			1
		}
	}

	private fun shouldTrimTransparentTop(assetPath: String): Boolean {
		val normalized = assetPath.lowercase()
		return normalized == ANIME_SAKURA_TEXTURE_PATH
	}

	private fun shouldBleedTransparentEdgeColors(assetPath: String): Boolean {
		return false
	}

	private fun shouldUsePreviewTextureSampling(assetPath: String): Boolean {
		val normalized = assetPath.lowercase()
		return normalized == TEEMO_BACKGROUND_TEXTURE_PATH ||
			normalized == TEEMO_SUN_TEXTURE_PATH ||
			normalized == TEEMO_MOON_TEXTURE_PATH
	}

	private fun shouldFeatherTransparentTopBoundary(assetPath: String): Boolean {
		return false
	}

	private fun trimTransparentTop(
		bitmap: Bitmap,
		minAlpha: Int
	): Bitmap? {
		val width = bitmap.width
		val height = bitmap.height
		if (width <= 0 || height <= 1) return null

		val rowPixels = IntArray(width)
		var firstVisibleRow = 0
		while (firstVisibleRow < height - 1) {
			bitmap.getPixels(rowPixels, 0, width, 0, firstVisibleRow, width, 1)
			var hasVisiblePixel = false
			for (pixel in rowPixels) {
				val alpha = (pixel ushr 24) and 0xFF
				if (alpha >= minAlpha) {
					hasVisiblePixel = true
					break
				}
			}
			if (hasVisiblePixel) break
			firstVisibleRow++
		}

		if (firstVisibleRow < ALPHA_TRIM_MIN_TOP_PIXELS) return null
		val maxAllowedTrim = (height * ALPHA_TRIM_MAX_RATIO).toInt().coerceAtLeast(ALPHA_TRIM_MIN_TOP_PIXELS)
		if (firstVisibleRow > maxAllowedTrim) return null
		if (firstVisibleRow >= height - 1) return null

		return Bitmap.createBitmap(
			bitmap,
			0,
			firstVisibleRow,
			width,
			height - firstVisibleRow
		)
	}

	private fun bleedTransparentEdgeColors(
		bitmap: Bitmap,
		minOpaqueAlpha: Int,
		passes: Int
	): Bitmap? {
		val width = bitmap.width
		val height = bitmap.height
		if (width <= 1 || height <= 1 || passes <= 0) return null

		val sourcePixels = IntArray(width * height)
		bitmap.getPixels(sourcePixels, 0, width, 0, 0, width, height)
		if (sourcePixels.none { ((it ushr 24) and 0xFF) in 1 until minOpaqueAlpha }) {
			return null
		}

		var current = sourcePixels
		var working = IntArray(sourcePixels.size)
		var changed = false

		repeat(passes) {
			System.arraycopy(current, 0, working, 0, current.size)
			var passChanged = false
			for (y in 0 until height) {
				val rowOffset = y * width
				for (x in 0 until width) {
					val index = rowOffset + x
					val pixel = current[index]
					val alpha = (pixel ushr 24) and 0xFF
					if (alpha >= minOpaqueAlpha) continue

					var redTotal = 0
					var greenTotal = 0
					var blueTotal = 0
					var neighborCount = 0
					for (offsetY in -1..1) {
						val neighborY = y + offsetY
						if (neighborY !in 0 until height) continue
						val neighborRow = neighborY * width
						for (offsetX in -1..1) {
							if (offsetX == 0 && offsetY == 0) continue
							val neighborX = x + offsetX
							if (neighborX !in 0 until width) continue
							val neighbor = current[neighborRow + neighborX]
							val neighborAlpha = (neighbor ushr 24) and 0xFF
							if (neighborAlpha < minOpaqueAlpha) continue
							redTotal += (neighbor ushr 16) and 0xFF
							greenTotal += (neighbor ushr 8) and 0xFF
							blueTotal += neighbor and 0xFF
							neighborCount++
						}
					}

					if (neighborCount == 0) continue
					val red = redTotal / neighborCount
					val green = greenTotal / neighborCount
					val blue = blueTotal / neighborCount
					working[index] = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
					passChanged = true
				}
			}

			if (!passChanged) return@repeat
			changed = true
			val swap = current
			current = working
			working = swap
		}

		if (!changed) return null
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
			setPixels(current, 0, width, 0, 0, width, height)
		}
	}

	private fun featherTransparentTopBoundary(
		bitmap: Bitmap,
		minVisibleAlpha: Int,
		colorCarryPixels: Int,
		fadePixels: Int
	): Bitmap? {
		val width = bitmap.width
		val height = bitmap.height
		if (width <= 1 || height <= 1 || fadePixels <= 0) return null

		val sourcePixels = IntArray(width * height)
		bitmap.getPixels(sourcePixels, 0, width, 0, 0, width, height)
		val result = sourcePixels.copyOf()
		var changed = false

		for (x in 0 until width) {
			var boundaryY = -1
			for (y in 0 until height) {
				val alpha = (sourcePixels[(y * width) + x] ushr 24) and 0xFF
				if (alpha >= minVisibleAlpha) {
					boundaryY = y
					break
				}
			}
			if (boundaryY <= 0) continue

			var redTotal = 0
			var greenTotal = 0
			var blueTotal = 0
			var weightTotal = 0
			val sampleEnd = (boundaryY + TEEMO_TOP_BOUNDARY_COLOR_SAMPLE_PIXELS).coerceAtMost(height - 1)
			for (sampleY in boundaryY..sampleEnd) {
				val pixel = sourcePixels[(sampleY * width) + x]
				val alpha = (pixel ushr 24) and 0xFF
				if (alpha < minVisibleAlpha) continue
				redTotal += ((pixel ushr 16) and 0xFF) * alpha
				greenTotal += ((pixel ushr 8) and 0xFF) * alpha
				blueTotal += (pixel and 0xFF) * alpha
				weightTotal += alpha
			}
			if (weightTotal == 0) continue

			val boundaryRed = (redTotal / weightTotal).coerceIn(0, 255)
			val boundaryGreen = (greenTotal / weightTotal).coerceIn(0, 255)
			val boundaryBlue = (blueTotal / weightTotal).coerceIn(0, 255)

			val carryStart = (boundaryY - colorCarryPixels).coerceAtLeast(0)
			for (carryY in carryStart until boundaryY) {
				val index = (carryY * width) + x
				val pixel = result[index]
				val alpha = (pixel ushr 24) and 0xFF
				val carriedPixel = (alpha shl 24) or (boundaryRed shl 16) or (boundaryGreen shl 8) or boundaryBlue
				if (carriedPixel != pixel) {
					result[index] = carriedPixel
					changed = true
				}
			}

			val fadeEnd = (boundaryY + fadePixels).coerceAtMost(height - 1)
			for (fadeY in boundaryY..fadeEnd) {
				val index = (fadeY * width) + x
				val pixel = sourcePixels[index]
				val alpha = (pixel ushr 24) and 0xFF
				if (alpha == 0) continue

				val progress = smoothStep01((fadeY - boundaryY).toFloat() / fadePixels.toFloat())
				val sourceRed = (pixel ushr 16) and 0xFF
				val sourceGreen = (pixel ushr 8) and 0xFF
				val sourceBlue = pixel and 0xFF
				val fadedAlpha = (alpha * progress).roundToInt().coerceIn(0, 255)
				val mixedRed = lerpChannel(boundaryRed, sourceRed, progress)
				val mixedGreen = lerpChannel(boundaryGreen, sourceGreen, progress)
				val mixedBlue = lerpChannel(boundaryBlue, sourceBlue, progress)
				val featheredPixel = (fadedAlpha shl 24) or (mixedRed shl 16) or (mixedGreen shl 8) or mixedBlue
				if (featheredPixel != result[index]) {
					result[index] = featheredPixel
					changed = true
				}
			}
		}

		if (!changed) return null
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
			setPixels(result, 0, width, 0, 0, width, height)
		}
	}

	private fun smoothStep01(value: Float): Float {
		val clamped = value.coerceIn(0f, 1f)
		return clamped * clamped * (3f - (2f * clamped))
	}

	private fun lerpChannel(start: Int, end: Int, progress: Float): Int {
		return (start + ((end - start) * progress)).roundToInt().coerceIn(0, 255)
	}

	private fun resolveTextureBytes(
		originalPath: String,
		loader: (String) -> ByteArray?
	): ResolvedTexture? {
		val lower = originalPath.lowercase()
		if (lower.endsWith(".webp")) {
			val bytes = runCatching { loader(originalPath) }.getOrNull()
				?.takeIf { it.isNotEmpty() }
				?: return null
			return ResolvedTexture(path = originalPath, bytes = bytes)
		}
		val extIndex = originalPath.lastIndexOf('.')
		if (extIndex != -1) {
			val webpCandidate = "${originalPath.substring(0, extIndex)}.webp"
			val webpBytes = runCatching { loader(webpCandidate) }.getOrNull()
			if (webpBytes != null && webpBytes.isNotEmpty()) {
				return ResolvedTexture(path = webpCandidate, bytes = webpBytes)
			}
		}
		val originalBytes = runCatching { loader(originalPath) }.getOrNull()
			?.takeIf { it.isNotEmpty() }
			?: return null
		return ResolvedTexture(path = originalPath, bytes = originalBytes)
	}

	private fun setVec2(handle: Int, x: Float, y: Float) {
		if (handle >= 0) GLES20.glUniform2f(handle, x, y)
	}

	private fun setVec3(handle: Int, x: Float, y: Float, z: Float) {
		if (handle >= 0) GLES20.glUniform3f(handle, x, y, z)
	}

	private fun setFloat(handle: Int, value: Float) {
		if (handle >= 0) GLES20.glUniform1f(handle, value)
	}

	private fun mapToLegacyShaderY(value: Float): Float {
		return 1f - value
	}

	private fun isPixelArtTexture(path: String): Boolean {
		val normalized = path.lowercase()
		return normalized.contains("16px")
	}

	private fun isPowerOfTwo(value: Int): Boolean {
		return value > 0 && (value and (value - 1)) == 0
	}

	private fun buildProgram(vertexShader: String, fragmentShader: String): Int {
		val vertexHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader) ?: return 0
		val fragmentHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader) ?: run {
			GLES20.glDeleteShader(vertexHandle)
			return 0
		}

		val program = GLES20.glCreateProgram()
		if (program == 0) {
			GLES20.glDeleteShader(vertexHandle)
			GLES20.glDeleteShader(fragmentHandle)
			return 0
		}

		GLES20.glAttachShader(program, vertexHandle)
		GLES20.glAttachShader(program, fragmentHandle)
		GLES20.glLinkProgram(program)

		val linkStatus = IntArray(1)
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
		GLES20.glDeleteShader(vertexHandle)
		GLES20.glDeleteShader(fragmentHandle)
		if (linkStatus[0] == 0) {
			GLES20.glDeleteProgram(program)
			return 0
		}
		return program
	}

	private fun compileShader(type: Int, source: String): Int? {
		val shader = GLES20.glCreateShader(type)
		if (shader == 0) return null

		GLES20.glShaderSource(shader, source)
		GLES20.glCompileShader(shader)

		val status = IntArray(1)
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
		if (status[0] == 0) {
			GLES20.glDeleteShader(shader)
			return null
		}
		return shader
	}

	companion object {
		private const val FLOAT_SIZE_BYTES = 4
		private const val VERTEX_COUNT = 4
		private const val MINUTES_PER_DAY = 1440
		private const val QUALITY_ATMOSPHERE_THRESHOLD = 0.45f
		private const val QUALITY_FLARE_THRESHOLD = 0.55f
		private const val QUALITY_STARS_THRESHOLD = 0.70f
		private const val ANIME_SAKURA_TEXTURE_PATH = "anime/anime_sakura.webp"
		private const val TEEMO_BACKGROUND_TEXTURE_PATH = "teemo/teemo.webp"
		private const val TEEMO_SUN_TEXTURE_PATH = "teemo/teemo_sun.webp"
		private const val TEEMO_MOON_TEXTURE_PATH = "teemo/teemo_moon.webp"
		private const val ALPHA_TRIM_MIN_VISIBLE_ALPHA = 8
		private const val ALPHA_TRIM_MIN_TOP_PIXELS = 24
		private const val ALPHA_TRIM_MAX_RATIO = 0.45f
		private const val ALPHA_BLEED_MIN_OPAQUE_ALPHA = 96
		private const val ALPHA_BLEED_PASSES = 8
		private const val PREVIEW_TEXTURE_FULL_QUALITY_THRESHOLD = 0.95f
		private const val PREVIEW_TEXTURE_SAMPLE_SIZE = 2
		private const val TEEMO_TOP_BOUNDARY_COLOR_CARRY_PIXELS = 24
		private const val TEEMO_TOP_BOUNDARY_ALPHA_FADE_PIXELS = 40
		private const val TEEMO_TOP_BOUNDARY_COLOR_SAMPLE_PIXELS = 12

		private val FULLSCREEN_VERTICES = floatArrayOf(
			-1f, -1f,
			-1f, 1f,
			1f, -1f,
			1f, 1f
		)

		private const val VERTEX_SHADER = """
			attribute vec4 a_Position;
			varying vec2 v_TexCoord;
			varying vec2 v_Uv;
			void main() {
				gl_Position = a_Position;
				v_Uv = a_Position.xy * 0.5 + 0.5;
				v_TexCoord = vec2(v_Uv.x, 1.0 - v_Uv.y);
			}
		"""

		private const val BUILTIN_FRAGMENT_SHADER = """
			precision mediump float;
			varying vec2 v_TexCoord;
			uniform vec3 uSkyColor;
			uniform vec2 uSun;
			uniform vec2 uMoon;
			uniform float uSunAlpha;
			uniform float uMoonAlpha;

			void main() {
				float sunMask = smoothstep(0.11, 0.04, distance(v_TexCoord, uSun));
				float moonMask = smoothstep(0.10, 0.03, distance(v_TexCoord, uMoon));
				vec3 color = uSkyColor;
				color += vec3(1.0, 0.85, 0.5) * sunMask * uSunAlpha;
				color += vec3(0.85, 0.9, 1.0) * moonMask * uMoonAlpha;
				gl_FragColor = vec4(color, 1.0);
			}
		"""
	}

	private data class ResolvedTexture(
		val path: String,
		val bytes: ByteArray
	)
}
