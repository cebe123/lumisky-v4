package com.adnan.lumisky.registry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EffectRegistryTest {

    @Test
    fun registryContainsDefaultRuntimeEffects() {
        val registry = EffectRegistry()

        assertNotNull(registry.get("fog"))
        assertNotNull(registry.get("parallax"))
        assertNotNull(registry.get("color_grade"))
    }

    @Test
    fun registerOverridesExistingEffectByName() {
        val registry = EffectRegistry()
        registry.register(
            "fog",
            EffectConfig(name = "fog", type = "FogLayer", defaultUniforms = mapOf("density" to 0.35f))
        )

        assertEquals(0.35f, registry.get("fog")?.defaultUniforms?.get("density"))
    }
}
