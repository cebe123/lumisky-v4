package com.example.lumisky.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SceneCommitTransactionTest {

    @Test
    fun candidateCommitsOnlyAfterFirstSuccessfulSwap() {
        val transaction = SceneCommitTransaction<String>()

        transaction.stage("candidate")

        assertNull(transaction.takeAfterSwap(succeeded = false))
        assertEquals("candidate", transaction.takeAfterSwap(succeeded = true))
        assertNull(transaction.takeAfterSwap(succeeded = true))
    }

    @Test
    fun newestCandidateReplacesUncommittedCandidate() {
        val transaction = SceneCommitTransaction<String>()

        transaction.stage("first")
        transaction.stage("second")

        assertEquals("second", transaction.takeAfterSwap(succeeded = true))
    }
}
