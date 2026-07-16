package com.example.lumisky.engine

import com.example.lumisky.definition.WallpaperDefinition
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CentralParallaxWallpaperContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun castleUsesFullLayerOverrideAndKeepsInternalDepthFactors() {
        val definition = json.decodeFromString<WallpaperDefinition>(
            File("src/main/assets/wallpapers/sky.json").readText()
        )
        val shader = File("src/main/assets/wallpapers/sky/fragment.glsl").readText()

        assertTrue(definition.parallax?.enabled == true)
        assertEquals(1f, definition.layers.single().parallax?.depth ?: -1f, 0.0001f)
        assertTrue(shader.contains("ustBulutParallax"))
        assertTrue(shader.contains("altBulutParallax"))
        assertTrue(shader.contains("castleParallax"))
    }

    @Test
    fun samuraiLayersRemainAutomaticAndOrderedBackToFront() {
        val definition = json.decodeFromString<WallpaperDefinition>(
            File("src/main/assets/wallpapers/samurai_fuji_twilight.json").readText()
        )

        assertTrue(definition.parallax?.enabled == true)
        assertTrue(definition.layers.zipWithNext().all { (back, front) -> back.zIndex < front.zIndex })
        definition.layers.forEach { assertNull(it.parallax) }
    }

    @Test
    fun commonTextureShaderKeepsShiftedUvsInsideTextureBounds() {
        val shader = File("src/main/assets/shaders/common/texture2d.frag").readText()

        assertTrue(shader.contains("2.0 * abs(u_ParallaxOffset)"))
        assertTrue(shader.contains("* safeScale"))
    }
}
