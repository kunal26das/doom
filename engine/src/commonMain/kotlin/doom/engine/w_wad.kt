// WAD loading, directory lookup and cached lump reads.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine


// Compatibility vocabulary for original algorithms; archive state remains service-owned.
internal val DoomEngineCore.lumpinfo: List<LumpInfo> get() = wadArchive.directory
internal val DoomEngineCore.numlumps: Int get() = wadArchive.size

internal fun DoomEngineCore.W_InitMultipleFiles(files: List<ByteArray>) = wadArchive.load(files)
internal fun DoomEngineCore.W_CheckNumForName(name: String): Int = wadArchive.find(name)
internal fun DoomEngineCore.W_GetNumForName(name: String): Int = wadArchive.requireIndex(name)
internal fun DoomEngineCore.W_LumpLength(lump: Int): Int = wadArchive.length(lump)
internal fun DoomEngineCore.W_CacheLumpNum(lump: Int): ByteArray = wadArchive.read(lump)
internal fun DoomEngineCore.W_CacheLumpName(name: String): ByteArray = wadArchive.read(name)
