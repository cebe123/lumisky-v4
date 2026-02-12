package com.example.engine.shader

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.example.engine.renderer.RenderFrameState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
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
	private var uNightAmountHandle: Int = -1

	private var uSunTextureHandle: Int = -1
	private var uMoonTextureHandle: Int = -1
	private var uForegroundTextureHandle: Int = -1
	private var uCloudTextureHandle: Int = -1

	private var viewportWidth: Int = 1
	private var viewportHeight: Int = 1
	private var fallbackTextureHandle: Int = 0

	fun init(fragmentShaderOverride: String? = null) {
		if (programHandle != 0) return
		val fragmentShader = fragmentShaderOverride ?: BUILTIN_FRAGMENT_SHADER
		programHandle = buildProgram(VERTEX_SHADER, fragmentShader)
		if (programHandle == 0) {
			// Fallback to built-in shader if override fails.
			programHandle = buildProgram(VERTEX_SHADER, BUILTIN_FRAGMENT_SHADER)
		}
		if (programHandle == 0) return

		positionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
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

		uSunTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_SunTexture")
		uMoonTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_MoonTexture")
		uForegroundTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_ForegroundTexture")
		uCloudTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_CloudTexture")
	}

	fun setViewport(width: Int, height: Int) {
		viewportWidth = width.coerceAtLeast(1)
		viewportHeight = height.coerceAtLeast(1)
	}

	fun draw(state: RenderFrameState) {
		if (programHandle == 0) return
		ensureFallbackTexture()

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
		val nightAmount = (1f - sunAlpha).coerceIn(0f, 1f)
		val minute = (state.dayProgress * MINUTES_PER_DAY).coerceIn(0f, MINUTES_PER_DAY.toFloat())
		val aspect = viewportWidth.toFloat() / viewportHeight.toFloat()

		setVec3(skyColorHandle, skyR, skyG, skyB)
		setVec2(sunHandle, state.sun.x, state.sun.y)
		setVec2(moonHandle, state.moon.x, state.moon.y)
		setFloat(sunAlphaHandle, sunAlpha)
		setFloat(moonAlphaHandle, nightAmount)

		setVec2(uSunPosHandle, state.sun.x, state.sun.y)
		setVec3(uSunColorHandle, 1.0f, 0.88f, 0.55f)
		setFloat(uAspectRatioHandle, aspect)
		setFloat(uDrawSunHandle, if (nightAmount < 0.95f) 1f else 0f)
		setVec2(uMoonPosHandle, state.moon.x, state.moon.y)
		setFloat(uIsNightHandle, if (nightAmount > 0.5f) 1f else 0f)
		setFloat(uMinuteHandle, minute)
		setFloat(uCloudOffsetHandle, state.dayProgress * 0.25f)
		setFloat(uCloudAlphaHandle, 0.5f)
		setFloat(uSunriseHandle, 6f * 60f)
		setFloat(uSunsetHandle, 18f * 60f)
		setFloat(uNightAmountHandle, nightAmount)

		bindTextureUnit(uSunTextureHandle, 0)
		bindTextureUnit(uMoonTextureHandle, 1)
		bindTextureUnit(uForegroundTextureHandle, 2)
		bindTextureUnit(uCloudTextureHandle, 3)

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT)
		GLES20.glDisableVertexAttribArray(positionHandle)
	}

	fun release() {
		if (programHandle != 0) {
			GLES20.glDeleteProgram(programHandle)
			programHandle = 0
		}
		if (fallbackTextureHandle != 0) {
			GLES20.glDeleteTextures(1, intArrayOf(fallbackTextureHandle), 0)
			fallbackTextureHandle = 0
		}
	}

	private fun ensureFallbackTexture() {
		if (fallbackTextureHandle != 0) return
		val handles = IntArray(1)
		GLES20.glGenTextures(1, handles, 0)
		fallbackTextureHandle = handles[0]
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fallbackTextureHandle)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

		val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
		bitmap.eraseColor(0xFFFFFFFF.toInt())
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
		bitmap.recycle()
	}

	private fun bindTextureUnit(uniformHandle: Int, textureUnit: Int) {
		if (uniformHandle < 0 || fallbackTextureHandle == 0) return
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureUnit)
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fallbackTextureHandle)
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

		private val FULLSCREEN_VERTICES = floatArrayOf(
			-1f, -1f,
			1f, -1f,
			-1f, 1f,
			1f, 1f
		)

		private const val VERTEX_SHADER = """
			attribute vec2 aPosition;
			varying vec2 v_TexCoord;
			void main() {
				v_TexCoord = (aPosition + 1.0) * 0.5;
				gl_Position = vec4(aPosition, 0.0, 1.0);
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
}
