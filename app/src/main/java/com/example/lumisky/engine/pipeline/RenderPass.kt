/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - BACKGROUND, OPAQUE, TRANSPARENT, POST_PROCESS, OVERLAY render geçişlerini tanımlar.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: BACKGROUND, OPAQUE, TRANSPARENT, POST_PROCESS, OVERLAY render geçişlerini tanımlar.
 */
package com.example.lumisky.engine.pipeline

enum class RenderPass {
    BACKGROUND,
    OPAQUE,
    ALPHA_TESTED,
    TRANSPARENT,
    POST_PROCESS,
    OVERLAY
}
