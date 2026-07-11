/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Liste item için stable, hafif UI modeli.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Liste item için stable, hafif UI modeli.
 */
package com.example.lumisky.ui.catalog

data class WallpaperCatalogUiItem(
    val id: String,
    val name: String,
    val category: String,
    val thumbnail: String,
    val isPremium: Boolean,
    val isDownloaded: Boolean
)
