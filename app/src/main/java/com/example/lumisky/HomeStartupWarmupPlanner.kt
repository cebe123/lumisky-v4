package com.example.lumisky

internal data class HomeStartupWarmupPlan<T>(
	val immediateSnapshotItems: List<T>,
	val deferredSnapshotItems: List<T>,
	val renderAssetItems: List<T>
)

internal fun <T> splitStartupWarmupItems(
	items: List<T>,
	immediateSnapshotLimit: Int,
	renderAssetLimit: Int
): HomeStartupWarmupPlan<T> {
	val immediateLimit = immediateSnapshotLimit.coerceAtLeast(0)
	val renderLimit = renderAssetLimit.coerceAtLeast(0)
	return HomeStartupWarmupPlan(
		immediateSnapshotItems = items.take(immediateLimit),
		deferredSnapshotItems = items.drop(immediateLimit),
		renderAssetItems = items.take(renderLimit)
	)
}
