package com.kunal26das.doom.data

import com.kunal26das.doom.domain.FileRepository

internal class DoomEngineAdapterMemoryFiles : FileRepository {
    val writes = mutableMapOf<String, ByteArray>()
    override fun read(name: String): ByteArray? = writes[name]?.copyOf()
    override fun write(name: String, data: ByteArray) { writes[name] = data.copyOf() }
}
