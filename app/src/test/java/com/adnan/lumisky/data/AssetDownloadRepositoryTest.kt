package com.adnan.lumisky.data

import com.adnan.lumisky.assets.AssetPackState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AssetDownloadRepositoryTest {

    @Test
    fun blankPackNameMeansAssetPackIsNotRequired() = runBlocking {
        val repository = AssetDownloadRepository()

        assertEquals(AssetPackState.NotRequired, repository.getDownloadState("").first())
    }

    @Test
    fun nonBlankPackNameIsNotInstalledUntilResolverConfirmsIt() = runBlocking {
        val repository = AssetDownloadRepository()

        assertEquals(AssetPackState.NotInstalled, repository.getDownloadState("premium_pack").first())
    }
}
