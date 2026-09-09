// Port of linuxdoom-1.10 doomdata.h -- external map data lump layout.
// The on-disk structs are read field-by-field from lump ByteArrays in
// p_setup.kt using the little-endian helpers; only runtime types live here.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

/**
 * Thing definition, position, orientation and type,
 * plus skill/visibility flags and attributes. (shorts on disk, Ints here)
 */
internal class mapthing_t(
    var x: Int = 0,
    var y: Int = 0,
    var angle: Int = 0,
    var type: Int = 0,
    var options: Int = 0,
) {
    fun copyFrom(o: mapthing_t) {
        x = o.x; y = o.y; angle = o.angle; type = o.type; options = o.options
    }
}
