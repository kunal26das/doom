package com.kunal26das.doom.presentation

data class GameHostState(
    val isBusy: Boolean = true,
    val game: GameEntry? = null,
    val error: String? = null,
    val canPlayWithoutSaving: Boolean = false,
)
