package com.kunal26das.doom.domain

/** Supplies the game data in the order in which the engine should load it. */
fun interface WadRepository {
    suspend fun load(): List<ByteArray>
}
