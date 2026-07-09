/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - ON_USER_PRESENT, ON_SUNSET gibi eventleri layer aksiyonlarına dönüştürür.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: ON_USER_PRESENT, ON_SUNSET gibi eventleri layer aksiyonlarına dönüştürür.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.core.WallpaperEvent
import com.adnan.lumisky.definition.WallpaperDefinition
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventTriggerSystem @Inject constructor() {
    fun handleEvent(event: WallpaperEvent, definition: WallpaperDefinition?, scene: RuntimeScene) {
        val eventDefList = definition?.events ?: return
        val triggerStr = when (event) {
            is WallpaperEvent.UserPresent -> "ON_USER_PRESENT"
            is WallpaperEvent.ScreenOn -> "ON_SCREEN_ON"
            is WallpaperEvent.ScreenOff -> "ON_SCREEN_OFF"
            else -> return
        }

        eventDefList.forEach { eventDef ->
            if (eventDef.trigger == triggerStr) {
                scene.layers.forEach { layer ->
                    if (layer.id == eventDef.targetLayer) {
                        layer.onEvent(event)
                    }
                }
            }
        }
    }
}
