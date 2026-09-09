package com.kunal26das.doom.data

import com.kunal26das.doom.domain.FileRepository

internal class GameFileRepositoryMemoryFiles : FileRepository {
    val values = mutableMapOf<String, ByteArray>()
    override fun read(name: String): ByteArray? = values[name]
    override fun write(name: String, data: ByteArray) { values[name] = data.copyOf() }
}
