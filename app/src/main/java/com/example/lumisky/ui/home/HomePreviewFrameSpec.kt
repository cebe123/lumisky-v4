package com.example.lumisky.ui.home

import kotlin.math.max
import kotlin.math.min

internal fun resolveHomePreviewFrameAspectRatio(
	primaryEdge: Int,
	secondaryEdge: Int
): Float {
	val shortEdge = min(primaryEdge, secondaryEdge).coerceAtLeast(1)
	val longEdge = max(primaryEdge, secondaryEdge).coerceAtLeast(shortEdge)
	return (longEdge.toFloat() / shortEdge.toFloat())
		.coerceIn(MIN_PREVIEW_ASPECT_RATIO, MAX_PREVIEW_ASPECT_RATIO) * HOME_PREVIEW_HEIGHT_SCALE
}

private const val MIN_PREVIEW_ASPECT_RATIO = 1.65f
private const val MAX_PREVIEW_ASPECT_RATIO = 2.25f
private const val HOME_PREVIEW_HEIGHT_SCALE = 0.9f
