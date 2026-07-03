// Port of linuxdoom-1.10 doomdata.h -- external map data lump layout.
// The on-disk structs are read field-by-field from lump ByteArrays in
// p_setup.kt using the little-endian helpers; only runtime types live here.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// Map data lump order, relative to the map-marker lump (e.g. E1M1).
const val ML_LABEL = 0     // A separator, name, ExMx or MAPxx
const val ML_THINGS = 1    // Monsters, items..
const val ML_LINEDEFS = 2  // LineDefs, from editing
const val ML_SIDEDEFS = 3  // SideDefs, from editing
const val ML_VERTEXES = 4  // Vertices, edited and BSP splits generated
const val ML_SEGS = 5      // LineSegs, from LineDefs split by BSP
const val ML_SSECTORS = 6  // SubSectors, list of LineSegs
const val ML_NODES = 7     // BSP nodes
const val ML_SECTORS = 8   // Sectors, from editing
const val ML_REJECT = 9    // LUT, sector-sector visibility
const val ML_BLOCKMAP = 10 // LUT, motion clipping, walls/grid element

// LineDef attributes.
const val ML_BLOCKING = 1        // Solid, is an obstacle.
const val ML_BLOCKMONSTERS = 2   // Blocks monsters only.
const val ML_TWOSIDED = 4        // Backside will not be present at all if not two sided.

// If a texture is pegged, the texture will have the end exposed to air held
// constant at the top or bottom of the texture (stairs or pulled down things)
// and will move with a height change of one of the neighbor sectors.
// Unpegged textures allways have the first row of the texture at the top
// pixel of the line for both top and bottom textures (use next to windows).
const val ML_DONTPEGTOP = 8      // upper texture unpegged
const val ML_DONTPEGBOTTOM = 16  // lower texture unpegged

const val ML_SECRET = 32         // In AutoMap: don't map as two sided: IT'S A SECRET!
const val ML_SOUNDBLOCK = 64     // Sound rendering: don't let sound cross two of these.
const val ML_DONTDRAW = 128      // Don't draw on the automap at all.
const val ML_MAPPED = 256        // Set if already seen, thus drawn in automap.

// Indicate a leaf (in BSP node children).
const val NF_SUBSECTOR = 0x8000

/**
 * Thing definition, position, orientation and type,
 * plus skill/visibility flags and attributes. (shorts on disk, Ints here)
 */
class mapthing_t(
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
