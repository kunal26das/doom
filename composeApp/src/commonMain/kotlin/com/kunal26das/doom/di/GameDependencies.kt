package com.kunal26das.doom.di

import com.kunal26das.doom.data.BundledWadRepository
import com.kunal26das.doom.data.DoomEngineAdapter
import com.kunal26das.doom.data.audio.DmxSoundDriver
import com.kunal26das.doom.domain.FileRepository
import com.kunal26das.doom.domain.RunGameSession
import com.kunal26das.doom.presentation.GameViewModel
import com.kunal26das.doom.presentation.GameHostViewModel

class GameDependencies(private val files: FileRepository) {
    fun createHostViewModel(): GameHostViewModel = GameHostViewModel()

    fun createViewModel(): GameViewModel = GameViewModel(
        RunGameSession(
            BundledWadRepository(),
            DoomEngineAdapter(files, ::DmxSoundDriver),
        ),
    )
}
