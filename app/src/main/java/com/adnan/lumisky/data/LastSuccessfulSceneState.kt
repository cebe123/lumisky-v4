/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Data katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Data katmanı bileşeni.
 */
package com.adnan.lumisky.data

import com.adnan.lumisky.definition.QualityTier

data class LastSuccessfulSceneState(
    val wallpaperId: String,
    val definitionVersion: Int,
    val qualityTier: QualityTier,
    val timestampMillis: Long
)
