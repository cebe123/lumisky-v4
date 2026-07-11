/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Layer oluşturma sonucunu crash atmadan typed result olarak döndürür.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Layer oluşturma sonucunu crash atmadan typed result olarak döndürür.
 */
package com.example.lumisky.registry

import com.example.lumisky.layers.RenderLayer

sealed interface LayerCreateResult {
    data class Created(val layer: RenderLayer) : LayerCreateResult
    
    data class UnknownType(
        val layerId: String,
        val type: String,
        val required: Boolean
    ) : LayerCreateResult
    
    data class CreateFailed(
        val layerId: String,
        val type: String,
        val required: Boolean,
        val cause: Throwable
    ) : LayerCreateResult
}
