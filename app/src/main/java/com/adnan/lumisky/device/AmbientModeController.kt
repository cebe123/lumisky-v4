/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - AOD/ambient/low-power durumda render loop ve burn-in policy kontrolü.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: AOD/ambient/low-power durumda render loop ve burn-in policy kontrolü.
 */
package com.adnan.lumisky.device

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmbientModeController @Inject constructor() {
    var isAmbientMode: Boolean = false
    
    fun setAmbient(ambient: Boolean) {
        isAmbientMode = ambient
    }
    
    fun shouldRender(): Boolean {
        return !isAmbientMode
    }
}
