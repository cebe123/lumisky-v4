package com.example.lumisky.engine.gl

internal object TexturePendingWorkPolicy {
    fun hasRenderBlockingWork(
        requestedKeys: Set<String>,
        decodingKeys: Set<String>,
        preparedKeys: Set<String>
    ): Boolean = requestedKeys.any { it in decodingKeys || it in preparedKeys }
}
