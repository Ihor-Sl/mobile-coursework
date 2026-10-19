package com.example.meditationapplication.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Індикатор активної вкладки та обраний чип у Material 3 використовують secondaryContainer,
// тому він дорівнює primaryContainer.
private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnSurface,
    secondary = Primary,
    onSecondary = Color.White,
    secondaryContainer = PrimaryContainer,
    onSecondaryContainer = OnSurface,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFFE6E8F0),
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLowest = Surface,
    surfaceContainerLow = Surface,
    surfaceContainer = Surface,
    surfaceContainerHigh = Color(0xFFF1F2F8),
    surfaceContainerHighest = Color(0xFFE6E8F0),
    outline = Outline,
    outlineVariant = Outline,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF1A2050),
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnSurfaceDark,
    secondary = PrimaryDark,
    onSecondary = Color(0xFF1A2050),
    secondaryContainer = PrimaryContainerDark,
    onSecondaryContainer = OnSurfaceDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF2A2E40),
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceDark,
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = Color(0xFF262A3B),
    surfaceContainerHighest = Color(0xFF2E3245),
    outline = OutlineDark,
    outlineVariant = OutlineDark,
)

/** Заокруглення: чипи — 8 dp, картки — 16 dp, обкладинка в плеєрі — 24 dp. */
private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

/** Тема додатка. Кольори не залежать від шпалер (без dynamic color) — лише світла/темна схема. */
@Composable
fun MeditationApplicationTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
