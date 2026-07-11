/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Parallax için tek merkezi sensor listener; throttling/smoothing/subscriber yönetimi.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Parallax için tek merkezi sensor listener; throttling/smoothing/subscriber yönetimi.
 */
package com.example.lumisky.device

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class SensorDispatcher @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val tiltSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val listeners = mutableListOf<SensorListener>()
    private var isRegistered = false

    private var gravityX = 0.0f
    private var gravityY = 0.0f
    private var gravityZ = 9.80665f

    var rawX = 0.0f
    var rawY = 0.0f

    fun registerListener(listener: SensorListener) {
        synchronized(listeners) {
            listeners.add(listener)
            if (!isRegistered && tiltSensor != null) {
                sensorManager.registerListener(this, tiltSensor, SensorManager.SENSOR_DELAY_GAME)
                isRegistered = true
            }
        }
    }

    fun unregisterListener(listener: SensorListener) {
        synchronized(listeners) {
            listeners.remove(listener)
            if (listeners.isEmpty() && isRegistered) {
                sensorManager.unregisterListener(this)
                isRegistered = false
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val inputSmoothing = if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            0.14f
        } else {
            1.0f
        }
        
        gravityX = gravityX + (event.values[0] - gravityX) * inputSmoothing
        gravityY = gravityY + (event.values[1] - gravityY) * inputSmoothing
        gravityZ = gravityZ + (event.values[2] - gravityZ) * inputSmoothing

        val magnitude = sqrt((gravityX * gravityX) + (gravityY * gravityY) + (gravityZ * gravityZ))
            .coerceAtLeast(0.1f)
        
        rawX = (-gravityX / magnitude).coerceIn(-0.85f, 0.85f)
        rawY = (gravityY / magnitude).coerceIn(-0.85f, 0.85f)
        
        synchronized(listeners) {
            listeners.forEach { it.onSensorValues(rawX, rawY) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    interface SensorListener {
        fun onSensorValues(x: Float, y: Float)
    }
}
