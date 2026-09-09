package com.kunal26das.doom.domain

fun interface WadRepository {
    suspend fun load(): List<ByteArray>
}
