package com.kunal26das.doom

import com.kunal26das.doom.domain.FileRepository
import io.github.kunal26das.startup.StartupContext
import kotlinx.browser.localStorage
import kotlin.io.encoding.Base64

internal actual fun createPlatformFileRepository(context: StartupContext): FileRepository =
    BrowserFileRepository()

/** Each localStorage item stores one complete, standard Base64 save/config file. */
private class BrowserFileRepository : FileRepository {
    override fun read(name: String): ByteArray? {
        val encoded = localStorage.getItem("doom:$name") ?: return null
        return Base64.decode(encoded)
    }

    override fun write(name: String, data: ByteArray) {
        localStorage.setItem("doom:$name", Base64.encode(data))
    }
}
