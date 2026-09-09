// Port of linuxdoom-1.10 sounds.h struct definitions (tables generated into
// gen/SoundsGen.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "ktlint")

package doom.engine

internal class musicinfo_t(
    val name: String,        // up to 6-character name (lump is D_<name>)
) {
    var lumpnum: Int = -1
    var data: ByteArray? = null
    var handle: Int = 0
}
