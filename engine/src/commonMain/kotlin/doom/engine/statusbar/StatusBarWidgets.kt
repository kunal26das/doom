
package doom.engine.statusbar

import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.rendering.vCopyRect
import doom.engine.rendering.vDrawPatch
import doom.engine.rendering.patchHeight
import doom.engine.rendering.patchLeftOffset
import doom.engine.rendering.patchTopOffset
import doom.engine.rendering.patchWidth
import doom.engine.resources.wCacheLumpName

internal const val BG = 4
internal const val FG = 0


internal var DoomEngineCore.sttminus: ByteArray
    get() = stateStLib.sttminus
    set(value) { stateStLib.sttminus = value }

internal fun DoomEngineCore.stLibInit() {
    sttminus = wCacheLumpName("STTMINUS")
}

internal fun DoomEngineCore.stLibInitNum(
    n: NumberWidget,
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

internal fun DoomEngineCore.stLibDrawNum(n: NumberWidget) {
    var numdigits = n.width
    var num = n.num()

    val w = patchWidth(n.p[0])
    val h = patchHeight(n.p[0])
    var x: Int

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

    x = n.x - numdigits * w

    if (n.y - ST_Y < 0)
        iError("drawNum: n->y - ST_Y < 0")

    vCopyRect(x, n.y - ST_Y, BG, w * numdigits, h, x, n.y, FG)

    if (num == 1994)
        return

    x = n.x

    if (num == 0)
        vDrawPatch(x - w, n.y, FG, n.p[0])

    while (num != 0 && numdigits-- != 0) {
        x -= w
        vDrawPatch(x, n.y, FG, n.p[num % 10])
        num /= 10
    }

    if (neg)
        vDrawPatch(x - 8, n.y, FG, sttminus)
}

internal fun DoomEngineCore.stLibUpdateNum(n: NumberWidget) {
    if (n.on()) stLibDrawNum(n)
}

internal fun DoomEngineCore.stLibInitPercent(
    p: PercentWidget,
    x: Int,
    y: Int,
    pl: Array<ByteArray>,
    num: () -> Int,
    on: () -> Boolean,
    percent: ByteArray,
) {
    stLibInitNum(p.n, x, y, pl, num, on, 3)
    p.p = percent
}

internal fun DoomEngineCore.stLibUpdatePercent(
    per: PercentWidget,
    refresh: Boolean,
) {
    if (refresh && per.n.on())
        vDrawPatch(per.n.x, per.n.y, FG, per.p)

    stLibUpdateNum(per.n)
}

internal fun DoomEngineCore.stLibInitMultIcon(
    i: MultiIconWidget,
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

internal fun DoomEngineCore.stLibUpdateMultIcon(
    mi: MultiIconWidget,
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
                iError("updateMultIcon: y - ST_Y < 0")

            vCopyRect(x, y - ST_Y, BG, w, h, x, y, FG)
        }
        vDrawPatch(mi.x, mi.y, FG, mi.p[mi.inum()])
        mi.oldinum = mi.inum()
    }
}

internal fun DoomEngineCore.stLibInitBinIcon(
    b: BooleanIconWidget,
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

internal fun DoomEngineCore.stLibUpdateBinIcon(
    bi: BooleanIconWidget,
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
            iError("updateBinIcon: y - ST_Y < 0")

        if (bi.`val`())
            vDrawPatch(bi.x, bi.y, FG, bi.p)
        else
            vCopyRect(x, y - ST_Y, BG, w, h, x, y, FG)

        bi.oldval = bi.`val`()
    }
}
