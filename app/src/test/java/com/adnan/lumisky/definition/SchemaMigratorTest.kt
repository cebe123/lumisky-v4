package com.example.lumisky.definition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemaMigratorTest {
    private val parser = WallpaperDefinitionParser(SchemaMigrator())

    @Test
    fun migratesLegacyWallpaperConfigIntoV5ShaderLayerDefinition() {
        val legacyJson = """
            {
              "id": "sky",
              "name": "Castle Fantasy HD",
              "textures": {
                "backgroundTexture": "wallpapers/sky/castle.png"
              },
              "shader": {
                "fragmentAssetPath": "wallpapers/sky/fragment.glsl"
              },
              "capabilities": {
                "dynamicMotion": true,
                "locationAwareLighting": false
              }
            }
        """.trimIndent()

        val result = parser.parseWallpaper(legacyJson)

        assertTrue("Expected successful parse, got $result", result is WallpaperParseResult.Success)
        val definition = (result as WallpaperParseResult.Success).definition
        assertEquals(5, definition.schemaVersion)
        assertEquals("sky", definition.id)
        assertEquals("wallpapers/sky/thumbnail.webp", definition.preview.thumbnail)
        assertEquals("THUMBNAIL", definition.preview.cardMode)
        assertEquals(true, definition.capabilities.parallax)
        assertEquals(false, definition.capabilities.locationAware)
        assertEquals(1, definition.layers.size)
        assertEquals("ShaderLayer", definition.layers.single().type)
        assertEquals("wallpapers/sky/fragment.glsl", definition.layers.single().shaderRef)
        assertEquals("wallpapers/sky/castle.png", definition.layers.single().textures.single().path)
    }
}
