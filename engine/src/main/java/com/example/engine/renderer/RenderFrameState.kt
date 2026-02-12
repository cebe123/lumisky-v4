package com.example.engine.renderer

import com.example.engine.sky.Vec2

data class RenderFrameState(
	val frameTimeMillis: Long,
	val mode: RenderMode,
	val dayProgress: Float,
	val sun: Vec2,
	val moon: Vec2,
	val skyColor: Int,
	val stateHash: Int
)
