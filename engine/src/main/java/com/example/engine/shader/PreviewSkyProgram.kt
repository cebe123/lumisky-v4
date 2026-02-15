package com.example.engine.shader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.SystemClock
import com.example.engine.config.WallpaperConfig
import com.example.engine.renderer.RenderMode
import com.example.engine.renderer.RenderFrameState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.floor
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

	private val spriteBuffer: FloatBuffer =
		ByteBuffer.allocateDirect(SPRITE_VERTEX_CAPACITY * FLOAT_SIZE_BYTES)
			.order(ByteOrder.nativeOrder())
			.asFloatBuffer()

	private val spriteVertices = FloatArray(SPRITE_VERTEX_CAPACITY)

	private var programHandle: Int = 0
	private var positionHandle: Int = -1
	private var spriteProgramHandle: Int = 0
	private var spritePositionHandle: Int = -1
	private var spriteTexCoordHandle: Int = -1
	private var spriteTextureHandle: Int = -1
	private var spriteAlphaHandle: Int = -1
	private var spriteTintHandle: Int = -1

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
	private var uNightAmountHandle: Int = -1
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
		uNightAmountHandle = GLES20.glGetUniformLocation(programHandle, "u_NightAmount")
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
		val seconds = resolveLegacyTimeSeconds(state)
		val cityTuning = resolveCityTuning(config.id)
		val shaderSunX = state.sun.x.coerceIn(0f, 1f)
		val shaderSunY = mapToLegacyShaderY(state.sun.y)
		val shaderMoonX = state.moon.x.coerceIn(0f, 1f)
		val shaderMoonY = mapToLegacyShaderY(state.moon.y)
		val sunColor = resolveLegacySunColor(state)
		val drawSun = if (minute >= state.sunriseMinute && minute <= state.sunsetMinute) 1f else 0f
		val legacyTimeOfDay = resolveLegacyTimeOfDay(state)
		val windSpeed = 1.0f + kotlin.math.sin(seconds * 0.1f) * 0.5f
		val legacyNightAmount = calculateLegacyNightAmount(
			minute = minute,
			sunrise = state.sunriseMinute.toFloat(),
			sunset = state.sunsetMinute.toFloat()
		)
		val effectiveAtmosphere = state.atmosphereEnabled && qualityScale >= QUALITY_ATMOSPHERE_THRESHOLD
		val effectiveFlare = state.lensFlareEnabled && qualityScale >= QUALITY_FLARE_THRESHOLD
		val effectiveStars = state.starsEnabled && qualityScale >= QUALITY_STARS_THRESHOLD
		val qualityScaledFlare = if (effectiveFlare) {
			(state.flareIntensity * qualityScale.coerceAtLeast(0.35f)).coerceIn(0f, 1f)
		} else {
			0f
		}
		val isWarriorTheme = isWarrior(config.id)

		setVec3(skyColorHandle, skyR, skyG, skyB)
		setVec2(sunHandle, shaderSunX, shaderSunY)
		setVec2(moonHandle, shaderMoonX, shaderMoonY)
		setFloat(sunAlphaHandle, sunAlpha)
		setFloat(moonAlphaHandle, if (state.isNight) 1f else 0f)

		setVec2(uSunPosHandle, shaderSunX, shaderSunY)
		setVec3(uSunColorHandle, sunColor[0], sunColor[1], sunColor[2])
		setFloat(uAspectRatioHandle, aspect)
		setFloat(uDrawSunHandle, drawSun)
		setVec2(uMoonPosHandle, shaderMoonX, shaderMoonY)
		setFloat(uIsNightHandle, if (state.isNight) 1f else 0f)
		setFloat(uMinuteHandle, minute)
		setFloat(uCloudOffsetHandle, 0f)
		setFloat(uCloudAlphaHandle, 0f)
		setFloat(uSunriseHandle, state.sunriseMinute.toFloat())
		setFloat(uSunsetHandle, state.sunsetMinute.toFloat())
		setFloat(uNightAmountHandle, legacyNightAmount)
		setFloat(uHasAtmosphereHandle, if (effectiveAtmosphere) 1f else 0f)
		setFloat(uHasFlareHandle, if (effectiveFlare) 1f else 0f)
		setFloat(uHasStarsHandle, if (effectiveStars) 1f else 0f)
		setFloat(uFlareIntensityHandle, qualityScaledFlare)
		setVec2(uResolutionHandle, viewportWidth.toFloat(), viewportHeight.toFloat())
		setFloat(uTimeHandle, seconds)
		setFloat(uTimeOfDayHandle, legacyTimeOfDay)
		setVec2(uTouchPositionHandle, 0f, 0f)
		setFloat(uTouchTimeHandle, 0f)
		setFloat(uWindSpeedHandle, windSpeed)
		setFloat(uCityZoomHandle, cityTuning.zoom)
		setFloat(uCityVerticalOffsetHandle, cityTuning.verticalOffset)
		setFloat(uCityHorizontalOffsetHandle, cityTuning.horizontalOffset)

		val background = textureOrFallback(backgroundTextureHandle, fallbackTransparentTextureHandle)
		val sunTexture = textureOrFallback(sunTextureHandle, fallbackSolidTextureHandle)
		val moonTexture = textureOrFallback(moonTextureHandle, fallbackSolidTextureHandle)
		val flareTexture = textureOrFallback(flareTextureHandle, fallbackTransparentTextureHandle)
		val texture1 = if (isWarriorTheme) background else flareTexture
		val texture2 = if (isWarriorTheme) {
			textureOrFallback(flareTextureHandle, background)
		} else {
			background
		}

		bindTextureUnit(uSunTextureHandle, 0, sunTexture)
		bindTextureUnit(uForegroundTextureHandle, 1, background)
		bindTextureUnit(uMoonTextureHandle, 2, moonTexture)
		bindTextureUnit(uCloudTextureHandle, 3, flareTexture)
		bindTextureUnit(uTextureHandle, 0, background)
		bindTextureUnit(uTexture1Handle, 0, texture1)
		bindTextureUnit(uTexture2Handle, 1, texture2)

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT)
		GLES20.glDisableVertexAttribArray(positionHandle)
	}

	fun release() {
		if (!hasCurrentGlContext()) {
			programHandle = 0
			spriteProgramHandle = 0
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
		if (spriteProgramHandle != 0) {
			GLES20.glDeleteProgram(spriteProgramHandle)
			spriteProgramHandle = 0
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
		val resolvedPath = resolvePreferredTexturePath(
			originalPath = normalized,
			loader = loader
		)
		val bytes = runCatching { loader(resolvedPath) }.getOrNull() ?: return 0
		if (bytes.isEmpty()) return 0

		val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return 0
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
			bitmap.recycle()
		}
	}

	private fun resolvePreferredTexturePath(
		originalPath: String,
		loader: (String) -> ByteArray?
	): String {
		val lower = originalPath.lowercase()
		if (lower.endsWith(".webp")) return originalPath
		val extIndex = originalPath.lastIndexOf('.')
		if (extIndex == -1) return originalPath
		val webpCandidate = "${originalPath.substring(0, extIndex)}.webp"
		val webpBytes = runCatching { loader(webpCandidate) }.getOrNull()
		return if (webpBytes != null && webpBytes.isNotEmpty()) webpCandidate else originalPath
	}

	private fun initSpriteProgram() {
		if (spriteProgramHandle != 0) return
		spriteProgramHandle = buildProgram(SPRITE_VERTEX_SHADER, SPRITE_FRAGMENT_SHADER)
		if (spriteProgramHandle == 0) return
		spritePositionHandle = GLES20.glGetAttribLocation(spriteProgramHandle, "aPosition")
		spriteTexCoordHandle = GLES20.glGetAttribLocation(spriteProgramHandle, "aTexCoord")
		spriteTextureHandle = GLES20.glGetUniformLocation(spriteProgramHandle, "uTexture")
		spriteAlphaHandle = GLES20.glGetUniformLocation(spriteProgramHandle, "uAlpha")
		spriteTintHandle = GLES20.glGetUniformLocation(spriteProgramHandle, "uTint")
	}

	private fun drawCelestialOverlays(
		state: RenderFrameState,
		sunTexture: Int,
		moonTexture: Int
	) {
		if (spriteProgramHandle == 0) return
		val sunAlpha = (state.sunAltitude * 1.4f).coerceIn(0f, 1f)
		val moonAlpha = (state.moonAltitude * (0.35f + (0.65f * state.nightBlend))).coerceIn(0f, 1f)
		if (sunAlpha <= MIN_CELESTIAL_ALPHA && moonAlpha <= MIN_CELESTIAL_ALPHA) return

		GLES20.glEnable(GLES20.GL_BLEND)
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
		if (sunAlpha > MIN_CELESTIAL_ALPHA) {
			drawSprite(
				textureHandle = sunTexture,
				x = state.sun.x,
				y = state.sun.y,
				size = SUN_SPRITE_SIZE,
				alpha = sunAlpha,
				tintR = 1.0f,
				tintG = 0.95f,
				tintB = 0.82f
			)
		}
		if (moonAlpha > MIN_CELESTIAL_ALPHA) {
			drawSprite(
				textureHandle = moonTexture,
				x = state.moon.x,
				y = state.moon.y,
				size = MOON_SPRITE_SIZE,
				alpha = moonAlpha,
				tintR = 0.90f,
				tintG = 0.94f,
				tintB = 1.0f
			)
		}
		GLES20.glDisable(GLES20.GL_BLEND)
	}

	private fun drawSprite(
		textureHandle: Int,
		x: Float,
		y: Float,
		size: Float,
		alpha: Float,
		tintR: Float,
		tintG: Float,
		tintB: Float
	) {
		if (textureHandle == 0 || alpha <= 0f) return
		val xClamped = x.coerceIn(0f, 1f)
		val yClamped = y.coerceIn(0f, 1f)
		val aspect = viewportWidth.toFloat() / viewportHeight.toFloat()
		val halfHeightNdc = (size * 2f).coerceAtLeast(0.001f)
		val halfWidthNdc = (halfHeightNdc / aspect).coerceAtLeast(0.001f)
		val centerX = (xClamped * 2f) - 1f
		val centerY = (yClamped * 2f) - 1f

		// Triangle strip vertices: pos(x,y) + uv(u,v)
		writeSpriteVertices(
			left = centerX - halfWidthNdc,
			right = centerX + halfWidthNdc,
			bottom = centerY - halfHeightNdc,
			top = centerY + halfHeightNdc
		)

		GLES20.glUseProgram(spriteProgramHandle)
		spriteBuffer.position(0)
		GLES20.glEnableVertexAttribArray(spritePositionHandle)
		GLES20.glVertexAttribPointer(
			spritePositionHandle,
			2,
			GLES20.GL_FLOAT,
			false,
			SPRITE_STRIDE_BYTES,
			spriteBuffer
		)
		spriteBuffer.position(2)
		GLES20.glEnableVertexAttribArray(spriteTexCoordHandle)
		GLES20.glVertexAttribPointer(
			spriteTexCoordHandle,
			2,
			GLES20.GL_FLOAT,
			false,
			SPRITE_STRIDE_BYTES,
			spriteBuffer
		)
		GLES20.glUniform1f(spriteAlphaHandle, alpha.coerceIn(0f, 1f))
		GLES20.glUniform3f(spriteTintHandle, tintR, tintG, tintB)
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
		GLES20.glUniform1i(spriteTextureHandle, 0)
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT)
		GLES20.glDisableVertexAttribArray(spritePositionHandle)
		GLES20.glDisableVertexAttribArray(spriteTexCoordHandle)
	}

	private fun writeSpriteVertices(
		left: Float,
		right: Float,
		bottom: Float,
		top: Float
	) {
		// Bottom-left.
		spriteVertices[0] = left
		spriteVertices[1] = bottom
		spriteVertices[2] = 0f
		spriteVertices[3] = 0f
		// Bottom-right.
		spriteVertices[4] = right
		spriteVertices[5] = bottom
		spriteVertices[6] = 1f
		spriteVertices[7] = 0f
		// Top-left.
		spriteVertices[8] = left
		spriteVertices[9] = top
		spriteVertices[10] = 0f
		spriteVertices[11] = 1f
		// Top-right.
		spriteVertices[12] = right
		spriteVertices[13] = top
		spriteVertices[14] = 1f
		spriteVertices[15] = 1f

		spriteBuffer.position(0)
		spriteBuffer.put(spriteVertices)
		spriteBuffer.position(0)
	}

	private fun textureOrFallback(handle: Int, fallback: Int): Int {
		return if (handle != 0) handle else fallback
	}

	private fun bindTextureUnit(uniformHandle: Int, textureUnit: Int, textureHandle: Int) {
		if (uniformHandle < 0 || textureHandle == 0) return
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureUnit)
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
		GLES20.glUniform1i(uniformHandle, textureUnit)
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

	private fun resolveCityTuning(configId: String): CityTuning {
		return when {
			configId.contains("city_istanbul") -> CityTuning(zoom = 0.60f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
			configId.contains("city_newyork") -> CityTuning(zoom = 0.70f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
			configId.contains("city_tokyo") -> CityTuning(zoom = 0.75f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
			configId.contains("city_paris") -> CityTuning(zoom = 0.70f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
			else -> CityTuning(zoom = 0.85f, verticalOffset = 0.04f, horizontalOffset = 0.0f)
		}
	}

	private fun mapToLegacyShaderY(value: Float): Float {
		return 1f - value
	}

	private fun resolveLegacySunColor(state: RenderFrameState): FloatArray {
		val minute = (state.dayProgress * MINUTES_PER_DAY).coerceIn(0f, MINUTES_PER_DAY.toFloat())
		val sunrise = state.sunriseMinute.toFloat()
		val sunset = state.sunsetMinute.toFloat()
		if (sunset <= sunrise || minute < sunrise || minute > sunset) {
			return floatArrayOf(1.0f, 0.9f, 0.8f)
		}
		val dayProgress = ((minute - sunrise) / (sunset - sunrise)).coerceIn(0f, 1f)
		var colorProgress = if (dayProgress < 0.5f) {
			dayProgress * 2f
		} else {
			2f - (dayProgress * 2f)
		}
		if (usesNaturalSunColor(config.id)) {
			colorProgress = 1f - colorProgress
		}
		val green = 0.9f - (0.4f * colorProgress)
		val blue = 0.8f - (0.6f * colorProgress)
		return floatArrayOf(1.0f, green, blue)
	}

	private fun resolveLegacyTimeOfDay(state: RenderFrameState): Float {
		val id = config.id.lowercase()
		return when {
			id.contains("mars") || id.contains("warrior") -> resolveMarsWarriorTimeOfDay(state)
			id.contains("optical_sunset") -> resolveOpticalSunsetTimeOfDay(state)
			else -> state.dayProgress.coerceIn(0f, 1f)
		}
	}

	private fun resolveMarsWarriorTimeOfDay(state: RenderFrameState): Float {
		val sunrise = state.sunriseMinute
		val sunset = state.sunsetMinute
		if (sunset <= sunrise) return state.dayProgress.coerceIn(0f, 1f)
		val minute = ((state.dayProgress * MINUTES_PER_DAY).toInt() % MINUTES_PER_DAY + MINUTES_PER_DAY) % MINUTES_PER_DAY
		var noon = (sunrise + sunset) / 2
		if (noon < sunrise) noon += 720
		val horizon = 0.40f
		val result = when {
			minute in sunrise..noon && noon > sunrise -> {
				val progress = (minute - sunrise).toFloat() / (noon - sunrise).toFloat()
				horizon * (1.0f - progress)
			}
			minute in noon..sunset && sunset > noon -> {
				val progress = (minute - noon).toFloat() / (sunset - noon).toFloat()
				horizon * progress
			}
			else -> {
				var minutesSinceSunset = minute - sunset
				if (minutesSinceSunset < 0) minutesSinceSunset += MINUTES_PER_DAY
				val nightDuration = ((MINUTES_PER_DAY - sunset) + sunrise).coerceAtLeast(1)
				val progress = minutesSinceSunset.toFloat() / nightDuration.toFloat()
				horizon + (progress * (1.0f - horizon))
			}
		}
		return result.coerceIn(0f, 1f)
	}

	private fun resolveOpticalSunsetTimeOfDay(state: RenderFrameState): Float {
		val sunrise = state.sunriseMinute
		val sunset = state.sunsetMinute
		if (sunset <= sunrise) return state.dayProgress.coerceIn(0f, 1f)
		val minute = ((state.dayProgress * MINUTES_PER_DAY).toInt() % MINUTES_PER_DAY + MINUTES_PER_DAY) % MINUTES_PER_DAY
		var noon = (sunrise + sunset) / 2
		if (noon < sunrise) noon += 720
		val horizon = 0.2666f
		val result = when {
			minute in sunrise..noon && noon > sunrise -> {
				val progress = (minute - sunrise).toFloat() / (noon - sunrise).toFloat()
				horizon * (1.0f - progress)
			}
			minute in noon..sunset && sunset > noon -> {
				val progress = (minute - noon).toFloat() / (sunset - noon).toFloat()
				horizon * progress
			}
			else -> {
				var minutesSinceSunset = minute - sunset
				if (minutesSinceSunset < 0) minutesSinceSunset += MINUTES_PER_DAY
				val nightDuration = ((MINUTES_PER_DAY - sunset) + sunrise).coerceAtLeast(1)
				val progress = minutesSinceSunset.toFloat() / nightDuration.toFloat()
				if (progress < 0.5f) {
					val p = progress * 2.0f
					horizon + (p * (1.0f - horizon))
				} else {
					val p = (progress - 0.5f) * 2.0f
					1.0f - (p * (1.0f - horizon))
				}
			}
		}
		return result.coerceIn(0f, 1f)
	}

	private fun usesNaturalSunColor(configId: String): Boolean {
		return configId.lowercase().contains("pixel_forest")
	}

	private fun isWarrior(configId: String): Boolean {
		return configId.lowercase().contains("warrior")
	}

	private fun resolveLegacyTimeSeconds(state: RenderFrameState): Float {
		val id = config.id.lowercase()
		if (!isWarrior(id)) {
			return (state.frameTimeMillis % LEGACY_TIME_WINDOW_MS).toFloat() / 1000f
		}

		return when (state.mode) {
			RenderMode.FOCUS -> {
				// Home screen: warrior textures run on real wall-clock time (no compressed timeline).
				(System.currentTimeMillis() % MILLIS_PER_DAY).toFloat() / 1000f
			}
			RenderMode.PREVIEW -> {
				// Set screen: match accelerated day progression and sample textures at 10 FPS.
				val acceleratedSeconds = (state.frameTimeMillis % MILLIS_PER_DAY).toFloat() / 1000f
				quantizeTimeSeconds(acceleratedSeconds, WARRIOR_TEXTURE_FPS)
			}
			RenderMode.WALLPAPER_SERVICE -> {
				// Applied wallpaper: keep texture animation at fixed 10 FPS.
				val realtimeSeconds =
					(SystemClock.elapsedRealtime() % LEGACY_REALTIME_WINDOW_MS).toFloat() / 1000f
				quantizeTimeSeconds(realtimeSeconds, WARRIOR_TEXTURE_FPS)
			}
			else -> (state.frameTimeMillis % LEGACY_TIME_WINDOW_MS).toFloat() / 1000f
		}
	}

	private fun quantizeTimeSeconds(seconds: Float, fps: Float): Float {
		if (fps <= 0f) return seconds
		val frameStep = 1f / fps
		return floor(seconds / frameStep) * frameStep
	}

	private fun isPixelArtTexture(path: String): Boolean {
		val normalized = path.lowercase()
		return normalized.contains("16px")
	}

	private fun isPowerOfTwo(value: Int): Boolean {
		return value > 0 && (value and (value - 1)) == 0
	}

	private fun calculateLegacyNightAmount(minute: Float, sunrise: Float, sunset: Float): Float {
		if (minute >= sunset && minute < sunset + NIGHT_TRANSITION_AFTER_SUNSET_MIN) {
			return smoothstep(sunset, sunset + NIGHT_TRANSITION_AFTER_SUNSET_MIN, minute)
		}
		if ((minute >= sunset + NIGHT_TRANSITION_AFTER_SUNSET_MIN && minute < MINUTES_PER_DAY.toFloat()) ||
			minute < sunrise - NIGHT_TRANSITION_BEFORE_SUNRISE_MIN
		) {
			return 1f
		}
		if (minute >= sunrise - NIGHT_TRANSITION_BEFORE_SUNRISE_WIDE_MIN &&
			minute <= sunrise + NIGHT_TRANSITION_AFTER_SUNRISE_MIN
		) {
			val t = smoothstep(
				sunrise - NIGHT_TRANSITION_BEFORE_SUNRISE_WIDE_MIN,
				sunrise + NIGHT_TRANSITION_AFTER_SUNRISE_MIN,
				minute
			)
			return 1f - t
		}
		return 0f
	}

	private fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
		if (edge0 == edge1) return if (value < edge0) 0f else 1f
		val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
		return t * t * (3f - (2f * t))
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
		private const val SPRITE_STRIDE_FLOATS = 4
		private const val SPRITE_VERTEX_CAPACITY = VERTEX_COUNT * SPRITE_STRIDE_FLOATS
		private const val SPRITE_STRIDE_BYTES = SPRITE_STRIDE_FLOATS * FLOAT_SIZE_BYTES
		private const val MINUTES_PER_DAY = 1440
		private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
		private const val LEGACY_TIME_WINDOW_MS = 100_000L
		private const val LEGACY_REALTIME_WINDOW_MS = 1_000_000L
		private const val WARRIOR_TEXTURE_FPS = 10f
		private const val NIGHT_TRANSITION_AFTER_SUNSET_MIN = 20f
		private const val NIGHT_TRANSITION_BEFORE_SUNRISE_MIN = 20f
		private const val NIGHT_TRANSITION_BEFORE_SUNRISE_WIDE_MIN = 30f
		private const val NIGHT_TRANSITION_AFTER_SUNRISE_MIN = 10f
		private const val QUALITY_ATMOSPHERE_THRESHOLD = 0.45f
		private const val QUALITY_FLARE_THRESHOLD = 0.55f
		private const val QUALITY_STARS_THRESHOLD = 0.70f
		private const val SUN_MIN_ALTITUDE = 0.01f
		private const val MIN_CELESTIAL_ALPHA = 0.02f
		private const val SUN_SPRITE_SIZE = 0.065f
		private const val MOON_SPRITE_SIZE = 0.058f

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

		private const val SPRITE_VERTEX_SHADER = """
			attribute vec2 aPosition;
			attribute vec2 aTexCoord;
			varying vec2 vTexCoord;
			void main() {
				vTexCoord = aTexCoord;
				gl_Position = vec4(aPosition, 0.0, 1.0);
			}
		"""

		private const val SPRITE_FRAGMENT_SHADER = """
			precision mediump float;
			varying vec2 vTexCoord;
			uniform sampler2D uTexture;
			uniform float uAlpha;
			uniform vec3 uTint;
			void main() {
				vec4 sampleColor = texture2D(uTexture, vTexCoord);
				float alpha = sampleColor.a * uAlpha;
				if (alpha <= 0.01) {
					discard;
				}
				gl_FragColor = vec4(sampleColor.rgb * uTint, alpha);
			}
		"""
	}

	private data class CityTuning(
		val zoom: Float,
		val verticalOffset: Float,
		val horizontalOffset: Float
	)
}
