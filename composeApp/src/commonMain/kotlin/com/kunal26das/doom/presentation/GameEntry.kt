package com.kunal26das.doom.presentation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.kunal26das.doom.domain.GameLaunchSelection

class GameEntry(val selection: GameLaunchSelection) : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}
