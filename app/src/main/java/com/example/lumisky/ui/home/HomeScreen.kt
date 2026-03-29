package com.example.lumisky.ui.home

import android.graphics.Bitmap
import android.os.Handler
import android.os.PowerManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.settings.PerformanceMode
import com.example.engine.config.WallpaperConfig
import com.example.engine.preview.PreviewGlRenderer
import com.example.engine.renderer.RenderMode
import com.example.lumisky.R
import com.example.lumisky.shader.RenderAssetCache
import com.example.lumisky.ui.common.PreviewRendererSurfaceView
import com.example.lumisky.ui.common.resolveDisplayRefreshRate
import com.example.lumisky.ui.components.BottomNavBar
import com.example.lumisky.viewmodel.HomeWallpaperItem
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

private data class StoreWallpaperModel(
	val id: String,
	val name: String,
	val category: String,
	val item: HomeWallpaperItem
)

@Immutable
private data class CategorySectionModel(
	val key: String,
	val title: String,
	val wallpapers: List<StoreWallpaperModel>
)

private data class FragmentShaderLoadState(
	val isReady: Boolean,
	val fragmentOverride: String?
)

@Composable
fun LaunchSkeleton() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(HomeScreenBackgroundColor),
		contentAlignment = Alignment.Center
	) {
		Icon(
			imageVector = Icons.Filled.FilterHdr,
			contentDescription = "App Logo",
			modifier = Modifier.size(42.dp),
			tint = Color.White.copy(alpha = 0.92f)
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
	items: List<HomeWallpaperItem>,
	selectedWallpaperId: String?,
	liveWallpaperId: String?,
	highRefreshEnabled: Boolean,
	performanceMode: PerformanceMode,
	onCategoryFocused: (List<String>) -> Unit,
	onFocusReady: (String) -> Unit,
	onFocusCleared: () -> Unit,
	onSetWallpaper: (String) -> Unit,
	onNavigateSettings: () -> Unit,
	startupDeferNonCriticalContentOnFirstRender: Boolean = false,
	startupAnimationsEnabled: Boolean = true
) {
	val specialCategory = stringResource(R.string.cat_special)
	val landscapesCategory = stringResource(R.string.cat_landscapes)
	val citiesCategory = stringResource(R.string.cat_cities)
	val animeCategory = stringResource(R.string.cat_abstract)
	val gamesCategory = stringResource(R.string.cat_games)
	val orderedCategories = listOf(
		specialCategory,
		landscapesCategory,
		citiesCategory,
		animeCategory,
		gamesCategory
	)

	val wallpapers = remember(
		items,
		specialCategory,
		landscapesCategory,
		citiesCategory,
		animeCategory,
		gamesCategory
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
					animeCategory = animeCategory,
					gamesCategory = gamesCategory
				),
				item = item
			)
		}
	}

	val groupedWallpapers = remember(wallpapers, orderedCategories) {
		val byCategory = wallpapers.groupBy { it.category }
		orderedCategories.mapNotNull { category ->
			byCategory[category]
				?.takeIf { it.isNotEmpty() }
				?.let { CategorySectionModel(key = category, title = category, wallpapers = it) }
		}
	}
	val configuration = LocalConfiguration.current
	val previewFrameAspectRatio = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
		resolveHomePreviewFrameAspectRatio(
			primaryEdge = configuration.screenWidthDp,
			secondaryEdge = configuration.screenHeightDp
		)
	}
	val appContext = LocalContext.current.applicationContext
	val density = LocalDensity.current
	val cardWidth = 276.dp
	val cardHeight = (cardWidth.value * previewFrameAspectRatio).dp
	val snapshotPreviewAssetLoader = remember(appContext) {
		SnapshotPreviewAssetLoader(appContext)
	}
	val previewWidthPx = remember(cardWidth, density) {
		with(density) { cardWidth.roundToPx() }
	}
	val previewHeightPx = remember(cardHeight, density) {
		with(density) { cardHeight.roundToPx() }
	}
	var startupHydrationActive by remember(items) {
		mutableStateOf(startupDeferNonCriticalContentOnFirstRender && items.isNotEmpty())
	}
	var visibleCategoryCount by remember(groupedWallpapers, startupHydrationActive) {
		mutableStateOf(
			if (startupHydrationActive && groupedWallpapers.isNotEmpty()) {
				1
			} else {
				groupedWallpapers.size
			}
		)
	}
	var showBottomNav by remember(groupedWallpapers, startupHydrationActive) {
		mutableStateOf(!startupHydrationActive)
	}
	val startupRenderLiteMode = startupHydrationActive || !startupAnimationsEnabled
	val renderedCategoryGroups by remember(groupedWallpapers, visibleCategoryCount) {
		derivedStateOf {
			groupedWallpapers.take(visibleCategoryCount.coerceAtLeast(0))
		}
	}

	val verticalState = rememberLazyListState()
	val centerCategoryIndex by remember {
		derivedStateOf {
			findCenteredIndex(verticalState)
		}
	}
	val activeCategoryIds by remember(renderedCategoryGroups, centerCategoryIndex) {
		derivedStateOf {
			if (renderedCategoryGroups.isEmpty()) return@derivedStateOf emptyList<String>()
			val index = centerCategoryIndex.takeIf { it in renderedCategoryGroups.indices } ?: 0
			renderedCategoryGroups[index].wallpapers.map { it.id }
		}
	}
	val allWallpaperIds by remember(renderedCategoryGroups) {
		derivedStateOf {
			renderedCategoryGroups.asSequence()
				.flatMap { section -> section.wallpapers.asSequence().map { it.id } }
				.toList()
		}
	}
	val allWallpaperIdSet by remember(allWallpaperIds) {
		derivedStateOf { allWallpaperIds.toHashSet() }
	}

	var focusCandidateId by remember { mutableStateOf<String?>(null) }
	var lastDispatchedFocusId by remember { mutableStateOf<String?>(null) }
	var lastCategoryDispatchKey by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(groupedWallpapers, startupHydrationActive) {
		if (!startupHydrationActive) {
			visibleCategoryCount = groupedWallpapers.size
			showBottomNav = true
			return@LaunchedEffect
		}
		if (groupedWallpapers.isEmpty()) {
			showBottomNav = false
			startupHydrationActive = false
			return@LaunchedEffect
		}
		withFrameNanos { }
		showBottomNav = true
		if (groupedWallpapers.size > 1) {
			withFrameNanos { }
		}
		visibleCategoryCount = groupedWallpapers.size
		startupHydrationActive = false
	}

	LaunchedEffect(renderedCategoryGroups, selectedWallpaperId, liveWallpaperId) {
		if (focusCandidateId != null) return@LaunchedEffect
		if (allWallpaperIds.isEmpty()) return@LaunchedEffect
		focusCandidateId = when {
			selectedWallpaperId != null && selectedWallpaperId in allWallpaperIdSet -> selectedWallpaperId
			liveWallpaperId != null && liveWallpaperId in allWallpaperIdSet -> liveWallpaperId
			else -> allWallpaperIds.first()
		}
	}

	LaunchedEffect(activeCategoryIds, verticalState.isScrollInProgress) {
		if (activeCategoryIds.isEmpty()) {
			focusCandidateId = null
			lastCategoryDispatchKey = null
			return@LaunchedEffect
		}
		if (verticalState.isScrollInProgress) return@LaunchedEffect
		if (focusCandidateId == null || focusCandidateId !in activeCategoryIds) {
			focusCandidateId = activeCategoryIds.firstOrNull()
		}
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
		delay(FOCUS_INIT_DELAY_MS)
		if (candidate != focusCandidateId) return@LaunchedEffect
		lastDispatchedFocusId = candidate
		onFocusReady(candidate)
	}

	Scaffold(
		containerColor = HomeScreenBackgroundColor,
		topBar = {
			HomeTopBar(
				simplified = startupRenderLiteMode
			)
		}
	) { innerPadding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(HomeScreenBackgroundColor)
		) {
			LazyColumn(
				state = verticalState,
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding),
				contentPadding = PaddingValues(bottom = HOME_CONTENT_BOTTOM_PADDING),
				verticalArrangement = Arrangement.spacedBy(24.dp)
			) {
				itemsIndexed(
					items = renderedCategoryGroups,
					key = { _, entry -> entry.key },
					contentType = { _, _ -> CATEGORY_SECTION_CONTENT_TYPE }
				) { index, entry ->
					val isCategoryActive = (index == centerCategoryIndex) || (centerCategoryIndex == -1 && index == 0)
					CategorySection(
						categoryName = entry.title,
						wallpapers = entry.wallpapers,
						selectedWallpaperId = selectedWallpaperId,
						liveWallpaperId = liveWallpaperId,
						isCategoryActive = isCategoryActive,
						parentScrollInProgress = verticalState.isScrollInProgress,
						highRefreshEnabled = highRefreshEnabled,
						performanceMode = performanceMode,
						cardWidth = cardWidth,
						cardHeight = cardHeight,
						snapshotPreviewAssetLoader = snapshotPreviewAssetLoader,
						previewWidthPx = previewWidthPx,
						previewHeightPx = previewHeightPx,
						simplifiedPlaceholderVisuals = startupRenderLiteMode,
						onFocusCandidate = { candidate ->
							if (candidate != null) {
								focusCandidateId = candidate
							}
						},
						onSetWallpaper = { id ->
							onSetWallpaper(id)
						}
					)
				}
			}

			Box(
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.fillMaxWidth()
			) {
				if (showBottomNav) {
					BottomNavBar(
						selectedItem = 0,
						onItemSelected = { index ->
							if (index == 1) onNavigateSettings()
						},
						animationsEnabled = startupAnimationsEnabled && !startupHydrationActive
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(simplified: Boolean) {
	if (simplified) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(HomeScreenBackgroundColor)
				.statusBarsPadding()
				.padding(top = 12.dp, bottom = 10.dp),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Filled.FilterHdr,
				contentDescription = "App Logo",
				modifier = Modifier.size(28.dp),
				tint = Color.White.copy(alpha = 0.90f)
			)
		}
		return
	}

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
					tint = Color.White.copy(alpha = 0.92f)
				)
			}
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = HomeScreenBackgroundColor,
			scrolledContainerColor = HomeScreenBackgroundColor,
			titleContentColor = Color.White
		)
	)
}

@Composable
private fun CategorySection(
	categoryName: String,
	wallpapers: List<StoreWallpaperModel>,
	selectedWallpaperId: String?,
	liveWallpaperId: String?,
	isCategoryActive: Boolean,
	parentScrollInProgress: Boolean,
	highRefreshEnabled: Boolean,
	performanceMode: PerformanceMode,
	cardWidth: Dp,
	cardHeight: Dp,
	snapshotPreviewAssetLoader: SnapshotPreviewAssetLoader,
	previewWidthPx: Int,
	previewHeightPx: Int,
	simplifiedPlaceholderVisuals: Boolean,
	onFocusCandidate: (String?) -> Unit,
	onSetWallpaper: (String) -> Unit
) {
	val rowState = rememberLazyListState()
	var centeredWallpaperId by remember(wallpapers) { mutableStateOf<String?>(null) }
	val wallpaperIdSet by remember(wallpapers) {
		derivedStateOf { wallpapers.asSequence().map { it.id }.toHashSet() }
	}

	LaunchedEffect(rowState, wallpapers) {
		snapshotFlow {
			findCenteredIndex(rowState)
		}
			.distinctUntilChanged()
			.collectLatest { centered ->
				if (centered in wallpapers.indices) {
					delay(FOCUS_INIT_DELAY_MS)
					centeredWallpaperId = wallpapers[centered].id
				}
			}
	}

	LaunchedEffect(wallpapers) {
		if (centeredWallpaperId == null || centeredWallpaperId !in wallpaperIdSet) {
			delay(FOCUS_INIT_DELAY_MS)
			centeredWallpaperId = wallpapers.firstOrNull()?.id
		}
	}

	LaunchedEffect(
		isCategoryActive,
		parentScrollInProgress,
		centeredWallpaperId,
		wallpapers,
		rowState.isScrollInProgress
	) {
		if (!isCategoryActive) {
			return@LaunchedEffect
		}
		if (parentScrollInProgress) return@LaunchedEffect
		if (rowState.isScrollInProgress) return@LaunchedEffect
		val focused = centeredWallpaperId ?: wallpapers.firstOrNull()?.id ?: return@LaunchedEffect
		onFocusCandidate(focused)
	}
	val shouldPauseLivePreview = parentScrollInProgress || rowState.isScrollInProgress
	val activeLiveId = if (isCategoryActive && !shouldPauseLivePreview) {
		centeredWallpaperId ?: liveWallpaperId
	} else {
		null
	}

	Column {
		Text(
			text = categoryName,
			style = MaterialTheme.typography.titleLarge.copy(
				fontWeight = FontWeight.Bold,
				color = Color.White.copy(alpha = 0.96f)
			),
			modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
		)

		LazyRow(
			state = rowState,
			modifier = Modifier.fillMaxWidth(),
			contentPadding = PaddingValues(horizontal = 16.dp),
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			itemsIndexed(
				items = wallpapers,
				key = { _, model -> model.id },
				contentType = { _, _ -> WALLPAPER_CARD_CONTENT_TYPE }
			) { _, model ->
				val isFocusedLive = isCategoryActive && model.id == activeLiveId
				val isSelected = model.id == selectedWallpaperId
				Column(
					modifier = Modifier.width(cardWidth),
					verticalArrangement = Arrangement.spacedBy(10.dp)
				) {
					WallpaperCard(
						title = model.name,
						item = model.item,
						isLive = isFocusedLive,
						isSelected = isSelected,
						highRefreshEnabled = highRefreshEnabled,
						performanceMode = performanceMode,
						snapshotPreviewAssetLoader = snapshotPreviewAssetLoader,
						previewWidthPx = previewWidthPx,
						previewHeightPx = previewHeightPx,
						simplifiedPlaceholderVisuals = simplifiedPlaceholderVisuals,
						modifier = Modifier
							.fillMaxWidth()
							.height(cardHeight),
						onClick = { onSetWallpaper(model.id) }
					)
				}
			}
		}
	}
}

@Composable
private fun WallpaperCard(
	title: String,
	item: HomeWallpaperItem,
	isLive: Boolean,
	isSelected: Boolean,
	highRefreshEnabled: Boolean,
	performanceMode: PerformanceMode,
	snapshotPreviewAssetLoader: SnapshotPreviewAssetLoader,
	previewWidthPx: Int,
	previewHeightPx: Int,
	simplifiedPlaceholderVisuals: Boolean,
	modifier: Modifier = Modifier,
	onClick: () -> Unit
) {
	val showLivePreview = isLive
	val snapshotBitmap = rememberSnapshotPreviewBitmap(
		configId = item.config.id,
		snapshotPreviewAssetLoader = snapshotPreviewAssetLoader,
		targetWidthPx = previewWidthPx,
		targetHeightPx = previewHeightPx
	)
	var livePreviewReady by remember(item.config.id, highRefreshEnabled, performanceMode) {
		mutableStateOf(false)
	}
	var hasActivePreviewSession by remember(item.config.id, highRefreshEnabled, performanceMode) {
		mutableStateOf(false)
	}
	LaunchedEffect(showLivePreview) {
		if (showLivePreview) {
			if (!hasActivePreviewSession) {
				livePreviewReady = false
				hasActivePreviewSession = true
			}
		} else {
			hasActivePreviewSession = false
			livePreviewReady = false
		}
	}
	val snapshotOverlayAlpha by animateFloatAsState(
		targetValue = if (showLivePreview && livePreviewReady) 0f else 1f,
		animationSpec = tween(durationMillis = SNAPSHOT_OVERLAY_FADE_DURATION_MS),
		label = "wallpaper_snapshot_overlay_alpha"
	)
	Card(
		modifier = modifier
			.padding(vertical = 3.dp)
			.clickable(onClick = onClick),
		shape = RoundedCornerShape(16.dp),
		elevation = CardDefaults.cardElevation(
			defaultElevation = if (simplifiedPlaceholderVisuals) 2.dp else 8.dp
		),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
	) {
		Box(modifier = Modifier.fillMaxSize()) {
			if (showLivePreview) {
				FocusedWallpaperPreview(
					config = item.config,
					highRefreshEnabled = highRefreshEnabled,
					performanceMode = performanceMode,
					playbackEnabled = isLive,
					modifier = Modifier.fillMaxSize(),
					onFirstFrameRendered = {
						livePreviewReady = true
					}
				)
			}
			if (snapshotBitmap != null) {
				Image(
					bitmap = snapshotBitmap.asImageBitmap(),
					contentDescription = title,
					contentScale = ContentScale.FillBounds,
					modifier = Modifier
						.fillMaxSize()
						.alpha(snapshotOverlayAlpha)
				)
			} else if (!showLivePreview || !livePreviewReady) {
				PreviewPlaceholderFrame(
					modifier = Modifier.fillMaxSize(),
					simplified = simplifiedPlaceholderVisuals
				)
			}

			if (simplifiedPlaceholderVisuals) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(Color.Black.copy(alpha = 0.22f))
				)
			} else {
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
			}

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
			}
		}
	}
}

@Composable
private fun rememberSnapshotPreviewBitmap(
	configId: String,
	snapshotPreviewAssetLoader: SnapshotPreviewAssetLoader,
	targetWidthPx: Int,
	targetHeightPx: Int
): Bitmap? {
	val bitmap by produceState<Bitmap?>(
		snapshotPreviewAssetLoader.cachedBitmap(
			configId = configId,
			targetWidthPx = targetWidthPx,
			targetHeightPx = targetHeightPx
		),
		configId,
		snapshotPreviewAssetLoader,
		targetWidthPx,
		targetHeightPx
	) {
		if (targetWidthPx <= 0 || targetHeightPx <= 0) {
			value = null
			return@produceState
		}
		value = snapshotPreviewAssetLoader.loadBitmap(
			configId = configId,
			targetWidthPx = targetWidthPx,
			targetHeightPx = targetHeightPx
		)
	}
	return bitmap
}

@Composable
private fun FocusedWallpaperPreview(
	config: WallpaperConfig,
	highRefreshEnabled: Boolean,
	performanceMode: PerformanceMode,
	playbackEnabled: Boolean,
	modifier: Modifier = Modifier,
	onFirstFrameRendered: () -> Unit
) {
	val appContext = LocalContext.current.applicationContext
	val previewConfig = remember(config) {
		config.copy(focusCatchUpDurationSeconds = HOME_FOCUS_CATCHUP_SECONDS)
	}
	val fragmentAssetPath = previewConfig.shader.fragmentAssetPath?.takeIf { it.isNotBlank() }
	val cachedFragmentOverride = remember(fragmentAssetPath) {
		RenderAssetCache.cachedFragment(fragmentAssetPath)
	}
	val fragmentShaderState by produceState(
		initialValue = FragmentShaderLoadState(
			isReady = fragmentAssetPath == null || cachedFragmentOverride != null,
			fragmentOverride = cachedFragmentOverride
		),
		appContext,
		fragmentAssetPath
	) {
		if (fragmentAssetPath == null) {
			value = FragmentShaderLoadState(
				isReady = true,
				fragmentOverride = null
			)
			return@produceState
		}
		val loadedOverride = withContext(kotlinx.coroutines.Dispatchers.IO) {
			RenderAssetCache.loadFragment(
				context = appContext,
				assetPath = fragmentAssetPath
			)
		}
		value = FragmentShaderLoadState(
			isReady = true,
			fragmentOverride = loadedOverride
		)
	}
	if (!fragmentShaderState.isReady) {
		return
	}
	key(
		previewConfig,
		highRefreshEnabled,
		performanceMode,
		fragmentShaderState.fragmentOverride
	) {
		AndroidView(
			modifier = modifier,
			factory = { context ->
				val readyFrameCount = AtomicInteger(0)
				val firstFrameDispatched = AtomicBoolean(false)
				val mainHandler = Handler(context.mainLooper)
				val renderer = PreviewGlRenderer(
					config = previewConfig,
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
					fragmentShaderOverride = fragmentShaderState.fragmentOverride,
					textureBytesLoader = { assetPath ->
						RenderAssetCache.loadTextureBytes(context, assetPath)
					},
					onFrameDrawn = {
						if (
							readyFrameCount.incrementAndGet() >= LIVE_PREVIEW_READY_FRAME_COUNT &&
							firstFrameDispatched.compareAndSet(false, true)
						) {
							mainHandler.post {
								mainHandler.post {
									onFirstFrameRendered()
								}
							}
						}
					}
				)
				PreviewRendererSurfaceView(
					context = context,
					previewRenderer = renderer,
					initialPlaybackEnabled = playbackEnabled,
					warmupFramesOnEnable = HOME_PREVIEW_ENABLE_WARMUP_FRAMES,
					requestRenderOnAttach = true,
					onPlaybackStateChanged = { enabled, enteringEnabled ->
						renderer.setFocusPlaybackEnabled(
							enabled = enabled,
							restartOnEnable = enteringEnabled
						)
					}
				).apply {
					setPlaybackEnabled(playbackEnabled)
				}
			},
			update = { view ->
				view.setPlaybackEnabled(playbackEnabled)
			}
		)
	}
}

@Composable
private fun PreviewPlaceholderFrame(
	modifier: Modifier = Modifier,
	simplified: Boolean = false
) {
	if (simplified) {
		Box(
			modifier = modifier
				.clip(PlaceholderOuterShape)
				.background(Color(0xFF10253B))
				.border(
					width = 1.dp,
					color = Color.White.copy(alpha = 0.06f),
					shape = PlaceholderOuterShape
				)
		) {
			Box(
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
					.size(width = 144.dp, height = 12.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(Color.White.copy(alpha = 0.14f))
			)
		}
		return
	}

	Box(
		modifier = modifier
			.clip(PlaceholderOuterShape)
			.border(
				width = 1.dp,
				color = Color.White.copy(alpha = 0.10f),
				shape = PlaceholderOuterShape
			)
			.background(brush = PlaceholderBaseBrush)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(9.dp)
				.clip(PlaceholderInnerShape)
				.background(brush = PlaceholderInnerBrush)
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
				.background(brush = PlaceholderGlowBrush)
		)

		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(brush = PlaceholderSheenBrush)
		)
	}
}

private fun resolveCategory(
	id: String,
	specialCategory: String,
	landscapesCategory: String,
	citiesCategory: String,
	animeCategory: String,
	gamesCategory: String
): String {
	return when {
		id.startsWith("city_") -> citiesCategory
		id.startsWith("anime_") -> animeCategory
		id == "warrior" || id.startsWith("game_") -> gamesCategory
		id.startsWith("solar_horizon") || id.startsWith("optical_sunset") || id.startsWith("mars") ->
			landscapesCategory
		else -> specialCategory
	}
}

internal val HomeScreenBackgroundColor = Color(0xFF07121F)

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

private const val HOME_FOCUS_CATCHUP_SECONDS = 4f
private const val HOME_PREVIEW_ENABLE_WARMUP_FRAMES = 2
private const val SNAPSHOT_OVERLAY_FADE_DURATION_MS = 160
private const val LIVE_PREVIEW_READY_FRAME_COUNT = 3
private const val CATEGORY_FOCUS_MAX_ITEMS = 1
private const val FOCUS_INIT_DELAY_MS = 100L
private const val CATEGORY_SECTION_CONTENT_TYPE = "category_section"
private const val WALLPAPER_CARD_CONTENT_TYPE = "wallpaper_card"
private val HOME_CONTENT_BOTTOM_PADDING = 112.dp

private val PlaceholderOuterShape = RoundedCornerShape(16.dp)
private val PlaceholderInnerShape = RoundedCornerShape(13.dp)
private val PlaceholderBaseBrush = Brush.linearGradient(
	colors = listOf(
		Color(0xFF07121F),
		Color(0xFF10253B),
		Color(0xFF153A55)
	),
	start = Offset(0f, 0f),
	end = Offset(1000f, 1100f)
)
private val PlaceholderInnerBrush = Brush.verticalGradient(
	colors = listOf(
		Color.White.copy(alpha = 0.08f),
		Color.White.copy(alpha = 0.03f)
	)
)
private val PlaceholderGlowBrush = Brush.radialGradient(
	colors = listOf(
		Color(0xFF9FD8FF).copy(alpha = 0.1008f),
		Color.Transparent
	),
	center = Offset(260f, 160f),
	radius = 540f
)
private val PlaceholderSheenBrush = Brush.linearGradient(
	colors = listOf(
		Color.Transparent,
		Color.White.copy(alpha = 0.048f),
		Color.Transparent
	),
	start = Offset(180f, 0f),
	end = Offset(730f, 900f)
)
