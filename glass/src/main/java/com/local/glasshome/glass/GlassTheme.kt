package com.local.glasshome.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/** Design-lock tokens — docs/launcher/design-lock.md, tightened in docs/launcher/v2-plan.md */
@Immutable
data class GlassColors(
    val base: Color = Color(0xFF0B0D10),
    val scrimTop: Color = Color(0xCC0B0D10),
    val scrimMid: Color = Color(0x8C0B0D10),
    val scrimBottom: Color = Color(0xE00B0D10),
    val cardFill: Color = Color(0x8C141820),
    val cardFillFocused: Color = Color(0xB81A2028),
    val cardEdge: Color = Color(0x24FFFFFF),
    val cardHighlight: Color = Color(0x1FFFFFFF),
    val focusRing: Color = Color(0xE6E8EEF6),
    val focusGlow: Color = Color(0x40E8EEF6),
    val noiseOverlay: Color = Color(0x08FFFFFF),
    val title: Color = Color(0xFFE8EEF6),
    val label: Color = Color(0xFFD4DAE2),
    val labelMuted: Color = Color(0xFF8B939E),
    val iconFallback: Color = Color(0xFF2A313C),
)

@Immutable
data class GlassDimens(
    val overscan: Dp = 48.dp,
    val cardCorner: Dp = 16.dp,
    val cardHairline: Dp = 1.dp,
    val cardPaddingH: Dp = 16.dp,
    val cardPaddingV: Dp = 14.dp,
    val gridTileWidth: Dp = 160.dp,
    val gridTileHeight: Dp = 90.dp,
    val favoriteTileWidth: Dp = 224.dp,
    val favoriteTileHeight: Dp = 126.dp,
    val focusRingWidth: Dp = 2.dp,
    val focusScale: Float = 1.06f,
    val focusGlowElevation: Dp = 14.dp,
    val rowGap: Dp = 28.dp,
    val gridGapH: Dp = 16.dp,
    val gridGapV: Dp = 10.dp,
    val headerToRow: Dp = 12.dp,
    val gridColumnsHd: Int = 5,
)

@Immutable
data class GlassType(
    val clockSize: TextUnit = 64.sp,
    val dateSize: TextUnit = 18.sp,
    val sectionSize: TextUnit = 13.sp,
    val sectionTracking: TextUnit = 1.sp,
    val cardLabelSize: TextUnit = 15.sp,
    val hintSize: TextUnit = 14.sp,
)

val LocalGlassColors = staticCompositionLocalOf { GlassColors() }
val LocalGlassDimens = staticCompositionLocalOf { GlassDimens() }
val LocalGlassType = staticCompositionLocalOf { GlassType() }

object GlassTheme {
    val colors: GlassColors
        @Composable get() = LocalGlassColors.current
    val dimens: GlassDimens
        @Composable get() = LocalGlassDimens.current
    val type: GlassType
        @Composable get() = LocalGlassType.current
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GlassTheme(content: @Composable () -> Unit) {
    val colors = GlassColors()
    val dimens = GlassDimens()
    val type = GlassType()
    CompositionLocalProvider(
        LocalGlassColors provides colors,
        LocalGlassDimens provides dimens,
        LocalGlassType provides type,
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = colors.focusRing,
                onPrimary = colors.base,
                background = colors.base,
                onBackground = colors.title,
                surface = colors.cardFill,
                onSurface = colors.label,
            ),
            content = content,
        )
    }
}
