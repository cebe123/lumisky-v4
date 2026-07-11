/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - NotInstalled, Pending, Downloading, Installed, Failed gibi PAD state modeli.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: NotInstalled, Pending, Downloading, Installed, Failed gibi PAD state modeli.
 */
package com.example.lumisky.assets

sealed interface AssetPackState {
    object NotRequired : AssetPackState
    object NotInstalled : AssetPackState
    object Pending : AssetPackState
    data class Downloading(val progress: Float) : AssetPackState
    data class Transferring(val progress: Float) : AssetPackState
    object Installed : AssetPackState
    data class Failed(val reason: AssetPackFailureReason, val errorCode: Int? = null) : AssetPackState
    object RequiresWifiConfirmation : AssetPackState
    object Canceled : AssetPackState
    object Removed : AssetPackState
}

enum class AssetPackFailureReason {
    NETWORK,
    STORAGE,
    PLAY_STORE,
    CORRUPT,
    UNKNOWN
}
