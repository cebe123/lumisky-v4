package com.example.lumisky.engine.gl

internal object EglSwapErrorPolicy {
    fun classify(success: Boolean, errorCode: Int, contextLostErrorCode: Int): EglSwapResult = when {
        success -> EglSwapResult.SUCCESS
        errorCode == contextLostErrorCode -> EglSwapResult.CONTEXT_LOST
        else -> EglSwapResult.SURFACE_LOST
    }
}
