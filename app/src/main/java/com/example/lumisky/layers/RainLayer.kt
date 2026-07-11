/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Particle/shader tabanlı yağmur layer.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Particle/shader tabanlı yağmur layer.
 */
package com.example.lumisky.layers

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition

class RainLayer(
    definition: LayerDefinition,
    shaderSourceLoader: ShaderSourceLoader
) : ShaderLayer(definition, shaderSourceLoader)
