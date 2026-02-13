package com.example.core

import android.os.SystemClock
import java.util.UUID

object Logger {
	enum class Level(val priority: Int) {
		VERBOSE(android.util.Log.VERBOSE),
		DEBUG(android.util.Log.DEBUG),
		INFO(android.util.Log.INFO),
		WARN(android.util.Log.WARN),
		ERROR(android.util.Log.ERROR),
		NONE(Int.MAX_VALUE)
	}

	data class Config(
		val enabled: Boolean = true,
		val minLevel: Level = Level.DEBUG,
		val tagPrefix: String = "Lumisky",
		val includeThread: Boolean = true,
		val includeUptimeMs: Boolean = true
	)

	@Volatile
	private var config = Config()
	@Volatile
	private var sessionId: String = newSessionId()
	@Volatile
	private var sessionStartElapsedMs: Long = SystemClock.elapsedRealtime()

	fun configure(newConfig: Config) {
		config = newConfig
	}

	fun restartSession() {
		sessionId = newSessionId()
		sessionStartElapsedMs = SystemClock.elapsedRealtime()
	}

	fun v(tag: String, msg: String) = emit(Level.VERBOSE, tag, msg, null)
	fun d(tag: String, msg: String) = emit(Level.DEBUG, tag, msg, null)
	fun i(tag: String, msg: String) = emit(Level.INFO, tag, msg, null)
	fun w(tag: String, msg: String, tr: Throwable? = null) = emit(Level.WARN, tag, msg, tr)
	fun e(tag: String, msg: String, tr: Throwable? = null) = emit(Level.ERROR, tag, msg, tr)

	fun event(tag: String, name: String, vararg fields: Pair<String, Any?>) {
		val details = fields.joinToString(separator = " ") { (key, value) ->
			"$key=${value ?: "null"}"
		}
		if (details.isBlank()) {
			d(tag, name)
		} else {
			d(tag, "$name $details")
		}
	}

	private fun emit(
		level: Level,
		tag: String,
		message: String,
		throwable: Throwable?
	) {
		val currentConfig = config
		if (!currentConfig.enabled) return
		if (level.priority < currentConfig.minLevel.priority) return
		val finalTag = buildTag(tag, currentConfig.tagPrefix)
		val decorated = decorateMessage(message, currentConfig)
		when (level) {
			Level.VERBOSE -> android.util.Log.v(finalTag, decorated, throwable)
			Level.DEBUG -> android.util.Log.d(finalTag, decorated, throwable)
			Level.INFO -> android.util.Log.i(finalTag, decorated, throwable)
			Level.WARN -> android.util.Log.w(finalTag, decorated, throwable)
			Level.ERROR -> android.util.Log.e(finalTag, decorated, throwable)
			Level.NONE -> Unit
		}
	}

	private fun buildTag(tag: String, prefix: String): String {
		if (prefix.isBlank()) return tag
		return "$prefix/$tag"
	}

	private fun decorateMessage(message: String, currentConfig: Config): String {
		val parts = ArrayList<String>(4)
		parts.add("sid=$sessionId")
		if (currentConfig.includeUptimeMs) {
			parts.add("t=${SystemClock.elapsedRealtime() - sessionStartElapsedMs}ms")
		}
		if (currentConfig.includeThread) {
			parts.add("thr=${Thread.currentThread().name}")
		}
		parts.add(message)
		return parts.joinToString(separator = " | ")
	}

	private fun newSessionId(): String = UUID.randomUUID().toString().takeLast(8)
}
