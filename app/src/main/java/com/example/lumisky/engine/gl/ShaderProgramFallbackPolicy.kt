package com.example.lumisky.engine.gl

internal object ShaderProgramFallbackPolicy {
    fun requireUsable(primaryProgramId: Int, fallbackProgramId: Int = 0): Int = when {
        primaryProgramId != 0 -> primaryProgramId
        fallbackProgramId != 0 -> fallbackProgramId
        else -> throw IllegalStateException("Neither shader nor its safe fallback linked")
    }
}
