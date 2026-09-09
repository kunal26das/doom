// Port of linuxdoom-1.10 r_segs.c -- All the clipping: columns, horizontal
// spans, sky columns.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class WallRendererState {
    var segtextured = false

    var markfloor = false

    var markceiling = false

    var maskedtexture = false

    var toptexture = 0

    var bottomtexture = 0

    var midtexture = 0

    var rw_normalangle: angle_t = 0u

    var rw_angle1 = 0

    var rw_x = 0

    var rw_stopx = 0

    var rw_centerangle: angle_t = 0u

    var rw_offset: fixed_t = 0

    var rw_distance: fixed_t = 0

    var rw_scale: fixed_t = 0

    var rw_scalestep: fixed_t = 0

    var rw_midtexturemid: fixed_t = 0

    var rw_toptexturemid: fixed_t = 0

    var rw_bottomtexturemid: fixed_t = 0

    var worldtop = 0

    var worldbottom = 0

    var worldhigh = 0

    var worldlow = 0

    var pixhigh: fixed_t = 0

    var pixlow: fixed_t = 0

    var pixhighstep: fixed_t = 0

    var pixlowstep: fixed_t = 0

    var topfrac: fixed_t = 0

    var topstep: fixed_t = 0

    var bottomfrac: fixed_t = 0

    var bottomstep: fixed_t = 0

    var walllights: IntArray = IntArray(0)

    var maskedtexturecol: ShortArray = ShortArray(0)

    var maskedtexturecol_base = 0
}
