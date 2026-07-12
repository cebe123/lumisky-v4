package com.example.lumisky.engine

internal object PreviewFrameRateCap {
    fun resolve(profileMaxFps: Int, displayMaxFps: Int): Int =
        minOf(profileMaxFps, displayMaxFps.coerceAtLeast(DEFAULT_FPS))

    private const val DEFAULT_FPS = 60
}
