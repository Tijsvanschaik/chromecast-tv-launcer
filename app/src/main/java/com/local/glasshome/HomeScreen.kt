package com.local.glasshome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.local.glasshome.apps.AppsActions
import com.local.glasshome.apps.AppsGrid
import com.local.glasshome.apps.AppsUiModel
import com.local.glasshome.apps.EmptyAppsHint
import com.local.glasshome.apps.FavoritesRow
import com.local.glasshome.apps.InstalledAppsRepository
import com.local.glasshome.apps.SettingsAction
import com.local.glasshome.background.NatureBackground
import com.local.glasshome.background.NatureBackgroundMode
import com.local.glasshome.glass.GlassScrim
import com.local.glasshome.glass.GlassTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: InstalledAppsRepository,
    actions: AppsActions,
) {
    val dimens = GlassTheme.dimens
    val colors = GlassTheme.colors
    val type = GlassTheme.type
    val model by repository.observe().collectAsStateWithLifecycle(initialValue = AppsUiModel())

    DisposableEffect(repository) {
        repository.start()
        onDispose { repository.stop() }
    }

    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NatureBackground(mode = NatureBackgroundMode.Still)
        GlassScrim()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimens.overscan)
                .alpha(enter.value)
                .offset(y = ((1f - enter.value) * 8f).dp),
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
                SettingsAction(onOpenSettings = actions::openSettings)
            }

            Spacer(modifier = Modifier.height(dimens.titleToFavorites))

            if (model.isEmpty) {
                EmptyAppsHint(hint = model.emptyHint.orEmpty())
            } else {
                if (model.favorites.isNotEmpty()) {
                    Text(
                        text = "Favorites",
                        color = colors.labelMuted,
                        fontSize = type.hintSize,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FavoritesRow(
                        favorites = model.favorites,
                        onLaunch = actions::launch,
                        onAppInfo = actions::openAppInfo,
                    )
                    Spacer(modifier = Modifier.height(dimens.rowGap))
                }

                Text(
                    text = "Apps",
                    color = colors.labelMuted,
                    fontSize = type.hintSize,
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppsGrid(
                    apps = model.grid,
                    onLaunch = actions::launch,
                    onAppInfo = actions::openAppInfo,
                    modifier = Modifier.weight(1f, fill = true),
                )
            }
        }
    }
}

@Composable
fun rememberAppsRuntime(): Pair<InstalledAppsRepository, AppsActions> {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { InstalledAppsRepository(context) }
    val actions = remember(repository) { AppsActions(context, repository) }
    return repository to actions
}
