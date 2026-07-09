/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Starfield shader/texture layer; düşük FPS twinkle destekler.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Starfield shader/texture layer; düşük FPS twinkle destekler.
 */
package com.adnan.lumisky.layers

import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.definition.LayerDefinition

class StarsLayer(
    definition: LayerDefinition,
    shaderSourceLoader: ShaderSourceLoader
) : ShaderLayer(definition, shaderSourceLoader)
