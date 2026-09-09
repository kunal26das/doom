package com.kunal26das.doom.presentation

import com.kunal26das.doom.domain.BuiltInGameSelection
import com.kunal26das.doom.domain.ImportedGameSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunal26das.doom.domain.GameLaunchSelection
import com.kunal26das.doom.domain.ImportedGameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameHostViewModel(private val repository: ImportedGameRepository) : ViewModel() {
    private val current = MutableStateFlow(GameHostState())
    val state = current.asStateFlow()
    private var pendingGame: ImportedGameSelection? = null

    init {
        viewModelScope.launch {
            try {
                val saved = repository.load()
                if (saved == null) current.value = GameHostState(isBusy = false)
                else start(saved)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                current.value = GameHostState(
                    isBusy = false,
                    error = "Your saved game file could not be opened. Choose DOOM.WAD again.",
                )
            }
        }
    }

    fun importGame(name: String, bytes: ByteArray) {
        if (current.value.isBusy || current.value.game != null) return
        pendingGame = null
        current.value = GameHostState(isBusy = true)
        viewModelScope.launch {
            val selected = try {
                ImportedGameSelection(name, bytes)
            } catch (invalid: IllegalArgumentException) {
                current.value = GameHostState(isBusy = false, error = invalid.message)
                return@launch
            }
            try {
                repository.save(selected)
                start(selected)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                pendingGame = selected
                current.value = GameHostState(
                    isBusy = false,
                    error = "This device could not remember the file. You can still play for this visit.",
                    canPlayWithoutSaving = true,
                )
            }
        }
    }

    fun showImportError(message: String) {
        if (!current.value.isBusy && current.value.game == null) {
            pendingGame = null
            current.value = GameHostState(isBusy = false, error = message)
        }
    }

    fun playWithoutSaving() {
        pendingGame?.let(::start)
    }

    fun playDemo() {
        if (!current.value.isBusy && current.value.game == null) {
            start(BuiltInGameSelection)
        }
    }

    fun restart() {
        current.value.game?.selection?.let(::start)
    }

    fun changeFile() {
        if (current.value.isBusy) return
        val previous = current.value.game
        pendingGame = null
        current.value = GameHostState(isBusy = false)
        previous?.viewModelStore?.clear()
    }

    private fun start(selection: GameLaunchSelection) {
        val previous = current.value.game
        pendingGame = null
        current.value = GameHostState(isBusy = false, game = GameEntry(selection))
        previous?.viewModelStore?.clear()
    }

    override fun onCleared() {
        pendingGame = null
        current.value.game?.viewModelStore?.clear()
    }
}
