package com.kunal26das.doom.startup

import io.github.kunal26das.startup.DefaultContext
import io.github.kunal26das.startup.Startup
import io.github.kunal26das.startup.initializerKey
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame

object ApplicationStartupVerificationProcess {
    @JvmStatic
    fun main(args: Array<String>) {
        FileStorageInitializer::class.java.getConstructor().newInstance()

        val first = initializeApplication(DefaultContext)
        val startup = Startup.getInstance(DefaultContext)
        val files = startup.initializeComponent(initializerKey<FileStorageInitializer>())

        val second = initializeApplication(DefaultContext)

        assertSame(startup, Startup.getInstance(DefaultContext))
        assertSame(files, startup.initializeComponent(initializerKey<FileStorageInitializer>()))
        assertNotSame(first.createViewModel(), second.createViewModel())
        assertFalse(File(System.getProperty("user.home"), ".doom-kmp").exists())
    }
}
