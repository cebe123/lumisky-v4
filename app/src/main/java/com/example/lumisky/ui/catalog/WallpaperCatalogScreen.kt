/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Compose katalog ekranı. Sadece thumbnail ve stable UI model kullanır.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Compose katalog ekranı. Sadece thumbnail ve stable UI model kullanır.
 */
package com.example.lumisky.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lumisky.engine.RuntimeProfile
import com.example.lumisky.ui.components.BottomNavBar
import com.example.lumisky.ui.components.LumiskyWallpaperPreviewView
import kotlin.math.abs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import java.util.Calendar
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

private data class CategorySection(
    val title: String,
    val items: List<WallpaperCatalogUiItem>
)

private val PlaceholderOuterShape = RoundedCornerShape(8.dp)

private fun resolveHomePreviewFrameAspectRatio(primaryEdge: Int, secondaryEdge: Int): Float {
    val shortEdge = min(primaryEdge, secondaryEdge).coerceAtLeast(1)
    val longEdge = max(primaryEdge, secondaryEdge).coerceAtLeast(shortEdge)
    return (longEdge.toFloat() / shortEdge.toFloat())
        .coerceIn(1.65f, 2.25f) * 0.9f
}
private val PlaceholderBaseBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF07121F), Color(0xFF10253B), Color(0xFF153A55)),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1100f)
)
private val PlaceholderGlowBrush = Brush.radialGradient(
    colors = listOf(Color(0xFF9FD8FF).copy(alpha = 0.10f), Color.Transparent),
    center = Offset(260f, 160f),
    radius = 540f
)
private val PlaceholderSheenBrush = Brush.linearGradient(
    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.048f), Color.Transparent),
    start = Offset(180f, 0f),
    end = Offset(730f, 900f)
)

@Composable
private fun rememberAssetBitmap(path: String, targetWidthPx: Int, targetHeightPx: Int): Bitmap? {
    val context = LocalContext.current
    val cacheKey = remember(path, targetWidthPx, targetHeightPx) {
        CatalogThumbnailMemoryCache.key(path, targetWidthPx, targetHeightPx)
    }
    var bitmap by remember(cacheKey) { mutableStateOf(CatalogThumbnailMemoryCache.get(cacheKey)) }
    LaunchedEffect(cacheKey) {
        if (bitmap != null) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            try {
                val bounds = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.assets.open(path).use { stream ->
                    BitmapFactory.decodeStream(stream, null, bounds)
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = CatalogThumbnailDecodePolicy.calculateInSampleSize(
                        sourceWidth = bounds.outWidth,
                        sourceHeight = bounds.outHeight,
                        targetWidth = targetWidthPx,
                        targetHeight = targetHeightPx
                    )
                    inPreferredConfig = Bitmap.Config.RGB_565
                }

                context.assets.open(path).use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }?.also { decoded ->
                    CatalogThumbnailMemoryCache.put(cacheKey, decoded)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    return bitmap
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
    return closestItem?.index ?: 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperCatalogScreen(
    viewModel: WallpaperCatalogViewModel,
    onItemClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val bgColor = MaterialTheme.colorScheme.background
    val configuration = LocalConfiguration.current
    val previewFrameAspectRatio = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        resolveHomePreviewFrameAspectRatio(
            primaryEdge = configuration.screenWidthDp,
            secondaryEdge = configuration.screenHeightDp
        )
    }
    val cardWidth = 276.dp
    val cardHeight = (cardWidth.value * previewFrameAspectRatio).dp

    val categories = remember(items) {
        val grouped = items.groupBy { it.category }
        grouped.map { (category, wallpapers) ->
            CategorySection(title = category, items = wallpapers)
        }
    }

    Scaffold(
        containerColor = bgColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.FilterHdr,
                            contentDescription = "App Logo",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    scrolledContainerColor = bgColor,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            val columnState = rememberLazyListState()
            var livePreviewLeaseEnabled by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(CatalogPreviewPolicy.initialPreviewDelayMillis())
                livePreviewLeaseEnabled = true
            }
            val activeSectionIndex by remember(categories) {
                derivedStateOf {
                    CatalogPreviewPolicy.resolveActiveSectionIndex(
                        centeredIndex = findCenteredIndex(columnState),
                        sectionCount = categories.size
                    )
                }
            }
            var settledSectionIndex by remember { mutableStateOf(-1) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(CatalogPreviewPolicy.previewFocusDelayMillis())
                settledSectionIndex = activeSectionIndex
            }
            LaunchedEffect(columnState.isScrollInProgress, activeSectionIndex) {
                if (!columnState.isScrollInProgress) {
                    kotlinx.coroutines.delay(CatalogPreviewPolicy.previewFocusDelayMillis())
                    settledSectionIndex = activeSectionIndex
                }
            }
            LazyColumn(
                state = columnState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                itemsIndexed(
                    items = categories,
                    key = { _, section -> section.title }
                ) { sectionIndex, section ->
                    val listState = rememberLazyListState()
                    val centeredIndex by remember {
                        derivedStateOf {
                            findCenteredIndex(listState)
                        }
                    }
                    var settledItemIndex by remember { mutableStateOf(-1) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(CatalogPreviewPolicy.previewFocusDelayMillis())
                        settledItemIndex = centeredIndex
                    }
                    LaunchedEffect(listState.isScrollInProgress, centeredIndex) {
                        if (!listState.isScrollInProgress) {
                            kotlinx.coroutines.delay(CatalogPreviewPolicy.previewFocusDelayMillis())
                            settledItemIndex = centeredIndex
                        }
                    }
                    Column {
                                                Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                        LazyRow(
                            state = listState,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(
                                items = section.items,
                                key = { _, item -> item.id },
                                contentType = { _, _ -> "wallpaper-card" }
                            ) { index, item ->
                                val shouldMountPreview = livePreviewLeaseEnabled && CatalogPreviewPolicy.shouldMountLivePreview(
                                    sectionIndex = sectionIndex,
                                    activeSectionIndex = settledSectionIndex,
                                    itemIndex = index,
                                    centeredItemIndex = settledItemIndex,
                                    parentScrollInProgress = columnState.isScrollInProgress,
                                    rowScrollInProgress = listState.isScrollInProgress
                                )
                                val shouldPlayPreview = livePreviewLeaseEnabled && CatalogPreviewPolicy.shouldRenderLivePreview(
                                    sectionIndex = sectionIndex,
                                    activeSectionIndex = settledSectionIndex,
                                    itemIndex = index,
                                    centeredItemIndex = settledItemIndex,
                                    parentScrollInProgress = columnState.isScrollInProgress,
                                    rowScrollInProgress = listState.isScrollInProgress
                                )
                                val shouldRenderCardChrome = CatalogPreviewPolicy.shouldRenderCardChrome(
                                    parentScrollInProgress = columnState.isScrollInProgress,
                                    rowScrollInProgress = listState.isScrollInProgress
                                )
                                Column(
                                    modifier = Modifier.width(cardWidth),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    WallpaperCard(
                                        item = item,
                                        mountLivePreview = shouldMountPreview,
                                        playLivePreview = shouldPlayPreview,
                                        showChrome = shouldRenderCardChrome,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(cardHeight),
                                        onClick = { onItemClick(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BottomNavBar(
                    selectedItem = 0,
                    animationsEnabled = false,
                    onItemSelected = { index ->
                        if (index == 1) onSettingsClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun WallpaperCard(
    item: WallpaperCatalogUiItem,
    mountLivePreview: Boolean,
    playLivePreview: Boolean,
    showChrome: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val targetWidthPx = remember(density) { with(density) { 276.dp.roundToPx() } }
    val targetHeightPx = remember(density) { with(density) { 524.dp.roundToPx() } }
    val bitmap = rememberAssetBitmap(item.thumbnail, targetWidthPx, targetHeightPx)
    val mountLivePreviewWhenWarm = CatalogPreviewPolicy.shouldStartLivePreview(
        showLivePreview = mountLivePreview,
        warmupReady = bitmap != null
    )
    var rendererDayProgress by remember(item.id) { mutableStateOf<Float?>(null) }

    val calendar = Calendar.getInstance()
    val timeText = CatalogPreviewPolicy.formatBadgeTime(
        rendererDayProgress = rendererDayProgress,
        fallbackHour = calendar.get(Calendar.HOUR_OF_DAY),
        fallbackMinute = calendar.get(Calendar.MINUTE)
    )

    Card(
        modifier = modifier
            .padding(vertical = 3.dp)
            .clickable(onClick = onClick),
        shape = PlaceholderOuterShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(PlaceholderOuterShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(PlaceholderOuterShape)
                        .background(brush = PlaceholderBaseBrush)
                )
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

            if (mountLivePreviewWhenWarm) {
                LumiskyWallpaperPreviewView(
                    wallpaperId = item.id,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(PlaceholderOuterShape),
                    playPlayback = playLivePreview,
                    runtimeProfile = RuntimeProfile.catalogCardPreview(),
                    onDayProgressChanged = { progress ->
                        rendererDayProgress = progress
                    }
                )
                // Capture touch events in Compose to prevent SurfaceView window interception
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onClick)
                )
            }

            if (showChrome) {
                // Title at bottom
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.58f),
                                offset = Offset(0f, 2f),
                                blurRadius = 5f
                            )
                        )
                    )
                }

                val badgeShape = RoundedCornerShape(999.dp)

                // Time badge at top-left
                Text(
                    text = timeText,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 14.dp, start = 14.dp)
                        .clip(badgeShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, Color.White.copy(alpha = 0.34f), badgeShape)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                )

                // Row of badges at top-right
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 14.dp, end = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.id == "sky") {
                        Text(
                            text = "Parallax",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier
                                .clip(badgeShape)
                                .background(Color(0xFF3B8AD9).copy(alpha = 0.34f))
                                .border(1.dp, Color(0xFF9FD8FF).copy(alpha = 0.42f), badgeShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (item.isPremium) {
                        Text(
                            text = "Premium",
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier
                                .clip(badgeShape)
                                .background(Color(0xFF3B8AD9).copy(alpha = 0.34f))
                                .border(1.dp, Color(0xFF9FD8FF).copy(alpha = 0.42f), badgeShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    val perfLabel = if (item.id == "starter_gradient") "Efficient" else "Smooth"
                    Text(
                        text = perfLabel,
                        maxLines = 1,
                        softWrap = false,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier
                            .clip(badgeShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.34f), badgeShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick)
            )
        }
    }
}
