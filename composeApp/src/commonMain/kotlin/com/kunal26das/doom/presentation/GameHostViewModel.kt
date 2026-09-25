package com.kunal26das.doom.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameHostViewModel : ViewModel() {
    private val current = MutableStateFlow(GameEntry())
    val game: StateFlow<GameEntry> = current.asStateFlow()

    fun restart() {
        val previous = current.value
        current.value = GameEntry()
        previous.viewModelStore.clear()
    }

    override fun onCleared() {
        current.value.viewModelStore.clear()
    }
}
