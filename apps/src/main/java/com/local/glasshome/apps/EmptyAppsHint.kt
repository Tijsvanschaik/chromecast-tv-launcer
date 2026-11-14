package com.local.glasshome.apps

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.local.glasshome.glass.GlassTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EmptyAppsHint(
    hint: String,
    modifier: Modifier = Modifier,
) {
    val colors = GlassTheme.colors
    val type = GlassTheme.type
    Text(
        text = hint,
        color = colors.labelMuted,
        fontSize = type.hintSize,
        modifier = modifier.padding(top = 8.dp),
    )
}
