package com.kunal26das.doom.domain

/** Named game files, such as settings and saved games. */
interface FileRepository {
    fun read(name: String): ByteArray?
    fun write(name: String, data: ByteArray)
}
