package com.kunal26das.doom.di

import com.kunal26das.doom.data.BundledWadRepository
import com.kunal26das.doom.data.DoomEngineAdapter
import com.kunal26das.doom.data.GameFileRepository
import com.kunal26das.doom.data.audio.DmxSoundDriver
import com.kunal26das.doom.domain.FileRepository
import com.kunal26das.doom.domain.GameLaunchSelection
import com.kunal26das.doom.domain.ImportedGameRepository
import com.kunal26das.doom.domain.RunGameSession
import com.kunal26das.doom.presentation.GameViewModel
import com.kunal26das.doom.presentation.GameHostViewModel

/** The composition root is the only place that chooses concrete implementations. */
class GameDependencies(
    private val files: FileRepository,
    private val importedGames: ImportedGameRepository,
) {
    fun createHostViewModel(): GameHostViewModel = GameHostViewModel(importedGames)

    fun createViewModel(selection: GameLaunchSelection): GameViewModel = GameViewModel(
        RunGameSession(
            BundledWadRepository(selection),
            DoomEngineAdapter(GameFileRepository(files, selection), ::DmxSoundDriver),
        ),
    )
}
