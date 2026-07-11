/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - zIndex, RenderPass ve BlendMode’a göre layer çizim sırasını yönetir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: zIndex, RenderPass ve BlendMode’a göre layer çizim sırasını yönetir.
 */
package com.example.lumisky.engine.pipeline

import com.example.lumisky.layers.RenderLayer

object LayerComposer {
    fun compose(layers: List<RenderLayer>): List<RenderLayer> {
        return layers.sortedWith(
            compareBy<RenderLayer> { it.renderPass.ordinal }
                .thenBy { it.zIndex }
        )
    }
}
