package com.local.glasshome.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.local.glasshome.glass.GlassTheme

@Composable
fun AppsGrid(
    apps: List<AppEntry>,
    onLaunch: (AppEntry) -> Unit,
    onAppInfo: (AppEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = GlassTheme.dimens
    LazyVerticalGrid(
        columns = GridCells.Fixed(dimens.gridColumnsHd),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.gridGapH),
        verticalArrangement = Arrangement.spacedBy(dimens.gridGapV),
        contentPadding = PaddingValues(0.dp),
        userScrollEnabled = true,
    ) {
        items(apps, key = { it.componentKey }) { app ->
            AppTile(
                entry = app,
                width = dimens.gridTileWidth,
                height = dimens.gridTileHeight,
                iconSize = 28.dp,
                onLaunch = { onLaunch(app) },
                onAppInfo = { onAppInfo(app) },
            )
        }
    }
}
