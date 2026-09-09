package com.kunal26das.doom.data

import com.kunal26das.doom.domain.FileRepository
import com.kunal26das.doom.domain.ImportedGameRepository

expect fun createImportedGameRepository(files: FileRepository): ImportedGameRepository
