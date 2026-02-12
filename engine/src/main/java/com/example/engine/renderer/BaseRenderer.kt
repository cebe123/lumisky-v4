package com.example.engine.renderer

abstract class BaseRenderer {

	open fun initGl() = Unit

	open fun resize(width: Int, height: Int) = Unit

	open fun pause() = Unit

	open fun resume() = Unit

	abstract fun drawFrame(frameTimeMillis: Long)
}
