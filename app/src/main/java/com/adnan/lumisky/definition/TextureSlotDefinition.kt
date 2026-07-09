/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Texture slot path, sampler uniform, filter/wrap, UV/parallax/opacity ayarları.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Texture slot path, sampler uniform, filter/wrap, UV/parallax/opacity ayarları.
 */
package com.adnan.lumisky.definition

import kotlinx.serialization.Serializable

@Serializable
data class TextureSlotDefinition(
    val uniform: String,
    val path: String,
    val filter: String = "LINEAR",
    val wrapS: String = "CLAMP_TO_EDGE",
    val wrapT: String = "CLAMP_TO_EDGE",
    val parallaxFactor: List<Float> = listOf(0.0f, 0.0f),
    val uvScale: List<Float> = listOf(1.0f, 1.0f),
    val opacity: Float = 1.0f
)
