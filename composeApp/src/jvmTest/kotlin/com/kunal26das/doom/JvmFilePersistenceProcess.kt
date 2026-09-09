package com.kunal26das.doom

import com.kunal26das.doom.startup.FileStorageInitializer
import com.kunal26das.doom.startup.initializeApplication
import io.github.kunal26das.startup.DefaultContext
import io.github.kunal26das.startup.Startup
import io.github.kunal26das.startup.initializerKey
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

object JvmFilePersistenceProcess {
    @JvmStatic
    fun main(args: Array<String>) {
        initializeApplication(DefaultContext)
        val files = Startup.getInstance(DefaultContext).initializeComponent(initializerKey<FileStorageInitializer>())
        val bytes = ByteArray(65_793) { it.toByte() }
        when (args.single()) {
            "write" -> files.write("doomsav2.dsg", bytes)
            "read" -> assertContentEquals(bytes, files.read("doomsav2.dsg"))
            else -> error("Unknown verification mode")
        }
        val directory = File(System.getProperty("user.home"), ".doom-kmp")
        assertEquals(listOf("doomsav2.dsg"), directory.list()?.toList())
        assertContentEquals(bytes, File(directory, "doomsav2.dsg").readBytes())
    }
}
