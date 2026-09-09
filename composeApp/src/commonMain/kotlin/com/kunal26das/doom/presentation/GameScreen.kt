package com.kunal26das.doom.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.kunal26das.doom.domain.GameInput
import com.kunal26das.doom.presentation.input.TouchControls
import com.kunal26das.doom.presentation.input.handleKeyEvent

@Composable
fun GameScreen(
    state: GameUiState,
    onInput: (GameInput) -> Unit,
    showTouchControls: Boolean,
    onRestart: () -> Unit,
    onChangeFile: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val presenter = remember { FramePresenter() }
    val bitmap = remember(state.frame) {
        state.frame?.let { presenter.present(it.pixels) }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        Modifier.fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { handleKeyEvent(it, onInput) },
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                painter = BitmapPainter(bitmap, filterQuality = FilterQuality.None),
                contentDescription = "DOOM",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.aspectRatio(4f / 3f).fillMaxSize(),
            )
        }
        if (showTouchControls && state.phase == GamePhase.Running) {
            TouchControls(onInput, Modifier.fillMaxSize())
        }
        val message = when (state.phase) {
            GamePhase.Loading -> "Loading game…"
            GamePhase.Failed -> state.error ?: "The game could not continue."
            GamePhase.Stopped -> "Game stopped"
            GamePhase.Running -> null
        }
        if (message != null) {
            Text(
                message,
                color = if (state.phase == GamePhase.Failed) Color.Red else Color.White,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.85f)).padding(24.dp),
            )
        }
        if (state.phase == GamePhase.Stopped || state.phase == GamePhase.Failed) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(24.dp).background(Color.Black.copy(alpha = 0.85f)),
            ) {
                TextButton(onClick = onRestart) { Text("RESTART", color = Color.White) }
                TextButton(onClick = onChangeFile) { Text("CHOOSE GAME", color = Color.White) }
            }
        }
    }
}
