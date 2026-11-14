package com.local.glasshome.glass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp

/**
 * Frosted tile: translucent fill + hairline edge + optional focus ring/scale.
 * No live RenderEffect blur (design lock).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    width: Dp = GlassTheme.dimens.gridTileWidth,
    height: Dp = GlassTheme.dimens.gridTileHeight,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = GlassTheme.colors
    val dimens = GlassTheme.dimens
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) dimens.focusScale else 1f,
        animationSpec = tween(
            durationMillis = dimens.focusDurationMs,
            easing = FastOutSlowInEasing,
        ),
        label = "glassFocusScale",
    )
    val shape = RoundedCornerShape(dimens.cardCorner)
    val fill = if (focused) colors.cardFillFocused else colors.cardFill
    val edge = if (focused) colors.cardEdgeFocused else colors.cardEdge

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .onFocusChanged { focused = it.isFocused }
            .scale(scale)
            .clip(shape)
            .background(fill, shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.noiseOverlay, colors.noiseOverlay.copy(alpha = 0f)),
                ),
                shape,
            )
            .border(dimens.cardHairline, edge, shape)
            .then(
                if (focused) {
                    Modifier.border(
                        width = dimens.focusRingWidth,
                        brush = SolidColor(colors.focusRing),
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = dimens.cardPaddingH, vertical = dimens.cardPaddingV),
        content = content,
    )
}

/** Focus ring + scale wiring helper for non-GlassCard focusables. */
fun Modifier.glassFocusModifier(onFocusChange: (Boolean) -> Unit): Modifier =
    onFocusChanged { onFocusChange(it.isFocused) }
