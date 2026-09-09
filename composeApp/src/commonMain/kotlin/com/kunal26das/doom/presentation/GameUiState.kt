package com.kunal26das.doom.presentation

import com.kunal26das.doom.domain.GameFrame


data class GameUiState(
    val phase: GamePhase = GamePhase.Loading,
    val frame: GameFrame? = null,
    val error: String? = null,
)
