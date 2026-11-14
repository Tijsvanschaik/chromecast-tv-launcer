package com.local.glasshome.apps

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.local.glasshome.glass.GlassCard
import com.local.glasshome.glass.GlassTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsAction(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = GlassTheme.colors
    val type = GlassTheme.type

    GlassCard(
        width = 148.dp,
        height = 56.dp,
        modifier = modifier
            .focusable()
            .clickable {
                runCatching {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Settings",
                color = colors.label,
                fontSize = type.cardLabelSize,
            )
        }
    }
}
