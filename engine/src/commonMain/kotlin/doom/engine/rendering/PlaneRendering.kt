
package doom.engine.rendering

import doom.engine.SCREENWIDTH
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.geometry.ANG90
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.fixedDiv
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.rendering.resources.getcolDATA
import doom.engine.rendering.resources.rGetColumn
import doom.engine.rendering.resources.firstflat
import doom.engine.rendering.resources.flattranslation
import doom.engine.resources.wCacheLumpNum

import kotlin.math.abs

internal var DoomEngineCore.floorfunc: ((Int, Int) -> Unit)?
    get() = statePlaneRenderer.floorfunc
    set(value) { statePlaneRenderer.floorfunc = value }
internal var DoomEngineCore.ceilingfunc: ((Int, Int) -> Unit)?
    get() = statePlaneRenderer.ceilingfunc
    set(value) { statePlaneRenderer.ceilingfunc = value }


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
internal var DoomEngineCore.planeheight: FixedPoint
    get() = statePlaneRenderer.planeheight
    set(value) { statePlaneRenderer.planeheight = value }

internal val DoomEngineCore.yslope
    get() = statePlaneRenderer.yslope
internal val DoomEngineCore.distscale
    get() = statePlaneRenderer.distscale
internal var DoomEngineCore.basexscale: FixedPoint
    get() = statePlaneRenderer.basexscale
    set(value) { statePlaneRenderer.basexscale = value }
internal var DoomEngineCore.baseyscale: FixedPoint
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

internal fun DoomEngineCore.rInitPlanes() {
}

internal fun DoomEngineCore.rMapPlane(y: Int, x1: Int, x2: Int) {
    val distance: FixedPoint
    val length: FixedPoint
    var index: UInt

    if (x2 < x1
        || x1 < 0
        || x2 >= viewwidth
        || y.toUInt() > viewheight.toUInt()
    ) {
        iError("R_MapPlane: $x1, $x2 at $y")
    }

    if (planeheight != cachedheight[y]) {
        cachedheight[y] = planeheight
        distance = fixedMul(planeheight, yslope[y])
        cacheddistance[y] = distance
        dsXstep = fixedMul(distance, basexscale)
        cachedxstep[y] = dsXstep
        dsYstep = fixedMul(distance, baseyscale)
        cachedystep[y] = dsYstep
    } else {
        distance = cacheddistance[y]
        dsXstep = cachedxstep[y]
        dsYstep = cachedystep[y]
    }

    length = fixedMul(distance, distscale[x1])
    val angle = ((viewangle + xtoviewangle[x1].toUInt()) shr ANGLETOFINESHIFT).toInt()
    dsXfrac = viewx + fixedMul(FineCosineTable[angle], length)
    dsYfrac = -viewy - fixedMul(finesine[angle], length)

    if (fixedcolormap != -1)
        dsColormap = fixedcolormap
    else {
        index = (distance shr LIGHTZSHIFT).toUInt()

        if (index >= MAXLIGHTZ.toUInt())
            index = (MAXLIGHTZ - 1).toUInt()

        dsColormap = planezlight[index.toInt()]
    }

    dsY = y
    dsX1 = x1
    dsX2 = x2

    spanfunc()
}

internal fun DoomEngineCore.rClearPlanes() {
    for (i in 0 until viewwidth) {
        floorclip[i] = viewheight.toShort()
        ceilingclip[i] = -1
    }

    lastvisplane = 0
    lastopening = 0

    cachedheight.fill(0)

    val angle = ((viewangle - ANG90) shr ANGLETOFINESHIFT).toInt()

    basexscale = fixedDiv(FineCosineTable[angle], centerxfrac)
    baseyscale = -fixedDiv(finesine[angle], centerxfrac)
}

internal fun DoomEngineCore.rFindPlane(planeHeight: FixedPoint, picnum: Int, planeLightLevel: Int): Int {
    var height = planeHeight
    var lightlevel = planeLightLevel
    var check: Int

    if (picnum == skyflatnum) {
        height = 0
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
        iError("R_FindPlane: no more visplanes")

    lastvisplane++

    visplanes[check].height = height
    visplanes[check].picnum = picnum
    visplanes[check].lightlevel = lightlevel
    visplanes[check].minx = SCREENWIDTH
    visplanes[check].maxx = -1

    visplanes[check].top.fill(0xff)

    return check
}

internal fun DoomEngineCore.rCheckPlane(plane: Int, start: Int, stop: Int): Int {
    var pl = plane
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

        return pl
    }

    visplanes[lastvisplane].height = visplanes[pl].height
    visplanes[lastvisplane].picnum = visplanes[pl].picnum
    visplanes[lastvisplane].lightlevel = visplanes[pl].lightlevel

    pl = lastvisplane
    lastvisplane++
    visplanes[pl].minx = start
    visplanes[pl].maxx = stop

    visplanes[pl].top.fill(0xff)

    return pl
}

internal fun DoomEngineCore.rMakeSpans(x: Int, previousTop: Int, previousBottom: Int, currentTop: Int, currentBottom: Int) {
    var t1 = previousTop
    var b1 = previousBottom
    var t2 = currentTop
    var b2 = currentBottom

    while (t1 < t2 && t1 <= b1) {
        rMapPlane(t1, spanstart[t1], x - 1)
        t1++
    }
    while (b1 > b2 && b1 >= t1) {
        rMapPlane(b1, spanstart[b1], x - 1)
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

internal fun DoomEngineCore.rDrawPlanes() {
    var light: Int
    var x: Int
    var stop: Int
    var angle: Int

    if (dsP > MAXDRAWSEGS)
        iError("R_DrawPlanes: drawsegs overflow ($dsP)")

    if (lastvisplane > MAXVISPLANES)
        iError("R_DrawPlanes: visplane overflow ($lastvisplane)")

    if (lastopening > MAXOPENINGS)
        iError("R_DrawPlanes: opening overflow ($lastopening)")

    for (pli in 0 until lastvisplane) {
        val pl = visplanes[pli]

        if (pl.minx > pl.maxx)
            continue

        if (pl.picnum == skyflatnum) {
            dcIscale = pspriteiscale shr detailshift

            dcColormap = 0
            dcTexturemid = skytexturemid
            x = pl.minx
            while (x <= pl.maxx) {
                dcYl = pl.top[x]
                dcYh = pl.bottom[x]

                if (dcYl <= dcYh) {
                    angle = ((viewangle + xtoviewangle[x].toUInt()) shr ANGLETOSKYSHIFT).toInt()
                    dcX = x
                    dcSourceOfs = rGetColumn(skytexture, angle)
                    dcSource = getcolDATA
                    colfunc()
                }
                x++
            }
            continue
        }

        dsSource = wCacheLumpNum(firstflat + flattranslation[pl.picnum])
        dsSourceOfs = 0

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
            rMakeSpans(x, pl.top[x - 1],
                pl.bottom[x - 1],
                pl.top[x],
                pl.bottom[x])
            x++
        }

    }
}
