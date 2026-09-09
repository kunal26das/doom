// Port of linuxdoom-1.10 r_plane.c -- Here is a core component: drawing the
// floors and ceilings, while maintaining a per column clipping list only.
// Moreover, the sky areas have to be determined.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

import kotlin.math.abs

internal var DoomEngineCore.floorfunc: ((Int, Int) -> Unit)?
    get() = statePlaneRenderer.floorfunc
    set(value) { statePlaneRenderer.floorfunc = value }
internal var DoomEngineCore.ceilingfunc: ((Int, Int) -> Unit)?
    get() = statePlaneRenderer.ceilingfunc
    set(value) { statePlaneRenderer.ceilingfunc = value }

//
// opening
//

// Here comes the obnoxious "visplane".
internal const val MAXVISPLANES = 128
internal val DoomEngineCore.visplanes
    get() = statePlaneRenderer.visplanes
internal var DoomEngineCore.lastvisplane
    get() = statePlaneRenderer.lastvisplane
    set(value) { statePlaneRenderer.lastvisplane = value }
internal var DoomEngineCore.floorplane
    get() = statePlaneRenderer.floorplane
    set(value) { statePlaneRenderer.floorplane = value }
internal var DoomEngineCore.ceilingplane
    get() = statePlaneRenderer.ceilingplane
    set(value) { statePlaneRenderer.ceilingplane = value }

// ?
internal const val MAXOPENINGS = SCREENWIDTH * 64
internal val DoomEngineCore.openings
    get() = statePlaneRenderer.openings
internal var DoomEngineCore.lastopening
    get() = statePlaneRenderer.lastopening
    set(value) { statePlaneRenderer.lastopening = value }

internal val DoomEngineCore.floorclip
    get() = statePlaneRenderer.floorclip
internal val DoomEngineCore.ceilingclip
    get() = statePlaneRenderer.ceilingclip

internal val DoomEngineCore.spanstart
    get() = statePlaneRenderer.spanstart
internal val DoomEngineCore.spanstop
    get() = statePlaneRenderer.spanstop

internal var DoomEngineCore.planezlight: IntArray
    get() = statePlaneRenderer.planezlight
    set(value) { statePlaneRenderer.planezlight = value }
internal var DoomEngineCore.planeheight: fixed_t
    get() = statePlaneRenderer.planeheight
    set(value) { statePlaneRenderer.planeheight = value }

internal val DoomEngineCore.yslope
    get() = statePlaneRenderer.yslope
internal val DoomEngineCore.distscale
    get() = statePlaneRenderer.distscale
internal var DoomEngineCore.basexscale: fixed_t
    get() = statePlaneRenderer.basexscale
    set(value) { statePlaneRenderer.basexscale = value }
internal var DoomEngineCore.baseyscale: fixed_t
    get() = statePlaneRenderer.baseyscale
    set(value) { statePlaneRenderer.baseyscale = value }

internal val DoomEngineCore.cachedheight
    get() = statePlaneRenderer.cachedheight
internal val DoomEngineCore.cacheddistance
    get() = statePlaneRenderer.cacheddistance
internal val DoomEngineCore.cachedxstep
    get() = statePlaneRenderer.cachedxstep
internal val DoomEngineCore.cachedystep
    get() = statePlaneRenderer.cachedystep

//
// R_InitPlanes
// Only at game startup.
//
internal fun DoomEngineCore.R_InitPlanes() {
    // Doh!
}

//
// R_MapPlane
//
// Uses global vars:
//  planeheight
//  ds_source
//  basexscale
//  baseyscale
//  viewx
//  viewy
//
// BASIC PRIMITIVE
//
internal fun DoomEngineCore.R_MapPlane(y: Int, x1: Int, x2: Int) {
    val distance: fixed_t
    val length: fixed_t
    var index: UInt  // unsigned in C

    // #ifdef RANGECHECK
    if (x2 < x1
        || x1 < 0
        || x2 >= viewwidth
        || y.toUInt() > viewheight.toUInt()
    ) {
        I_Error("R_MapPlane: $x1, $x2 at $y")
    }
    // #endif

    if (planeheight != cachedheight[y]) {
        cachedheight[y] = planeheight
        distance = FixedMul(planeheight, yslope[y])
        cacheddistance[y] = distance
        ds_xstep = FixedMul(distance, basexscale)
        cachedxstep[y] = ds_xstep
        ds_ystep = FixedMul(distance, baseyscale)
        cachedystep[y] = ds_ystep
    } else {
        distance = cacheddistance[y]
        ds_xstep = cachedxstep[y]
        ds_ystep = cachedystep[y]
    }

    length = FixedMul(distance, distscale[x1])
    val angle = ((viewangle + xtoviewangle[x1].toUInt()) shr ANGLETOFINESHIFT).toInt()
    ds_xfrac = viewx + FixedMul(finecosine[angle], length)
    ds_yfrac = -viewy - FixedMul(finesine[angle], length)

    if (fixedcolormap != -1)
        ds_colormap = fixedcolormap
    else {
        index = (distance shr LIGHTZSHIFT).toUInt()

        if (index >= MAXLIGHTZ.toUInt())
            index = (MAXLIGHTZ - 1).toUInt()

        ds_colormap = planezlight[index.toInt()]
    }

    ds_y = y
    ds_x1 = x1
    ds_x2 = x2

    // high or low detail
    spanfunc()
}

//
// R_ClearPlanes
// At begining of frame.
//
internal fun DoomEngineCore.R_ClearPlanes() {
    // opening / clipping determination
    for (i in 0 until viewwidth) {
        floorclip[i] = viewheight.toShort()
        ceilingclip[i] = -1
    }

    lastvisplane = 0  // lastvisplane = visplanes
    lastopening = 0   // lastopening = openings

    // texture calculation
    cachedheight.fill(0)  // memset (cachedheight, 0, sizeof(cachedheight))

    // left to right mapping
    val angle = ((viewangle - ANG90) shr ANGLETOFINESHIFT).toInt()

    // scale will be unit scale at SCREENWIDTH/2 distance
    basexscale = FixedDiv(finecosine[angle], centerxfrac)
    baseyscale = -FixedDiv(finesine[angle], centerxfrac)
}

//
// R_FindPlane
// (C returns visplane_t*; here: index into visplanes.)
//
internal fun DoomEngineCore.R_FindPlane(height: fixed_t, picnum: Int, lightlevel: Int): Int {
    var height = height
    var lightlevel = lightlevel
    var check: Int

    if (picnum == skyflatnum) {
        height = 0  // all skys map together
        lightlevel = 0
    }

    check = 0
    while (check < lastvisplane) {
        if (height == visplanes[check].height
            && picnum == visplanes[check].picnum
            && lightlevel == visplanes[check].lightlevel
        ) {
            break
        }
        check++
    }

    if (check < lastvisplane)
        return check

    if (lastvisplane == MAXVISPLANES)
        I_Error("R_FindPlane: no more visplanes")

    lastvisplane++

    visplanes[check].height = height
    visplanes[check].picnum = picnum
    visplanes[check].lightlevel = lightlevel
    visplanes[check].minx = SCREENWIDTH
    visplanes[check].maxx = -1

    visplanes[check].top.fill(0xff)  // memset (check->top,0xff,sizeof(check->top))

    return check
}

//
// R_CheckPlane
// (C takes/returns visplane_t*; here: index into visplanes.)
//
internal fun DoomEngineCore.R_CheckPlane(pl: Int, start: Int, stop: Int): Int {
    var pl = pl
    val intrl: Int
    val intrh: Int
    val unionl: Int
    val unionh: Int
    var x: Int

    if (start < visplanes[pl].minx) {
        intrl = visplanes[pl].minx
        unionl = start
    } else {
        unionl = visplanes[pl].minx
        intrl = start
    }

    if (stop > visplanes[pl].maxx) {
        intrh = visplanes[pl].maxx
        unionh = stop
    } else {
        unionh = visplanes[pl].maxx
        intrh = stop
    }

    x = intrl
    while (x <= intrh) {
        if (visplanes[pl].top[x] != 0xff)
            break
        x++
    }

    if (x > intrh) {
        visplanes[pl].minx = unionl
        visplanes[pl].maxx = unionh

        // use the same one
        return pl
    }

    // make a new visplane
    visplanes[lastvisplane].height = visplanes[pl].height
    visplanes[lastvisplane].picnum = visplanes[pl].picnum
    visplanes[lastvisplane].lightlevel = visplanes[pl].lightlevel

    pl = lastvisplane
    lastvisplane++
    visplanes[pl].minx = start
    visplanes[pl].maxx = stop

    visplanes[pl].top.fill(0xff)  // memset (pl->top,0xff,sizeof(pl->top))

    return pl
}

//
// R_MakeSpans
//
internal fun DoomEngineCore.R_MakeSpans(x: Int, t1: Int, b1: Int, t2: Int, b2: Int) {
    var t1 = t1
    var b1 = b1
    var t2 = t2
    var b2 = b2

    while (t1 < t2 && t1 <= b1) {
        R_MapPlane(t1, spanstart[t1], x - 1)
        t1++
    }
    while (b1 > b2 && b1 >= t1) {
        R_MapPlane(b1, spanstart[b1], x - 1)
        b1--
    }

    while (t2 < t1 && t2 <= b2) {
        spanstart[t2] = x
        t2++
    }
    while (b2 > b1 && b2 >= t2) {
        spanstart[b2] = x
        b2--
    }
}

//
// R_DrawPlanes
// At the end of each frame.
//
internal fun DoomEngineCore.R_DrawPlanes() {
    var light: Int
    var x: Int
    var stop: Int
    var angle: Int

    // #ifdef RANGECHECK
    if (ds_p > MAXDRAWSEGS)
        I_Error("R_DrawPlanes: drawsegs overflow ($ds_p)")

    if (lastvisplane > MAXVISPLANES)
        I_Error("R_DrawPlanes: visplane overflow ($lastvisplane)")

    if (lastopening > MAXOPENINGS)
        I_Error("R_DrawPlanes: opening overflow ($lastopening)")
    // #endif

    for (pli in 0 until lastvisplane) {  // for (pl = visplanes ; pl < lastvisplane ; pl++)
        val pl = visplanes[pli]

        if (pl.minx > pl.maxx)
            continue

        // sky flat
        if (pl.picnum == skyflatnum) {
            dc_iscale = pspriteiscale shr detailshift

            // Sky is allways drawn full bright,
            //  i.e. colormaps[0] is used.
            // Because of this hack, sky is not affected
            //  by INVUL inverse mapping.
            dc_colormap = 0  // colormaps
            dc_texturemid = skytexturemid
            x = pl.minx
            while (x <= pl.maxx) {
                dc_yl = pl.top[x]
                dc_yh = pl.bottom[x]

                if (dc_yl <= dc_yh) {
                    angle = ((viewangle + xtoviewangle[x].toUInt()) shr ANGLETOSKYSHIFT).toInt()
                    dc_x = x
                    dc_source_ofs = R_GetColumn(skytexture, angle)
                    dc_source = GETCOL_DATA
                    colfunc()
                }
                x++
            }
            continue
        }

        // regular flat
        ds_source = W_CacheLumpNum(firstflat + flattranslation[pl.picnum])
        ds_source_ofs = 0

        planeheight = abs(pl.height - viewz)
        light = (pl.lightlevel shr LIGHTSEGSHIFT) + extralight

        if (light >= LIGHTLEVELS)
            light = LIGHTLEVELS - 1

        if (light < 0)
            light = 0

        planezlight = zlight[light]

        pl.top[pl.maxx + 1] = 0xff
        pl.top[pl.minx - 1] = 0xff

        stop = pl.maxx + 1

        x = pl.minx
        while (x <= stop) {
            R_MakeSpans(x, pl.top[x - 1],
                pl.bottom[x - 1],
                pl.top[x],
                pl.bottom[x])
            x++
        }

        // Z_ChangeTag (ds_source, PU_CACHE); -- deleted
    }
}
