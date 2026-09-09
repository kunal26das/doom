
package doom.engine.rendering

import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.geometry.ANG180
import doom.engine.geometry.ANG90
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.geometry.finetangent
import doom.engine.rendering.resources.getcolDATA
import doom.engine.rendering.resources.rGetColumn
import doom.engine.rendering.resources.textureheight
import doom.engine.rendering.resources.texturetranslation
import doom.engine.resources.MAXINT
import doom.engine.resources.MAXSHORT
import doom.engine.resources.MININT
import doom.engine.world.ML_DONTPEGBOTTOM
import doom.engine.world.ML_DONTPEGTOP
import doom.engine.world.ML_MAPPED

import kotlin.math.abs


internal var DoomEngineCore.segtextured
    get() = stateWallRenderer.segtextured
    set(value) { stateWallRenderer.segtextured = value }

internal var DoomEngineCore.markfloor
    get() = stateWallRenderer.markfloor
    set(value) { stateWallRenderer.markfloor = value }
internal var DoomEngineCore.markceiling
    get() = stateWallRenderer.markceiling
    set(value) { stateWallRenderer.markceiling = value }

internal var DoomEngineCore.maskedtexture
    get() = stateWallRenderer.maskedtexture
    set(value) { stateWallRenderer.maskedtexture = value }
internal var DoomEngineCore.toptexture
    get() = stateWallRenderer.toptexture
    set(value) { stateWallRenderer.toptexture = value }
internal var DoomEngineCore.bottomtexture
    get() = stateWallRenderer.bottomtexture
    set(value) { stateWallRenderer.bottomtexture = value }
internal var DoomEngineCore.midtexture
    get() = stateWallRenderer.midtexture
    set(value) { stateWallRenderer.midtexture = value }

internal var DoomEngineCore.rwNormalangle: BinaryAngle
    get() = stateWallRenderer.rwNormalangle
    set(value) { stateWallRenderer.rwNormalangle = value }

internal var DoomEngineCore.rwAngle1
    get() = stateWallRenderer.rwAngle1
    set(value) { stateWallRenderer.rwAngle1 = value }

internal var DoomEngineCore.rwX
    get() = stateWallRenderer.rwX
    set(value) { stateWallRenderer.rwX = value }
internal var DoomEngineCore.rwStopx
    get() = stateWallRenderer.rwStopx
    set(value) { stateWallRenderer.rwStopx = value }
internal var DoomEngineCore.rwCenterangle: BinaryAngle
    get() = stateWallRenderer.rwCenterangle
    set(value) { stateWallRenderer.rwCenterangle = value }
internal var DoomEngineCore.rwOffset: FixedPoint
    get() = stateWallRenderer.rwOffset
    set(value) { stateWallRenderer.rwOffset = value }
internal var DoomEngineCore.rwDistance: FixedPoint
    get() = stateWallRenderer.rwDistance
    set(value) { stateWallRenderer.rwDistance = value }
internal var DoomEngineCore.rwScale: FixedPoint
    get() = stateWallRenderer.rwScale
    set(value) { stateWallRenderer.rwScale = value }
internal var DoomEngineCore.rwScalestep: FixedPoint
    get() = stateWallRenderer.rwScalestep
    set(value) { stateWallRenderer.rwScalestep = value }
internal var DoomEngineCore.rwMidtexturemid: FixedPoint
    get() = stateWallRenderer.rwMidtexturemid
    set(value) { stateWallRenderer.rwMidtexturemid = value }
internal var DoomEngineCore.rwToptexturemid: FixedPoint
    get() = stateWallRenderer.rwToptexturemid
    set(value) { stateWallRenderer.rwToptexturemid = value }
internal var DoomEngineCore.rwBottomtexturemid: FixedPoint
    get() = stateWallRenderer.rwBottomtexturemid
    set(value) { stateWallRenderer.rwBottomtexturemid = value }

internal var DoomEngineCore.worldtop
    get() = stateWallRenderer.worldtop
    set(value) { stateWallRenderer.worldtop = value }
internal var DoomEngineCore.worldbottom
    get() = stateWallRenderer.worldbottom
    set(value) { stateWallRenderer.worldbottom = value }
internal var DoomEngineCore.worldhigh
    get() = stateWallRenderer.worldhigh
    set(value) { stateWallRenderer.worldhigh = value }
internal var DoomEngineCore.worldlow
    get() = stateWallRenderer.worldlow
    set(value) { stateWallRenderer.worldlow = value }

internal var DoomEngineCore.pixhigh: FixedPoint
    get() = stateWallRenderer.pixhigh
    set(value) { stateWallRenderer.pixhigh = value }
internal var DoomEngineCore.pixlow: FixedPoint
    get() = stateWallRenderer.pixlow
    set(value) { stateWallRenderer.pixlow = value }
internal var DoomEngineCore.pixhighstep: FixedPoint
    get() = stateWallRenderer.pixhighstep
    set(value) { stateWallRenderer.pixhighstep = value }
internal var DoomEngineCore.pixlowstep: FixedPoint
    get() = stateWallRenderer.pixlowstep
    set(value) { stateWallRenderer.pixlowstep = value }

internal var DoomEngineCore.topfrac: FixedPoint
    get() = stateWallRenderer.topfrac
    set(value) { stateWallRenderer.topfrac = value }
internal var DoomEngineCore.topstep: FixedPoint
    get() = stateWallRenderer.topstep
    set(value) { stateWallRenderer.topstep = value }

internal var DoomEngineCore.bottomfrac: FixedPoint
    get() = stateWallRenderer.bottomfrac
    set(value) { stateWallRenderer.bottomfrac = value }
internal var DoomEngineCore.bottomstep: FixedPoint
    get() = stateWallRenderer.bottomstep
    set(value) { stateWallRenderer.bottomstep = value }

internal var DoomEngineCore.walllights: IntArray
    get() = stateWallRenderer.walllights
    set(value) { stateWallRenderer.walllights = value }

internal var DoomEngineCore.maskedtexturecol: ShortArray
    get() = stateWallRenderer.maskedtexturecol
    set(value) { stateWallRenderer.maskedtexturecol = value }
internal var DoomEngineCore.maskedtexturecolBase
    get() = stateWallRenderer.maskedtexturecolBase
    set(value) { stateWallRenderer.maskedtexturecolBase = value }

private fun DoomEngineCore.rSnapshotClipArray(src: ShortArray, start: Int): Int {
    src.copyInto(openings, lastopening, start, rwStopx)
    val base = lastopening - start
    lastopening += rwStopx - start
    return base
}

internal fun DoomEngineCore.rRenderMaskedSegRange(ds: DrawSegment, x1: Int, x2: Int) {
    var index: Int
    var col: Int
    var lightnum: Int
    val texnum: Int

    curline = ds.curline
    frontsector = curline!!.frontsector
    backsector = curline!!.backsector
    texnum = texturetranslation[curline!!.sidedef!!.midtexture]

    lightnum = (frontsector!!.lightlevel shr LIGHTSEGSHIFT) + extralight

    if (curline!!.v1.y == curline!!.v2.y)
        lightnum--
    else if (curline!!.v1.x == curline!!.v2.x)
        lightnum++

    if (lightnum < 0)
        walllights = scalelight[0]
    else if (lightnum >= LIGHTLEVELS)
        walllights = scalelight[LIGHTLEVELS - 1]
    else
        walllights = scalelight[lightnum]

    maskedtexturecol = openings
    maskedtexturecolBase = ds.maskedtexturecol

    rwScalestep = ds.scalestep
    spryscale = ds.scale1 + (x1 - ds.x1) * rwScalestep
    mfloorclip = openings
    mfloorclipBase = ds.sprbottomclip
    mceilingclip = openings
    mceilingclipBase = ds.sprtopclip

    if ((curline!!.linedef!!.flags and ML_DONTPEGBOTTOM) != 0) {
        dcTexturemid = if (frontsector!!.floorheight > backsector!!.floorheight)
            frontsector!!.floorheight else backsector!!.floorheight
        dcTexturemid = dcTexturemid + textureheight[texnum] - viewz
    } else {
        dcTexturemid = if (frontsector!!.ceilingheight < backsector!!.ceilingheight)
            frontsector!!.ceilingheight else backsector!!.ceilingheight
        dcTexturemid = dcTexturemid - viewz
    }
    dcTexturemid += curline!!.sidedef!!.rowoffset

    if (fixedcolormap != -1)
        dcColormap = fixedcolormap

    dcX = x1
    while (dcX <= x2) {
        if (maskedtexturecol[maskedtexturecolBase + dcX].toInt() != MAXSHORT) {
            if (fixedcolormap == -1) {
                index = spryscale shr LIGHTSCALESHIFT

                if (index.toUInt() >= MAXLIGHTSCALE.toUInt())
                    index = MAXLIGHTSCALE - 1

                dcColormap = walllights[index]
            }

            sprtopscreen = centeryfrac - fixedMul(dcTexturemid, spryscale)
            dcIscale = (0xffffffffu / spryscale.toUInt()).toInt()

            col = rGetColumn(texnum,
                maskedtexturecol[maskedtexturecolBase + dcX].toInt()) - 3

            rDrawMaskedColumn(getcolDATA, col)
            maskedtexturecol[maskedtexturecolBase + dcX] = MAXSHORT.toShort()
        }
        spryscale += rwScalestep
        dcX++
    }
}

private const val HEIGHTBITS = 12
private const val HEIGHTUNIT = 1 shl HEIGHTBITS

internal fun DoomEngineCore.rRenderSegLoop() {
    var angle: BinaryAngle
    var index: Int
    var yl: Int
    var yh: Int
    var mid: Int
    var texturecolumn: FixedPoint = 0
    var top: Int
    var bottom: Int

    while (rwX < rwStopx) {
        yl = (topfrac + HEIGHTUNIT - 1) shr HEIGHTBITS

        if (yl < ceilingclip[rwX] + 1)
            yl = ceilingclip[rwX] + 1

        if (markceiling) {
            top = ceilingclip[rwX] + 1
            bottom = yl - 1

            if (bottom >= floorclip[rwX])
                bottom = floorclip[rwX] - 1

            if (top <= bottom) {
                visplanes[ceilingplane].top[rwX] = top
                visplanes[ceilingplane].bottom[rwX] = bottom
            }
        }

        yh = bottomfrac shr HEIGHTBITS

        if (yh >= floorclip[rwX])
            yh = floorclip[rwX] - 1

        if (markfloor) {
            top = yh + 1
            bottom = floorclip[rwX] - 1
            if (top <= ceilingclip[rwX])
                top = ceilingclip[rwX] + 1
            if (top <= bottom) {
                visplanes[floorplane].top[rwX] = top
                visplanes[floorplane].bottom[rwX] = bottom
            }
        }

        if (segtextured) {
            angle = (rwCenterangle + xtoviewangle[rwX].toUInt()) shr ANGLETOFINESHIFT
            texturecolumn = rwOffset - fixedMul(finetangent[angle.toInt()], rwDistance)
            texturecolumn = texturecolumn shr FRACBITS
            index = rwScale shr LIGHTSCALESHIFT

            if (index.toUInt() >= MAXLIGHTSCALE.toUInt())
                index = MAXLIGHTSCALE - 1

            dcColormap = walllights[index]
            dcX = rwX
            dcIscale = (0xffffffffu / rwScale.toUInt()).toInt()
        }

        if (midtexture != 0) {
            dcYl = yl
            dcYh = yh
            dcTexturemid = rwMidtexturemid
            dcSourceOfs = rGetColumn(midtexture, texturecolumn)
            dcSource = getcolDATA
            colfunc()
            ceilingclip[rwX] = viewheight.toShort()
            floorclip[rwX] = -1
        } else {
            if (toptexture != 0) {
                mid = pixhigh shr HEIGHTBITS
                pixhigh += pixhighstep

                if (mid >= floorclip[rwX])
                    mid = floorclip[rwX] - 1

                if (mid >= yl) {
                    dcYl = yl
                    dcYh = mid
                    dcTexturemid = rwToptexturemid
                    dcSourceOfs = rGetColumn(toptexture, texturecolumn)
                    dcSource = getcolDATA
                    colfunc()
                    ceilingclip[rwX] = mid.toShort()
                } else
                    ceilingclip[rwX] = (yl - 1).toShort()
            } else {
                if (markceiling)
                    ceilingclip[rwX] = (yl - 1).toShort()
            }

            if (bottomtexture != 0) {
                mid = (pixlow + HEIGHTUNIT - 1) shr HEIGHTBITS
                pixlow += pixlowstep

                if (mid <= ceilingclip[rwX])
                    mid = ceilingclip[rwX] + 1

                if (mid <= yh) {
                    dcYl = mid
                    dcYh = yh
                    dcTexturemid = rwBottomtexturemid
                    dcSourceOfs = rGetColumn(bottomtexture, texturecolumn)
                    dcSource = getcolDATA
                    colfunc()
                    floorclip[rwX] = mid.toShort()
                } else
                    floorclip[rwX] = (yh + 1).toShort()
            } else {
                if (markfloor)
                    floorclip[rwX] = (yh + 1).toShort()
            }

            if (maskedtexture) {
                maskedtexturecol[maskedtexturecolBase + rwX] = texturecolumn.toShort()
            }
        }

        rwScale += rwScalestep
        topfrac += topstep
        bottomfrac += bottomstep
        rwX++
    }
}

internal fun DoomEngineCore.rStoreWallRange(start: Int, stop: Int) {
    val hyp: FixedPoint
    var sineval: FixedPoint
    val distangle: BinaryAngle
    var offsetangle: BinaryAngle
    val vtop: FixedPoint
    var lightnum: Int

    if (dsP == MAXDRAWSEGS)
        return

    if (start >= viewwidth || start > stop)
        iError("Bad R_RenderWallRange: $start to $stop")

    sidedef = curline!!.sidedef
    linedef = curline!!.linedef

    linedef!!.flags = linedef!!.flags or ML_MAPPED

    rwNormalangle = curline!!.angle + ANG90
    offsetangle = abs((rwNormalangle - rwAngle1.toUInt()).toInt()).toUInt()

    if (offsetangle > ANG90)
        offsetangle = ANG90

    distangle = ANG90 - offsetangle
    hyp = rPointToDist(curline!!.v1.x, curline!!.v1.y)
    sineval = finesine[(distangle shr ANGLETOFINESHIFT).toInt()]
    rwDistance = fixedMul(hyp, sineval)

    rwX = start
    drawsegs[dsP].x1 = rwX
    drawsegs[dsP].x2 = stop
    drawsegs[dsP].curline = curline
    rwStopx = stop + 1

    rwScale = rScaleFromGlobalAngle(viewangle + xtoviewangle[start].toUInt())
    drawsegs[dsP].scale1 = rwScale

    if (stop > start) {
        drawsegs[dsP].scale2 = rScaleFromGlobalAngle(viewangle + xtoviewangle[stop].toUInt())
        rwScalestep = (drawsegs[dsP].scale2 - rwScale) / (stop - start)
        drawsegs[dsP].scalestep = rwScalestep
    } else {
        drawsegs[dsP].scale2 = drawsegs[dsP].scale1
    }

    worldtop = frontsector!!.ceilingheight - viewz
    worldbottom = frontsector!!.floorheight - viewz

    midtexture = 0
    toptexture = 0
    bottomtexture = 0
    maskedtexture = false
    drawsegs[dsP].maskedtexturecol = -1

    if (backsector == null) {
        midtexture = texturetranslation[sidedef!!.midtexture]
        markfloor = true
        markceiling = true
        if ((linedef!!.flags and ML_DONTPEGBOTTOM) != 0) {
            vtop = frontsector!!.floorheight +
                textureheight[sidedef!!.midtexture]
            rwMidtexturemid = vtop - viewz
        } else {
            rwMidtexturemid = worldtop
        }
        rwMidtexturemid += sidedef!!.rowoffset

        drawsegs[dsP].silhouette = SIL_BOTH
        drawsegs[dsP].sprtopclip = rSnapshotClipArray(screenheightarray, start)
        drawsegs[dsP].sprbottomclip = rSnapshotClipArray(negonearray, start)
        drawsegs[dsP].bsilheight = MAXINT
        drawsegs[dsP].tsilheight = MININT
    } else {
        drawsegs[dsP].sprtopclip = -1
        drawsegs[dsP].sprbottomclip = -1
        drawsegs[dsP].silhouette = 0

        if (frontsector!!.floorheight > backsector!!.floorheight) {
            drawsegs[dsP].silhouette = SIL_BOTTOM
            drawsegs[dsP].bsilheight = frontsector!!.floorheight
        } else if (backsector!!.floorheight > viewz) {
            drawsegs[dsP].silhouette = SIL_BOTTOM
            drawsegs[dsP].bsilheight = MAXINT
        }

        if (frontsector!!.ceilingheight < backsector!!.ceilingheight) {
            drawsegs[dsP].silhouette = drawsegs[dsP].silhouette or SIL_TOP
            drawsegs[dsP].tsilheight = frontsector!!.ceilingheight
        } else if (backsector!!.ceilingheight < viewz) {
            drawsegs[dsP].silhouette = drawsegs[dsP].silhouette or SIL_TOP
            drawsegs[dsP].tsilheight = MININT
        }

        if (backsector!!.ceilingheight <= frontsector!!.floorheight) {
            drawsegs[dsP].sprbottomclip = rSnapshotClipArray(negonearray, start)
            drawsegs[dsP].bsilheight = MAXINT
            drawsegs[dsP].silhouette = drawsegs[dsP].silhouette or SIL_BOTTOM
        }

        if (backsector!!.floorheight >= frontsector!!.ceilingheight) {
            drawsegs[dsP].sprtopclip = rSnapshotClipArray(screenheightarray, start)
            drawsegs[dsP].tsilheight = MININT
            drawsegs[dsP].silhouette = drawsegs[dsP].silhouette or SIL_TOP
        }

        worldhigh = backsector!!.ceilingheight - viewz
        worldlow = backsector!!.floorheight - viewz

        if (frontsector!!.ceilingpic == skyflatnum
            && backsector!!.ceilingpic == skyflatnum
        ) {
            worldtop = worldhigh
        }

        if (worldlow != worldbottom
            || backsector!!.floorpic != frontsector!!.floorpic
            || backsector!!.lightlevel != frontsector!!.lightlevel
        ) {
            markfloor = true
        } else {
            markfloor = false
        }

        if (worldhigh != worldtop
            || backsector!!.ceilingpic != frontsector!!.ceilingpic
            || backsector!!.lightlevel != frontsector!!.lightlevel
        ) {
            markceiling = true
        } else {
            markceiling = false
        }

        if (backsector!!.ceilingheight <= frontsector!!.floorheight
            || backsector!!.floorheight >= frontsector!!.ceilingheight
        ) {
            markceiling = true
            markfloor = true
        }

        if (worldhigh < worldtop) {
            toptexture = texturetranslation[sidedef!!.toptexture]
            if ((linedef!!.flags and ML_DONTPEGTOP) != 0) {
                rwToptexturemid = worldtop
            } else {
                vtop = backsector!!.ceilingheight +
                    textureheight[sidedef!!.toptexture]

                rwToptexturemid = vtop - viewz
            }
        }
        if (worldlow > worldbottom) {
            bottomtexture = texturetranslation[sidedef!!.bottomtexture]

            if ((linedef!!.flags and ML_DONTPEGBOTTOM) != 0) {
                rwBottomtexturemid = worldtop
            } else {
                rwBottomtexturemid = worldlow
            }
        }
        rwToptexturemid += sidedef!!.rowoffset
        rwBottomtexturemid += sidedef!!.rowoffset

        if (sidedef!!.midtexture != 0) {
            maskedtexture = true
            maskedtexturecol = openings
            maskedtexturecolBase = lastopening - rwX
            drawsegs[dsP].maskedtexturecol = maskedtexturecolBase
            lastopening += rwStopx - rwX
        }
    }

    segtextured = midtexture != 0 || toptexture != 0 || bottomtexture != 0 || maskedtexture

    if (segtextured) {
        offsetangle = rwNormalangle - rwAngle1.toUInt()

        if (offsetangle > ANG180)
            offsetangle = 0u - offsetangle

        if (offsetangle > ANG90)
            offsetangle = ANG90

        sineval = finesine[(offsetangle shr ANGLETOFINESHIFT).toInt()]
        rwOffset = fixedMul(hyp, sineval)

        if (rwNormalangle - rwAngle1.toUInt() < ANG180)
            rwOffset = -rwOffset

        rwOffset += sidedef!!.textureoffset + curline!!.offset
        rwCenterangle = ANG90 + viewangle - rwNormalangle

        if (fixedcolormap == -1) {
            lightnum = (frontsector!!.lightlevel shr LIGHTSEGSHIFT) + extralight

            if (curline!!.v1.y == curline!!.v2.y)
                lightnum--
            else if (curline!!.v1.x == curline!!.v2.x)
                lightnum++

            if (lightnum < 0)
                walllights = scalelight[0]
            else if (lightnum >= LIGHTLEVELS)
                walllights = scalelight[LIGHTLEVELS - 1]
            else
                walllights = scalelight[lightnum]
        }
    }


    if (frontsector!!.floorheight >= viewz) {
        markfloor = false
    }

    if (frontsector!!.ceilingheight <= viewz
        && frontsector!!.ceilingpic != skyflatnum
    ) {
        markceiling = false
    }

    worldtop = worldtop shr 4
    worldbottom = worldbottom shr 4

    topstep = -fixedMul(rwScalestep, worldtop)
    topfrac = (centeryfrac shr 4) - fixedMul(worldtop, rwScale)

    bottomstep = -fixedMul(rwScalestep, worldbottom)
    bottomfrac = (centeryfrac shr 4) - fixedMul(worldbottom, rwScale)

    if (backsector != null) {
        worldhigh = worldhigh shr 4
        worldlow = worldlow shr 4

        if (worldhigh < worldtop) {
            pixhigh = (centeryfrac shr 4) - fixedMul(worldhigh, rwScale)
            pixhighstep = -fixedMul(rwScalestep, worldhigh)
        }

        if (worldlow > worldbottom) {
            pixlow = (centeryfrac shr 4) - fixedMul(worldlow, rwScale)
            pixlowstep = -fixedMul(rwScalestep, worldlow)
        }
    }

    if (markceiling)
        ceilingplane = rCheckPlane(ceilingplane, rwX, rwStopx - 1)

    if (markfloor)
        floorplane = rCheckPlane(floorplane, rwX, rwStopx - 1)

    rRenderSegLoop()

    if (((drawsegs[dsP].silhouette and SIL_TOP) != 0 || maskedtexture)
        && drawsegs[dsP].sprtopclip == -1
    ) {
        ceilingclip.copyInto(openings, lastopening, start, rwStopx)
        drawsegs[dsP].sprtopclip = lastopening - start
        lastopening += rwStopx - start
    }

    if (((drawsegs[dsP].silhouette and SIL_BOTTOM) != 0 || maskedtexture)
        && drawsegs[dsP].sprbottomclip == -1
    ) {
        floorclip.copyInto(openings, lastopening, start, rwStopx)
        drawsegs[dsP].sprbottomclip = lastopening - start
        lastopening += rwStopx - start
    }

    if (maskedtexture && (drawsegs[dsP].silhouette and SIL_TOP) == 0) {
        drawsegs[dsP].silhouette = drawsegs[dsP].silhouette or SIL_TOP
        drawsegs[dsP].tsilheight = MININT
    }
    if (maskedtexture && (drawsegs[dsP].silhouette and SIL_BOTTOM) == 0) {
        drawsegs[dsP].silhouette = drawsegs[dsP].silhouette or SIL_BOTTOM
        drawsegs[dsP].bsilheight = MAXINT
    }
    dsP++
}
