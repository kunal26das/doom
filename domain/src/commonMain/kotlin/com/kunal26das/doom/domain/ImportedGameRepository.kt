package com.kunal26das.doom.domain

interface ImportedGameRepository {
    suspend fun load(): ImportedGameSelection?
    suspend fun save(game: ImportedGameSelection)
    suspend fun clear()
}
