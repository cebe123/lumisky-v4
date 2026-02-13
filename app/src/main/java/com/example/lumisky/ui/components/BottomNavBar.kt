package com.example.lumisky.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
	selectedItem: Int,
	onItemSelected: (Int) -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
			.padding(bottom = 16.dp)
			.windowInsetsPadding(WindowInsets.navigationBars)
			.height(64.dp),
		shape = RoundedCornerShape(32.dp),
		color = MaterialTheme.colorScheme.surfaceContainer,
		tonalElevation = 16.dp,
		shadowElevation = 12.dp
	) {
		NavigationBar(
			containerColor = Color.Transparent,
			contentColor = Color.White,
			modifier = Modifier.fillMaxSize(),
			windowInsets = WindowInsets(0, 0, 0, 0)
		) {
			NavigationBarItem(
				icon = {
					Icon(
						Icons.Filled.Home,
						contentDescription = "Home",
						modifier = Modifier.size(32.dp)
					)
				},
				label = null,
				selected = selectedItem == 0,
				onClick = { onItemSelected(0) },
				colors = NavigationBarItemDefaults.colors(
					selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
					unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
					indicatorColor = MaterialTheme.colorScheme.secondaryContainer
				)
			)
			NavigationBarItem(
				icon = {
					Icon(
						Icons.Filled.Settings,
						contentDescription = "Settings",
						modifier = Modifier.size(32.dp)
					)
				},
				label = null,
				selected = selectedItem == 1,
				onClick = { onItemSelected(1) },
				colors = NavigationBarItemDefaults.colors(
					selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
					unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
					indicatorColor = MaterialTheme.colorScheme.secondaryContainer
				)
			)
		}
	}
}
