package com.kunal26das.doom.startup

import com.kunal26das.doom.data.createImportedGameRepository
import com.kunal26das.doom.domain.ImportedGameRepository
import io.github.kunal26das.startup.AnyInitializerKey
import io.github.kunal26das.startup.Initializer
import io.github.kunal26das.startup.Startup
import io.github.kunal26das.startup.StartupContext
import io.github.kunal26das.startup.initializerKey

/** Native imported-game storage shares the initialized save/config storage adapter. */
class ImportedGameStorageInitializer : Initializer<ImportedGameRepository> {
    override fun dependencies(): List<AnyInitializerKey> = listOf(initializerKey<FileStorageInitializer>())

    override fun create(context: StartupContext): ImportedGameRepository = createImportedGameRepository(
        Startup.getInstance(context).initializeComponent(initializerKey<FileStorageInitializer>()),
    )
}
