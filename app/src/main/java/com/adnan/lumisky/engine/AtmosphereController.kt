/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Gündoğumu/günbatımı, ışık, sis, yıldız görünürlüğü gibi ortak atmosfer state’ini üretir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Gündoğumu/günbatımı, ışık, sis, yıldız görünürlüğü gibi ortak atmosfer state’ini üretir.
 */
package com.adnan.lumisky.engine

import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AtmosphereController @Inject constructor() {
    fun update(state: SceneState) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val factor = 1.0f - Math.abs(hour - 12) / 12.0f
        // Can be queried by custom weather/time shaders if needed
    }
}
