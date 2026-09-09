package com.kunal26das.doom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kunal26das.doom.di.GameDependencies
import com.kunal26das.doom.domain.FrameClock
import com.kunal26das.doom.presentation.GameScreen
import com.kunal26das.doom.presentation.GameViewModel
import com.kunal26das.doom.presentation.GameHostViewModel
import com.kunal26das.doom.presentation.GameEntry
import com.kunal26das.doom.presentation.launcher.OwnedGameScreen

@Composable
fun App(
    dependencies: GameDependencies,
    host: GameHostViewModel = viewModel { dependencies.createHostViewModel() },
) {
    val state by host.state.collectAsState()
    val game = state.game
    if (game == null) {
        OwnedGameScreen(state, host::importGame, host::showImportError, host::playWithoutSaving, host::playDemo)
    } else {
        key(game) {
            GameRoute(game, dependencies, host::restart, host::changeFile)
        }
    }
}

@Composable
private fun GameRoute(
    entry: GameEntry,
    dependencies: GameDependencies,
    onRestart: () -> Unit,
    onChangeFile: () -> Unit,
) {
    val model: GameViewModel = viewModel(viewModelStoreOwner = entry) {
        dependencies.createViewModel(entry.selection)
    }
    val state by model.state.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(model, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            model.run(FrameClock { withFrameNanos { } })
        }
    }

    GameScreen(state, model::onInput, isTouchPlatform, onRestart, onChangeFile)
}
