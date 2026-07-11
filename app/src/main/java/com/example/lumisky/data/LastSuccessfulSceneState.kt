/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Data katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Data katmanı bileşeni.
 */
package com.example.lumisky.data

import com.example.lumisky.definition.QualityTier

data class LastSuccessfulSceneState(
    val wallpaperId: String,
    val definitionVersion: Int,
    val qualityTier: QualityTier,
    val timestampMillis: Long
)
