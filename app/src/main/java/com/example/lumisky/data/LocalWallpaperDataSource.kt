/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Local app assets ve starter wallpaper definition erişimi.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Local app assets ve starter wallpaper definition erişimi.
 */
package com.example.lumisky.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalWallpaperDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun loadFileFromAssets(fileName: String): String {
        return try {
            val stream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(stream))
            val sb = java.lang.StringBuilder()
            var line = reader.readLine()
            while (line != null) {
                sb.append(line).append("\n")
                line = reader.readLine()
            }
            reader.close()
            sb.toString()
        } catch (e: Throwable) {
            ""
        }
    }
}
