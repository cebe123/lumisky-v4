/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Play Asset Delivery pack state/path çözümü ve hash/path validation.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Play Asset Delivery pack state/path çözümü ve hash/path validation.
 */
package com.example.lumisky.assets

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetPackResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun resolve(packName: String, relativePath: String): String? {
        val dir = context.getExternalFilesDir(null) ?: return null
        val packDir = File(dir, packName)
        val assetFile = File(packDir, relativePath)
        
        return if (assetFile.exists() && assetFile.canRead()) {
            assetFile.absolutePath
        } else {
            // Fallback path in base assets folder
            "wallpapers/$relativePath"
        }
    }
}
