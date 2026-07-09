/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Google Play Billing/premium yetki doğrulama kaynağı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Google Play Billing/premium yetki doğrulama kaynağı.
 */
package com.adnan.lumisky.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntitlementRepository @Inject constructor() {
    private var debugPremiumOverride: Boolean = false

    constructor(debugPremiumOverride: Boolean) : this() {
        this.debugPremiumOverride = debugPremiumOverride
    }

    fun isPremiumPurchased(): Flow<Boolean> {
        return flowOf(debugPremiumOverride)
    }
}
