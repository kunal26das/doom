// Port of linuxdoom-1.10 r_draw.c -- The actual span/column drawing functions.
// Here find the main potential for optimization,
//  e.g. inline assembly, different algorithms.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

// ?
const val MAXWIDTH = 1120
const val MAXHEIGHT = 832

// status bar height at bottom of screen
const val SBARHEIGHT = 32

//
// All drawing to the view buffer is accomplished in this file.
// The other refresh files only know about ccordinates,
//  not the architecture of the frame buffer.
// Conveniently, the frame buffer is a linear one,
//  and we need only the base address,
//  and the total size == width*height*depth/8.,
//

var viewimage: ByteArray = ByteArray(0)
var viewwidth = 0
var scaledviewwidth = 0
var viewheight = 0
var viewwindowx = 0
var viewwindowy = 0

// byte* ylookup[MAXHEIGHT] --> byte offsets into screens[0].
val ylookup = IntArray(MAXHEIGHT)
val columnofs = IntArray(MAXWIDTH)

// Color tables for different players,
//  translate a limited part to another
//  (color ramps used for  suit colors).
//
val translations = Array(3) { ByteArray(256) }

//
// R_DrawColumn
// Source is the top of the column to scale.
//
var dc_colormap = -1  // lighttable_t* --> byte offset into colormaps; -1 == NULL
var dc_x = 0
var dc_yl = 0
var dc_yh = 0
var dc_iscale: fixed_t = 0
var dc_texturemid: fixed_t = 0

// first pixel in a column (possibly virtual)
// byte* dc_source --> (dc_source, dc_source_ofs) pair.
var dc_source: ByteArray = ByteArray(0)
var dc_source_ofs = 0

// just for profiling
var dccount = 0

//
// A column is a vertical slice/span from a wall texture that,
//  given the DOOM style restrictions on the view orientation,
//  will always have constant z depth.
// Thus a special case loop for very fast rendering can
//  be used. It has also been used with Wolfenstein 3D.
//
fun R_DrawColumn() {
    var count: Int
    var dest: Int  // byte* into screens[0]
    var frac: fixed_t
    val fracstep: fixed_t

    count = dc_yh - dc_yl

    // Zero length, column does not exceed a pixel.
    if (count < 0)
        return

    // #ifdef RANGECHECK
    if (dc_x.toUInt() >= SCREENWIDTH.toUInt()
        || dc_yl < 0
        || dc_yh >= SCREENHEIGHT
    )
        I_Error("R_DrawColumn: $dc_yl to $dc_yh at $dc_x")
    // #endif

    // Framebuffer destination address.
    // Use ylookup LUT to avoid multiply with ScreenWidth.
    // Use columnofs LUT for subwindows?
    dest = ylookup[dc_yl] + columnofs[dc_x]

    // Determine scaling,
    //  which is the only mapping to be done.
    fracstep = dc_iscale
    frac = dc_texturemid + (dc_yl - centery) * fracstep

    // Inner loop that does the actual texture mapping,
    //  e.g. a DDA-lile scaling.
    // This is as fast as it gets.
    do {
        // Re-map color indices from wall texture column
        //  using a lighting/special effects LUT.
        screens[0][dest] =
            colormaps[dc_colormap + dc_source.srcByte(dc_source_ofs + ((frac shr FRACBITS) and 127))]

        dest += SCREENWIDTH
        frac += fracstep

        count--  // } while (count--);
    } while (count >= 0)
}

fun R_DrawColumnLow() {
    var count: Int
    var dest: Int
    var dest2: Int
    var frac: fixed_t
    val fracstep: fixed_t

    count = dc_yh - dc_yl

    // Zero length.
    if (count < 0)
        return

    // #ifdef RANGECHECK
    if (dc_x.toUInt() >= SCREENWIDTH.toUInt()
        || dc_yl < 0
        || dc_yh >= SCREENHEIGHT
    ) {
        I_Error("R_DrawColumn: $dc_yl to $dc_yh at $dc_x")
    }
    //	dccount++;
    // #endif

    // Blocky mode, need to multiply by 2.
    dc_x = dc_x shl 1

    dest = ylookup[dc_yl] + columnofs[dc_x]
    dest2 = ylookup[dc_yl] + columnofs[dc_x + 1]

    fracstep = dc_iscale
    frac = dc_texturemid + (dc_yl - centery) * fracstep

    do {
        // Hack. Does not work corretly.
        // *dest2 = *dest = dc_colormap[dc_source[(frac>>FRACBITS)&127]];
        val pixel =
            colormaps[dc_colormap + dc_source.srcByte(dc_source_ofs + ((frac shr FRACBITS) and 127))]
        screens[0][dest2] = pixel
        screens[0][dest] = pixel
        dest += SCREENWIDTH
        dest2 += SCREENWIDTH
        frac += fracstep

        count--  // } while (count--);
    } while (count >= 0)
}

//
// Spectre/Invisibility.
//
const val FUZZTABLE = 50
const val FUZZOFF = SCREENWIDTH

val fuzzoffset = intArrayOf(
    FUZZOFF, -FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
    FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
    FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF,
    FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF,
    FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF,
    FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF,
    FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF, FUZZOFF, -FUZZOFF, FUZZOFF
)

var fuzzpos = 0

//
// Framebuffer postprocessing.
// Creates a fuzzy image by copying pixels
//  from adjacent ones to left and right.
// Used with an all black colormap, this
//  could create the SHADOW effect,
//  i.e. spectres and invisible players.
//
fun R_DrawFuzzColumn() {
    var count: Int
    var dest: Int
    var frac: fixed_t
    val fracstep: fixed_t

    // Adjust borders. Low...
    if (dc_yl == 0)
        dc_yl = 1

    // .. and high.
    if (dc_yh == viewheight - 1)
        dc_yh = viewheight - 2

    count = dc_yh - dc_yl

    // Zero length.
    if (count < 0)
        return

    // #ifdef RANGECHECK
    if (dc_x.toUInt() >= SCREENWIDTH.toUInt()
        || dc_yl < 0 || dc_yh >= SCREENHEIGHT
    ) {
        I_Error("R_DrawFuzzColumn: $dc_yl to $dc_yh at $dc_x")
    }
    // #endif

    // Keep till detailshift bug in blocky mode fixed,
    //  or blocky mode removed.
    // (WATCOM VGA code omitted.)

    // Does not work with blocky mode.
    dest = ylookup[dc_yl] + columnofs[dc_x]

    // Looks familiar.
    fracstep = dc_iscale
    frac = dc_texturemid + (dc_yl - centery) * fracstep

    // Looks like an attempt at dithering,
    //  using the colormap #6 (of 0-31, a bit
    //  brighter than average).
    do {
        // Lookup framebuffer, and retrieve
        //  a pixel that is either one column
        //  left or right of the current one.
        // Add index from colormap to index.
        screens[0][dest] =
            colormaps[6 * 256 + (screens[0][dest + fuzzoffset[fuzzpos]].toInt() and 0xFF)]

        // Clamp table lookup index.
        fuzzpos++
        if (fuzzpos == FUZZTABLE)
            fuzzpos = 0

        dest += SCREENWIDTH

        frac += fracstep

        count--  // } while (count--);
    } while (count >= 0)
}

//
// R_DrawTranslatedColumn
// Used to draw player sprites
//  with the green colorramp mapped to others.
// Could be used with different translation
//  tables, e.g. the lighter colored version
//  of the BaronOfHell, the HellKnight, uses
//  identical sprites, kinda brightened up.
//
var dc_translation = 0  // byte* --> byte offset into translationtables
var translationtables: ByteArray = ByteArray(0)

fun R_DrawTranslatedColumn() {
    var count: Int
    var dest: Int
    var frac: fixed_t
    val fracstep: fixed_t

    count = dc_yh - dc_yl
    if (count < 0)
        return

    // #ifdef RANGECHECK
    if (dc_x.toUInt() >= SCREENWIDTH.toUInt()
        || dc_yl < 0
        || dc_yh >= SCREENHEIGHT
    ) {
        I_Error("R_DrawColumn: $dc_yl to $dc_yh at $dc_x")
    }
    // #endif

    // WATCOM VGA specific. (Keep for fixing. -- omitted)

    // FIXME. As above.
    dest = ylookup[dc_yl] + columnofs[dc_x]

    // Looks familiar.
    fracstep = dc_iscale
    frac = dc_texturemid + (dc_yl - centery) * fracstep

    // Here we do an additional index re-mapping.
    do {
        // Translation tables are used
        //  to map certain colorramps to other ones,
        //  used with PLAY sprites.
        // Thus the "green" ramp of the player 0 sprite
        //  is mapped to gray, red, black/indigo.
        // *dest = dc_colormap[dc_translation[dc_source[frac>>FRACBITS]]];
        screens[0][dest] = colormaps[dc_colormap +
            (translationtables[dc_translation +
                dc_source.srcByte(dc_source_ofs + (frac shr FRACBITS))].toInt() and 0xFF)]
        dest += SCREENWIDTH

        frac += fracstep

        count--  // } while (count--);
    } while (count >= 0)
}

//
// R_InitTranslationTables
// Creates the translation tables to map
//  the green color ramp to gray, brown, red.
// Assumes a given structure of the PLAYPAL.
// Could be read from a lump instead.
//
fun R_InitTranslationTables() {
    // (Z_Malloc 256*3+255 + 256-byte alignment hack deleted.)
    translationtables = ByteArray(256 * 3)

    // translate just the 16 green colors
    for (i in 0 until 256) {
        if (i >= 0x70 && i <= 0x7f) {
            // map green ramp to gray, brown, red
            translationtables[i] = (0x60 + (i and 0xf)).toByte()
            translationtables[i + 256] = (0x40 + (i and 0xf)).toByte()
            translationtables[i + 512] = (0x20 + (i and 0xf)).toByte()
        } else {
            // Keep all other colors as is.
            translationtables[i] = i.toByte()
            translationtables[i + 256] = i.toByte()
            translationtables[i + 512] = i.toByte()
        }
    }
}

//
// R_DrawSpan
// With DOOM style restrictions on view orientation,
//  the floors and ceilings consist of horizontal slices
//  or spans with constant z depth.
// However, rotation around the world z axis is possible,
//  thus this mapping, while simpler and faster than
//  perspective correct texture mapping, has to traverse
//  the texture at an angle in all but a few cases.
// In consequence, flats are not stored by column (like walls),
//  and the inner loop has to step in texture space u and v.
//
var ds_y = 0
var ds_x1 = 0
var ds_x2 = 0

var ds_colormap = -1  // lighttable_t* --> byte offset into colormaps; -1 == NULL

var ds_xfrac: fixed_t = 0
var ds_yfrac: fixed_t = 0
var ds_xstep: fixed_t = 0
var ds_ystep: fixed_t = 0

// start of a 64*64 tile image
// byte* ds_source --> (ds_source, ds_source_ofs) pair.
var ds_source: ByteArray = ByteArray(0)
var ds_source_ofs = 0

// just for profiling
var dscount = 0

//
// Draws the actual span.
fun R_DrawSpan() {
    var xfrac: fixed_t
    var yfrac: fixed_t
    var dest: Int
    var count: Int
    var spot: Int

    // #ifdef RANGECHECK
    if (ds_x2 < ds_x1
        || ds_x1 < 0
        || ds_x2 >= SCREENWIDTH
        || ds_y.toUInt() > SCREENHEIGHT.toUInt()
    ) {
        I_Error("R_DrawSpan: $ds_x1 to $ds_x2 at $ds_y")
    }
    //	dscount++;
    // #endif

    xfrac = ds_xfrac
    yfrac = ds_yfrac

    dest = ylookup[ds_y] + columnofs[ds_x1]

    // We do not check for zero spans here?
    count = ds_x2 - ds_x1

    do {
        // Current texture index in u,v.
        spot = ((yfrac shr (16 - 6)) and (63 * 64)) + ((xfrac shr 16) and 63)

        // Lookup pixel from flat texture tile,
        //  re-index using light/colormap.
        screens[0][dest] =
            colormaps[ds_colormap + ds_source.srcByte(ds_source_ofs + spot)]
        dest++

        // Next step in u,v.
        xfrac += ds_xstep
        yfrac += ds_ystep

        count--  // } while (count--);
    } while (count >= 0)
}

//
// Again..
//
fun R_DrawSpanLow() {
    var xfrac: fixed_t
    var yfrac: fixed_t
    var dest: Int
    var count: Int
    var spot: Int

    // #ifdef RANGECHECK
    if (ds_x2 < ds_x1
        || ds_x1 < 0
        || ds_x2 >= SCREENWIDTH
        || ds_y.toUInt() > SCREENHEIGHT.toUInt()
    ) {
        I_Error("R_DrawSpan: $ds_x1 to $ds_x2 at $ds_y")
    }
    //	dscount++;
    // #endif

    xfrac = ds_xfrac
    yfrac = ds_yfrac

    // Blocky mode, need to multiply by 2.
    ds_x1 = ds_x1 shl 1
    ds_x2 = ds_x2 shl 1

    dest = ylookup[ds_y] + columnofs[ds_x1]

    count = ds_x2 - ds_x1
    do {
        spot = ((yfrac shr (16 - 6)) and (63 * 64)) + ((xfrac shr 16) and 63)
        // Lowres/blocky mode does it twice,
        //  while scale is adjusted appropriately.
        screens[0][dest] =
            colormaps[ds_colormap + ds_source.srcByte(ds_source_ofs + spot)]
        dest++
        screens[0][dest] =
            colormaps[ds_colormap + ds_source.srcByte(ds_source_ofs + spot)]
        dest++

        xfrac += ds_xstep
        yfrac += ds_ystep

        count--  // } while (count--);
    } while (count >= 0)
}

//
// R_InitBuffer
// Creats lookup tables that avoid
//  multiplies and other hazzles
//  for getting the framebuffer address
//  of a pixel to draw.
//
fun R_InitBuffer(width: Int, height: Int) {
    // Handle resize,
    //  e.g. smaller view windows
    //  with border and/or status bar.
    viewwindowx = (SCREENWIDTH - width) shr 1

    // Column offset. For windows.
    for (i in 0 until width)
        columnofs[i] = viewwindowx + i

    // Samw with base row offset.
    if (width == SCREENWIDTH)
        viewwindowy = 0
    else
        viewwindowy = (SCREENHEIGHT - SBARHEIGHT - height) shr 1

    // Preclaculate all row offsets.
    for (i in 0 until height)
        ylookup[i] = (i + viewwindowy) * SCREENWIDTH  // screens[0] + ...
}

//
// R_FillBackScreen
// Fills the back screen with a pattern
//  for variable screen sizes
// Also draws a beveled edge.
//
fun R_FillBackScreen() {
    val src: ByteArray
    var dest: Int  // byte* into screens[1]
    var x: Int
    var y: Int
    var patch: ByteArray

    // DOOM border patch.
    val name1 = "FLOOR7_2"

    // DOOM II border patch.
    val name2 = "GRNROCK"

    val name: String

    if (scaledviewwidth == 320)
        return

    if (gamemode == commercial)
        name = name2
    else
        name = name1

    src = W_CacheLumpName(name)
    dest = 0  // dest = screens[1]

    for (y in 0 until SCREENHEIGHT - SBARHEIGHT) {
        for (x in 0 until SCREENWIDTH / 64) {
            // memcpy (dest, src+((y&63)<<6), 64);
            src.copyInto(screens[1], dest, (y and 63) shl 6, ((y and 63) shl 6) + 64)
            dest += 64
        }

        if ((SCREENWIDTH and 63) != 0) {
            // memcpy (dest, src+((y&63)<<6), SCREENWIDTH&63);
            src.copyInto(screens[1], dest, (y and 63) shl 6, ((y and 63) shl 6) + (SCREENWIDTH and 63))
            dest += (SCREENWIDTH and 63)
        }
    }

    patch = W_CacheLumpName("brdr_t")

    x = 0
    while (x < scaledviewwidth) {
        V_DrawPatch(viewwindowx + x, viewwindowy - 8, 1, patch)
        x += 8
    }
    patch = W_CacheLumpName("brdr_b")

    x = 0
    while (x < scaledviewwidth) {
        V_DrawPatch(viewwindowx + x, viewwindowy + viewheight, 1, patch)
        x += 8
    }
    patch = W_CacheLumpName("brdr_l")

    y = 0
    while (y < viewheight) {
        V_DrawPatch(viewwindowx - 8, viewwindowy + y, 1, patch)
        y += 8
    }
    patch = W_CacheLumpName("brdr_r")

    y = 0
    while (y < viewheight) {
        V_DrawPatch(viewwindowx + scaledviewwidth, viewwindowy + y, 1, patch)
        y += 8
    }

    // Draw beveled edge.
    V_DrawPatch(viewwindowx - 8,
        viewwindowy - 8,
        1,
        W_CacheLumpName("brdr_tl"))

    V_DrawPatch(viewwindowx + scaledviewwidth,
        viewwindowy - 8,
        1,
        W_CacheLumpName("brdr_tr"))

    V_DrawPatch(viewwindowx - 8,
        viewwindowy + viewheight,
        1,
        W_CacheLumpName("brdr_bl"))

    V_DrawPatch(viewwindowx + scaledviewwidth,
        viewwindowy + viewheight,
        1,
        W_CacheLumpName("brdr_br"))
}

//
// Copy a screen buffer.
//
fun R_VideoErase(ofs: Int, count: Int) {
    // LFB copy.
    // This might not be a good idea if memcpy
    //  is not optiomal, e.g. byte by byte on
    //  a 32bit CPU, as GNU GCC/Linux libc did
    //  at one point.
    // memcpy (screens[0]+ofs, screens[1]+ofs, count);
    screens[1].copyInto(screens[0], ofs, ofs, ofs + count)
}

//
// R_DrawViewBorder
// Draws the border around the view
//  for different size windows?
//
fun R_DrawViewBorder() {
    val top: Int
    var side: Int
    var ofs: Int

    if (scaledviewwidth == SCREENWIDTH)
        return

    top = ((SCREENHEIGHT - SBARHEIGHT) - viewheight) / 2
    side = (SCREENWIDTH - scaledviewwidth) / 2

    // copy top and one line of left side
    R_VideoErase(0, top * SCREENWIDTH + side)

    // copy one line of right side and bottom
    ofs = (viewheight + top) * SCREENWIDTH - side
    R_VideoErase(ofs, top * SCREENWIDTH + side)

    // copy sides using wraparound
    ofs = top * SCREENWIDTH + SCREENWIDTH - side
    side = side shl 1

    for (i in 1 until viewheight) {
        R_VideoErase(ofs, side)
        ofs += SCREENWIDTH
    }

    // ?
    V_MarkRect(0, 0, SCREENWIDTH, SCREENHEIGHT - SBARHEIGHT)
}

/**
 * Vanilla's drawers index lump bytes unchecked; slightly-negative frac at a
 * sprite post's top edge (or an undersized flat) makes C read a few bytes of
 * adjacent zone memory -- a harmless garbage pixel on DOS. Emulate that
 * tolerated out-of-bounds read by clamping into the lump.
 */
internal fun ByteArray.srcByte(i: Int): Int {
    val ci = if (i < 0) 0 else if (i >= size) size - 1 else i
    return this[ci].toInt() and 0xFF
}
