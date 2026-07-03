// Port of linuxdoom-1.10 v_video.c -- gamma correction LUT, framebuffer
// functions to blit a block to the screen. (gammatable is generated into
// gen/GammaGen.kt; patches stay raw ByteArrays, read via the helpers below.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// Each screen is [SCREENWIDTH*SCREENHEIGHT];
// screens[0] is the game view, [1] the border backdrop save, [2]/[3] wipe
// buffers, [4] the status bar work buffer.
val screens: Array<ByteArray> = Array(5) { ByteArray(SCREENWIDTH * SCREENHEIGHT) }

val dirtybox = IntArray(4)

// Now where did these came from? (vanilla comment) -- gammatable in gen/GammaGen.kt
var usegamma = 0

// patch_t field helpers (the C struct is read straight from the lump bytes)
fun patchWidth(p: ByteArray): Int = p.i16(0)
fun patchHeight(p: ByteArray): Int = p.i16(2)
fun patchLeftOffset(p: ByteArray): Int = p.i16(4)
fun patchTopOffset(p: ByteArray): Int = p.i16(6)
fun patchColumnOfs(p: ByteArray, col: Int): Int = p.i32(8 + col * 4)

fun V_MarkRect(x: Int, y: Int, width: Int, height: Int) {
    M_AddToBox(dirtybox, x, y)
    M_AddToBox(dirtybox, x + width - 1, y + height - 1)
}

fun V_CopyRect(
    srcx: Int, srcy: Int, srcscrn: Int,
    width: Int, height: Int,
    destx: Int, desty: Int, destscrn: Int,
) {
    if (srcx < 0 || srcx + width > SCREENWIDTH ||
        srcy < 0 || srcy + height > SCREENHEIGHT ||
        destx < 0 || destx + width > SCREENWIDTH ||
        desty < 0 || desty + height > SCREENHEIGHT ||
        srcscrn > 4 || destscrn > 4
    ) {
        I_Error("Bad V_CopyRect")
    }
    V_MarkRect(destx, desty, width, height)

    val src = screens[srcscrn]
    val dest = screens[destscrn]
    var srcofs = SCREENWIDTH * srcy + srcx
    var destofs = SCREENWIDTH * desty + destx
    for (h in 0 until height) {
        src.copyInto(dest, destofs, srcofs, srcofs + width)
        srcofs += SCREENWIDTH
        destofs += SCREENWIDTH
    }
}

/** Masked blit of a column-based patch lump onto screens[scrn]. */
fun V_DrawPatch(x0: Int, y0: Int, scrn: Int, patch: ByteArray) {
    val x = x0 - patchLeftOffset(patch)
    val y = y0 - patchTopOffset(patch)
    val w = patchWidth(patch)

    if (x < 0 || x + w > SCREENWIDTH || y < 0 ||
        y + patchHeight(patch) > SCREENHEIGHT || scrn > 4
    ) {
        println("Patch at $x,$y exceeds LFB")
        // No I_Error abort - what is up with TNT.WAD? (vanilla comment)
        println("V_DrawPatch: bad patch (ignored)")
        return
    }

    if (scrn == 0) V_MarkRect(x, y, w, patchHeight(patch))

    val dest = screens[scrn]
    val desttop = y * SCREENWIDTH + x

    for (col in 0 until w) {
        var ofs = patchColumnOfs(patch, col)
        // step through the posts in a column
        while (patch.u8(ofs) != 0xff) {
            val topdelta = patch.u8(ofs)
            val count = patch.u8(ofs + 1)
            var source = ofs + 3
            var destofs = desttop + col + topdelta * SCREENWIDTH
            for (i in 0 until count) {
                dest[destofs] = patch[source]
                source++
                destofs += SCREENWIDTH
            }
            ofs += 4 + count
        }
    }
}

/** V_DrawPatchDirect draws straight to the (same) framebuffer here. */
fun V_DrawPatchDirect(x: Int, y: Int, scrn: Int, patch: ByteArray) {
    V_DrawPatch(x, y, scrn, patch)
}

/** Draw a linear block of pixels into the view buffer. */
fun V_DrawBlock(x: Int, y: Int, scrn: Int, width: Int, height: Int, src: ByteArray) {
    if (x < 0 || x + width > SCREENWIDTH || y < 0 || y + height > SCREENHEIGHT || scrn > 4) {
        I_Error("Bad V_DrawBlock")
    }
    V_MarkRect(x, y, width, height)

    val dest = screens[scrn]
    var srcofs = 0
    var destofs = y * SCREENWIDTH + x
    for (h in 0 until height) {
        src.copyInto(dest, destofs, srcofs, srcofs + width)
        srcofs += width
        destofs += SCREENWIDTH
    }
}

/** Gets a linear block of pixels from the view buffer. */
fun V_GetBlock(x: Int, y: Int, scrn: Int, width: Int, height: Int, dest: ByteArray) {
    if (x < 0 || x + width > SCREENWIDTH || y < 0 || y + height > SCREENHEIGHT || scrn > 4) {
        I_Error("Bad V_GetBlock")
    }
    val src = screens[scrn]
    var srcofs = y * SCREENWIDTH + x
    var destofs = 0
    for (h in 0 until height) {
        src.copyInto(dest, destofs, srcofs, srcofs + width)
        srcofs += SCREENWIDTH
        destofs += width
    }
}

fun V_Init() {
    // screens are statically allocated above (C Z_Malloc'd a base block).
}
