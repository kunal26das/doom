package com.kunal26das.doom.domain

/** Private device storage for the player's base game, never a network upload. */
interface ImportedGameRepository {
    /** Restores and validates the last imported game, or returns null when none is stored. */
    suspend fun load(): ImportedGameSelection?
    suspend fun save(game: ImportedGameSelection)
    suspend fun clear()
}
