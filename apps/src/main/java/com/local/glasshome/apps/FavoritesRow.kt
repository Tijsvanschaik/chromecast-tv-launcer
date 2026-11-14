package com.local.glasshome.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.local.glasshome.glass.GlassTheme

@Composable
fun FavoritesRow(
    favorites: List<AppEntry>,
    onLaunch: (AppEntry) -> Unit,
    onAppInfo: (AppEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = GlassTheme.dimens
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimens.gridGapH),
        contentPadding = PaddingValues(0.dp),
    ) {
        items(favorites, key = { it.componentKey }) { app ->
            AppTile(
                entry = app,
                width = dimens.favoriteTileWidth,
                height = dimens.favoriteTileHeight,
                iconSize = 36.dp,
                onLaunch = { onLaunch(app) },
                onAppInfo = { onAppInfo(app) },
            )
        }
    }
}
