package com.kunal26das.doom.presentation

import com.kunal26das.doom.domain.ImportedGameSelection

import com.kunal26das.doom.domain.GameLaunchSelection
import com.kunal26das.doom.domain.ImportedGameRepository

internal class GameHostFakeRepository : ImportedGameRepository {
    var loadCalls = 0
        private set
    var clearCalls = 0
        private set
    val saveAttempts = mutableListOf<ImportedGameSelection>()
    var onLoad: suspend () -> ImportedGameSelection? = { null }
    var onSave: suspend (ImportedGameSelection) -> Unit = {}

    override suspend fun load(): ImportedGameSelection? {
        loadCalls++
        return onLoad()
    }

    override suspend fun save(game: ImportedGameSelection) {
        saveAttempts += game
        onSave(game)
    }

    override suspend fun clear() {
        clearCalls++
    }
}
