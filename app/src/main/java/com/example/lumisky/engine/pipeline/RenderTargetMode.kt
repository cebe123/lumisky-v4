/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - DIRECT, OFFSCREEN_FBO, CACHED_TEXTURE, POST_PROCESS hedeflerini tanımlar.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: DIRECT, OFFSCREEN_FBO, CACHED_TEXTURE, POST_PROCESS hedeflerini tanımlar.
 */
package com.example.lumisky.engine.pipeline

enum class RenderTargetMode {
    DIRECT,
    OFFSCREEN_FBO,
    CACHED_TEXTURE,
    POST_PROCESS
}
