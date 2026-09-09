package com.kunal26das.doom.domain

data class GameMouseInput(val deltaX: Int, val deltaY: Int = 0) : GameInput
