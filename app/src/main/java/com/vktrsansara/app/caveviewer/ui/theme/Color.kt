package com.vktrsansara.app.caveviewer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Accent Colors
val AccentSkyBlue = Color(0xFF38BDF8)
val AccentRed = Color(0xFFEF4444)

// Dark Theme Colors (CaveViewer2 Spec)
val BgMainDark = Color(0xFF121820)
val BgSurfaceDark = Color(0xFF1A222D)
val BgCardDark = Color(0xFF1E2736)
val BarBackgroundDark = Color(0xD91E2736)
val BorderColorDark = Color(0x1FFFFFFF)
val TextPrimaryDark = Color(0xFFE6EDF3)
val TextSecondaryDark = Color(0xFF8B949E)
val PressedColorDark = Color(0x2938BDF8)

// Light Theme Colors (CaveViewer2 Clean Light)
val BgMainLight = Color(0xFFF1F5F9)
val BgSurfaceLight = Color(0xFFFFFFFF)
val BgCardLight = Color(0xFFFFFFFF)
val BarBackgroundLight = Color(0xD9FFFFFF)
val BorderColorLight = Color(0x1A000000)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)
val PressedColorLight = Color(0x1A0284C7)

// Material 3 Palette tokens
val PrimaryLight = Color(0xFF0284C7)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE0F2FE)
val OnPrimaryContainerLight = Color(0xFF0369A1)

val SecondaryLight = Color(0xFF475569)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFF1F5F9)
val OnSecondaryContainerLight = Color(0xFF0F172A)

val TertiaryLight = Color(0xFF6366F1)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFEEF2FF)
val OnTertiaryContainerLight = Color(0xFF312E81)

val BackgroundLight = BgMainLight
val OnBackgroundLight = TextPrimaryLight
val SurfaceLight = BgSurfaceLight
val OnSurfaceLight = TextPrimaryLight
val SurfaceVariantLight = Color(0xFFE2E8F0)
val OnSurfaceVariantLight = TextSecondaryLight
val OutlineLight = Color(0xFF94A3B8)

val PrimaryDark = AccentSkyBlue
val OnPrimaryDark = Color(0xFF082F49)
val PrimaryContainerDark = Color(0xFF0369A1)
val OnPrimaryContainerDark = Color(0xFFE0F2FE)

val SecondaryDark = Color(0xFF94A3B8)
val OnSecondaryDark = Color(0xFF0F172A)
val SecondaryContainerDark = Color(0xFF334155)
val OnSecondaryContainerDark = Color(0xFFF1F5F9)

val TertiaryDark = Color(0xFFA5B4FC)
val OnTertiaryDark = Color(0xFF1E1B4B)
val TertiaryContainerDark = Color(0xFF3730A3)
val OnTertiaryContainerDark = Color(0xFFEEF2FF)

val BackgroundDark = BgMainDark
val OnBackgroundDark = TextPrimaryDark
val SurfaceDark = BgSurfaceDark
val OnSurfaceDark = TextPrimaryDark
val SurfaceVariantDark = BgCardDark
val OnSurfaceVariantDark = TextSecondaryDark
val OutlineDark = Color(0xFF475569)

/**
 * Dynamic theme colors holder for CaveViewer.
 */
data class CaveViewerColors(
    val bgMain: Color,
    val bgSurface: Color,
    val bgCard: Color,
    val barBackground: Color,
    val borderColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val pressedColor: Color,
    val accent: Color = AccentSkyBlue,
    val accentRed: Color = AccentRed,
    val isDark: Boolean
)

val LocalCaveViewerColors = staticCompositionLocalOf<CaveViewerColors> {
    error("No CaveViewerColors provided")
}

/**
 * Accessor for active theme colors anywhere in Composable tree.
 */
object AppColors {
    val bgMain: Color
        @Composable get() = LocalCaveViewerColors.current.bgMain
    val bgSurface: Color
        @Composable get() = LocalCaveViewerColors.current.bgSurface
    val bgCard: Color
        @Composable get() = LocalCaveViewerColors.current.bgCard
    val barBackground: Color
        @Composable get() = LocalCaveViewerColors.current.barBackground
    val borderColor: Color
        @Composable get() = LocalCaveViewerColors.current.borderColor
    val textPrimary: Color
        @Composable get() = LocalCaveViewerColors.current.textPrimary
    val textSecondary: Color
        @Composable get() = LocalCaveViewerColors.current.textSecondary
    val pressedColor: Color
        @Composable get() = LocalCaveViewerColors.current.pressedColor
    val accent: Color
        @Composable get() = LocalCaveViewerColors.current.accent
    val accentRed: Color
        @Composable get() = LocalCaveViewerColors.current.accentRed
    val isDark: Boolean
        @Composable get() = LocalCaveViewerColors.current.isDark
}