package com.local.glasshome.background

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

enum class NatureBackgroundMode {
    Still,
    Loop,
}

/**
 * Calm nature plate API (nature-background contract).
 *
 * - Default mode: [NatureBackgroundMode.Still]
 * - Loop stays off unless [BuildConfig.NATURE_LOOP_ENABLED] is true **and** a loop asset exists
 * - Playback / decode pauses when the hosting activity is not resumed
 *
 * Glass applies the design-lock scrim on top of this plate.
 */
@Composable
fun NatureBackground(
    mode: NatureBackgroundMode = NatureBackgroundMode.Still,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var resumed by remember { mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> resumed = true
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP,
                -> {
                    resumed = false
                    // Loop playback would stop here (no loop asset in v1; flag defaults false).
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val loopAllowed = BuildConfig.NATURE_LOOP_ENABLED && mode == NatureBackgroundMode.Loop && LOOP_ASSET_PRESENT
    val effectiveMode =
        if (loopAllowed && resumed) NatureBackgroundMode.Loop else NatureBackgroundMode.Still

    val stillBitmap: ImageBitmap? = remember(effectiveMode, resumed) {
        if (!resumed && effectiveMode == NatureBackgroundMode.Loop) {
            null
        } else {
            runCatching {
                context.assets.open(STILL_ASSET).use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D10)),
    ) {
        // Still is the shipped plate. Loop asset is not packaged yet (Phase 4 flag off).
        if (stillBitmap != null && (resumed || effectiveMode == NatureBackgroundMode.Still)) {
            Image(
                bitmap = stillBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Packaged calm still — original generated asset from the Project store. */
private const val STILL_ASSET = "calm-nature-still.webp"

/** No loop file in v1; keep false until a capped silent loop is packaged. */
private const val LOOP_ASSET_PRESENT = false
