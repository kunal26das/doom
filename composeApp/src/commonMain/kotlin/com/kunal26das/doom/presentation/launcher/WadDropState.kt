package com.kunal26das.doom.presentation.launcher

/** Platform input state only; imported bytes follow the same validation path as the picker. */
data class WadDropState(val supported: Boolean = false, val isDragging: Boolean = false)
