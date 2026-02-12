package com.example.engine.shader

data class ShaderProgram(
	val vertexSource: String,
	val fragmentSource: String,
	val flags: Set<String> = emptySet()
)

class ShaderCache {
	private val cache = mutableMapOf<Int, ShaderProgram>()

	fun get(hash: Int): ShaderProgram? = cache[hash]

	fun put(hash: Int, program: ShaderProgram) {
		cache[hash] = program
	}
}

class ShaderCompiler {
	fun compile(vertex: String, fragment: String, flags: Set<String> = emptySet()): ShaderProgram {
		return ShaderProgram(vertex, fragment, flags)
	}
}

object SkyMegaShader {
	const val VERTEX = "attribute vec4 aPosition; void main(){ gl_Position = aPosition; }"
	const val FRAGMENT = "precision mediump float; void main(){ gl_FragColor = vec4(0.1,0.2,0.3,1.0); }"
}
