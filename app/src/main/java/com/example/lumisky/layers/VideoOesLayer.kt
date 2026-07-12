/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - SurfaceTexture + GL_TEXTURE_EXTERNAL_OES ile video layer pipeline.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: SurfaceTexture + GL_TEXTURE_EXTERNAL_OES ile video layer pipeline.
 */
package com.example.lumisky.layers

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.view.Surface
import com.example.lumisky.core.RenderLifecycleGate
import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.core.WallpaperEvent
import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.engine.MutableRenderFrameState
import com.example.lumisky.engine.FrameDemandReason
import com.example.lumisky.engine.RenderContext
import com.example.lumisky.engine.gl.GlProgram
import com.example.lumisky.engine.gl.GlResourceManager
import java.util.concurrent.atomic.AtomicBoolean

class VideoOesLayer(
    definition: LayerDefinition,
    private val shaderSourceLoader: ShaderSourceLoader,
    private val mediaController: VideoMediaController
) : BaseLayer(definition) {
    private var program: GlProgram? = null
    private var oesTexId = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private val transformMatrix = FloatArray(16) { if (it % 5 == 0) 1.0f else 0.0f } // Identity matrix
    private val frameAvailable = AtomicBoolean(false)
    private val playbackFailed = AtomicBoolean(false)
    private var playbackAllowed = false

    var onSurfaceReady: ((Surface) -> Unit)? = null

    init {
        mediaController.onPlaybackFailure = {
            playbackFailed.set(true)
            mediaController.setPlaybackAllowed(false)
        }
    }

    override fun onCreateGl(gl: GlResourceManager, context: RenderContext) {
        program = gl.programs.get("common.video.oes", shaderSourceLoader)

        // Generate external OES texture
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        oesTexId = textures[0]
        
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTexId)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

        surfaceTexture = SurfaceTexture(oesTexId).apply {
            setOnFrameAvailableListener {
                frameAvailable.set(true)
            }
        }
        surface = Surface(surfaceTexture)
        onSurfaceReady?.invoke(surface!!)
        val source = definition.source
        if (source.isNullOrBlank()) {
            playbackFailed.set(true)
        } else {
            playbackFailed.set(false)
            mediaController.attach(surface!!, source)
        }
    }

    override fun update(frame: MutableRenderFrameState) {
        val canPlay = RenderLifecycleGate.canRunVideo(frame.isVisible, frame.videoPlaybackEnabled) &&
            !playbackFailed.get()
        if (playbackAllowed != canPlay) {
            playbackAllowed = canPlay
            mediaController.setPlaybackAllowed(canPlay)
        }
        if (!canPlay) return
        if (frameAvailable.getAndSet(false)) {
            try {
                surfaceTexture?.updateTexImage()
                surfaceTexture?.getTransformMatrix(transformMatrix)
            } catch (e: Throwable) {
                // Occurs during context loss or surface destruction
            }
        }
    }

    override fun render(frame: MutableRenderFrameState) {
        if (!RenderLifecycleGate.canRunVideo(frame.isVisible, frame.videoPlaybackEnabled)) return
        if (playbackFailed.get()) return
        val activeProgram = program ?: return
        if (oesTexId == 0) return

        activeProgram.use()
        
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTexId)
        activeProgram.setUniform("u_VideoTexture", 0)
        activeProgram.setUniformMatrix("u_VideoTransform", transformMatrix)

        blendMode.apply()
        frame.gl.meshes.drawQuad()
        
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }

    override fun onDestroyGl(gl: GlResourceManager) {
        releaseVideoResources()
        mediaController.release()
        if (oesTexId != 0) {
            val textures = intArrayOf(oesTexId)
            GLES30.glDeleteTextures(1, textures, 0)
            oesTexId = 0
        }
    }

    override fun onContextLost() {
        releaseVideoResources()
        mediaController.clearSurface()
        oesTexId = 0
    }

    override fun onEvent(event: WallpaperEvent) {
        if (event === WallpaperEvent.ScreenOff) {
            playbackAllowed = false
            mediaController.setPlaybackAllowed(false)
        }
    }

    override val hasPendingFrameDemand: Boolean
        get() = frameAvailable.get()

    override fun pendingFrameDemandReason(): FrameDemandReason? =
        if (frameAvailable.get()) FrameDemandReason.VIDEO_FRAME else null

    private fun releaseVideoResources() {
        frameAvailable.set(false)
        surface?.release()
        surface = null
        surfaceTexture?.release()
        surfaceTexture = null
    }
}
