// Port of linuxdoom-1.10 r_things.c -- refresh of things, i.e. objects
// represented by sprites.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class SpriteRendererState {
    var pspritescale: fixed_t = 0

    var pspriteiscale: fixed_t = 0

    var spritelights: IntArray = IntArray(0)

    val negonearray by lazy(LazyThreadSafetyMode.NONE) { ShortArray(SCREENWIDTH) }

    val screenheightarray by lazy(LazyThreadSafetyMode.NONE) { ShortArray(SCREENWIDTH) }

    var sprites: Array<spritedef_t> = emptyArray()

    var numsprites = 0

    val sprtemp by lazy(LazyThreadSafetyMode.NONE) { Array(29) { spriteframe_t() } }

    var maxframe = 0

    var spritename: String = ""

    val vissprites by lazy(LazyThreadSafetyMode.NONE) { Array(MAXVISSPRITES) { vissprite_t() } }

    var vissprite_p = 0

    var newvissprite = 0

    val overflowsprite by lazy(LazyThreadSafetyMode.NONE) { vissprite_t() }

    var mfloorclip: ShortArray = ShortArray(0)

    var mfloorclip_base = 0

    var mceilingclip: ShortArray = ShortArray(0)

    var mceilingclip_base = 0

    var spryscale: fixed_t = 0

    var sprtopscreen: fixed_t = 0

    val vsprsortedhead by lazy(LazyThreadSafetyMode.NONE) { vissprite_t() }
}
