/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Ui katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Ui katmanı bileşeni.
 */
package com.adnan.lumisky.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
	background = Color(0xFF0D0E13),
	surface = Color(0xFF121319),
	surfaceVariant = Color(0xFF1E1F26),
	surfaceTint = Color(0xFF24252D),
	primary = Color(0xFF81ECFF),
	onPrimary = Color(0xFF003840),
	primaryContainer = Color(0xFF004E5F),
	onPrimaryContainer = Color(0xFFC6F5FF),
	secondary = Color(0xFFB884FF),
	secondaryContainer = Color(0xFF4B3672),
	onSecondaryContainer = Color(0xFFF0DBFF),
	tertiary = Color(0xFFFF84AA),
	tertiaryContainer = Color(0xFF71233F),
	onTertiaryContainer = Color(0xFFFFD9E3),
	onSurface = Color(0xFFF7F5FD),
	onSurfaceVariant = Color(0xFFABAAB1),
	outline = Color(0xFF47474E)
)

private val LightColorScheme = lightColorScheme(
	background = Color(0xFFF8F9FE),
	surface = Color(0xFFFFFFFF),
	surfaceVariant = Color(0xFFF0F2F8),
	surfaceTint = Color(0xFFE6E9F2),
	primary = Color(0xFF00677D),
	onPrimary = Color(0xFFFFFFFF),
	primaryContainer = Color(0xFFD7F4FF),
	onPrimaryContainer = Color(0xFF003640),
	secondary = Color(0xFF6A4FA3),
	secondaryContainer = Color(0xFFEAE0FF),
	onSecondaryContainer = Color(0xFF25124A),
	tertiary = Color(0xFFA13C68),
	tertiaryContainer = Color(0xFFFFD9E6),
	onTertiaryContainer = Color(0xFF45142A),
	onSurface = Color(0xFF1B1B1F),
	onSurfaceVariant = Color(0xFF45464F),
	outline = Color(0xFFB0B0B8)
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
