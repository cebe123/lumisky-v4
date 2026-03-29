package com.example.engine.renderer

import com.example.engine.sky.Vec2

class RenderFrameState(
	var frameTimeMillis: Long = 0L,
	var mode: RenderMode = RenderMode.PREVIEW,
	var dayProgress: Float = 0f,
	var isNight: Boolean = false,
	var dayLengthMinutes: Int = 0,
	var nightLengthMinutes: Int = 0,
	val sun: Vec2 = Vec2(),
	val moon: Vec2 = Vec2(),
	var skyColor: Int = 0,
	var skyTopColor: Int = 0,
	var skyHorizonColor: Int = 0,
	var sunAltitude: Float = 0f,
	var moonAltitude: Float = 0f,
	var nightBlend: Float = 0f,
	var preSunriseGlow: Float = 0f,
	var atmosphereEnabled: Boolean = false,
	var lensFlareEnabled: Boolean = false,
	var starsEnabled: Boolean = false,
	var flareIntensity: Float = 0f,
	var sunriseMinute: Int = 0,
	var sunsetMinute: Int = 0,
	var stateHash: Int = 0
)
