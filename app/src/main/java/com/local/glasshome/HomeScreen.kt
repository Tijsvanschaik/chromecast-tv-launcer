package com.local.glasshome

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.local.glasshome.apps.AppEntry
import com.local.glasshome.apps.AppsViewModel
import com.local.glasshome.apps.FavoritesRow
import com.local.glasshome.apps.appsGrid
import com.local.glasshome.background.NatureBackground
import com.local.glasshome.glass.GlassPill
import com.local.glasshome.glass.GlassScrim
import com.local.glasshome.glass.GlassTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(vm: AppsViewModel = viewModel()) {
    val model by vm.model.collectAsState()
    val banner by vm.banner.collectAsState()
    var actionsFor by remember { mutableStateOf<AppEntry?>(null) }
    var actionsFromFavorites by remember { mutableStateOf(false) }
    var moving by remember { mutableStateOf<AppEntry?>(null) }
    var refocusFirst by remember { mutableStateOf(false) }
    val dimens = GlassTheme.dimens
    val colors = GlassTheme.colors
    val type = GlassTheme.type

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val enter by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "rowEnter",
    )

    val firstFocus = remember { FocusRequester() }
    var didInitialFocus by remember { mutableStateOf(false) }
    LaunchedEffect(model) {
        if (!model.loading && (!didInitialFocus || refocusFirst)) {
            runCatching { firstFocus.requestFocus() }
            didInitialFocus = true
            refocusFirst = false
        }
    }

    BackHandler(enabled = moving != null) { moving = null }

    Box(modifier = Modifier.fillMaxSize()) {
        NatureBackground()
        GlassScrim()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = enter
                    translationY = (1f - enter) * 8.dp.toPx()
                },
            contentPadding = PaddingValues(top = dimens.overscan, bottom = dimens.overscan),
        ) {
            item(key = "hero") {
                Hero(
                    onWifi = vm::openWifi,
                    onBluetooth = vm::openBluetooth,
                    onSettings = vm::openSettings,
                    settingsModifier = if (model.isEmpty && !model.loading) Modifier.focusRequester(firstFocus) else Modifier,
                    modifier = Modifier.padding(horizontal = dimens.overscan),
                )
            }
            item(key = "hero-gap") { Spacer(modifier = Modifier.height(dimens.rowGap)) }

            when {
                model.loading -> item(key = "loading") { Hint("Loading apps…") }
                model.isEmpty -> item(key = "empty") { Hint(model.emptyHint.orEmpty()) }
                else -> {
                    item(key = "favorites-header") { SectionHeader("Favorites") }
                    item(key = "favorites") {
                        if (model.favorites.isEmpty()) {
                            Hint("Hold OK on an app to pin it here.")
                        } else {
                            FavoritesRow(
                                favorites = model.favorites,
                                onLaunch = vm::launch,
                                onLongPress = {
                                    actionsFor = it
                                    actionsFromFavorites = true
                                },
                                moving = moving,
                                onMove = vm::moveFavorite,
                                onMoveDone = { moving = null },
                                firstItemModifier = Modifier.focusRequester(firstFocus),
                            )
                        }
                    }
                    item(key = "favorites-gap") { Spacer(modifier = Modifier.height(dimens.rowGap - 8.dp)) }
                    item(key = "apps-header") { SectionHeader("Apps") }
                    appsGrid(
                        apps = model.grid,
                        columns = dimens.gridColumnsHd,
                        tileWidth = dimens.gridTileWidth,
                        tileHeight = dimens.gridTileHeight,
                        gapH = dimens.gridGapH,
                        gapV = dimens.gridGapV,
                        horizontalInset = dimens.overscan,
                        hidden = model.hidden,
                        hiddenInstalledCount = model.hiddenInstalledCount,
                        showingHidden = model.showingHidden,
                        onLaunch = vm::launch,
                        onLongPress = {
                            actionsFor = it
                            actionsFromFavorites = false
                        },
                        onToggleHidden = vm::toggleShowHidden,
                        firstItemModifier = if (model.favorites.isEmpty()) Modifier.focusRequester(firstFocus) else Modifier,
                    )
                }
            }
        }

        actionsFor?.let { entry ->
            val pinned = model.favorites.any { it.packageName == entry.packageName }
            val hidden = entry.packageName in model.hidden
            ActionSheet(
                label = entry.label,
                actions = buildList {
                    add((if (pinned) "Unpin" else "Pin to Favorites") to {
                        vm.setPinned(entry, pinned = !pinned)
                        if (pinned) refocusFirst = true
                    })
                    if (pinned && actionsFromFavorites && model.favorites.size > 1) add("Move" to { moving = entry })
                    add((if (hidden) "Unhide" else "Hide") to {
                        vm.setHidden(entry, hidden = !hidden)
                        if (!hidden) refocusFirst = true
                    })
                    add("App info" to { vm.openAppInfo(entry) })
                },
                onClose = { actionsFor = null },
            )
        }

        banner?.let { text ->
            Text(
                text = text,
                color = colors.title,
                fontSize = type.hintSize,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimens.overscan)
                    .background(colors.cardFillFocused, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun Hero(
    onWifi: () -> Unit,
    onBluetooth: () -> Unit,
    onSettings: () -> Unit,
    settingsModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val colors = GlassTheme.colors
    val type = GlassTheme.type
    val now = rememberClock()
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = now.time,
                color = colors.title,
                fontSize = type.clockSize,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp,
            )
            Text(text = now.date, color = colors.labelMuted, fontSize = type.dateSize)
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassPill(text = "Wi‑Fi", onClick = onWifi, width = 96.dp)
            GlassPill(text = "Bluetooth", onClick = onBluetooth, width = 120.dp)
            GlassPill(text = "Settings", onClick = onSettings, width = 112.dp, modifier = settingsModifier)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        color = GlassTheme.colors.labelMuted,
        fontSize = GlassTheme.type.sectionSize,
        fontWeight = FontWeight.Medium,
        letterSpacing = GlassTheme.type.sectionTracking,
        modifier = Modifier.padding(
            start = GlassTheme.dimens.overscan,
            bottom = GlassTheme.dimens.headerToRow,
        ),
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        color = GlassTheme.colors.labelMuted,
        fontSize = GlassTheme.type.hintSize,
        modifier = Modifier.padding(horizontal = GlassTheme.dimens.overscan),
    )
}

/** Dialog window: focus can't reach the grid behind it, and Back closes it. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ActionSheet(
    label: String,
    actions: List<Pair<String, () -> Unit>>,
    onClose: () -> Unit,
) {
    val colors = GlassTheme.colors
    val shape = RoundedCornerShape(24.dp)
    val firstAction = remember { FocusRequester() }
    Dialog(onDismissRequest = onClose) {
        LaunchedEffect(Unit) { runCatching { firstAction.requestFocus() } }
        Column(
            modifier = Modifier
                .width(360.dp)
                .background(Color(0xF2141820), shape)
                .border(1.dp, colors.cardEdge, shape)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = label, color = colors.title, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            actions.forEachIndexed { index, (text, action) ->
                GlassPill(
                    text = text,
                    onClick = {
                        onClose()
                        action()
                    },
                    width = 312.dp,
                    height = 48.dp,
                    modifier = if (index == 0) Modifier.focusRequester(firstAction) else Modifier,
                )
            }
        }
    }
}
