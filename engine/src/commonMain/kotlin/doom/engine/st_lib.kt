// Port of linuxdoom-1.10 st_lib.c/st_lib.h -- the status bar widget code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

//
// Background and foreground screen numbers
//
internal const val BG = 4
internal const val FG = 0

//
// Typedefs of widgets
//


internal var DoomEngineCore.sttminus: ByteArray
    get() = stateStLib.sttminus
    set(value) { stateStLib.sttminus = value }

internal fun DoomEngineCore.STlib_init() {
    sttminus = W_CacheLumpName("STTMINUS")
}

// ?
internal fun DoomEngineCore.STlib_initNum(
    n: st_number_t,
    x: Int,
    y: Int,
    pl: Array<ByteArray>,
    num: () -> Int,
    on: () -> Boolean,
    width: Int,
) {
    n.x = x
    n.y = y
    n.oldnum = 0
    n.width = width
    n.num = num
    n.on = on
    n.p = pl
}

//
// A fairly efficient way to draw a number
//  based on differences from the old number.
// Note: worth the trouble?
//
internal fun DoomEngineCore.STlib_drawNum(
    n: st_number_t,
    refresh: Boolean,
) {
    var numdigits = n.width
    var num = n.num()

    val w = patchWidth(n.p[0])
    val h = patchHeight(n.p[0])
    var x = n.x

    val neg: Boolean

    n.oldnum = n.num()

    neg = num < 0

    if (neg) {
        if (numdigits == 2 && num < -9)
            num = -9
        else if (numdigits == 3 && num < -99)
            num = -99

        num = -num
    }

    // clear the area
    x = n.x - numdigits * w

    if (n.y - ST_Y < 0)
        I_Error("drawNum: n->y - ST_Y < 0")

    V_CopyRect(x, n.y - ST_Y, BG, w * numdigits, h, x, n.y, FG)

    // if non-number, do not draw it
    if (num == 1994)
        return

    x = n.x

    // in the special case of 0, you draw 0
    if (num == 0)
        V_DrawPatch(x - w, n.y, FG, n.p[0])

    // draw the new number
    while (num != 0 && numdigits-- != 0) {
        x -= w
        V_DrawPatch(x, n.y, FG, n.p[num % 10])
        num /= 10
    }

    // draw a minus sign if necessary
    if (neg)
        V_DrawPatch(x - 8, n.y, FG, sttminus)
}

//
internal fun DoomEngineCore.STlib_updateNum(
    n: st_number_t,
    refresh: Boolean,
) {
    if (n.on()) STlib_drawNum(n, refresh)
}

//
internal fun DoomEngineCore.STlib_initPercent(
    p: st_percent_t,
    x: Int,
    y: Int,
    pl: Array<ByteArray>,
    num: () -> Int,
    on: () -> Boolean,
    percent: ByteArray,
) {
    STlib_initNum(p.n, x, y, pl, num, on, 3)
    p.p = percent
}

internal fun DoomEngineCore.STlib_updatePercent(
    per: st_percent_t,
    refresh: Boolean,  // C: int refresh
) {
    if (refresh && per.n.on())
        V_DrawPatch(per.n.x, per.n.y, FG, per.p)

    STlib_updateNum(per.n, refresh)
}

internal fun DoomEngineCore.STlib_initMultIcon(
    i: st_multicon_t,
    x: Int,
    y: Int,
    il: Array<ByteArray>,
    inum: () -> Int,
    on: () -> Boolean,
) {
    i.x = x
    i.y = y
    i.oldinum = -1
    i.inum = inum
    i.on = on
    i.p = il
}

internal fun DoomEngineCore.STlib_updateMultIcon(
    mi: st_multicon_t,
    refresh: Boolean,
) {
    val w: Int
    val h: Int
    val x: Int
    val y: Int

    if (mi.on()
        && (mi.oldinum != mi.inum() || refresh)
        && (mi.inum() != -1)
    ) {
        if (mi.oldinum != -1) {
            x = mi.x - patchLeftOffset(mi.p[mi.oldinum])
            y = mi.y - patchTopOffset(mi.p[mi.oldinum])
            w = patchWidth(mi.p[mi.oldinum])
            h = patchHeight(mi.p[mi.oldinum])

            if (y - ST_Y < 0)
                I_Error("updateMultIcon: y - ST_Y < 0")

            V_CopyRect(x, y - ST_Y, BG, w, h, x, y, FG)
        }
        V_DrawPatch(mi.x, mi.y, FG, mi.p[mi.inum()])
        mi.oldinum = mi.inum()
    }
}

internal fun DoomEngineCore.STlib_initBinIcon(
    b: st_binicon_t,
    x: Int,
    y: Int,
    i: ByteArray,
    `val`: () -> Boolean,
    on: () -> Boolean,
) {
    b.x = x
    b.y = y
    b.oldval = false
    b.`val` = `val`
    b.on = on
    b.p = i
}

internal fun DoomEngineCore.STlib_updateBinIcon(
    bi: st_binicon_t,
    refresh: Boolean,
) {
    val x: Int
    val y: Int
    val w: Int
    val h: Int

    if (bi.on()
        && (bi.oldval != bi.`val`() || refresh)
    ) {
        x = bi.x - patchLeftOffset(bi.p)
        y = bi.y - patchTopOffset(bi.p)
        w = patchWidth(bi.p)
        h = patchHeight(bi.p)

        if (y - ST_Y < 0)
            I_Error("updateBinIcon: y - ST_Y < 0")

        if (bi.`val`())
            V_DrawPatch(bi.x, bi.y, FG, bi.p)
        else
            V_CopyRect(x, y - ST_Y, BG, w, h, x, y, FG)

        bi.oldval = bi.`val`()
    }
}
