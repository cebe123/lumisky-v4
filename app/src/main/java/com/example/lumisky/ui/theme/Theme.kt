package com.example.lumisky.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
	background = androidx.compose.ui.graphics.Color(0xFF0D0E13),
	surface = androidx.compose.ui.graphics.Color(0xFF121319),
	surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1E1F26),
	surfaceTint = androidx.compose.ui.graphics.Color(0xFF24252D),
	primary = androidx.compose.ui.graphics.Color(0xFF81ECFF),
	onPrimary = androidx.compose.ui.graphics.Color(0xFF003840),
	primaryContainer = androidx.compose.ui.graphics.Color(0xFF004E5F),
	onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFC6F5FF),
	secondary = androidx.compose.ui.graphics.Color(0xFFB884FF),
	secondaryContainer = androidx.compose.ui.graphics.Color(0xFF4B3672),
	onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFF0DBFF),
	tertiary = androidx.compose.ui.graphics.Color(0xFFFF84AA),
	tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF71233F),
	onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFD9E3),
	onSurface = androidx.compose.ui.graphics.Color(0xFFF7F5FD),
	onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFABAAB1),
	outline = androidx.compose.ui.graphics.Color(0xFF47474E)
)

private val LightColorScheme = lightColorScheme(
	background = androidx.compose.ui.graphics.Color(0xFFF8F9FE),
	surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
	surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF0F2F8),
	surfaceTint = androidx.compose.ui.graphics.Color(0xFFE6E9F2),
	primary = androidx.compose.ui.graphics.Color(0xFF00677D),
	onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
	primaryContainer = androidx.compose.ui.graphics.Color(0xFFD7F4FF),
	onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF003640),
	secondary = androidx.compose.ui.graphics.Color(0xFF6A4FA3),
	secondaryContainer = androidx.compose.ui.graphics.Color(0xFFEAE0FF),
	onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF25124A),
	tertiary = androidx.compose.ui.graphics.Color(0xFFA13C68),
	tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFD9E6),
	onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF45142A),
	onSurface = androidx.compose.ui.graphics.Color(0xFF1B1B1F),
	onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF45464F),
	outline = androidx.compose.ui.graphics.Color(0xFFB0B0B8)
)

@Composable
fun LumiskyTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography,
		content = content
	)
}
