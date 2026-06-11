package com.example.lumisky

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeStartupWarmupPlannerTest {
	@Test
	fun keepsImmediateItemsAtFrontAndDefersTheRest() {
		val plan = splitStartupWarmupItems(
			items = listOf("a", "b", "c", "d"),
			immediateSnapshotLimit = 2,
			renderAssetLimit = 1
		)

		assertEquals(listOf("a", "b"), plan.immediateSnapshotItems)
		assertEquals(listOf("c", "d"), plan.deferredSnapshotItems)
		assertEquals(listOf("a"), plan.renderAssetItems)
	}

	@Test
	fun handlesEmptyItems() {
		val plan = splitStartupWarmupItems(
			items = emptyList<String>(),
			immediateSnapshotLimit = 2,
			renderAssetLimit = 1
		)

		assertEquals(emptyList<String>(), plan.immediateSnapshotItems)
		assertEquals(emptyList<String>(), plan.deferredSnapshotItems)
		assertEquals(emptyList<String>(), plan.renderAssetItems)
	}
}
