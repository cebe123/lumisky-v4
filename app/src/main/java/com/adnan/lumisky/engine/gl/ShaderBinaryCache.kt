/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Desteklenen cihazlarda glGetProgramBinary/glProgramBinary tabanlı disk cache katmanı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Desteklenen cihazlarda glGetProgramBinary/glProgramBinary tabanlı disk cache katmanı.
 */
package com.adnan.lumisky.engine.gl

import android.content.Context
import java.io.File

class ShaderBinaryCache(private val context: Context) {
    fun loadBinary(key: String): ByteArray? {
        val file = File(context.cacheDir, "shader_bin_$key")
        return if (file.exists() && file.isFile) {
            try {
                file.readBytes()
            } catch (e: Throwable) {
                null
            }
        } else {
            null
        }
    }

    fun saveBinary(key: String, binary: ByteArray) {
        try {
            val file = File(context.cacheDir, "shader_bin_$key")
            file.writeBytes(binary)
        } catch (e: Throwable) {
            // Ignore saving errors
        }
    }

    fun clear() {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("shader_bin_")) {
                    file.delete()
                }
            }
        } catch (e: Throwable) {
            // Ignore clearing errors
        }
    }
}
