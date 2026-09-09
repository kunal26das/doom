package com.kunal26das.doom.presentation

import androidx.lifecycle.ViewModel

internal class GameHostTrackedViewModel : ViewModel() {
    var clearCalls = 0
        private set

    override fun onCleared() {
        clearCalls++
    }
}
