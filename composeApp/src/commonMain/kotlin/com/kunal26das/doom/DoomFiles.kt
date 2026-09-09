package com.kunal26das.doom

import com.kunal26das.doom.domain.FileRepository
import io.github.kunal26das.startup.StartupContext

internal expect fun createPlatformFileRepository(context: StartupContext): FileRepository
