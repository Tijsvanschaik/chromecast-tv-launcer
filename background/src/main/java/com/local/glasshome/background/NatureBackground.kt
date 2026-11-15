package com.local.glasshome.background

import android.content.res.AssetManager
import android.graphics.ImageDecoder
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class NatureBackgroundMode {
    Still,
    Loop,
}

/**
 * Calm nature plate. Rotates between bundled stills every [ROTATION_HOURS] hours; the slot is
 * re-checked only while the activity is started, so nothing runs in the background.
 * Loop playback stays off (BuildConfig.NATURE_LOOP_ENABLED = false) to keep HD frame time for focus.
 */
@Composable
fun NatureBackground(
    mode: NatureBackgroundMode = NatureBackgroundMode.Still,
    modifier: Modifier = Modifier,
) {
    val assets = LocalContext.current.assets
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var slot by remember { mutableIntStateOf(currentSlot()) }

    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                slot = currentSlot()
                delay(millisToNextSlot())
            }
        }
    }

    val plate by produceState<ImageBitmap?>(initialValue = null, slot) {
        value = withContext(Dispatchers.IO) { decodeStill(assets, STILLS[slot % STILLS.size]) }
    }

    Crossfade(
        targetState = plate,
        animationSpec = tween(durationMillis = 1_200),
        modifier = modifier.fillMaxSize(),
        label = "plate",
    ) { bitmap ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

private fun decodeStill(assets: AssetManager, name: String): ImageBitmap? = runCatching {
    val source = ImageDecoder.createSource(assets, name)
    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_HARDWARE
    }.asImageBitmap()
}.getOrNull()

private const val ROTATION_HOURS = 3
private const val SLOT_MS = ROTATION_HOURS * 60L * 60L * 1_000L

private fun currentSlot(): Int = (System.currentTimeMillis() / SLOT_MS).toInt()

private fun millisToNextSlot(): Long = SLOT_MS - System.currentTimeMillis() % SLOT_MS

private val STILLS = listOf(
    "calm-nature-still.webp",
    "calm-alpine-lake.webp",
    "calm-fog-forest.webp",
    "calm-dunes-dusk.webp",
)
