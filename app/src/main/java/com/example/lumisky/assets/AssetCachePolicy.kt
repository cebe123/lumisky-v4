/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Disk/RAM cache limitleri, LRU ve eviction kuralları.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Disk/RAM cache limitleri, LRU ve eviction kuralları.
 */
package com.example.lumisky.assets

object AssetCachePolicy {
    const val MAX_MEMORY_CACHE_BYTES = 64 * 1024 * 1024 // 64MB memory limit
    const val MAX_DISK_CACHE_BYTES = 256 * 1024 * 1024  // 256MB disk limit
}
