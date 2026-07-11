package com.example.lumisky.assets

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class IndependentStarsEffectContractTest {
    @Test
    fun nightWallpapersDeclareIndependentStarsLayer() {
        val wallpaperIds = listOf("pixel_forest", "classic_sun", "anime_sakura", "tablo")

        wallpaperIds.forEach { id ->
            val source = File("src/main/assets/wallpapers/$id.json").readText()
            assertTrue("$id must declare StarsLayer", source.contains("\"type\": \"StarsLayer\""))
            assertTrue("$id must use common stars shader", source.contains("\"shaderRef\": \"shaders/common/stars.glsl\""))
        }
    }
}
