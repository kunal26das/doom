package com.kunal26das.doom.startup

import io.github.kunal26das.startup.StartupPlanner
import io.github.kunal26das.startup.initializerKey
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationStartupTest {
    @Test
    fun eagerFileStorageIsTheOnlyStartupComponentOnEveryPlatform() {
        StartupPlanner.validate(applicationStartup)

        val plan = StartupPlanner.plan(
            applicationStartup,
            applicationStartup.eagerComponents,
            emptySet(),
        )

        assertEquals(listOf(initializerKey<FileStorageInitializer>()), plan.order)
    }
}
