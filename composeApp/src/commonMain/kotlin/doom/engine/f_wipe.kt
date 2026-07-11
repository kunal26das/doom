// Port of linuxdoom-1.10 f_wipe.c + f_wipe.h -- mission begin melt/wipe screen
// special effect. The C code casts the byte framebuffers to short* and moves
// 16-bit pixel pairs; here the casts are emulated with little-endian pair
// reads/writes on the same byte screens.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

//
//                       SCREEN WIPE PACKAGE
//

// f_wipe.h wipe types
// simple gradual pixel change for 8-bit only
const val wipe_ColorXForm = 0

// weird screen melt
const val wipe_Melt = 1

const val wipe_NUMWIPES = 2

// when zero, stop the wipe
private var go = false

private var wipe_scr_start = ByteArray(0)
private var wipe_scr_end = ByteArray(0)
private var wipe_scr = ByteArray(0)

// Emulate the C "(short*)screen" cast: short i covers screen bytes 2i/2i+1
// (little-endian pixel pairs, exactly the DOS/x86 layout).
private fun wipe_getShort(b: ByteArray, i: Int): Short =
    ((b[2 * i].toInt() and 0xff) or ((b[2 * i + 1].toInt() and 0xff) shl 8)).toShort()

private fun wipe_putShort(b: ByteArray, i: Int, v: Short) {
    b[2 * i] = v.toByte()
    b[2 * i + 1] = (v.toInt() shr 8).toByte()
}

fun wipe_shittyColMajorXform(array: ByteArray, width: Int, height: Int) {
    val dest = ShortArray(width * height)

    for (y in 0 until height)
        for (x in 0 until width)
            dest[x * height + y] = wipe_getShort(array, y * width + x)

    // memcpy(array, dest, width*height*2)
    for (i in 0 until width * height)
        wipe_putShort(array, i, dest[i])
}

fun wipe_initColorXForm(width: Int, height: Int, ticks: Int): Int {
    wipe_scr_start.copyInto(wipe_scr, 0, 0, width * height)
    return 0
}

fun wipe_doColorXForm(width: Int, height: Int, ticks: Int): Int {
    var changed = false
    var w = 0 // index into wipe_scr
    var e = 0 // index into wipe_scr_end

    while (w != width * height) {
        val wv = wipe_scr[w].toInt() and 0xff // *w (byte is unsigned char)
        val ev = wipe_scr_end[e].toInt() and 0xff // *e
        if (wv != ev) {
            if (wv > ev) {
                val newval = wv - ticks
                if (newval < ev)
                    wipe_scr[w] = wipe_scr_end[e]
                else
                    wipe_scr[w] = newval.toByte()
                changed = true
            } else if (wv < ev) {
                val newval = wv + ticks
                if (newval > ev)
                    wipe_scr[w] = wipe_scr_end[e]
                else
                    wipe_scr[w] = newval.toByte()
                changed = true
            }
        }
        w++
        e++
    }

    return if (!changed) 1 else 0
}

fun wipe_exitColorXForm(width: Int, height: Int, ticks: Int): Int {
    return 0
}

private var y = IntArray(0) // static int* y

fun wipe_initMelt(width: Int, height: Int, ticks: Int): Int {
    // copy start screen to main screen
    wipe_scr_start.copyInto(wipe_scr, 0, 0, width * height)

    // makes this wipe faster (in theory)
    // to have stuff in column-major format
    wipe_shittyColMajorXform(wipe_scr_start, width / 2, height)
    wipe_shittyColMajorXform(wipe_scr_end, width / 2, height)

    // setup initial column positions
    // (y<0 => not ready to scroll yet)
    y = IntArray(width)
    y[0] = -(M_Random() % 16)
    for (i in 1 until width) {
        val r = (M_Random() % 3) - 1
        y[i] = y[i - 1] + r
        if (y[i] > 0) y[i] = 0
        else if (y[i] == -16) y[i] = -15
    }

    return 0
}

fun wipe_doMelt(width0: Int, height: Int, ticks0: Int): Int {
    var width = width0
    var ticks = ticks0
    var dy: Int
    var idx: Int
    var s: Int // index into (short*)wipe_scr_end / wipe_scr_start
    var d: Int // index into (short*)wipe_scr
    var done = true

    width /= 2

    while (ticks-- != 0) {
        for (i in 0 until width) {
            if (y[i] < 0) {
                y[i]++
                done = false
            } else if (y[i] < height) {
                dy = if (y[i] < 16) y[i] + 1 else 8
                if (y[i] + dy >= height) dy = height - y[i]
                s = i * height + y[i] // &((short *)wipe_scr_end)[i*height+y[i]]
                d = y[i] * width + i // &((short *)wipe_scr)[y[i]*width+i]
                idx = 0
                var j = dy
                while (j != 0) {
                    wipe_putShort(wipe_scr, d + idx, wipe_getShort(wipe_scr_end, s)) // d[idx] = *(s++)
                    s++
                    idx += width
                    j--
                }
                y[i] += dy
                s = i * height // &((short *)wipe_scr_start)[i*height]
                d = y[i] * width + i // &((short *)wipe_scr)[y[i]*width+i]
                idx = 0
                j = height - y[i]
                while (j != 0) {
                    wipe_putShort(wipe_scr, d + idx, wipe_getShort(wipe_scr_start, s)) // d[idx] = *(s++)
                    s++
                    idx += width
                    j--
                }
                done = false
            }
        }
    }

    return if (done) 1 else 0
}

fun wipe_exitMelt(width: Int, height: Int, ticks: Int): Int {
    // Z_Free(y) -- Kotlin GC
    return 0
}

fun wipe_StartScreen(x: Int, y: Int, width: Int, height: Int): Int {
    wipe_scr_start = screens[2]
    I_ReadScreen(wipe_scr_start)
    return 0
}

fun wipe_EndScreen(x: Int, y: Int, width: Int, height: Int): Int {
    wipe_scr_end = screens[3]
    I_ReadScreen(wipe_scr_end)
    V_DrawBlock(x, y, 0, width, height, wipe_scr_start) // restore start scr.
    return 0
}

private val wipes = arrayOf<(Int, Int, Int) -> Int>(
    ::wipe_initColorXForm, ::wipe_doColorXForm, ::wipe_exitColorXForm,
    ::wipe_initMelt, ::wipe_doMelt, ::wipe_exitMelt,
)

fun wipe_ScreenWipe(wipeno: Int, x: Int, y: Int, width: Int, height: Int, ticks: Int): Boolean {
    val rc: Int

    // initial stuff
    if (!go) {
        go = true
        // wipe_scr = (byte *) Z_Malloc(width*height, PU_STATIC, 0); // DEBUG
        wipe_scr = screens[0]
        wipes[wipeno * 3](width, height, ticks)
    }

    // do a piece of wipe-in
    V_MarkRect(0, 0, width, height)
    rc = wipes[wipeno * 3 + 1](width, height, ticks)
    //  V_DrawBlock(x, y, 0, width, height, wipe_scr); // DEBUG

    // final stuff
    if (rc != 0) {
        go = false
        wipes[wipeno * 3 + 2](width, height, ticks)
    }

    return !go
}
