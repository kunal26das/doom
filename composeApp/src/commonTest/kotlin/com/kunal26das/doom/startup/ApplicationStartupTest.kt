package com.kunal26das.doom.startup

import io.github.kunal26das.startup.StartupPlanner
import io.github.kunal26das.startup.initializerKey
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationStartupTest {
    @Test
    fun eagerImportedGameStorageResolvesItsFileDependencyOnEveryPlatform() {
        StartupPlanner.validate(applicationStartup)

        val plan = StartupPlanner.plan(
            applicationStartup,
            applicationStartup.eagerComponents,
            emptySet(),
        )

        assertEquals(
            listOf(initializerKey<FileStorageInitializer>(), initializerKey<ImportedGameStorageInitializer>()),
            plan.order,
        )
    }
}
