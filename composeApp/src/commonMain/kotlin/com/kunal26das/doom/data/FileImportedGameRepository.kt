package com.kunal26das.doom.data

import com.kunal26das.doom.domain.ImportedGameSelection

import com.kunal26das.doom.domain.FileRepository
import com.kunal26das.doom.domain.ImportedGameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class FileImportedGameRepository(
    private val files: FileRepository,
    private val dispatcher: CoroutineDispatcher,
) : ImportedGameRepository {
    override suspend fun load(): ImportedGameSelection? = withContext(dispatcher) {
        files.read(FILE_NAME)?.let(StoredImportedGame::decode)
    }

    override suspend fun save(game: ImportedGameSelection) = withContext(dispatcher) {
        files.write(FILE_NAME, StoredImportedGame.encode(game))
    }

    override suspend fun clear() = withContext(dispatcher) {
        files.write(FILE_NAME, ByteArray(0))
    }

    private companion object { const val FILE_NAME = "imported-game.dat" }
}
