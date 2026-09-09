package com.kunal26das.doom.startup

import com.kunal26das.doom.createPlatformFileRepository
import com.kunal26das.doom.domain.FileRepository
import io.github.kunal26das.startup.BaseInitializer
import io.github.kunal26das.startup.StartupContext

class FileStorageInitializer : BaseInitializer<FileRepository>() {
    override fun create(context: StartupContext): FileRepository = createPlatformFileRepository(context)
}
