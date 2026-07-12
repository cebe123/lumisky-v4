package com.example.lumisky.layers

import android.content.Context
import android.net.Uri
import android.view.Surface
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VideoMediaController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var player: ExoPlayer? = null
    private var source: String? = null
    private var playbackAllowed = false
    var onPlaybackFailure: (() -> Unit)? = null

    fun attach(surface: Surface, assetPath: String) {
        val activePlayer = player ?: createPlayer().also { player = it }
        activePlayer.setVideoSurface(surface)
        if (source != assetPath) {
            source = assetPath
            activePlayer.setMediaItem(MediaItem.fromUri(assetUri(assetPath)))
            activePlayer.prepare()
        }
        activePlayer.playWhenReady = playbackAllowed
    }

    fun setPlaybackAllowed(allowed: Boolean) {
        if (playbackAllowed == allowed) return
        playbackAllowed = allowed
        player?.playWhenReady = allowed
    }

    fun clearSurface() {
        player?.clearVideoSurface()
    }

    fun release() {
        player?.release()
        player = null
        source = null
        playbackAllowed = false
    }

    private fun createPlayer(): ExoPlayer = ExoPlayer.Builder(context).build().apply {
        volume = 0f
        repeatMode = Player.REPEAT_MODE_ONE
        trackSelectionParameters = trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
            .build()
        addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playWhenReady = false
                onPlaybackFailure?.invoke()
            }
        })
    }

    private fun assetUri(path: String): Uri = when {
        "://" in path -> Uri.parse(path)
        else -> Uri.parse("asset:///${path.trimStart('/')}")
    }
}
