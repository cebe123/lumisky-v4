/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - UV scroll ve çoklu texture slot destekli bulut layer.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: UV scroll ve çoklu texture slot destekli bulut layer.
 */
package com.example.lumisky.layers

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition

class CloudLayer(
    definition: LayerDefinition,
    shaderSourceLoader: ShaderSourceLoader
) : ShaderLayer(definition, shaderSourceLoader)
