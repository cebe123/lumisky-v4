package com.adnan.lumisky.assets

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class CelestialShaderContractTest {
    @Test
    fun celestialMasksDoNotUseReversedSmoothstepEdges() {
        val shaders = File("src/main/assets/wallpapers")
            .walkTopDown()
            .filter { it.isFile && it.extension == "glsl" }
            .toList()

        shaders.forEach { shaderFile ->
            val shader = shaderFile.readText()
            assertFalse("${shaderFile.path} uses reversed circle smoothstep", shader.contains("smoothstep(radius, radius * 0.8"))
            assertFalse("${shaderFile.path} uses reversed circle smoothstep", shader.contains("smoothstep(radius, radius * 0.82"))
            assertFalse("${shaderFile.path} uses reversed radius smoothstep", shader.contains("smoothstep(radius, radius -"))
            assertFalse("${shaderFile.path} uses reversed glow smoothstep", shader.contains("smoothstep(radius +"))
            assertFalse("${shaderFile.path} uses reversed alpha smoothstep", shader.contains("smoothstep(0.085, 0.02"))
            assertFalse("${shaderFile.path} uses reversed dynamic smoothstep", shader.contains("smoothstep(mix("))
            assertFalse("${shaderFile.path} uses reversed fog smoothstep", shader.contains("smoothstep(1.0, 0."))
            assertFalse("${shaderFile.path} uses reversed phase smoothstep", shader.contains("smoothstep(0.5, 0.3"))
            assertFalse("${shaderFile.path} uses reversed ocean smoothstep", shader.contains("smoothstep(0.1, 0.0"))
            assertFalse("${shaderFile.path} uses reversed ocean smoothstep", shader.contains("smoothstep(1.0, 0.6"))
            assertFalse("${shaderFile.path} uses reversed boost smoothstep", shader.contains("smoothstep(1.0, 0.7"))
        }
    }
}
