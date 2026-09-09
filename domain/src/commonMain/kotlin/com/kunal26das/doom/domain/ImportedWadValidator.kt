package com.kunal26das.doom.domain

const val MAX_WAD_BYTES: Int = 64 * 1024 * 1024



object ImportedWadValidator {
    fun validate(bytes: ByteArray): ImportedWadInformation {
        require(bytes.size <= MAX_WAD_BYTES) { "Choose a WAD no larger than 64 MB." }
        require(bytes.size >= 12) { "This file is too short to be a WAD." }
        val magic = bytes.ascii(0, 4)
        require(magic != "PWAD") {
            "This is an add-on WAD. Choose a complete DOOM or DOOM II IWAD instead."
        }
        require(magic == "IWAD") { "Choose a DOOM or DOOM II IWAD file (.wad)." }

        val count = bytes.littleEndianInt(4)
        val directory = bytes.littleEndianInt(8)
        require(count <= 65_536) { "This WAD has too many entries for a classic base game. Choose the original IWAD." }
        require(count > 0 && directory >= 12 && directory.toLong() + count.toLong() * 16 <= bytes.size) {
            "The WAD directory is incomplete or invalid. Try the original game file."
        }
        val names = mutableSetOf<String>()
        repeat(count) { index ->
            val entry = directory + index * 16
            val offset = bytes.littleEndianInt(entry)
            val size = bytes.littleEndianInt(entry + 4)
            require(offset >= 0 && size >= 0 && offset.toLong() + size <= bytes.size) {
                "The WAD contains incomplete game data. Try the original game file."
            }
            names += bytes.ascii(entry + 8, 8).substringBefore('\u0000').uppercase()
        }
        require(listOf("PLAYPAL", "COLORMAP", "TEXTURE1", "PNAMES", "M_DOOM").all { it in names }) {
            "This WAD does not contain the base assets required by DOOM. Choose a DOOM or DOOM II IWAD."
        }
        return when {
            "MAP01" in names -> ImportedWadInformation(
                WadMapStyle.NumberedMaps,
                names.count { it.length == 5 && it.startsWith("MAP") && it[3].isDigit() && it[4].isDigit() },
            )
            "E1M1" in names -> ImportedWadInformation(
                WadMapStyle.Episodes,
                names.count { it.length == 4 && it[0] == 'E' && it[1] in '1'..'9' && it[2] == 'M' && it[3] in '1'..'9' },
            )
            else -> throw IllegalArgumentException("This WAD has no supported DOOM maps.")
        }
    }

    private fun ByteArray.littleEndianInt(offset: Int): Int =
        (this[offset].toInt() and 0xff) or
            ((this[offset + 1].toInt() and 0xff) shl 8) or
            ((this[offset + 2].toInt() and 0xff) shl 16) or
            ((this[offset + 3].toInt() and 0xff) shl 24)

    private fun ByteArray.ascii(offset: Int, count: Int): String =
        buildString(count) { repeat(count) { append((this@ascii[offset + it].toInt() and 0xff).toChar()) } }
}
