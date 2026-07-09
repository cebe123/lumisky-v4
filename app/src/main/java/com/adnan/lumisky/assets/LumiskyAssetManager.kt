/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Definition, texture, shader, video, thumbnail asset path çözüm katmanı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Definition, texture, shader, video, thumbnail asset path çözüm katmanı.
 */
package com.adnan.lumisky.assets

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LumiskyAssetManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val assetPackResolver: AssetPackResolver
) {
    fun openAsset(path: String, assetPack: String? = null): InputStream {
        return if (assetPack != null) {
            val resolvedPath = assetPackResolver.resolve(assetPack, path)
            if (resolvedPath != null && !resolvedPath.startsWith("wallpapers/")) {
                java.io.FileInputStream(resolvedPath)
            } else {
                context.assets.open(path)
            }
        } else {
            context.assets.open(path)
        }
    }

    fun getAssetPath(path: String, assetPack: String? = null): String {
        if (assetPack != null) {
            val resolved = assetPackResolver.resolve(assetPack, path)
            if (resolved != null) return resolved
        }
        return path
    }
}
