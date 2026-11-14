package com.local.glasshome.apps

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.local.glasshome.glass.GlassCard
import com.local.glasshome.glass.GlassTheme

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppTile(
    entry: AppEntry,
    width: Dp,
    height: Dp,
    iconSize: Dp,
    onLaunch: () -> Unit,
    onAppInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GlassTheme.colors
    val type = GlassTheme.type
    val bitmap = remember(entry.componentKey, entry.icon) {
        entry.icon?.let { drawable ->
            runCatching {
                drawable.toBitmap(
                    width = drawable.intrinsicWidth.coerceAtLeast(1),
                    height = drawable.intrinsicHeight.coerceAtLeast(1),
                ).asImageBitmap()
            }.getOrNull()
        }
    }

    GlassCard(
        width = width,
        height = height,
        modifier = modifier
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = onAppInfo,
            )
            .focusable(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = entry.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(iconSize),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .background(colors.iconFallback, RoundedCornerShape(6.dp)),
                )
            }
            Text(
                text = entry.label,
                color = colors.label,
                fontSize = type.cardLabelSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
