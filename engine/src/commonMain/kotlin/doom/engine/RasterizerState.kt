// Port of linuxdoom-1.10 r_draw.c -- The actual span/column drawing functions.
// Here find the main potential for optimization,
//  e.g. inline assembly, different algorithms.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class RasterizerState {
    var viewimage: ByteArray = ByteArray(0)

    var viewwidth = 0

    var scaledviewwidth = 0

    var viewheight = 0

    var viewwindowx = 0

    var viewwindowy = 0

    val ylookup by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXHEIGHT) }

    val columnofs by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXWIDTH) }

    val translations by lazy(LazyThreadSafetyMode.NONE) { Array(3) { ByteArray(256) } }

    var dc_colormap = -1

    var dc_x = 0

    var dc_yl = 0

    var dc_yh = 0

    var dc_iscale: fixed_t = 0

    var dc_texturemid: fixed_t = 0

    var dc_source: ByteArray = ByteArray(0)

    var dc_source_ofs = 0

    var dccount = 0

    val fuzzoffset by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        FUZZOFF, -FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
        FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
        FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF,
        FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
        FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF,
        FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF,
        FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF
    ) }

    var fuzzpos = 0

    var dc_translation = 0

    var translationtables: ByteArray = ByteArray(0)

    var ds_y = 0

    var ds_x1 = 0

    var ds_x2 = 0

    var ds_colormap = -1

    var ds_xfrac: fixed_t = 0

    var ds_yfrac: fixed_t = 0

    var ds_xstep: fixed_t = 0

    var ds_ystep: fixed_t = 0

    var ds_source: ByteArray = ByteArray(0)

    var ds_source_ofs = 0

    var dscount = 0
}
