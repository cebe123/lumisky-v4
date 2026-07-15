package com.example.lumisky.ui.catalog

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityWallpaperFlowContractTest {
    @Test
    fun catalogClickStagesCandidateAndOnlyCommitsAfterSystemPickerResultOk() {
        val source = File("src/main/java/com/example/lumisky/MainActivity.kt").readText()

        assertFalse(source.contains("SCREEN_PREVIEW_PREFIX"))
        assertFalse(source.contains("WallpaperPreviewScreen"))
        assertTrue(source.contains("prepareWallpaperForSet(id)"))
        assertTrue(source.contains("Intent.ACTION_WALLPAPER_CHANGED"))
        assertTrue(source.contains("settingsRepository.promotePreviewWallpaper()"))
        assertTrue(source.contains("cancelWallpaperPickerFlowIfActive()"))
        assertTrue(source.contains("LiveWallpaperSetLauncher.open(context, wallpaperPickerLauncher::launch)"))
        assertFalse(source.contains("selectWallpaperForSet(id)"))
        assertFalse(source.contains("completeWallpaperSet(result.resultCode == Activity.RESULT_OK)"))
    }
}
