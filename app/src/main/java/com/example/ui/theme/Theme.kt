package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = CobaltPrimaryDark,
    onPrimary = OnCobaltPrimaryDark,
    primaryContainer = CobaltPrimaryContainerDark,
    onPrimaryContainer = OnCobaltPrimaryContainerDark,
    secondary = XboxEmeraldSecondaryDark,
    onSecondary = OnXboxEmeraldSecondaryDark,
    secondaryContainer = XboxEmeraldContainerDark,
    onSecondaryContainer = OnXboxEmeraldContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = OnAmberTertiaryDark,
    tertiaryContainer = AmberTertiaryContainerDark,
    onTertiaryContainer = OnAmberTertiaryContainerDark,
    background = SlateBackgroundDark,
    onBackground = OnSlateBackgroundDark,
    surface = SlateSurfaceDark,
    onSurface = OnSlateSurfaceDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = OnSlateSurfaceVariantDark,
    outline = SlateOutlineDark,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

private val LightColorScheme = lightColorScheme(
    primary = CobaltPrimaryLight,
    onPrimary = OnCobaltPrimaryLight,
    primaryContainer = CobaltPrimaryContainerLight,
    onPrimaryContainer = OnCobaltPrimaryContainerLight,
    secondary = XboxEmeraldSecondaryLight,
    onSecondary = OnXboxEmeraldSecondaryLight,
    secondaryContainer = XboxEmeraldContainerLight,
    onSecondaryContainer = OnXboxEmeraldContainerLight,
    tertiary = AmberTertiaryLight,
    onTertiary = OnAmberTertiaryLight,
    tertiaryContainer = AmberTertiaryContainerLight,
    onTertiaryContainer = OnAmberTertiaryContainerLight,
    background = SlateBackgroundLight,
    onBackground = OnSlateBackgroundLight,
    surface = SlateSurfaceLight,
    onSurface = OnSlateSurfaceLight,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = OnSlateSurfaceVariantLight,
    outline = SlateOutlineLight,
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
