package com.kunal26das.doom.domain

class ImportedGameSelection(name: String, bytes: ByteArray) : GameLaunchSelection {
    val name: String = name.substringAfterLast('/').substringAfterLast('\\')
        .take(128).ifBlank { "Imported WAD" }
    val information: ImportedWadInformation = ImportedWadValidator.validate(bytes)
    val bytes: ByteArray = bytes.copyOf()
}
