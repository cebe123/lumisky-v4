/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Telemetry’ye gönderilecek fallback event modelleri.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Telemetry’ye gönderilecek fallback event modelleri.
 */
package com.example.lumisky.telemetry

sealed interface RenderFallbackEvent {
    data class ShaderFallback(val layerId: String, val error: String) : RenderFallbackEvent
    data class TextureFallback(val path: String, val error: String) : RenderFallbackEvent
    data class GlInitFallback(val error: String) : RenderFallbackEvent
}
