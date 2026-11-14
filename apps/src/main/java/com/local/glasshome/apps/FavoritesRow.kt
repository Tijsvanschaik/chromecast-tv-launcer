package com.local.glasshome.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun FavoritesRow(
    favorites: List<PlaceholderApp>,
    modifier: Modifier = Modifier,
) {
    val dimens = GlassTheme.dimens
    val colors = GlassTheme.colors
    val type = GlassTheme.type

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimens.gridGapH),
        contentPadding = PaddingValues(0.dp),
    ) {
        items(favorites, key = { it.id }) { app ->
            GlassCard(
                width = dimens.favoriteTileWidth,
                height = dimens.favoriteTileHeight,
                modifier = Modifier.focusable(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.iconFallback, RoundedCornerShape(8.dp)),
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
