package com.kunal26das.doom.startup

import com.kunal26das.doom.domain.BuiltInGameSelection

import com.kunal26das.doom.domain.GameLaunchSelection
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
        // AndroidX constructs these reflectively, ignoring Kotlin manifest factories.
        FileStorageInitializer::class.java.getConstructor().newInstance()
        ImportedGameStorageInitializer::class.java.getConstructor().newInstance()

        val first = initializeApplication(DefaultContext)
        val startup = Startup.getInstance(DefaultContext)
        val files = startup.initializeComponent(initializerKey<FileStorageInitializer>())
        val importedGames = startup.initializeComponent(initializerKey<ImportedGameStorageInitializer>())

        val second = initializeApplication(DefaultContext)

        assertSame(startup, Startup.getInstance(DefaultContext))
        assertSame(files, startup.initializeComponent(initializerKey<FileStorageInitializer>()))
        assertSame(importedGames, startup.initializeComponent(initializerKey<ImportedGameStorageInitializer>()))
        assertNotSame(
            first.createViewModel(BuiltInGameSelection),
            second.createViewModel(BuiltInGameSelection),
        )
        assertFalse(File(System.getProperty("user.home"), ".doom-kmp").exists())
    }
}
