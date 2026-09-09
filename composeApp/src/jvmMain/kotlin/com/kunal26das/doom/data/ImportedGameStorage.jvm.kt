package com.kunal26das.doom.data

import com.kunal26das.doom.domain.FileRepository
import com.kunal26das.doom.domain.ImportedGameRepository
import kotlinx.coroutines.Dispatchers

actual fun createImportedGameRepository(files: FileRepository): ImportedGameRepository =
    FileImportedGameRepository(files, Dispatchers.IO)
