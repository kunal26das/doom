package com.kunal26das.doom.data

import com.kunal26das.doom.domain.ImportedGameSelection

import com.kunal26das.doom.domain.MAX_WAD_BYTES

internal object StoredImportedGame {
    private const val MAGIC = "DOOMWAD1"
    private const val HEADER_BYTES = 10
    private const val MAX_NAME_BYTES = 512
    const val MAX_RECORD_BYTES = MAX_WAD_BYTES + HEADER_BYTES + MAX_NAME_BYTES

    fun encode(game: ImportedGameSelection): ByteArray {
        val name = game.name.encodeToByteArray()
        require(name.size <= MAX_NAME_BYTES)
        return ByteArray(HEADER_BYTES + name.size + game.bytes.size).also { record ->
            MAGIC.encodeToByteArray().copyInto(record)
            record[8] = name.size.toByte()
            record[9] = (name.size ushr 8).toByte()
            name.copyInto(record, HEADER_BYTES)
            game.bytes.copyInto(record, HEADER_BYTES + name.size)
        }
    }

    fun decode(record: ByteArray): ImportedGameSelection? {
        if (record.isEmpty()) return null
        require(record.size in HEADER_BYTES..MAX_RECORD_BYTES && record.decodeToString(0, 8) == MAGIC) {
            "The saved game file is damaged. Choose your original DOOM.WAD again."
        }
        val nameSize = (record[8].toInt() and 0xff) or ((record[9].toInt() and 0xff) shl 8)
        require(nameSize in 1..MAX_NAME_BYTES && HEADER_BYTES + nameSize <= record.size) {
            "The saved game file is incomplete. Choose your original DOOM.WAD again."
        }
        return ImportedGameSelection(
            record.decodeToString(HEADER_BYTES, HEADER_BYTES + nameSize),
            record.copyOfRange(HEADER_BYTES + nameSize, record.size),
        )
    }
}
