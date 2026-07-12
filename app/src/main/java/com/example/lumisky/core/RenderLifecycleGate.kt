package com.example.lumisky.core

object RenderLifecycleGate {
    fun canRender(visible: Boolean, hasSurface: Boolean): Boolean = visible && hasSurface
    fun canRunSensor(visible: Boolean): Boolean = visible
    fun canRunVideo(visible: Boolean, playbackEnabled: Boolean): Boolean = visible && playbackEnabled
    fun canPublishCallback(visible: Boolean): Boolean = visible
}
