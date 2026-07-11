// Port of linuxdoom-1.10 hu_lib.c/hu_lib.h -- heads-up text and input code.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// background and foreground screen numbers
// different from other modules.
// (C: file-scope "#define BG 1 / #define FG 0"; renamed HU_BG/HU_FG here
//  because st_lib.kt owns the public BG/FG values, 4/0.)
private const val HU_BG = 1
private const val HU_FG = 0

// font stuff
const val HU_CHARERASE = KEY_BACKSPACE

const val HU_MAXLINES = 4
const val HU_MAXLINELENGTH = 80

//
// Typedefs of widgets
//

// Text Line widget
//  (parent of Scrolling Text and Input Text widgets)
class hu_textline_t {
    // left-justified position of scrolling text window
    var x = 0
    var y = 0

    var f: Array<ByteArray>? = null       // font (patch_t**; null == C NULL)
    var sc = 0                            // start character
    val l = CharArray(HU_MAXLINELENGTH + 1)  // line of text
    var len = 0                           // current line length

    // whether this line needs to be udpated
    var needsupdate = 0
}

// Scrolling Text window widget
//  (child of Text Line widget)
class hu_stext_t {
    val l = Array(HU_MAXLINES) { hu_textline_t() }  // text lines to draw
    var h = 0                             // height in lines
    var cl = 0                            // current line number

    // pointer to boolean stating whether to update window
    // (C boolean* --> read-only accessor lambda)
    var on: () -> Boolean = { false }
    var laston = false                    // last value of *->on.
}

// Input Text Line widget
//  (child of Text Line widget)
class hu_itext_t {
    val l = hu_textline_t()               // text line to input on

    // left margin past which I am not to delete characters
    var lm = 0

    // pointer to boolean stating whether to update window
    // (C boolean* --> read-only accessor lambda)
    var on: () -> Boolean = { false }
    var laston = false                    // last value of *->on;
}

// boolean : whether the screen is always erased
// #define noterased viewwindowx

fun HUlib_init() {
}

fun HUlib_clearTextLine(t: hu_textline_t) {
    t.len = 0
    t.l[0] = '\u0000'
    t.needsupdate = 1 // true
}

fun HUlib_initTextLine(
    t: hu_textline_t,
    x: Int,
    y: Int,
    f: Array<ByteArray>?,
    sc: Int,
) {
    t.x = x
    t.y = y
    t.f = f
    t.sc = sc
    HUlib_clearTextLine(t)
}

fun HUlib_addCharToTextLine(
    t: hu_textline_t,
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

fun HUlib_delCharFromTextLine(t: hu_textline_t): Boolean {
    if (t.len == 0) return false
    else {
        t.len--
        t.l[t.len] = '\u0000'
        t.needsupdate = 4
        return true
    }
}

fun HUlib_drawTextLine(
    l: hu_textline_t,
    drawcursor: Boolean,
) {
    var w: Int
    var x: Int
    var c: Int

    // draw the new stuff
    x = l.x
    for (i in 0 until l.len) {
        c = l.l[i].code
        if (c >= 'a'.code && c <= 'z'.code) c -= 32 // toupper (C locale)
        if (c != ' '.code
            && c >= l.sc
            && c <= '_'.code
        ) {
            w = patchWidth(l.f!![c - l.sc])
            if (x + w > SCREENWIDTH)
                break
            V_DrawPatchDirect(x, l.y, HU_FG, l.f!![c - l.sc])
            x += w
        } else {
            x += 4
            if (x >= SCREENWIDTH)
                break
        }
    }

    // draw the cursor if requested
    if (drawcursor
        && x + patchWidth(l.f!!['_'.code - l.sc]) <= SCREENWIDTH
    ) {
        V_DrawPatchDirect(x, l.y, HU_FG, l.f!!['_'.code - l.sc])
    }
}

// static boolean lastautomapactive (in HUlib_eraseTextLine)
private var lastautomapactive = true

// sorta called by HU_Erase and just better darn get things straight
fun HUlib_eraseTextLine(l: hu_textline_t) {
    val lh: Int
    var y: Int
    var yoffset: Int

    // Only erases when NOT in automap and the screen is reduced,
    // and the text must either need updating or refreshing
    // (because of a recent change back from the automap)

    if (!automapactive &&
        viewwindowx != 0 && l.needsupdate != 0
    ) {
        lh = patchHeight(l.f!![0]) + 1
        y = l.y
        yoffset = y * SCREENWIDTH
        while (y < l.y + lh) {
            if (y < viewwindowy || y >= viewwindowy + viewheight)
                R_VideoErase(yoffset, SCREENWIDTH) // erase entire line
            else {
                R_VideoErase(yoffset, viewwindowx) // erase left border
                R_VideoErase(yoffset + viewwindowx + viewwidth, viewwindowx)
                // erase right border
            }
            y++
            yoffset += SCREENWIDTH
        }
    }

    lastautomapactive = automapactive
    if (l.needsupdate != 0) l.needsupdate--
}

fun HUlib_initSText(
    s: hu_stext_t,
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
        HUlib_initTextLine(
            s.l[i],
            x, y - i * (patchHeight(font[0]) + 1),
            font, startchar
        )
}

fun HUlib_addLineToSText(s: hu_stext_t) {
    // add a clear line
    s.cl++
    if (s.cl == s.h)
        s.cl = 0
    HUlib_clearTextLine(s.l[s.cl])

    // everything needs updating
    for (i in 0 until s.h)
        s.l[i].needsupdate = 4
}

fun HUlib_addMessageToSText(
    s: hu_stext_t,
    prefix: String?,
    msg: String,
) {
    HUlib_addLineToSText(s)
    if (prefix != null)
        for (ch in prefix)
            HUlib_addCharToTextLine(s.l[s.cl], ch)

    for (ch in msg)
        HUlib_addCharToTextLine(s.l[s.cl], ch)
}

fun HUlib_drawSText(s: hu_stext_t) {
    var idx: Int
    var l: hu_textline_t

    if (!s.on())
        return // if not on, don't draw

    // draw everything
    for (i in 0 until s.h) {
        idx = s.cl - i
        if (idx < 0)
            idx += s.h // handle queue of lines

        l = s.l[idx]

        // need a decision made here on whether to skip the draw
        HUlib_drawTextLine(l, false) // no cursor, please
    }
}

fun HUlib_eraseSText(s: hu_stext_t) {
    for (i in 0 until s.h) {
        if (s.laston && !s.on())
            s.l[i].needsupdate = 4
        HUlib_eraseTextLine(s.l[i])
    }
    s.laston = s.on()
}

fun HUlib_initIText(
    it: hu_itext_t,
    x: Int,
    y: Int,
    font: Array<ByteArray>?,
    startchar: Int,
    on: () -> Boolean,
) {
    it.lm = 0 // default left margin is start of text
    it.on = on
    it.laston = true
    HUlib_initTextLine(it.l, x, y, font, startchar)
}

// The following deletion routines adhere to the left margin restriction
fun HUlib_delCharFromIText(it: hu_itext_t) {
    if (it.l.len != it.lm)
        HUlib_delCharFromTextLine(it.l)
}

fun HUlib_eraseLineFromIText(it: hu_itext_t) {
    while (it.lm != it.l.len)
        HUlib_delCharFromTextLine(it.l)
}

// Resets left margin as well
fun HUlib_resetIText(it: hu_itext_t) {
    it.lm = 0
    HUlib_clearTextLine(it.l)
}

fun HUlib_addPrefixToIText(
    it: hu_itext_t,
    str: String,
) {
    for (ch in str)
        HUlib_addCharToTextLine(it.l, ch)
    it.lm = it.l.len
}

// wrapper function for handling general keyed input.
// returns true if it ate the key
fun HUlib_keyInIText(
    it: hu_itext_t,
    ch: Int,
): Boolean {
    if (ch >= ' '.code && ch <= '_'.code)
        HUlib_addCharToTextLine(it.l, ch.toChar())
    else if (ch == KEY_BACKSPACE)
        HUlib_delCharFromIText(it)
    else if (ch != KEY_ENTER)
        return false // did not eat key

    return true // ate the key
}

fun HUlib_drawIText(it: hu_itext_t) {
    val l = it.l

    if (!it.on())
        return
    HUlib_drawTextLine(l, true) // draw the line w/ cursor
}

fun HUlib_eraseIText(it: hu_itext_t) {
    if (it.laston && !it.on())
        it.l.needsupdate = 4
    HUlib_eraseTextLine(it.l)
    it.laston = it.on()
}
