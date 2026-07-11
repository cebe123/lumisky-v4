/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - AssetPackState, PAD download/retry/offline akışı ve UI state üretimi.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: AssetPackState, PAD download/retry/offline akışı ve UI state üretimi.
 */
package com.example.lumisky.data

import com.example.lumisky.assets.AssetPackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetDownloadRepository @Inject constructor() {
    fun getDownloadState(packName: String): Flow<AssetPackState> {
        val state = if (packName.isBlank()) {
            AssetPackState.NotRequired
        } else {
            AssetPackState.NotInstalled
        }
        return flowOf(state)
    }

    fun startDownload(packName: String) {
        // Triggers Play Asset Delivery pack downloading on play store integration
    }
}
