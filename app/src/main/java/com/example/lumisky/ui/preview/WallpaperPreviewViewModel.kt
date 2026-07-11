/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Preview açma, asset hazırlık ve set wallpaper aksiyonlarını yönetir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Preview açma, asset hazırlık ve set wallpaper aksiyonlarını yönetir.
 */
package com.example.lumisky.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lumisky.data.WallpaperRepository
import com.example.lumisky.definition.WallpaperDefinition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperPreviewViewModel @Inject constructor(
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _definition = MutableStateFlow<WallpaperDefinition?>(null)
    val definition: StateFlow<WallpaperDefinition?> = _definition.asStateFlow()
    val previewTimeSimulation: StateFlow<Boolean> = repository.settings.previewTimeSimulation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun loadWallpaper(id: String) {
        viewModelScope.launch {
            _definition.value = repository.getDefinition(id)
        }
    }

    fun setWallpaper(id: String) {
        viewModelScope.launch {
            repository.settings.setSelectedWallpaperId(id)
        }
    }

    suspend fun setWallpaperNow(id: String) {
        repository.settings.setSelectedWallpaperId(id)
    }
}
