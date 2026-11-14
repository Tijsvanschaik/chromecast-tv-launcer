package com.local.glasshome.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

/** Vertical scrim over the nature plate (design-lock §6). */
@Composable
fun GlassScrim(modifier: Modifier = Modifier) {
    val colors = GlassTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to colors.scrimTop,
                        0.28f to colors.scrimTop,
                        0.45f to colors.scrimMid,
                        0.62f to colors.scrimMid,
                        1.00f to colors.scrimBottom,
                    ),
                ),
            ),
    )
}
