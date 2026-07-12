/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Ui katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Ui katmanı bileşeni.
 */
package com.example.lumisky.ui.catalog

object CatalogPreviewPolicy {
    fun previewFocusDelayMillis(): Long = 100L
    fun initialPreviewDelayMillis(): Long = 1_500L

    fun shouldRenderLivePreview(
        sectionIndex: Int,
        activeSectionIndex: Int,
        itemIndex: Int,
        centeredItemIndex: Int,
        parentScrollInProgress: Boolean,
        rowScrollInProgress: Boolean
    ): Boolean {
        if (parentScrollInProgress || rowScrollInProgress) return false
        return sectionIndex == activeSectionIndex && itemIndex == centeredItemIndex
    }

    fun shouldMountLivePreview(
        sectionIndex: Int,
        activeSectionIndex: Int,
        itemIndex: Int,
        centeredItemIndex: Int,
        parentScrollInProgress: Boolean,
        rowScrollInProgress: Boolean
    ): Boolean {
        if (parentScrollInProgress || rowScrollInProgress) return false
        if (sectionIndex != activeSectionIndex) return false
        return itemIndex == centeredItemIndex
    }

    fun resolveActiveSectionIndex(centeredIndex: Int, sectionCount: Int): Int {
        if (sectionCount <= 0) return -1
        if (centeredIndex < 0) return -1
        return if (centeredIndex in 0 until sectionCount) centeredIndex else 0
    }

    fun formatLoopTime(progress: Float): String {
        val wrappedProgress = ((progress % 1f) + 1f) % 1f
        val totalMinutes = (wrappedProgress * MinutesPerDay).toInt().coerceIn(0, MinutesPerDay - 1)
        val hours = totalMinutes / MinutesPerHour
        val minutes = totalMinutes % MinutesPerHour
        return "%02d:%02d".format(hours, minutes)
    }

    fun formatBadgeTime(rendererDayProgress: Float?, fallbackHour: Int, fallbackMinute: Int): String {
        return if (rendererDayProgress != null) {
            formatLoopTime(rendererDayProgress)
        } else {
            "%02d:%02d".format(
                fallbackHour.coerceIn(0, 23),
                fallbackMinute.coerceIn(0, 59)
            )
        }
    }

    fun livePreviewBadgeTickMillis(): Long = 1_000L

    fun loopProgressForElapsedMillis(elapsedMillis: Long): Float {
        val elapsedSeconds = (elapsedMillis.coerceAtLeast(0L) / 1000f)
        return (elapsedSeconds * LoopProgressPerSecond) % 1.0f
    }

    fun shouldStartLivePreview(showLivePreview: Boolean, warmupReady: Boolean): Boolean {
        return showLivePreview && warmupReady
    }

    fun shouldRenderCardChrome(parentScrollInProgress: Boolean, rowScrollInProgress: Boolean): Boolean {
        return true
    }

    private const val MinutesPerHour = 60
    private const val MinutesPerDay = 24 * MinutesPerHour
    private const val LoopProgressPerSecond = 0.08333f
}
