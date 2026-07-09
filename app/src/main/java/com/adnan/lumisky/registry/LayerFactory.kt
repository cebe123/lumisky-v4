/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - CompiledLayerDefinition’dan RenderLayer instance üreten factory sözleşmesi.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: CompiledLayerDefinition’dan RenderLayer instance üreten factory sözleşmesi.
 */
package com.adnan.lumisky.registry

import com.adnan.lumisky.definition.LayerDefinition
import com.adnan.lumisky.layers.RenderLayer

interface LayerFactory {
    fun create(definition: LayerDefinition): RenderLayer
}
