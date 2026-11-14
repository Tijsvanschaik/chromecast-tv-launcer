package com.local.glasshome.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api

/**
 * Frosted tile: translucent fill + hairline + focus ring/scale.
 * D-pad OK calls [onClick]. Hold OK calls [onLongClick].
 * No live RenderEffect blur.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    width: Dp = GlassTheme.dimens.gridTileWidth,
    height: Dp = GlassTheme.dimens.gridTileHeight,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = GlassTheme.colors
    val dimens = GlassTheme.dimens
    val shape = RoundedCornerShape(dimens.cardCorner)
    Card(
        onClick = onClick,
        onLongClick = { onLongClick?.invoke() },
        modifier = modifier
            .width(width)
            .height(height),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = colors.cardFill,
            contentColor = colors.label,
            focusedContainerColor = colors.cardFillFocused,
            focusedContentColor = colors.label,
            pressedContainerColor = colors.cardFillFocused,
            pressedContentColor = colors.label,
        ),
        scale = CardDefaults.scale(
            scale = 1f,
            focusedScale = dimens.focusScale,
            pressedScale = dimens.focusScale,
        ),
        border = CardDefaults.border(
            border = Border(
                border = BorderStroke(dimens.cardHairline, colors.cardEdge),
                inset = 0.dp,
                shape = shape,
            ),
            focusedBorder = Border(
                border = BorderStroke(dimens.focusRingWidth, colors.focusRing),
                inset = 0.dp,
                shape = shape,
            ),
            pressedBorder = Border(
                border = BorderStroke(dimens.focusRingWidth, colors.focusRing),
                inset = 0.dp,
                shape = shape,
            ),
        ),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colors.noiseOverlay, Color.Transparent),
                    ),
                )
                .padding(horizontal = dimens.cardPaddingH, vertical = dimens.cardPaddingV),
            content = content,
        )
    }
}

fun Modifier.glassFocusModifier(onFocusChange: (Boolean) -> Unit): Modifier =
    onFocusChanged { onFocusChange(it.isFocused) }
