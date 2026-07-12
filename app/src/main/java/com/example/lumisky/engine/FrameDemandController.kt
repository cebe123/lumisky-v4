package com.example.lumisky.engine

enum class FrameDemandReason {
    INITIAL_FRAME, SURFACE_CHANGED, SCENE_SWITCH, PARALLAX_CHANGED, TOUCH_CHANGED,
    TIME_TICK, VIDEO_FRAME, EVENT_ANIMATION, QUALITY_CHANGED, LOCATION_CHANGED
}

class FrameDemandController {
    private var dirtyMask = 0L
    private var deadlineNanos = Long.MAX_VALUE

    fun request(reason: FrameDemandReason, deadlineNanos: Long = Long.MIN_VALUE) {
        dirtyMask = dirtyMask or (1L shl reason.ordinal)
        if (deadlineNanos != Long.MIN_VALUE) this.deadlineNanos = minOf(this.deadlineNanos, deadlineNanos)
    }

    fun hasDemand(nowNanos: Long): Boolean = dirtyMask != 0L || nowNanos >= deadlineNanos

    fun onFramePresented() {
        dirtyMask = 0L
        deadlineNanos = Long.MAX_VALUE
    }
}
