package com.example.lumisky.ui.catalog

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogCardSemanticsContractTest {
    @Test
    fun wallpaperCardExposesOneButtonAndHidesDecorativeSemantics() {
        val source = File("src/main/java/com/example/lumisky/ui/catalog/WallpaperCatalogScreen.kt").readText()
        val cardSource = source.substringAfter("private fun WallpaperCard(")

        assertEquals(1, Regex("\\.clickable\\(").findAll(cardSource).count())
        assertTrue(cardSource.contains("role = Role.Button"))
        assertTrue(cardSource.contains("clearAndSetSemantics { }"))
    }
}
