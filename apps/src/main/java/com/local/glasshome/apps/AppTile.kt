package com.local.glasshome.apps

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.local.glasshome.glass.GlassCard
import com.local.glasshome.glass.GlassTheme

/**
 * Banner apps show their art as the whole card face, with the name fading in under the card
 * while focused. Apps without a banner show icon + name on glass.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppTile(
    app: AppEntry,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    moving: Boolean = false,
    dimmed: Boolean = false,
) {
    val colors = GlassTheme.colors
    val type = GlassTheme.type
    val packageManager = LocalContext.current.packageManager
    var art by remember(app.packageName) { mutableStateOf(AppArtCache.peek(app.packageName)) }
    LaunchedEffect(app.packageName) {
        art = AppArtCache.load(packageManager, app)
    }
    var focused by remember { mutableStateOf(false) }
    val isBanner = art?.isBanner == true
    val captionAlpha by animateFloatAsState(
        targetValue = if (moving || (focused && isBanner)) 1f else 0f,
        animationSpec = tween(durationMillis = 160, easing = LinearOutSlowInEasing),
        label = "caption",
    )

    Column(modifier = Modifier.width(width).alpha(if (dimmed) 0.45f else 1f)) {
        GlassCard(
            onClick = onClick,
            onLongClick = onLongClick,
            width = width,
            height = height,
            contentPadding = if (isBanner) PaddingValues(0.dp) else PaddingValues(14.dp),
            modifier = modifier.onFocusChanged { focused = it.isFocused || it.hasFocus },
        ) {
            val current = art
            if (current != null && current.isBanner) {
                Image(
                    bitmap = current.bitmap,
                    contentDescription = app.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    val iconModifier = Modifier.size(height * 0.36f).clip(RoundedCornerShape(8.dp))
                    if (current != null) {
                        Image(bitmap = current.bitmap, contentDescription = null, modifier = iconModifier)
                    } else {
                        Box(modifier = iconModifier.background(colors.iconFallback))
                    }
                    Text(
                        text = app.label,
                        fontSize = type.cardLabelSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (moving) "◀  ${app.label}  ▶" else app.label,
            color = colors.label,
            fontSize = type.hintSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.alpha(captionAlpha),
        )
    }
}
