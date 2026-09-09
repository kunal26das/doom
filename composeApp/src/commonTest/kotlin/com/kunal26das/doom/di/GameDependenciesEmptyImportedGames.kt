package com.kunal26das.doom.di

import com.kunal26das.doom.domain.ImportedGameSelection

import com.kunal26das.doom.domain.GameLaunchSelection
import com.kunal26das.doom.domain.ImportedGameRepository

internal class GameDependenciesEmptyImportedGames : ImportedGameRepository {
    var loads = 0
    override suspend fun load(): ImportedGameSelection? {
        loads++
        return null
    }
    override suspend fun save(game: ImportedGameSelection) = error("Playing the demo must not replace an import")
    override suspend fun clear() = error("Restart must not clear the remembered import")
}
