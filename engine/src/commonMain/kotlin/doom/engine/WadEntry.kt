// WAD loading metadata. Original code (C) 1993-1996 id Software, Inc., GNU GPL v2.
package doom.engine

internal data class WadEntry(val info: LumpInfo, val image: ByteArray, val position: Int)
