package com.example.lumisky.core

import org.junit.Assert.assertEquals
import org.junit.Test

class WallpaperApplyPolicyTest {

    @Test
    fun switchesSceneWithoutRecreatingSurfaceWhenGlSurfaceIsAlive() {
        assertEquals(
            WallpaperApplyAction.SWITCH_SCENE,
            WallpaperApplyPolicy.resolve(
                hasEngineSurface = true,
                hasGlSurface = true,
                sameWallpaperAlreadyApplied = false
            )
        )
    }

    @Test
    fun storesPendingSceneWhenNoSurfaceExistsYet() {
        assertEquals(
            WallpaperApplyAction.STORE_PENDING_SCENE,
            WallpaperApplyPolicy.resolve(
                hasEngineSurface = false,
                hasGlSurface = false,
                sameWallpaperAlreadyApplied = false
            )
        )
    }

    @Test
    fun skipsExactSameWallpaperWhenSceneAlreadyExists() {
        assertEquals(
            WallpaperApplyAction.SKIP,
            WallpaperApplyPolicy.resolve(
                hasEngineSurface = true,
                hasGlSurface = true,
                sameWallpaperAlreadyApplied = true
            )
        )
    }
}
