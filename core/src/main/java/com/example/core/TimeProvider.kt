package com.example.core

interface TimeProvider {
	fun nowMillis(): Long
}

class SystemTimeProvider : TimeProvider {
	override fun nowMillis(): Long = System.currentTimeMillis()
}
