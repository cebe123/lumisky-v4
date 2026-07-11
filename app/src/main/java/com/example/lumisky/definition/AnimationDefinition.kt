/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - UV_SCROLL, PULSE, PATH_FOLLOW gibi layer animasyon tanımı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: UV_SCROLL, PULSE, PATH_FOLLOW gibi layer animasyon tanımı.
 */
package com.example.lumisky.definition

import kotlinx.serialization.Serializable

@Serializable
data class AnimationDefinition(
    val type: String, // UV_SCROLL, PULSE, PATH_FOLLOW
    val durationMs: Long = 1000L,
    val loop: Boolean = true,
    val parameters: Map<String, Float> = emptyMap()
)
