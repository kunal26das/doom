// WAD loading, directory lookup and cached lump reads.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

/** Read-only directory metadata; archive bytes and offsets remain private. */
internal data class LumpInfo(val name: String, val size: Int)
