package com.kunal26das.doom.di

import com.kunal26das.doom.domain.FileRepository

internal object GameDependenciesUnusedFiles : FileRepository {
    override fun read(name: String): ByteArray? = error("Creating a game must not read $name")
    override fun write(name: String, data: ByteArray) = error("Creating a game must not write $name")
}
