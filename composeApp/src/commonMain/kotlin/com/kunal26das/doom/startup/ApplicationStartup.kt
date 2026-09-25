package com.kunal26das.doom.startup

import com.kunal26das.doom.di.GameDependencies
import io.github.kunal26das.startup.Startup
import io.github.kunal26das.startup.StartupContext
import io.github.kunal26das.startup.StartupManifest
import io.github.kunal26das.startup.initializerKey



internal val applicationStartup = StartupManifest {
    metaData<FileStorageInitializer> { FileStorageInitializer() }
}

internal fun initializeApplication(context: StartupContext): GameDependencies {
    val startup = Startup.install(context, applicationStartup)
    return GameDependencies(startup.initializeComponent(initializerKey<FileStorageInitializer>()))
}
