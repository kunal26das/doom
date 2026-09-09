package com.kunal26das.doom.domain

interface FileRepository {
    fun read(name: String): ByteArray?
    fun write(name: String, data: ByteArray)
}
