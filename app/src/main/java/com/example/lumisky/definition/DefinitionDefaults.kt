/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Eksik optional alanlar için default üretir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Eksik optional alanlar için default üretir.
 */
package com.example.lumisky.definition

object DefinitionDefaults {
    const val DEFAULT_BLEND_MODE = "NONE"
    const val DEFAULT_RENDER_TARGET = "DIRECT"
    const val DEFAULT_RENDER_PASS = "BACKGROUND"
    
    val DEFAULT_FRAME_POLICY = LayerFramePolicyDefinition(
        mode = "MATCH_SCENE",
        cacheMode = "NONE"
    )
    
    val DEFAULT_PARALLAX = LayerParallaxDefinition(
        factorX = 0.0f,
        factorY = 0.0f,
        depth = 0.0f
    )
}
