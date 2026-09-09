package com.kunal26das.doom.domain

/** Player-owned assets; repositories may retain them privately on this device. */
class ImportedGameSelection(name: String, bytes: ByteArray) : GameLaunchSelection {
    val name: String = name.substringAfterLast('/').substringAfterLast('\\')
        .take(128).ifBlank { "Imported WAD" }
    val information: ImportedWadInformation = ImportedWadValidator.validate(bytes)
    val bytes: ByteArray = bytes.copyOf()
}
