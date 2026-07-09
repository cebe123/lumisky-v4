/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - thumbnail, cardMode, fullscreenMode preview davranışları.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: thumbnail, cardMode, fullscreenMode preview davranışları.
 */
package com.adnan.lumisky.definition

import kotlinx.serialization.Serializable

@Serializable
data class PreviewDefinition(
    val thumbnail: String = "",
    val cardMode: String = "THUMBNAIL",
    val fullscreenMode: String = "FAST_TIME_SIMULATION"
)
