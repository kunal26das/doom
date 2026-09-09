package com.kunal26das.doom.startup

import com.kunal26das.doom.di.GameDependencies
import io.github.kunal26das.startup.Startup
import io.github.kunal26das.startup.StartupContext
import io.github.kunal26das.startup.StartupManifest
import io.github.kunal26das.startup.initializerKey



internal val applicationStartup = StartupManifest {
    lazyInitializer<FileStorageInitializer> { FileStorageInitializer() }
    metaData<ImportedGameStorageInitializer> { ImportedGameStorageInitializer() }
}

/**
 * Hosts install this graph explicitly, including Android, before creating the UI.
 * Only lightweight storage adapters are cached. Imported-game restoration remains asynchronous,
 * and the returned factory creates fresh ViewModels and engines for each game.
 */
internal fun initializeApplication(context: StartupContext): GameDependencies {
    val startup = Startup.install(context, applicationStartup)
    return GameDependencies(
        startup.initializeComponent(initializerKey<FileStorageInitializer>()),
        startup.initializeComponent(initializerKey<ImportedGameStorageInitializer>()),
    )
}
