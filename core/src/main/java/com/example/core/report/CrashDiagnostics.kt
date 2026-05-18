package com.example.core.report

object CrashDiagnostics {
	interface Sink {
		fun log(message: String)
		fun setCustomKey(key: String, value: String)
		fun setCustomKey(key: String, value: Boolean)
		fun setCustomKey(key: String, value: Long)
		fun recordException(throwable: Throwable)
	}

	@Volatile
	private var sink: Sink? = null

	fun install(value: Sink) {
		sink = value
	}

	fun log(message: String) {
		sink?.log(message)
	}

	fun setCustomKey(key: String, value: String?) {
		sink?.setCustomKey(key, value?.takeIf { it.isNotBlank() } ?: "unknown")
	}

	fun setCustomKey(key: String, value: Boolean) {
		sink?.setCustomKey(key, value)
	}

	fun setCustomKey(key: String, value: Int) {
		sink?.setCustomKey(key, value.toLong())
	}

	fun setCustomKey(key: String, value: Long) {
		sink?.setCustomKey(key, value)
	}

	fun recordException(throwable: Throwable) {
		sink?.recordException(throwable)
	}
}
