// Port of linuxdoom-1.10 r_main.c -- rendering main loop and setup functions,
// utility functions (BSP, geometry, trigonometry). See tables.c, too.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class RendererViewState {
    var viewangleoffset = 0

    var validcount = 1

    var fixedcolormap = -1

    var centerx = 0

    var centery = 0

    var centerxfrac: fixed_t = 0

    var centeryfrac: fixed_t = 0

    var projection: fixed_t = 0

    var framecount = 0

    var sscount = 0

    var linecount = 0

    var loopcount = 0

    var viewx: fixed_t = 0

    var viewy: fixed_t = 0

    var viewz: fixed_t = 0

    var viewangle: angle_t = 0u

    var viewcos: fixed_t = 0

    var viewsin: fixed_t = 0

    var viewplayer: player_t? = null

    var detailshift = 0

    var clipangle: angle_t = 0u

    val viewangletox by lazy(LazyThreadSafetyMode.NONE) { IntArray(FINEANGLES / 2) }

    val xtoviewangle by lazy(LazyThreadSafetyMode.NONE) { IntArray(SCREENWIDTH + 1) }

    val scalelight by lazy(LazyThreadSafetyMode.NONE) { Array(LIGHTLEVELS) { IntArray(MAXLIGHTSCALE) } }

    val scalelightfixed by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXLIGHTSCALE) }

    val zlight by lazy(LazyThreadSafetyMode.NONE) { Array(LIGHTLEVELS) { IntArray(MAXLIGHTZ) } }

    var extralight = 0

    lateinit var colfunc: () -> Unit

    lateinit var basecolfunc: () -> Unit

    lateinit var fuzzcolfunc: () -> Unit

    lateinit var transcolfunc: () -> Unit

    lateinit var spanfunc: () -> Unit

    var setsizeneeded = false

    var setblocks = 0

    var setdetail = 0
}
