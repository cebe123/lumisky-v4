/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Shader source metinlerini asset pack veya app assets içinden yükler.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Shader source metinlerini asset pack veya app assets içinden yükler.
 */
package com.adnan.lumisky.assets

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShaderSourceLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun load(assetPath: String): String {
        if (assetPath.isEmpty() || assetPath.endsWith("fullscreen_quad.vert")) {
            return ""
        }
        return try {
            val inputStream = context.assets.open(assetPath)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                sb.append(line).append("\n")
                line = reader.readLine()
            }
            reader.close()
            sb.toString()
        } catch (e: Throwable) {
            throw RuntimeException("Failed to load shader source from: $assetPath", e)
        }
    }
}
