package com.example.engine.renderer

import com.example.engine.sky.Vec2

data class RenderFrameState(
	val frameTimeMillis: Long,
	val mode: RenderMode,
	val dayProgress: Float,
	val isNight: Boolean,
	val dayLengthMinutes: Int,
	val nightLengthMinutes: Int,
	val sun: Vec2,
	val moon: Vec2,
	val skyColor: Int,
	val skyTopColor: Int,
	val skyHorizonColor: Int,
	val sunAltitude: Float,
	val moonAltitude: Float,
	val nightBlend: Float,
	val preSunriseGlow: Float,
	val atmosphereEnabled: Boolean,
	val lensFlareEnabled: Boolean,
	val starsEnabled: Boolean,
	val flareIntensity: Float,
	val sunriseMinute: Int,
	val sunsetMinute: Int,
	val stateHash: Int
)
