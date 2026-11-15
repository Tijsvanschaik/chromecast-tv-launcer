package com.local.glasshome.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Text

/**
 * Frosted tile: translucent fill, hairline, top specular edge, focus ring, scale and glow.
 * D-pad OK calls [onClick]. Hold OK calls [onLongClick].
 * No live RenderEffect blur; the glow is drawn only for the one focused card.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    width: Dp = GlassTheme.dimens.gridTileWidth,
    height: Dp = GlassTheme.dimens.gridTileHeight,
    corner: Dp = GlassTheme.dimens.cardCorner,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = GlassTheme.dimens.cardPaddingH,
        vertical = GlassTheme.dimens.cardPaddingV,
    ),
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = GlassTheme.colors
    val dimens = GlassTheme.dimens
    val shape = RoundedCornerShape(corner)
    val focusedBorder = Border(
        border = BorderStroke(dimens.focusRingWidth, colors.focusRing),
        inset = 0.dp,
        shape = shape,
    )
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .width(width)
            .height(height),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = colors.cardFill,
            contentColor = colors.label,
            focusedContainerColor = colors.cardFillFocused,
            focusedContentColor = colors.title,
            pressedContainerColor = colors.cardFillFocused,
            pressedContentColor = colors.title,
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
            focusedBorder = focusedBorder,
            pressedBorder = focusedBorder,
        ),
        glow = CardDefaults.glow(
            glow = Glow.None,
            focusedGlow = Glow(elevationColor = colors.focusGlow, elevation = dimens.focusGlowElevation),
            pressedGlow = Glow.None,
        ),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to colors.cardHighlight,
                        0.04f to colors.noiseOverlay,
                        1f to Color.Transparent,
                    ),
                )
                .padding(contentPadding),
            content = content,
        )
    }
}

/** Small glass control for the hero (quick settings) and the action sheet. */
@Composable
fun GlassPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 120.dp,
    height: Dp = 44.dp,
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier,
        width = width,
        height = height,
        corner = height / 2,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = GlassTheme.type.hintSize,
                maxLines = 1,
            )
        }
    }
}
