/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Catalog UI state, premium/download kararları ve filtreleme akışları.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Catalog UI state, premium/download kararları ve filtreleme akışları.
 */
package com.example.lumisky.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumisky.data.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperCatalogViewModel @Inject constructor(
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<WallpaperCatalogUiItem>>(emptyList())
    val items: StateFlow<List<WallpaperCatalogUiItem>> = _items.asStateFlow()

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            val catalog = repository.getCatalog()
            val uiItems = catalog.wallpapers.map { item ->
                WallpaperCatalogUiItem(
                    id = item.id,
                    name = item.name,
                    category = item.category,
                    thumbnail = item.thumbnail,
                    isPremium = item.isPremium,
                    isDownloaded = true
                )
            }
            _items.value = uiItems
        }
    }

    suspend fun selectWallpaperForSet(id: String) {
        repository.settings.setSelectedWallpaperId(id)
    }
}
