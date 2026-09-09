package com.kunal26das.doom.data

import com.kunal26das.doom.domain.BuiltInGameSelection
import com.kunal26das.doom.domain.ImportedGameSelection

import com.kunal26das.doom.domain.FileRepository
import com.kunal26das.doom.domain.GameLaunchSelection

class GameFileRepository(
    private val delegate: FileRepository,
    selection: GameLaunchSelection,
) : FileRepository {
    private val prefix = when (selection) {
        BuiltInGameSelection -> ""
        is ImportedGameSelection -> "wad-${fingerprint(selection.bytes)}-"
    }

    override fun read(name: String): ByteArray? = delegate.read(prefix + name)
    override fun write(name: String, data: ByteArray) = delegate.write(prefix + name, data)

    private fun fingerprint(bytes: ByteArray): String {
        var hash = 14695981039346656037uL
        for (byte in bytes) hash = (hash xor byte.toUByte().toULong()) * 1099511628211uL
        return hash.toString(16)
    }
}
