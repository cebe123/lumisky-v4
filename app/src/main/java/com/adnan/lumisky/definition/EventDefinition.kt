/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Event trigger ve targetLayer/action tanımı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Event trigger ve targetLayer/action tanımı.
 */
package com.adnan.lumisky.definition

import kotlinx.serialization.Serializable

@Serializable
data class EventDefinition(
    val trigger: String, // ON_USER_PRESENT, ON_SUNSET, etc.
    val action: String,  // PLAY_ANIMATION, SET_UNIFORM, etc.
    val targetLayer: String,
    val then: String? = null
)
