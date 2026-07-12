package com.example.lumisky.layers

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition

class ParticleLayer(
    definition: LayerDefinition,
    shaderSourceLoader: ShaderSourceLoader
) : ShaderLayer(definition, shaderSourceLoader)
