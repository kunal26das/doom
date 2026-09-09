package com.kunal26das.doom.data

import com.kunal26das.doom.domain.FileRepository

internal class ImportedGameMemoryFiles : FileRepository {
    var record: ByteArray? = null
    var rejectWrites = false
    override fun read(name: String): ByteArray? = record?.copyOf()
    override fun write(name: String, data: ByteArray) {
        check(!rejectWrites) { "Storage full" }
        record = data.copyOf()
    }
}
