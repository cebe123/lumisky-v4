package com.example.core.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.core.Logger
import kotlin.math.abs
import kotlin.math.sqrt

class TiltParallaxTracker(
	context: Context,
	private val onParallaxChanged: (x: Float, y: Float) -> Unit
) : SensorEventListener, AutoCloseable {

	private val appContext = context.applicationContext
	private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
	private val tiltSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
		?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

	private var active: Boolean = false
	private var gravityX: Float = 0f
	private var gravityY: Float = 0f
	private var gravityZ: Float = SensorManager.GRAVITY_EARTH
	private var filteredX: Float = 0f
	private var filteredY: Float = 0f
	private var lastDispatchedX: Float = Float.NaN
	private var lastDispatchedY: Float = Float.NaN
	private var sensorUnavailableLogged: Boolean = false

	fun start() {
		if (active) return
		val manager = sensorManager
		val sensor = tiltSensor
		if (manager == null || sensor == null) {
			logSensorUnavailableOnce()
			dispatch(0f, 0f, force = true)
			return
		}
		active = manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
		if (!active) {
			logSensorUnavailableOnce()
			dispatch(0f, 0f, force = true)
		}
	}

	fun stop() {
		if (active) {
			sensorManager?.unregisterListener(this)
			active = false
		}
		gravityX = 0f
		gravityY = 0f
		gravityZ = SensorManager.GRAVITY_EARTH
		filteredX = 0f
		filteredY = 0f
		dispatch(0f, 0f, force = true)
	}

	override fun close() {
		stop()
	}

	override fun onSensorChanged(event: SensorEvent) {
		if (!active) return
		val inputSmoothing = if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
			ACCELEROMETER_INPUT_SMOOTHING
		} else {
			1f
		}
		gravityX = lerp(gravityX, event.values[0], inputSmoothing)
		gravityY = lerp(gravityY, event.values[1], inputSmoothing)
		gravityZ = lerp(gravityZ, event.values[2], inputSmoothing)

		val magnitude = sqrt((gravityX * gravityX) + (gravityY * gravityY) + (gravityZ * gravityZ))
			.coerceAtLeast(MIN_GRAVITY_MAGNITUDE)
		val targetX = (-gravityX / magnitude).coerceIn(-MAX_PARALLAX_INPUT, MAX_PARALLAX_INPUT)
		val targetY = (gravityY / magnitude).coerceIn(-MAX_PARALLAX_INPUT, MAX_PARALLAX_INPUT)

		filteredX = lerp(filteredX, targetX, OUTPUT_SMOOTHING)
		filteredY = lerp(filteredY, targetY, OUTPUT_SMOOTHING)
		dispatch(filteredX, filteredY, force = false)
	}

	override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

	private fun dispatch(
		x: Float,
		y: Float,
		force: Boolean
	) {
		val resolvedX = x.coerceIn(-MAX_PARALLAX_INPUT, MAX_PARALLAX_INPUT)
		val resolvedY = y.coerceIn(-MAX_PARALLAX_INPUT, MAX_PARALLAX_INPUT)
		if (!force &&
			abs(resolvedX - lastDispatchedX) < DISPATCH_EPSILON &&
			abs(resolvedY - lastDispatchedY) < DISPATCH_EPSILON
		) {
			return
		}
		lastDispatchedX = resolvedX
		lastDispatchedY = resolvedY
		onParallaxChanged(resolvedX, resolvedY)
	}

	private fun logSensorUnavailableOnce() {
		if (sensorUnavailableLogged) return
		sensorUnavailableLogged = true
		Logger.w(TAG, "tilt parallax sensor unavailable")
	}

	private fun lerp(
		start: Float,
		end: Float,
		factor: Float
	): Float {
		return start + ((end - start) * factor.coerceIn(0f, 1f))
	}

	companion object {
		private const val TAG = "TiltParallaxTracker"
		private const val MIN_GRAVITY_MAGNITUDE = 0.1f
		private const val MAX_PARALLAX_INPUT = 0.85f
		private const val ACCELEROMETER_INPUT_SMOOTHING = 0.14f
		private const val OUTPUT_SMOOTHING = 0.18f
		private const val DISPATCH_EPSILON = 0.0025f
	}
}
