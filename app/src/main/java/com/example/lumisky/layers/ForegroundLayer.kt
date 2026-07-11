/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Statik veya parallax destekli ön plan image layer.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Statik veya parallax destekli ön plan image layer.
 */
package com.example.lumisky.layers

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition

class ForegroundLayer(
    definition: LayerDefinition,
    shaderSourceLoader: ShaderSourceLoader
) : TextureLayer(definition, shaderSourceLoader)
