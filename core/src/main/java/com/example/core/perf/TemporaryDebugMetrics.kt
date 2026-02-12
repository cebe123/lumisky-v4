package com.example.core.perf

data class DebugMetricLine(
	val tag: String,
	val summary: String,
	val updatedAtMs: Long
)

object TemporaryDebugMetrics {
	private val byTag = LinkedHashMap<String, DebugMetricLine>()

	@Synchronized
	fun publish(line: DebugMetricLine) {
		byTag[line.tag] = line
	}

	@Synchronized
	fun snapshot(): List<DebugMetricLine> {
		return byTag.values.sortedByDescending { it.updatedAtMs }
	}
}
