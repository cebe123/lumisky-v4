/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Ay pozisyonu/fazı ve gece görünürlüğü için layer.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Ay pozisyonu/fazı ve gece görünürlüğü için layer.
 */
package com.adnan.lumisky.layers

import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.definition.LayerDefinition

class MoonLayer(
    definition: LayerDefinition,
    shaderSourceLoader: ShaderSourceLoader
) : ShaderLayer(definition, shaderSourceLoader)
