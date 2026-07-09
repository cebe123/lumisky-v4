/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Düşük FPS cached FBO ile sis/haze layer.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Düşük FPS cached FBO ile sis/haze layer.
 */
package com.adnan.lumisky.layers

import com.adnan.lumisky.assets.ShaderSourceLoader
import com.adnan.lumisky.definition.LayerDefinition

class FogLayer(
    definition: LayerDefinition,
    shaderSourceLoader: ShaderSourceLoader
) : ShaderLayer(definition, shaderSourceLoader)
