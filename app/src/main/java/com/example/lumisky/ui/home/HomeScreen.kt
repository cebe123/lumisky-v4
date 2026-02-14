package com.example.lumisky.ui.home

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.opengl.GLSurfaceView
import android.view.Choreographer
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.settings.PerformanceMode
import com.example.engine.config.WallpaperConfig
import com.example.engine.preview.PreviewGlRenderer
import com.example.engine.renderer.RenderMode
import com.example.lumisky.R
import com.example.lumisky.shader.RenderAssetCache
import com.example.lumisky.ui.components.BottomNavBar
import com.example.lumisky.viewmodel.HomeWallpaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlin.math.abs

private data class StoreWallpaperModel(
	val id: String,
	val name: String,
	val category: String,
	val item: HomeWallpaperItem
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
	items: List<HomeWallpaperItem>,
	selectedWallpaperId: String?,
	liveWallpaperId: String?,
	daylightLabel: String,
	startupLoading: Boolean,
	startupProgress: Float,
	highRefreshEnabled: Boolean,
	performanceMode: PerformanceMode,
	onWallpaperSelected: (String) -> Unit,
	onCategoryFocused: (List<String>) -> Unit,
	onFocusReady: (String) -> Unit,
	onFocusCleared: () -> Unit,
	onOpenPreview: (String) -> Unit,
	onNavigateSettings: () -> Unit
) {
	val specialCategory = androidx.compose.ui.res.stringResource(R.string.cat_special)
	val landscapesCategory = androidx.compose.ui.res.stringResource(R.string.cat_landscapes)
	val citiesCategory = androidx.compose.ui.res.stringResource(R.string.cat_cities)
	val animeCategory = androidx.compose.ui.res.stringResource(R.string.cat_abstract)
	val orderedCategories = listOf(
		specialCategory,
		landscapesCategory,
		citiesCategory,
		animeCategory
	)

	val wallpapers = remember(
		items,
		specialCategory,
		landscapesCategory,
		citiesCategory,
		animeCategory
	) {
		items.map { item ->
			StoreWallpaperModel(
				id = item.config.id,
				name = item.config.name,
				category = resolveCategory(
					id = item.config.id,
					specialCategory = specialCategory,
					landscapesCategory = landscapesCategory,
					citiesCategory = citiesCategory,
					animeCategory = animeCategory
				),
				item = item
			)
		}
	}

	val groupedWallpapers = remember(wallpapers, orderedCategories) {
		orderedCategories.mapNotNull { category ->
			val list = wallpapers.filter { it.category == category }
			if (list.isEmpty()) null else category to list
		}
	}
	val configuration = LocalConfiguration.current
	val wallpaperAspectRatio = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
		val shortEdge = minOf(configuration.screenWidthDp, configuration.screenHeightDp).coerceAtLeast(1)
		val longEdge = maxOf(configuration.screenWidthDp, configuration.screenHeightDp).coerceAtLeast(shortEdge)
		(longEdge.toFloat() / shortEdge.toFloat()).coerceIn(1.65f, 2.25f)
	}
	val cardWidth = 276.dp
	val cardHeight = (cardWidth.value * wallpaperAspectRatio * 0.9f).dp

	val verticalState = rememberLazyListState()
	val centerCategoryIndex by remember {
		derivedStateOf {
			findCenteredIndex(verticalState)
		}
	}
	val activeCategoryIds by remember(groupedWallpapers, centerCategoryIndex) {
		derivedStateOf {
			if (groupedWallpapers.isEmpty()) return@derivedStateOf emptyList<String>()
			val index = centerCategoryIndex.takeIf { it in groupedWallpapers.indices } ?: 0
			groupedWallpapers[index].second.map { it.id }
		}
	}

	var focusCandidateId by remember { mutableStateOf<String?>(null) }
	var lastDispatchedFocusId by remember { mutableStateOf<String?>(null) }
	var lastCategoryDispatchKey by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(activeCategoryIds, verticalState.isScrollInProgress) {
		if (activeCategoryIds.isEmpty()) {
			focusCandidateId = null
			lastCategoryDispatchKey = null
			return@LaunchedEffect
		}
		if (verticalState.isScrollInProgress) return@LaunchedEffect
		delay(CATEGORY_FOCUS_DISPATCH_DEBOUNCE_MS)
		if (verticalState.isScrollInProgress) return@LaunchedEffect
		val limitedIds = activeCategoryIds.take(CATEGORY_FOCUS_MAX_ITEMS)
		val dispatchKey = limitedIds.joinToString(separator = "|")
		if (dispatchKey == lastCategoryDispatchKey) return@LaunchedEffect
		lastCategoryDispatchKey = dispatchKey
		onCategoryFocused(limitedIds)
	}

	LaunchedEffect(focusCandidateId) {
		val candidate = focusCandidateId
		if (candidate == null) {
			if (lastDispatchedFocusId != null) {
				lastDispatchedFocusId = null
				onFocusCleared()
			}
			return@LaunchedEffect
		}
		if (candidate == lastDispatchedFocusId) return@LaunchedEffect
		delay(FOCUS_DISPATCH_DEBOUNCE_MS)
		if (focusCandidateId != candidate) return@LaunchedEffect
		lastDispatchedFocusId = candidate
		onFocusReady(candidate)
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							imageVector = Icons.Filled.FilterHdr,
							contentDescription = "App Logo",
							modifier = Modifier.size(32.dp),
							tint = MaterialTheme.colorScheme.primary
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
					titleContentColor = MaterialTheme.colorScheme.onBackground
				)
			)
		},
		bottomBar = {
			BottomNavBar(
				selectedItem = 0,
				onItemSelected = { index ->
					if (index == 1) onNavigateSettings()
				}
			)
		}
	) { innerPadding ->
		Box(modifier = Modifier.fillMaxSize()) {
			if (startupLoading) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding),
					contentAlignment = Alignment.Center
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.spacedBy(12.dp)
					) {
						LinearProgressIndicator(
							progress = { startupProgress.coerceIn(0f, 1f) },
							modifier = Modifier
								.fillMaxWidth(0.68f)
								.height(10.dp)
								.clip(RoundedCornerShape(10.dp))
						)
						Text(
							text = "Preparing ${(startupProgress * 100f).toInt()}%",
							style = MaterialTheme.typography.bodyMedium
						)
					}
				}
			} else {
				LazyColumn(
					state = verticalState,
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding),
					contentPadding = PaddingValues(bottom = 80.dp),
					verticalArrangement = Arrangement.spacedBy(24.dp)
				) {
					itemsIndexed(groupedWallpapers) { index, entry ->
						val isCategoryActive = (index == centerCategoryIndex) || (centerCategoryIndex == -1 && index == 0)
						CategorySection(
							categoryName = entry.first,
							wallpapers = entry.second,
							selectedWallpaperId = selectedWallpaperId,
							liveWallpaperId = liveWallpaperId,
							isCategoryActive = isCategoryActive,
							highRefreshEnabled = highRefreshEnabled,
							performanceMode = performanceMode,
							cardWidth = cardWidth,
							cardHeight = cardHeight,
							onFocusCandidate = { candidate ->
								if (isCategoryActive && candidate != null) {
									focusCandidateId = candidate
								}
							},
							onWallpaperClick = { id ->
								onWallpaperSelected(id)
								onOpenPreview(id)
							}
						)
					}
				}
			}

		}
	}
}

@Composable
private fun CategorySection(
	categoryName: String,
	wallpapers: List<StoreWallpaperModel>,
	selectedWallpaperId: String?,
	liveWallpaperId: String?,
	isCategoryActive: Boolean,
	highRefreshEnabled: Boolean,
	performanceMode: PerformanceMode,
	cardWidth: Dp,
	cardHeight: Dp,
	onFocusCandidate: (String?) -> Unit,
	onWallpaperClick: (String) -> Unit
) {
	val context = LocalContext.current.applicationContext
	val rowState = rememberLazyListState()
	var centeredWallpaperId by remember(wallpapers) {
		mutableStateOf(wallpapers.firstOrNull()?.id)
	}
	var lastPrewarmIds by remember { mutableStateOf("") }

	LaunchedEffect(rowState, wallpapers) {
		snapshotFlow {
			findCenteredIndex(rowState)
		}
			.distinctUntilChanged()
			.collectLatest { centered ->
				if (centered in wallpapers.indices) {
					centeredWallpaperId = wallpapers[centered].id
				}
			}
	}

	LaunchedEffect(wallpapers) {
		if (centeredWallpaperId == null || wallpapers.none { it.id == centeredWallpaperId }) {
			centeredWallpaperId = wallpapers.firstOrNull()?.id
		}
	}

	LaunchedEffect(isCategoryActive, centeredWallpaperId, wallpapers, rowState.isScrollInProgress) {
		if (!isCategoryActive) {
			return@LaunchedEffect
		}
		if (rowState.isScrollInProgress) return@LaunchedEffect
		val focused = centeredWallpaperId ?: wallpapers.firstOrNull()?.id ?: return@LaunchedEffect
		onFocusCandidate(focused)
		val focusedIndex = wallpapers.indexOfFirst { it.id == focused }
		if (focusedIndex < 0) return@LaunchedEffect
		val candidates = listOf(focusedIndex - 1, focusedIndex, focusedIndex + 1)
			.filter { it in wallpapers.indices }
			.map { wallpapers[it] }
		val warmupKey = candidates.joinToString("|") { it.id }
		if (warmupKey == lastPrewarmIds) return@LaunchedEffect
		lastPrewarmIds = warmupKey
		withContext(Dispatchers.IO) {
			candidates.forEach { model ->
				RenderAssetCache.prewarmWallpaper(context, model.item.config)
			}
		}
	}

	Column {
		Text(
			text = categoryName,
			style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
			modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
		)

		LazyRow(
			state = rowState,
			modifier = Modifier.fillMaxWidth(),
			contentPadding = PaddingValues(horizontal = 16.dp),
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			val activeLiveId = if (isCategoryActive) {
				centeredWallpaperId ?: liveWallpaperId
			} else {
				null
			}
			val liveIndex = wallpapers.indexOfFirst { it.id == activeLiveId }
			itemsIndexed(wallpapers, key = { _, model -> model.id }) { index, model ->
				val isFocusedLive = isCategoryActive &&
					liveIndex >= 0 &&
					index == liveIndex
				val isPreparedNeighbor = isCategoryActive &&
					liveIndex >= 0 &&
					abs(index - liveIndex) == 1
				val isSelected = model.id == selectedWallpaperId
				WallpaperCard(
					title = model.name,
					item = model.item,
					isSelected = isSelected,
					isLive = isFocusedLive,
					isPrepared = isPreparedNeighbor,
					highRefreshEnabled = highRefreshEnabled,
					performanceMode = performanceMode,
					modifier = Modifier.size(width = cardWidth, height = cardHeight),
					onClick = { onWallpaperClick(model.id) }
				)
			}
		}
	}
}

@Composable
private fun WallpaperCard(
	title: String,
	item: HomeWallpaperItem,
	isSelected: Boolean,
	isLive: Boolean,
	isPrepared: Boolean,
	highRefreshEnabled: Boolean,
	performanceMode: PerformanceMode,
	modifier: Modifier = Modifier,
	onClick: () -> Unit
) {
	Card(
		modifier = modifier
			.padding(vertical = 3.dp)
			.clickable(onClick = onClick),
		shape = RoundedCornerShape(16.dp),
		elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
	) {
		Box(modifier = Modifier.fillMaxSize()) {
			PreviewPlaceholderFrame(modifier = Modifier.fillMaxSize())
			if (isLive || isPrepared) {
				FocusedWallpaperPreview(
					config = item.config,
					highRefreshEnabled = highRefreshEnabled,
					performanceMode = performanceMode,
					playbackEnabled = isLive,
					modifier = Modifier.fillMaxSize()
				)
			}

			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(
						brush = Brush.verticalGradient(
							colors = listOf(
								Color.Transparent,
								Color.Black.copy(alpha = 0.7f)
							)
						)
					)
			)

			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(20.dp),
				verticalArrangement = Arrangement.Bottom,
				horizontalAlignment = Alignment.Start
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.headlineSmall.copy(
						fontWeight = FontWeight.Bold,
						color = Color.White
					)
				)
				if (isSelected) {
					Text(
						text = "SELECTED",
						style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.85f))
					)
				}
			}
		}
	}
}

@Composable
private fun FocusedWallpaperPreview(
	config: WallpaperConfig,
	highRefreshEnabled: Boolean,
	performanceMode: PerformanceMode,
	playbackEnabled: Boolean,
	modifier: Modifier = Modifier
) {
	AndroidView(
		modifier = modifier,
		factory = { context ->
			val fragmentOverride = RenderAssetCache.loadFragment(
				context = context,
				assetPath = config.shader.fragmentAssetPath
			)
			val renderer = PreviewGlRenderer(
				config = if (playbackEnabled) {
					config.copy(focusCatchUpDurationSeconds = HOME_FOCUS_CATCHUP_SECONDS)
				} else {
					config
				},
				mode = RenderMode.FOCUS,
				animateFullDayLoop = false,
				initialFocusCatchUpEnabled = playbackEnabled,
				highRefreshEnabled = highRefreshEnabled,
				performanceMode = performanceMode,
				deviceRefreshRateProvider = { resolveDisplayRefreshRate(context) },
				isPowerSaveModeProvider = {
					context.getSystemService(PowerManager::class.java)?.isPowerSaveMode == true
				},
				qualityScale = 0.7f,
				fragmentShaderOverride = fragmentOverride,
				textureBytesLoader = { assetPath ->
					RenderAssetCache.loadTextureBytes(context, assetPath)
				}
			)
			HomeFocusPreviewView(context, renderer).apply {
				setPlaybackEnabled(playbackEnabled)
			}
		},
		update = { view ->
			view.setPlaybackEnabled(playbackEnabled)
		}
	)
}

private class HomeFocusPreviewView(
	context: Context,
	private val renderer: PreviewGlRenderer
) : GLSurfaceView(context) {
	private var playbackEnabled: Boolean = false
	private var lastRenderFrameNs: Long = 0L
	private var frameCallbackPosted: Boolean = false
	private val frameTicker = object : Choreographer.FrameCallback {
		override fun doFrame(frameTimeNanos: Long) {
			frameCallbackPosted = false
			val minIntervalNs = renderer.nextFrameDelayMs() * 1_000_000L
			if (frameTimeNanos - lastRenderFrameNs >= minIntervalNs) {
				requestRender()
				lastRenderFrameNs = frameTimeNanos
			}
			if (shouldScheduleFrame()) {
				postFrameCallbackIfNeeded()
			}
		}
	}

	init {
		setEGLContextClientVersion(2)
		setRenderer(renderer)
		renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
	}

	fun setPlaybackEnabled(enabled: Boolean) {
		if (playbackEnabled == enabled) {
			if (enabled && windowVisibility == View.VISIBLE) {
				postFrameCallbackIfNeeded()
			}
			return
		}
		val enteringFocus = !playbackEnabled && enabled
		playbackEnabled = enabled
		runCatching {
			queueEvent {
				renderer.setFocusPlaybackEnabled(
					enabled = enabled,
					restartOnEnable = enteringFocus
				)
			}
		}
		if (enabled && windowVisibility == View.VISIBLE) {
			lastRenderFrameNs = 0L
			postFrameCallbackIfNeeded()
		} else {
			removeFrameCallback()
		}
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		lastRenderFrameNs = 0L
		requestRender()
		if (shouldScheduleFrame()) {
			postFrameCallbackIfNeeded()
		}
	}

	override fun onWindowVisibilityChanged(visibility: Int) {
		super.onWindowVisibilityChanged(visibility)
		if (visibility == View.VISIBLE && shouldScheduleFrame()) {
			postFrameCallbackIfNeeded()
		} else {
			removeFrameCallback()
		}
	}

	override fun onDetachedFromWindow() {
		removeFrameCallback()
		runCatching {
			queueEvent { renderer.release() }
		}
		super.onDetachedFromWindow()
	}

	private fun shouldScheduleFrame(): Boolean {
		return windowVisibility == View.VISIBLE && playbackEnabled && renderer.shouldContinueRendering()
	}

	private fun postFrameCallbackIfNeeded() {
		if (frameCallbackPosted) return
		frameCallbackPosted = true
		Choreographer.getInstance().postFrameCallback(frameTicker)
	}

	private fun removeFrameCallback() {
		if (!frameCallbackPosted) return
		frameCallbackPosted = false
		Choreographer.getInstance().removeFrameCallback(frameTicker)
	}
}

@Composable
private fun PreviewPlaceholderFrame(modifier: Modifier = Modifier) {
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(16.dp))
			.border(
				width = 1.dp,
				color = Color.White.copy(alpha = 0.10f),
				shape = RoundedCornerShape(16.dp)
			)
			.background(
				brush = Brush.linearGradient(
					colors = listOf(
						Color(0xFF07121F),
						Color(0xFF10253B),
						Color(0xFF153A55)
					),
					start = Offset(0f, 0f),
					end = Offset(1000f, 1100f)
				)
			)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(9.dp)
				.clip(RoundedCornerShape(13.dp))
				.background(
					Brush.verticalGradient(
						colors = listOf(
							Color.White.copy(alpha = 0.08f),
							Color.White.copy(alpha = 0.03f)
						)
					)
				)
		)

		Box(
			modifier = Modifier
				.align(Alignment.TopStart)
				.padding(start = 14.dp, top = 12.dp)
				.size(width = 104.dp, height = 20.dp)
				.clip(RoundedCornerShape(999.dp))
				.background(Color(0xFF8FD2FF).copy(alpha = 0.22f))
		)

		Box(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(top = 14.dp, end = 14.dp)
				.size(14.dp)
				.clip(RoundedCornerShape(7.dp))
				.background(Color(0xFFB9E7FF).copy(alpha = 0.35f))
		)

		Column(
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
			verticalArrangement = Arrangement.spacedBy(7.dp)
		) {
			Box(
				modifier = Modifier
					.size(width = 150.dp, height = 11.dp)
					.clip(RoundedCornerShape(7.dp))
					.background(Color.White.copy(alpha = 0.20f))
			)
			Box(
				modifier = Modifier
					.size(width = 112.dp, height = 9.dp)
					.clip(RoundedCornerShape(6.dp))
					.background(Color.White.copy(alpha = 0.14f))
			)
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.graphicsLayer(alpha = 0.42f)
				.background(
					brush = Brush.radialGradient(
						colors = listOf(
							Color(0xFF9FD8FF).copy(alpha = 0.24f),
							Color.Transparent
						),
						center = Offset(260f, 160f),
						radius = 540f
					)
				)
		)

		Box(
			modifier = Modifier
				.fillMaxSize()
				.graphicsLayer(alpha = 0.30f)
				.background(
					brush = Brush.linearGradient(
						colors = listOf(
							Color.Transparent,
							Color.White.copy(alpha = 0.16f),
							Color.Transparent
						),
						start = Offset(180f, 0f),
						end = Offset(730f, 900f)
					)
				)
		)
	}
}

private fun resolveDisplayRefreshRate(context: Context): Int {
	val refresh = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
		context.display?.refreshRate
	} else {
		val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
		@Suppress("DEPRECATION")
		windowManager?.defaultDisplay?.refreshRate
	} ?: DEFAULT_REFRESH_RATE.toFloat()
	return refresh.toInt().coerceIn(DEFAULT_REFRESH_RATE, MAX_REFRESH_RATE)
}

private fun resolveCategory(
	id: String,
	specialCategory: String,
	landscapesCategory: String,
	citiesCategory: String,
	animeCategory: String
): String {
	return when {
		id.startsWith("city_") -> citiesCategory
		id.startsWith("anime_") -> animeCategory
		id.startsWith("solar_horizon") || id.startsWith("optical_sunset") || id.startsWith("mars") ->
			landscapesCategory
		else -> specialCategory
	}
}

private fun findCenteredIndex(listState: LazyListState): Int {
	val layoutInfo = listState.layoutInfo
	val visibleItems = layoutInfo.visibleItemsInfo
	if (visibleItems.isEmpty()) return -1
	val viewportCenter = (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2
	val closestItem = visibleItems.minByOrNull { item ->
		val itemCenter = item.offset + item.size / 2
		abs(itemCenter - viewportCenter)
	}
	return closestItem?.index ?: -1
}

private const val DEFAULT_REFRESH_RATE = 60
private const val MAX_REFRESH_RATE = 120
private const val HOME_FOCUS_CATCHUP_SECONDS = 4f
private const val CATEGORY_FOCUS_DISPATCH_DEBOUNCE_MS = 200L
private const val CATEGORY_FOCUS_MAX_ITEMS = 24
private const val FOCUS_DISPATCH_DEBOUNCE_MS = 160L
