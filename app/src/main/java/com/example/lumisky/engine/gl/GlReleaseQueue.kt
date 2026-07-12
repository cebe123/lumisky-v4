/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - GL kaynaklarının doğru thread/context üzerinde silinmesini garantiler.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: GL kaynaklarının doğru thread/context üzerinde silinmesini garantiler.
 */
package com.example.lumisky.engine.gl

import java.util.concurrent.ConcurrentLinkedQueue

class GlReleaseQueue {
    private val queue = ConcurrentLinkedQueue<() -> Unit>()

    fun post(block: () -> Unit) {
        queue.offer(block)
    }

    fun postRelease(resource: GlResource) {
        queue.offer { resource.release() }
    }

    fun drain() {
        while (true) {
            val action = queue.poll() ?: break
            try {
                action()
            } catch (e: Throwable) {
                // Rate-limited logging or ignore
            }
        }
    }

    fun discard() {
        queue.clear()
    }
}
