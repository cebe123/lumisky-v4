package com.example.engine.renderer

class FrameScheduler {
	fun shouldRender(
		mode: RenderMode,
		stateHash: Int,
		previousHash: Int?,
		force: Boolean = false
	): Boolean {
		if (force) return true
		if (mode == RenderMode.IDLE) return false
		if (previousHash == null) return true
		return stateHash != previousHash
	}
}
