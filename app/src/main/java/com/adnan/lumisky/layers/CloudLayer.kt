/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - UV scroll ve çoklu texture slot destekli bulut layer.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: UV scroll ve çoklu texture slot destekli bulut layer.
 */
package com.adnan.lumisky.layers

import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.definition.LayerDefinition

class CloudLayer(
    definition: LayerDefinition,
    shaderSourceLoader: ShaderSourceLoader
) : ShaderLayer(definition, shaderSourceLoader)
