package com.local.glasshome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.local.glasshome.apps.AppsGrid
import com.local.glasshome.apps.FavoritesRow
import com.local.glasshome.apps.PlaceholderCatalog
import com.local.glasshome.apps.SettingsAction
import com.local.glasshome.background.NatureBackground
import com.local.glasshome.background.NatureBackgroundMode
import com.local.glasshome.glass.GlassScrim
import com.local.glasshome.glass.GlassTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen() {
    val dimens = GlassTheme.dimens
    val colors = GlassTheme.colors
    val type = GlassTheme.type

    Box(modifier = Modifier.fillMaxSize()) {
        NatureBackground(mode = NatureBackgroundMode.Still)
        GlassScrim()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimens.overscan),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Glass Home",
                    color = colors.title,
                    fontSize = type.titleSize,
                )
                SettingsAction()
            }

            Spacer(modifier = Modifier.height(dimens.titleToFavorites))

            Text(
                text = "Favorites",
                color = colors.labelMuted,
                fontSize = type.hintSize,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FavoritesRow(favorites = PlaceholderCatalog.favorites)

            Spacer(modifier = Modifier.height(dimens.rowGap))

            Text(
                text = "Apps",
                color = colors.labelMuted,
                fontSize = type.hintSize,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppsGrid(
                apps = PlaceholderCatalog.grid,
                modifier = Modifier.weight(1f, fill = true),
            )
        }
    }
}
