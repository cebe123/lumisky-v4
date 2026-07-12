package com.example.lumisky.engine.gl

internal class TextureUploadBudget(
    private val maxUploadsPerFrame: Int
) {
    private var remainingUploads = maxUploadsPerFrame

    fun beginFrame() {
        remainingUploads = maxUploadsPerFrame
    }

    fun tryAcquireUpload(): Boolean {
        if (remainingUploads <= 0) return false
        remainingUploads--
        return true
    }
}
