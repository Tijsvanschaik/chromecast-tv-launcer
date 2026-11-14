package com.local.glasshome.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.local.glasshome.glass.GlassCard
import com.local.glasshome.glass.GlassTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppsGrid(
    apps: List<PlaceholderApp>,
    modifier: Modifier = Modifier,
) {
    val dimens = GlassTheme.dimens
    val colors = GlassTheme.colors
    val type = GlassTheme.type

    LazyVerticalGrid(
        columns = GridCells.Fixed(dimens.gridColumnsHd),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.gridGapH),
        verticalArrangement = Arrangement.spacedBy(dimens.gridGapV),
        contentPadding = PaddingValues(0.dp),
        userScrollEnabled = true,
    ) {
        items(apps, key = { it.id }) { app ->
            GlassCard(
                width = dimens.gridTileWidth,
                height = dimens.gridTileHeight,
                modifier = Modifier.focusable(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(colors.iconFallback, RoundedCornerShape(6.dp)),
                    )
                    Text(
                        text = app.label,
                        color = colors.label,
                        fontSize = type.cardLabelSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
