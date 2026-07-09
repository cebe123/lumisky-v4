/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Runtime layer instance listesini, event dispatch’i ve release akışını yönetir.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Runtime layer instance listesini, event dispatch’i ve release akışını yönetir.
 */
package com.adnan.lumisky.engine

import com.adnan.lumisky.core.WallpaperEvent
import com.adnan.lumisky.engine.gl.GlResourceManager
import com.adnan.lumisky.layers.RenderLayer

class RuntimeScene(
    val id: String,
    val layers: List<RenderLayer>
) {
    fun onCreateGl(gl: GlResourceManager, context: RenderContext) {
        layers.forEach { it.onCreateGl(gl, context) }
    }

    fun onSurfaceChanged(context: RenderContext, width: Int, height: Int) {
        layers.forEach { it.onSurfaceChanged(context, width, height) }
    }

    fun onEvent(event: WallpaperEvent) {
        layers.forEach { it.onEvent(event) }
    }

    fun update(frame: MutableRenderFrameState) {
        layers.forEach { it.update(frame) }
    }

    fun onDestroyGl(gl: GlResourceManager) {
        layers.forEach { it.onDestroyGl(gl) }
    }
}
