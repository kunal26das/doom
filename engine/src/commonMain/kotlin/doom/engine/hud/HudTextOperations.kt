
package doom.engine.hud

import doom.engine.KEY_BACKSPACE
import doom.engine.KEY_ENTER
import doom.engine.SCREENWIDTH
import doom.engine.automap.automapactive
import doom.engine.core.DoomEngineCore
import doom.engine.rendering.rVideoErase
import doom.engine.rendering.vDrawPatchDirect
import doom.engine.rendering.patchHeight
import doom.engine.rendering.patchWidth
import doom.engine.rendering.viewheight
import doom.engine.rendering.viewwidth
import doom.engine.rendering.viewwindowx
import doom.engine.rendering.viewwindowy

private const val HU_BG = 1
private const val HU_FG = 0

internal const val HU_CHARERASE = KEY_BACKSPACE

internal const val HU_MAXLINES = 4
internal const val HU_MAXLINELENGTH = 80



internal fun DoomEngineCore.huLibInit() {
}

internal fun DoomEngineCore.huLibClearTextLine(t: HudTextLine) {
    t.len = 0
    t.l[0] = '\u0000'
    t.needsupdate = 1
}

internal fun DoomEngineCore.huLibInitTextLine(
    t: HudTextLine,
    x: Int,
    y: Int,
    f: Array<ByteArray>?,
    sc: Int,
) {
    t.x = x
    t.y = y
    t.f = f
    t.sc = sc
    huLibClearTextLine(t)
}

internal fun DoomEngineCore.huLibAddCharToTextLine(
    t: HudTextLine,
    ch: Char,
): Boolean {
    if (t.len == HU_MAXLINELENGTH)
        return false
    else {
        t.l[t.len] = ch
        t.len++
        t.l[t.len] = '\u0000'
        t.needsupdate = 4
        return true
    }
}

internal fun DoomEngineCore.huLibDelCharFromTextLine(t: HudTextLine): Boolean {
    if (t.len == 0) return false
    else {
        t.len--
        t.l[t.len] = '\u0000'
        t.needsupdate = 4
        return true
    }
}

internal fun DoomEngineCore.huLibDrawTextLine(
    l: HudTextLine,
    drawcursor: Boolean,
) {
    var w: Int
    var x: Int
    var c: Int

    x = l.x
    for (i in 0 until l.len) {
        c = l.l[i].code
        if (c >= 'a'.code && c <= 'z'.code) c -= 32
        if (c != ' '.code
            && c >= l.sc
            && c <= '_'.code
        ) {
            w = patchWidth(l.f!![c - l.sc])
            if (x + w > SCREENWIDTH)
                break
            vDrawPatchDirect(x, l.y, HU_FG, l.f!![c - l.sc])
            x += w
        } else {
            x += 4
            if (x >= SCREENWIDTH)
                break
        }
    }

    if (drawcursor
        && x + patchWidth(l.f!!['_'.code - l.sc]) <= SCREENWIDTH
    ) {
        vDrawPatchDirect(x, l.y, HU_FG, l.f!!['_'.code - l.sc])
    }
}

private var DoomEngineCore.lastautomapactive
    get() = stateHuLib.lastautomapactive
    set(value) { stateHuLib.lastautomapactive = value }

internal fun DoomEngineCore.huLibEraseTextLine(l: HudTextLine) {
    val lh: Int
    var y: Int
    var yoffset: Int


    if (!automapactive &&
        viewwindowx != 0 && l.needsupdate != 0
    ) {
        lh = patchHeight(l.f!![0]) + 1
        y = l.y
        yoffset = y * SCREENWIDTH
        while (y < l.y + lh) {
            if (y < viewwindowy || y >= viewwindowy + viewheight)
                rVideoErase(yoffset, SCREENWIDTH)
            else {
                rVideoErase(yoffset, viewwindowx)
                rVideoErase(yoffset + viewwindowx + viewwidth, viewwindowx)
            }
            y++
            yoffset += SCREENWIDTH
        }
    }

    lastautomapactive = automapactive
    if (l.needsupdate != 0) l.needsupdate--
}

internal fun DoomEngineCore.huLibInitSText(
    s: HudScrollingText,
    x: Int,
    y: Int,
    h: Int,
    font: Array<ByteArray>,
    startchar: Int,
    on: () -> Boolean,
) {
    s.h = h
    s.on = on
    s.laston = true
    s.cl = 0
    for (i in 0 until h)
        huLibInitTextLine(
            s.l[i],
            x, y - i * (patchHeight(font[0]) + 1),
            font, startchar
        )
}

internal fun DoomEngineCore.huLibAddLineToSText(s: HudScrollingText) {
    s.cl++
    if (s.cl == s.h)
        s.cl = 0
    huLibClearTextLine(s.l[s.cl])

    for (i in 0 until s.h)
        s.l[i].needsupdate = 4
}

internal fun DoomEngineCore.huLibAddMessageToSText(
    s: HudScrollingText,
    prefix: String?,
    msg: String,
) {
    huLibAddLineToSText(s)
    if (prefix != null)
        for (ch in prefix)
            huLibAddCharToTextLine(s.l[s.cl], ch)

    for (ch in msg)
        huLibAddCharToTextLine(s.l[s.cl], ch)
}

internal fun DoomEngineCore.huLibDrawSText(s: HudScrollingText) {
    var idx: Int
    var l: HudTextLine

    if (!s.on())
        return

    for (i in 0 until s.h) {
        idx = s.cl - i
        if (idx < 0)
            idx += s.h

        l = s.l[idx]

        huLibDrawTextLine(l, false)
    }
}

internal fun DoomEngineCore.huLibEraseSText(s: HudScrollingText) {
    for (i in 0 until s.h) {
        if (s.laston && !s.on())
            s.l[i].needsupdate = 4
        huLibEraseTextLine(s.l[i])
    }
    s.laston = s.on()
}

internal fun DoomEngineCore.huLibInitIText(
    it: HudInputText,
    x: Int,
    y: Int,
    font: Array<ByteArray>?,
    startchar: Int,
    on: () -> Boolean,
) {
    it.lm = 0
    it.on = on
    it.laston = true
    huLibInitTextLine(it.l, x, y, font, startchar)
}

internal fun DoomEngineCore.huLibDelCharFromIText(it: HudInputText) {
    if (it.l.len != it.lm)
        huLibDelCharFromTextLine(it.l)
}

internal fun DoomEngineCore.huLibEraseLineFromIText(it: HudInputText) {
    while (it.lm != it.l.len)
        huLibDelCharFromTextLine(it.l)
}

internal fun DoomEngineCore.huLibResetIText(it: HudInputText) {
    it.lm = 0
    huLibClearTextLine(it.l)
}

internal fun DoomEngineCore.huLibAddPrefixToIText(
    it: HudInputText,
    str: String,
) {
    for (ch in str)
        huLibAddCharToTextLine(it.l, ch)
    it.lm = it.l.len
}

internal fun DoomEngineCore.huLibKeyInIText(
    it: HudInputText,
    ch: Int,
): Boolean {
    if (ch >= ' '.code && ch <= '_'.code)
        huLibAddCharToTextLine(it.l, ch.toChar())
    else if (ch == KEY_BACKSPACE)
        huLibDelCharFromIText(it)
    else if (ch != KEY_ENTER)
        return false

    return true
}

internal fun DoomEngineCore.huLibDrawIText(it: HudInputText) {
    val l = it.l

    if (!it.on())
        return
    huLibDrawTextLine(l, true)
}

internal fun DoomEngineCore.huLibEraseIText(it: HudInputText) {
    if (it.laston && !it.on())
        it.l.needsupdate = 4
    huLibEraseTextLine(it.l)
    it.laston = it.on()
}
