/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - shaderRef veya custom shader path tanımı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: shaderRef veya custom shader path tanımı.
 */
package com.adnan.lumisky.definition

import kotlinx.serialization.Serializable

@Serializable
data class ShaderDefinition(
    val shaderRef: String,
    val vertexPath: String? = null,
    val fragmentPath: String? = null
)
