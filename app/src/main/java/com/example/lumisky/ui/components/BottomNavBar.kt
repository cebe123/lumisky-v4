package com.example.lumisky.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lumisky.R

@Composable
fun BottomNavBar(
	selectedItem: Int,
	onItemSelected: (Int) -> Unit
) {
	val isHomeBackdrop = selectedItem == HOME_INDEX
	val frameShape = RoundedCornerShape(999.dp)
	val frameBorderColor = if (isHomeBackdrop) {
		Color.White.copy(alpha = 0.18f)
	} else {
		MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
	}
	val frameBackground = if (isHomeBackdrop) {
		listOf(
			Color.White.copy(alpha = 0.09f),
			Color(0xFF0C1A2C).copy(alpha = 0.28f)
		)
	} else {
		listOf(
			Color.White.copy(alpha = 0.12f),
			MaterialTheme.colorScheme.surface.copy(alpha = 0.20f)
		)
	}

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 8.dp)
			.windowInsetsPadding(WindowInsets.navigationBars),
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = Modifier
				.clip(frameShape)
				.background(brush = Brush.linearGradient(frameBackground))
				.border(width = 1.dp, color = frameBorderColor, shape = frameShape)
				.padding(horizontal = 8.dp, vertical = 8.dp)
		) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				GlassNavItem(
					icon = Icons.Filled.Home,
					label = stringResource(R.string.nav_home),
					selected = selectedItem == HOME_INDEX,
					isHomeBackdrop = isHomeBackdrop,
					onClick = { onItemSelected(HOME_INDEX) }
				)
				GlassNavItem(
					icon = Icons.Filled.Settings,
					label = stringResource(R.string.nav_settings),
					selected = selectedItem == SETTINGS_INDEX,
					isHomeBackdrop = isHomeBackdrop,
					onClick = { onItemSelected(SETTINGS_INDEX) }
				)
			}
		}
	}
}

@Composable
private fun GlassNavItem(
	icon: ImageVector,
	label: String,
	selected: Boolean,
	isHomeBackdrop: Boolean,
	onClick: () -> Unit
) {
	val contentColor by animateColorAsState(
		targetValue = when {
			selected && isHomeBackdrop -> Color.White
			selected -> MaterialTheme.colorScheme.primary
			isHomeBackdrop -> Color.White.copy(alpha = 0.82f)
			else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f)
		},
		animationSpec = tween(durationMillis = 220),
		label = "bottom_nav_content_color"
	)
	val borderColor by animateColorAsState(
		targetValue = when {
			selected && isHomeBackdrop -> Color.White.copy(alpha = 0.16f)
			selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
			isHomeBackdrop -> Color.White.copy(alpha = 0.08f)
			else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
		},
		animationSpec = tween(durationMillis = 220),
		label = "bottom_nav_border_color"
	)
	val glassGradient = if (isHomeBackdrop) {
		listOf(
			Color.White.copy(alpha = if (selected) 0.20f else 0.14f),
			Color(0xFF102238).copy(alpha = if (selected) 0.46f else 0.34f)
		)
	} else if (selected) {
		listOf(
			Color.White.copy(alpha = 0.26f),
			MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
		)
	} else {
		listOf(
			Color.White.copy(alpha = 0.18f),
			MaterialTheme.colorScheme.surface.copy(alpha = 0.24f)
		)
	}
	val topGlow = if (isHomeBackdrop) {
		Color.White.copy(alpha = if (selected) 0.11f else 0.06f)
	} else {
		MaterialTheme.colorScheme.primary.copy(alpha = if (selected) 0.08f else 0.04f)
	}
	val pillWidth by animateDpAsState(
		targetValue = if (selected) 147.dp else 62.dp,
		animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
		label = "bottom_nav_width"
	)

	Box(
		modifier = Modifier
			.width(pillWidth)
			.height(53.dp)
			.clip(CircleShape)
			.background(brush = Brush.linearGradient(glassGradient))
			.border(width = 1.dp, color = borderColor, shape = CircleShape)
			.clickable(onClick = onClick),
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					brush = Brush.radialGradient(
						colors = listOf(topGlow, Color.Transparent),
						center = Offset(56f, -8f),
						radius = 150f
					)
				)
		)
		Row(
			modifier = Modifier
				.animateContentSize()
				.padding(horizontal = 14.dp, vertical = 9.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = label,
				modifier = Modifier.size(21.dp),
				tint = contentColor
			)
			AnimatedVisibility(
				visible = selected,
				enter = expandHorizontally(
					expandFrom = Alignment.Start,
					animationSpec = tween(
						durationMillis = 260,
						easing = FastOutSlowInEasing
					)
				) + fadeIn(
					animationSpec = tween(durationMillis = 180, delayMillis = 50)
				),
				exit = shrinkHorizontally(
					shrinkTowards = Alignment.Start,
					animationSpec = tween(durationMillis = 180)
				) + fadeOut(
					animationSpec = tween(durationMillis = 120)
				)
			) {
				Text(
					text = label,
					modifier = Modifier.padding(start = 7.dp),
					style = MaterialTheme.typography.labelMedium.copy(
						fontWeight = FontWeight.SemiBold
					),
					color = contentColor,
					maxLines = 1
				)
			}
		}
	}
}

private const val HOME_INDEX = 0
private const val SETTINGS_INDEX = 1
