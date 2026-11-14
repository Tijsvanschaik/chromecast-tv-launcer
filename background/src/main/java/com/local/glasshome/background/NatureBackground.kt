package com.local.glasshome.background

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * Calm nature plate. Default is still (HD-safe).
 * Loop asset is Phase 4; flag defaults off via BuildConfig.NATURE_LOOP_ENABLED.
 */
@Composable
fun NatureBackground(
    mode: NatureBackgroundMode = NatureBackgroundMode.Still,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val effectiveMode = when {
        !BuildConfig.NATURE_LOOP_ENABLED -> NatureBackgroundMode.Still
        mode == NatureBackgroundMode.Loop -> NatureBackgroundMode.Still // no loop asset in v1 scaffold
        else -> NatureBackgroundMode.Still
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Pause hook for future loop playback (Phase 4).
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                // no-op for still
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val stillBitmap = remember(effectiveMode) {
        runCatching {
            context.assets.open(STILL_ASSET).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D10)),
    ) {
        if (stillBitmap != null) {
            Image(
                bitmap = stillBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private const val STILL_ASSET = "calm-nature-still.webp"
