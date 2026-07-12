package com.example.lumisky.core

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

class RenderCommandMailbox {
    private val fifo = ConcurrentLinkedQueue<RenderCommand>()
    private val resize = AtomicReference<RenderCommand.ResizeSurface?>(null)
    private val visibility = AtomicReference<RenderCommand.SetVisibility?>(null)
    private val parallax = AtomicReference<RenderCommand.SetParallax?>(null)
    private val touch = AtomicReference<RenderCommand.SetTouch?>(null)
    private val runtimePolicy = AtomicReference<RenderCommand.SetRuntimePolicy?>(null)
    private val powerPolicy = AtomicReference<RenderCommand.SetPowerPolicy?>(null)
    private val daylight = AtomicReference<RenderCommand.SetDaylight?>(null)

    fun offer(command: RenderCommand) {
        when (command) {
            is RenderCommand.ResizeSurface -> resize.set(command)
            is RenderCommand.SetVisibility -> visibility.set(command)
            is RenderCommand.SetParallax -> parallax.set(command)
            is RenderCommand.SetTouch -> touch.set(command)
            is RenderCommand.SetRuntimePolicy -> runtimePolicy.set(command)
            is RenderCommand.SetPowerPolicy -> powerPolicy.set(command)
            is RenderCommand.SetDaylight -> daylight.set(command)
            else -> fifo.offer(command)
        }
    }

    fun drainTo(target: MutableList<RenderCommand>) {
        while (true) target.add(fifo.poll() ?: break)
        resize.getAndSet(null)?.let(target::add)
        visibility.getAndSet(null)?.let(target::add)
        parallax.getAndSet(null)?.let(target::add)
        touch.getAndSet(null)?.let(target::add)
        runtimePolicy.getAndSet(null)?.let(target::add)
        powerPolicy.getAndSet(null)?.let(target::add)
        daylight.getAndSet(null)?.let(target::add)
    }
}
