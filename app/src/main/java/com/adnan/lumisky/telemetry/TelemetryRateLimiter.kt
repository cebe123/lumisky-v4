/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Telemetry katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Telemetry katmanı bileşeni.
 */
package com.adnan.lumisky.telemetry

class TelemetryRateLimiter {
    private val seenThisSession = HashSet<String>()

    fun shouldReport(event: RenderTelemetryEvent): Boolean {
        return seenThisSession.add(event.dedupeKey())
    }

    fun clear() {
        seenThisSession.clear()
    }
}
