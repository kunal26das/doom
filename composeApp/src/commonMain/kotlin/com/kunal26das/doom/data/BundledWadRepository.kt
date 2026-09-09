package com.kunal26das.doom.data

import com.kunal26das.doom.domain.BuiltInGameSelection
import com.kunal26das.doom.domain.ImportedGameSelection

import com.kunal26das.doom.domain.WadRepository
import com.kunal26das.doom.domain.GameLaunchSelection
import doom.composeapp.generated.resources.Res

/** Resource loading stays outside the domain and is cached across lifecycle resumes. */
class BundledWadRepository(
    private val selection: GameLaunchSelection = BuiltInGameSelection,
) : WadRepository {
    private var cached: List<ByteArray>? = null

    override suspend fun load(): List<ByteArray> = cached ?: listOf(
        when (val game = selection) {
            BuiltInGameSelection -> Res.readBytes("files/doom1.wad")
            is ImportedGameSelection -> game.bytes
        },
    ).also { cached = it }
}
