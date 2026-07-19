package com.kunal26das.doom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import doom.composeapp.generated.resources.Res
import doom.engine.D_DoomMain
import doom.engine.D_DoomStep
import doom.engine.DoomError
import doom.engine.I_VideoSink
import doom.engine.soundDriver
import kotlinx.coroutines.CancellationException

/**
 * Boots the engine and drives it at display refresh rate on the UI thread
 * (the engine itself decides how many 35Hz tics to run per step, exactly
 * like the original DoomLoop). Single-threaded by design: the engine is a
 * sea of global state, same as the C original.
 */
@Composable
fun App() {
    var frame by remember { mutableStateOf<ImageBitmap?>(null) }
    var crash by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            wireEnginePersistence()
            val wad = Res.readBytes("files/doom1.wad")

            val mixer = DmxSoundDriver()
            soundDriver = mixer
            startAudioOutput(mixer::render)

            I_VideoSink = { px ->
                // A transient presentation failure (Skiko/GL hiccup) must
                // drop the frame, not unwind the whole game loop.
                try {
                    frame = frameToImageBitmap(px)
                } catch (_: RuntimeException) {
                }
            }

            D_DoomMain(listOf(wad), emptyList())

            while (true) {
                withFrameNanos { }
                D_DoomStep()
            }
        } catch (e: DoomError) {
            crash = e.message
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            crash = "Engine crashed: $e"
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { handleKeyEvent(it) },
        contentAlignment = Alignment.Center,
    ) {
        val f = frame
        if (f != null) {
            Image(
                painter = BitmapPainter(f, filterQuality = FilterQuality.None),
                contentDescription = "DOOM",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().aspectRatio(4f / 3f),
            )
        } else {
            Text(crash ?: "DOOM", color = Color.Red)
        }
        if (isTouchPlatform) {
            TouchControls(Modifier.fillMaxSize())
        }
    }
}
