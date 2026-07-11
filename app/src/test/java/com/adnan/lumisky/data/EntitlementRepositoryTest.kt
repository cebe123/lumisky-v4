package com.example.lumisky.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementRepositoryTest {

    @Test
    fun unknownEntitlementDoesNotGrantPremiumByDefault() = runBlocking {
        val repository = EntitlementRepository()

        assertFalse(repository.isPremiumPurchased().first())
    }

    @Test
    fun explicitLocalDebugOverrideCanGrantPremium() = runBlocking {
        val repository = EntitlementRepository(debugPremiumOverride = true)

        assertTrue(repository.isPremiumPurchased().first())
    }
}
