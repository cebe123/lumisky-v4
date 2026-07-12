package com.example.lumisky.buildlogic

import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WallpaperPackCompilerTest {
    @Test
    fun outputIsDeterministicAndSorted() {
        val firstRoot = Files.createTempDirectory("pack-a")
        firstRoot.resolve("z.txt").writeText("z")
        firstRoot.resolve("nested").createDirectories()
        firstRoot.resolve("nested/a.txt").writeText("a")
        val secondRoot = Files.createTempDirectory("pack-b")
        secondRoot.resolve("nested").createDirectories()
        secondRoot.resolve("nested/a.txt").writeText("a")
        secondRoot.resolve("z.txt").writeText("z")
        val firstOutput = Files.createTempDirectory("out-a").toFile()
        val secondOutput = Files.createTempDirectory("out-b").toFile()

        WallpaperPackCompiler.compile(firstRoot.toFile(), firstOutput)
        WallpaperPackCompiler.compile(secondRoot.toFile(), secondOutput)

        assertEquals(
            firstOutput.resolve("content-manifest.json").readText(),
            secondOutput.resolve("content-manifest.json").readText()
        )
        assertTrue(firstOutput.resolve("content-hashes.txt").readLines().first().startsWith("nested/a.txt "))
    }

    @Test
    fun contentHashChangesWhenAssetChanges() {
        val root = Files.createTempDirectory("pack")
        root.resolve("asset.bin").writeText("before")
        val firstOutput = Files.createTempDirectory("out-before").toFile()
        val secondOutput = Files.createTempDirectory("out-after").toFile()

        WallpaperPackCompiler.compile(root.toFile(), firstOutput)
        root.resolve("asset.bin").writeText("after")
        WallpaperPackCompiler.compile(root.toFile(), secondOutput)

        assertNotEquals(
            firstOutput.resolve("content-manifest.json").readText(),
            secondOutput.resolve("content-manifest.json").readText()
        )
    }
}
