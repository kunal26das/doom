
package doom.engine.resources

import doom.engine.core.DoomEngineCore

internal val DoomEngineCore.lumpinfo: List<LumpInfo> get() = wadArchive.directory
internal val DoomEngineCore.numlumps: Int get() = wadArchive.size

internal fun DoomEngineCore.wInitMultipleFiles(files: List<ByteArray>) = wadArchive.load(files)
internal fun DoomEngineCore.wCheckNumForName(name: String): Int = wadArchive.find(name)
internal fun DoomEngineCore.wGetNumForName(name: String): Int = wadArchive.requireIndex(name)
internal fun DoomEngineCore.wLumpLength(lump: Int): Int = wadArchive.length(lump)
internal fun DoomEngineCore.wCacheLumpNum(lump: Int): ByteArray = wadArchive.read(lump)
internal fun DoomEngineCore.wCacheLumpName(name: String): ByteArray = wadArchive.read(name)
