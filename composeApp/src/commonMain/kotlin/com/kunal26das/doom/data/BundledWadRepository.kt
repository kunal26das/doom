package com.kunal26das.doom.data

import com.kunal26das.doom.domain.WadRepository
import doom.composeapp.generated.resources.Res

class BundledWadRepository : WadRepository {
    private var cached: List<ByteArray>? = null

    override suspend fun load(): List<ByteArray> =
        cached ?: listOf(Res.readBytes("files/doom1.wad")).also { cached = it }
}
