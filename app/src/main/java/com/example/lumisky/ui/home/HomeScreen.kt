package com.example.lumisky.ui.home

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.util.Base64
import android.util.LruCache
import android.view.Choreographer
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.config.WallpaperConfig
import com.example.engine.preview.PreviewGlRenderer
import com.example.engine.renderer.RenderMode
import com.example.lumisky.R
import com.example.lumisky.shader.ShaderAssetLoader
import com.example.lumisky.ui.components.BottomNavBar
import com.example.lumisky.ui.debug.TemporaryDebugPanel
import com.example.lumisky.viewmodel.HomeWallpaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
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
	highRefreshEnabled: Boolean,
	onWallpaperSelected: (String) -> Unit,
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
	val cardHeight = (cardWidth.value * wallpaperAspectRatio).dp

	val verticalState = rememberLazyListState()
	val centerCategoryIndex by remember {
		derivedStateOf {
			findCenteredIndex(verticalState)
		}
	}

	var focusCandidateId by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(verticalState) {
		snapshotFlow { verticalState.isScrollInProgress }
			.distinctUntilChanged()
			.collectLatest { isScrolling ->
				if (isScrolling) {
					focusCandidateId = null
				}
			}
	}

	LaunchedEffect(focusCandidateId) {
		onFocusCleared()
		val candidate = focusCandidateId ?: return@LaunchedEffect
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
						CircularProgressIndicator()
						Text(
							text = "Preparing snapshots...",
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
							cardWidth = cardWidth,
							cardHeight = cardHeight,
							onFocusCandidate = { candidate ->
								if (isCategoryActive) {
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

			TemporaryDebugPanel(
				modifier = Modifier
					.align(Alignment.TopEnd)
					.padding(12.dp)
			)
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
	cardWidth: Dp,
	cardHeight: Dp,
	onFocusCandidate: (String?) -> Unit,
	onWallpaperClick: (String) -> Unit
) {
	val rowState = rememberLazyListState()
	val centerIndex by remember {
		derivedStateOf { findCenteredIndex(rowState) }
	}

	LaunchedEffect(rowState, wallpapers, isCategoryActive) {
		snapshotFlow {
			Triple(isCategoryActive, rowState.isScrollInProgress, findCenteredIndex(rowState))
		}
			.map { (categoryActive, rowScrolling, centered) ->
				if (!categoryActive || rowScrolling || centered !in wallpapers.indices) {
					null
				} else {
					wallpapers[centered].id
				}
			}
			.distinctUntilChanged()
			.collectLatest { focused ->
				onFocusCandidate(focused)
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
			itemsIndexed(wallpapers, key = { _, model -> model.id }) { index, model ->
				val isItemFocused = index == centerIndex || (centerIndex == -1 && index == 0)
				val shouldRenderLive = isCategoryActive && isItemFocused && model.id == liveWallpaperId
				val isSelected = model.id == selectedWallpaperId
				WallpaperCard(
					title = model.name,
					item = model.item,
					isSelected = isSelected,
					isLive = shouldRenderLive,
					highRefreshEnabled = highRefreshEnabled,
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
	highRefreshEnabled: Boolean,
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
			if (isLive) {
				FocusedWallpaperPreview(
					config = item.config,
					highRefreshEnabled = highRefreshEnabled,
					modifier = Modifier.fillMaxSize()
				)
			} else {
				SnapshotThumbnail(
					path = item.snapshotPath,
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
	modifier: Modifier = Modifier
) {
	AndroidView(
		modifier = modifier,
		factory = { context ->
			val fragmentOverride = ShaderAssetLoader.loadFragment(
				context = context,
				assetPath = config.shader.fragmentAssetPath
			)
			val renderer = PreviewGlRenderer(
				config = config,
				mode = RenderMode.FOCUS,
				animateFullDayLoop = false,
				focusCatchUpEnabled = true,
				highRefreshEnabled = highRefreshEnabled,
				qualityScale = 0.7f,
				fragmentShaderOverride = fragmentOverride,
				textureBytesLoader = { assetPath ->
					runCatching {
						context.assets.open(assetPath).use { it.readBytes() }
					}.getOrNull()
				}
			)
			object : GLSurfaceView(context) {
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
						if (renderer.shouldContinueRendering() && windowVisibility == View.VISIBLE) {
							postFrameCallbackIfNeeded()
						}
					}
				}

				override fun onAttachedToWindow() {
					super.onAttachedToWindow()
					lastRenderFrameNs = 0L
					postFrameCallbackIfNeeded()
				}

				override fun onWindowVisibilityChanged(visibility: Int) {
					super.onWindowVisibilityChanged(visibility)
					if (visibility == View.VISIBLE && renderer.shouldContinueRendering()) {
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
			}.apply {
				setEGLContextClientVersion(2)
				setRenderer(renderer)
				renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
			}
		}
	)
}

@Composable
private fun SnapshotThumbnail(
	path: String?,
	modifier: Modifier = Modifier
) {
	val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = path) {
		value = withContext(Dispatchers.IO) {
			decodeSnapshot(path)
		}
	}
	if (bitmap != null) {
		Image(
			bitmap = bitmap!!.asImageBitmap(),
			contentDescription = null,
			modifier = modifier,
			contentScale = ContentScale.Crop
		)
	} else {
		Box(
			modifier = modifier
				.clip(RoundedCornerShape(16.dp))
				.background(Color(0xFF1A1A1A)),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = androidx.compose.ui.res.stringResource(R.string.coming_soon),
				color = Color.White.copy(alpha = 0.5f),
				style = MaterialTheme.typography.titleMedium
			)
		}
	}
}

private fun decodeSnapshot(path: String?) = when {
	path.isNullOrBlank() -> null
	path.startsWith("data:image") -> decodeDataUri(path)
	else -> decodeFile(path)
}

private fun decodeFile(path: String) = runCatching {
	val file = File(path)
	if (!file.exists()) return@runCatching null
	SnapshotBitmapMemoryCache.get(file.absolutePath)?.let { cached ->
		return@runCatching cached
	}
	val options = BitmapFactory.Options().apply {
		inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
	}
	val decoded = BitmapFactory.decodeFile(file.absolutePath, options)
	if (decoded != null) {
		SnapshotBitmapMemoryCache.put(file.absolutePath, decoded)
	}
	decoded
}.getOrNull()

private fun decodeDataUri(dataUri: String) = runCatching {
	val payload = dataUri.substringAfter("base64,", missingDelimiterValue = "")
	if (payload.isBlank()) return@runCatching null
	val bytes = Base64.decode(payload, Base64.DEFAULT)
	BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()

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

private object SnapshotBitmapMemoryCache {
	private const val MAX_CACHE_BYTES = 24 * 1024 * 1024
	private val cache = object : LruCache<String, android.graphics.Bitmap>(MAX_CACHE_BYTES) {
		override fun sizeOf(
			key: String,
			value: android.graphics.Bitmap
		): Int {
			return value.byteCount
		}
	}

	@Synchronized
	fun get(path: String): android.graphics.Bitmap? = cache.get(path)

	@Synchronized
	fun put(
		path: String,
		bitmap: android.graphics.Bitmap
	) {
		cache.put(path, bitmap)
	}
}
