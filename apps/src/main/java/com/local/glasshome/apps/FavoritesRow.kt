package com.local.glasshome.apps

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.key
import com.local.glasshome.glass.GlassTheme

/**
 * Pinned apps in pin order. While [moving] is set, Left/Right move that card and every other
 * key except OK/Back is swallowed so focus stays on it; OK or Back calls [onMoveDone].
 */
@Composable
fun FavoritesRow(
    favorites: List<AppEntry>,
    onLaunch: (AppEntry) -> Unit,
    onLongPress: (AppEntry) -> Unit,
    moving: AppEntry?,
    onMove: (AppEntry, Int) -> Unit,
    onMoveDone: () -> Unit,
    modifier: Modifier = Modifier,
    firstItemModifier: Modifier = Modifier,
) {
    val dimens = GlassTheme.dimens
    val listState = rememberLazyListState()
    val movingIndex = moving?.let { m -> favorites.indexOfFirst { it.packageName == m.packageName } } ?: -1
    LaunchedEffect(movingIndex) {
        if (movingIndex >= 0) listState.animateScrollToItem((movingIndex - 1).coerceAtLeast(0))
    }
    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { event ->
                val target = moving ?: return@onPreviewKeyEvent false
                when (event.key.nativeKeyCode) {
                    AndroidKeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (event.type == KeyEventType.KeyDown) onMove(target, -1)
                        true
                    }
                    AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (event.type == KeyEventType.KeyDown) onMove(target, 1)
                        true
                    }
                    AndroidKeyEvent.KEYCODE_DPAD_CENTER,
                    AndroidKeyEvent.KEYCODE_ENTER,
                    AndroidKeyEvent.KEYCODE_NUMPAD_ENTER,
                    -> {
                        if (event.type == KeyEventType.KeyUp) onMoveDone()
                        true
                    }
                    AndroidKeyEvent.KEYCODE_BACK -> false
                    else -> true
                }
            },
        horizontalArrangement = Arrangement.spacedBy(dimens.gridGapH),
        contentPadding = PaddingValues(horizontal = dimens.overscan),
    ) {
        items(favorites, key = { it.packageName }) { app ->
            AppTile(
                app = app,
                width = dimens.favoriteTileWidth,
                height = dimens.favoriteTileHeight,
                onClick = { onLaunch(app) },
                onLongClick = { onLongPress(app) },
                moving = moving?.packageName == app.packageName,
                modifier = if (app.packageName == favorites.first().packageName) firstItemModifier else Modifier,
            )
        }
    }
}
