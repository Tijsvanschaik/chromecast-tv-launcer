package com.local.glasshome.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.local.glasshome.glass.GlassPill

/**
 * Apps grid as rows inside the home LazyColumn, so the hero and favorites scroll away with it
 * and there is a single scroll container for D-pad bring-into-view.
 */
fun LazyListScope.appsGrid(
    apps: List<AppEntry>,
    columns: Int,
    tileWidth: Dp,
    tileHeight: Dp,
    gapH: Dp,
    gapV: Dp,
    horizontalInset: Dp,
    hidden: Set<String>,
    hiddenInstalledCount: Int,
    showingHidden: Boolean,
    onLaunch: (AppEntry) -> Unit,
    onLongPress: (AppEntry) -> Unit,
    onToggleHidden: () -> Unit,
    firstItemModifier: Modifier = Modifier,
) {
    val firstPackage = apps.firstOrNull()?.packageName
    items(apps.chunked(columns), key = { row -> "row:" + row.first().packageName }) { row ->
        Row(
            modifier = Modifier.padding(start = horizontalInset, end = horizontalInset, bottom = gapV),
            horizontalArrangement = Arrangement.spacedBy(gapH),
        ) {
            for (app in row) {
                AppTile(
                    app = app,
                    width = tileWidth,
                    height = tileHeight,
                    onClick = { onLaunch(app) },
                    onLongClick = { onLongPress(app) },
                    dimmed = app.packageName in hidden,
                    modifier = if (app.packageName == firstPackage) firstItemModifier else Modifier,
                )
            }
        }
    }
    if (hiddenInstalledCount > 0) {
        item(key = "hidden-toggle") {
            GlassPill(
                text = if (showingHidden) "Done" else "Hidden apps ($hiddenInstalledCount)",
                onClick = onToggleHidden,
                width = 200.dp,
                modifier = Modifier.padding(start = horizontalInset, top = 8.dp),
            )
        }
    }
}
