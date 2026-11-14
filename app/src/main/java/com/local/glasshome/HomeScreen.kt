package com.local.glasshome

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.local.glasshome.apps.AppEntry
import com.local.glasshome.apps.AppsController
import com.local.glasshome.apps.AppsGrid
import com.local.glasshome.apps.FavoritesRow
import com.local.glasshome.apps.SettingsAction
import com.local.glasshome.background.NatureBackground
import com.local.glasshome.background.NatureBackgroundMode
import com.local.glasshome.glass.GlassCard
import com.local.glasshome.glass.GlassScrim
import com.local.glasshome.glass.GlassTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val controller = remember { AppsController(context) }
    DisposableEffect(controller) {
        onDispose { controller.close() }
    }
    val model by controller.model.collectAsState()
    val banner by controller.banner.collectAsState()
    var actionsFor by remember { mutableStateOf<AppEntry?>(null) }
    val dimens = GlassTheme.dimens
    val colors = GlassTheme.colors
    val type = GlassTheme.type

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val enterAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "rowEnter",
    )

    val firstFocus = remember { FocusRequester() }
    var didInitialFocus by remember { mutableStateOf(false) }
    LaunchedEffect(model.loading, model.favorites.size, model.grid.size, model.isEmpty) {
        if (!model.loading && !didInitialFocus) {
            runCatching { firstFocus.requestFocus() }
            didInitialFocus = true
        }
    }

    BackHandler(enabled = actionsFor != null) {
        actionsFor = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NatureBackground(mode = NatureBackgroundMode.Still)
        GlassScrim()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimens.overscan)
                .alpha(enterAlpha),
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
                val settingsModifier = if (model.isEmpty && !model.loading) {
                    Modifier.focusRequester(firstFocus)
                } else {
                    Modifier
                }
                SettingsAction(
                    onClick = { controller.openSettings() },
                    modifier = settingsModifier,
                )
            }

            Spacer(modifier = Modifier.height(dimens.titleToFavorites))

            when {
                model.loading -> {
                    Text(
                        text = "Loading apps…",
                        color = colors.labelMuted,
                        fontSize = type.hintSize,
                    )
                }
                model.isEmpty -> {
                    Text(
                        text = model.emptyHint.orEmpty(),
                        color = colors.labelMuted,
                        fontSize = type.hintSize,
                    )
                }
                else -> {
                    Text(
                        text = "Favorites",
                        color = colors.labelMuted,
                        fontSize = type.hintSize,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (model.favorites.isEmpty()) {
                        Text(
                            text = "Hold OK on an app to pin it here.",
                            color = colors.labelMuted,
                            fontSize = type.hintSize,
                        )
                    } else {
                        FavoritesRow(
                            favorites = model.favorites,
                            onLaunch = controller::launch,
                            onLongPress = { actionsFor = it },
                            modifier = Modifier,
                            firstItemModifier = Modifier.focusRequester(firstFocus),
                        )
                    }

                    Spacer(modifier = Modifier.height(dimens.rowGap))

                    Text(
                        text = "Apps",
                        color = colors.labelMuted,
                        fontSize = type.hintSize,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppsGrid(
                        apps = model.grid,
                        onLaunch = controller::launch,
                        onLongPress = { actionsFor = it },
                        modifier = Modifier.weight(1f, fill = true),
                        firstItemModifier = if (model.favorites.isEmpty()) {
                            Modifier.focusRequester(firstFocus)
                        } else {
                            Modifier
                        },
                    )
                }
            }
        }

        actionsFor?.let { entry ->
            val pinned = model.favorites.any { it.componentKey == entry.componentKey }
            ActionOverlay(
                label = entry.label,
                pinned = pinned,
                onInfo = {
                    actionsFor = null
                    controller.openAppInfo(entry)
                },
                onTogglePin = {
                    controller.setPinned(entry, pinned = !pinned)
                    actionsFor = null
                },
                onClose = { actionsFor = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(dimens.overscan),
            )
        }

        banner?.let { text ->
            Text(
                text = text,
                color = colors.title,
                fontSize = type.hintSize,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimens.overscan),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ActionOverlay(
    label: String,
    pinned: Boolean,
    onInfo: () -> Unit,
    onTogglePin: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GlassTheme.colors
    val type = GlassTheme.type
    val infoFocus = remember { FocusRequester() }
    LaunchedEffect(label) {
        runCatching { infoFocus.requestFocus() }
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = label,
            color = colors.title,
            fontSize = type.cardLabelSize,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GlassCard(
                onClick = onInfo,
                width = 180.dp,
                height = 64.dp,
                modifier = Modifier.focusRequester(infoFocus),
            ) {
                Text(text = "App info", color = colors.label, fontSize = type.cardLabelSize)
            }
            GlassCard(
                onClick = onTogglePin,
                width = 180.dp,
                height = 64.dp,
            ) {
                Text(
                    text = if (pinned) "Unpin" else "Pin",
                    color = colors.label,
                    fontSize = type.cardLabelSize,
                )
            }
            GlassCard(
                onClick = onClose,
                width = 140.dp,
                height = 64.dp,
            ) {
                Text(text = "Close", color = colors.label, fontSize = type.cardLabelSize)
            }
        }
    }
}
