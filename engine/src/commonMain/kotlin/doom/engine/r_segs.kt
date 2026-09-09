// Port of linuxdoom-1.10 r_segs.c -- All the clipping: columns, horizontal
// spans, sky columns.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

import kotlin.math.abs

// OPTIMIZE: closed two sided lines as single sided

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

internal var DoomEngineCore.rw_normalangle: angle_t
    get() = stateWallRenderer.rw_normalangle
    set(value) { stateWallRenderer.rw_normalangle = value }

internal var DoomEngineCore.rw_angle1
    get() = stateWallRenderer.rw_angle1
    set(value) { stateWallRenderer.rw_angle1 = value }

internal var DoomEngineCore.rw_x
    get() = stateWallRenderer.rw_x
    set(value) { stateWallRenderer.rw_x = value }
internal var DoomEngineCore.rw_stopx
    get() = stateWallRenderer.rw_stopx
    set(value) { stateWallRenderer.rw_stopx = value }
internal var DoomEngineCore.rw_centerangle: angle_t
    get() = stateWallRenderer.rw_centerangle
    set(value) { stateWallRenderer.rw_centerangle = value }
internal var DoomEngineCore.rw_offset: fixed_t
    get() = stateWallRenderer.rw_offset
    set(value) { stateWallRenderer.rw_offset = value }
internal var DoomEngineCore.rw_distance: fixed_t
    get() = stateWallRenderer.rw_distance
    set(value) { stateWallRenderer.rw_distance = value }
internal var DoomEngineCore.rw_scale: fixed_t
    get() = stateWallRenderer.rw_scale
    set(value) { stateWallRenderer.rw_scale = value }
internal var DoomEngineCore.rw_scalestep: fixed_t
    get() = stateWallRenderer.rw_scalestep
    set(value) { stateWallRenderer.rw_scalestep = value }
internal var DoomEngineCore.rw_midtexturemid: fixed_t
    get() = stateWallRenderer.rw_midtexturemid
    set(value) { stateWallRenderer.rw_midtexturemid = value }
internal var DoomEngineCore.rw_toptexturemid: fixed_t
    get() = stateWallRenderer.rw_toptexturemid
    set(value) { stateWallRenderer.rw_toptexturemid = value }
internal var DoomEngineCore.rw_bottomtexturemid: fixed_t
    get() = stateWallRenderer.rw_bottomtexturemid
    set(value) { stateWallRenderer.rw_bottomtexturemid = value }

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

internal var DoomEngineCore.pixhigh: fixed_t
    get() = stateWallRenderer.pixhigh
    set(value) { stateWallRenderer.pixhigh = value }
internal var DoomEngineCore.pixlow: fixed_t
    get() = stateWallRenderer.pixlow
    set(value) { stateWallRenderer.pixlow = value }
internal var DoomEngineCore.pixhighstep: fixed_t
    get() = stateWallRenderer.pixhighstep
    set(value) { stateWallRenderer.pixhighstep = value }
internal var DoomEngineCore.pixlowstep: fixed_t
    get() = stateWallRenderer.pixlowstep
    set(value) { stateWallRenderer.pixlowstep = value }

internal var DoomEngineCore.topfrac: fixed_t
    get() = stateWallRenderer.topfrac
    set(value) { stateWallRenderer.topfrac = value }
internal var DoomEngineCore.topstep: fixed_t
    get() = stateWallRenderer.topstep
    set(value) { stateWallRenderer.topstep = value }

internal var DoomEngineCore.bottomfrac: fixed_t
    get() = stateWallRenderer.bottomfrac
    set(value) { stateWallRenderer.bottomfrac = value }
internal var DoomEngineCore.bottomstep: fixed_t
    get() = stateWallRenderer.bottomstep
    set(value) { stateWallRenderer.bottomstep = value }

internal var DoomEngineCore.walllights: IntArray
    get() = stateWallRenderer.walllights
    set(value) { stateWallRenderer.walllights = value }

internal var DoomEngineCore.maskedtexturecol: ShortArray
    get() = stateWallRenderer.maskedtexturecol
    set(value) { stateWallRenderer.maskedtexturecol = value }
internal var DoomEngineCore.maskedtexturecol_base
    get() = stateWallRenderer.maskedtexturecol_base
    set(value) { stateWallRenderer.maskedtexturecol_base = value }

//
// Port shim: in C, R_StoreWallRange assigns the constant clip arrays
// (screenheightarray / negonearray) directly to ds_p->sprtopclip /
// ds_p->sprbottomclip. In this port those drawseg fields are Int indexes
// into `openings`, so snapshot the constant array's [start, rw_stopx)
// range into openings and return the adjusted base index, so that
// openings[base + x] reads the exact values C read. The arrays are
// constant within a frame, so copy semantics match the C aliasing.
// (Rendering-only: consumes a little more of `openings` than vanilla.)
//
private fun DoomEngineCore.R_SnapshotClipArray(src: ShortArray, start: Int): Int {
    src.copyInto(openings, lastopening, start, rw_stopx)
    val base = lastopening - start
    lastopening += rw_stopx - start
    return base
}

//
// R_RenderMaskedSegRange
//
internal fun DoomEngineCore.R_RenderMaskedSegRange(ds: drawseg_t, x1: Int, x2: Int) {
    var index: Int  // unsigned in C
    var col: Int    // column_t* --> byte offset into GETCOL_DATA
    var lightnum: Int
    val texnum: Int

    // Calculate light table.
    // Use different light tables
    //   for horizontal / vertical / diagonal. Diagonal?
    // OPTIMIZE: get rid of LIGHTSEGSHIFT globally
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

    // maskedtexturecol = ds->maskedtexturecol;
    maskedtexturecol = openings
    maskedtexturecol_base = ds.maskedtexturecol

    rw_scalestep = ds.scalestep
    spryscale = ds.scale1 + (x1 - ds.x1) * rw_scalestep
    // mfloorclip = ds->sprbottomclip;
    mfloorclip = openings
    mfloorclip_base = ds.sprbottomclip
    // mceilingclip = ds->sprtopclip;
    mceilingclip = openings
    mceilingclip_base = ds.sprtopclip

    // find positioning
    if ((curline!!.linedef!!.flags and ML_DONTPEGBOTTOM) != 0) {
        dc_texturemid = if (frontsector!!.floorheight > backsector!!.floorheight)
            frontsector!!.floorheight else backsector!!.floorheight
        dc_texturemid = dc_texturemid + textureheight[texnum] - viewz
    } else {
        dc_texturemid = if (frontsector!!.ceilingheight < backsector!!.ceilingheight)
            frontsector!!.ceilingheight else backsector!!.ceilingheight
        dc_texturemid = dc_texturemid - viewz
    }
    dc_texturemid += curline!!.sidedef!!.rowoffset

    if (fixedcolormap != -1)
        dc_colormap = fixedcolormap

    // draw the columns
    dc_x = x1
    while (dc_x <= x2) {
        // calculate lighting
        if (maskedtexturecol[maskedtexturecol_base + dc_x].toInt() != MAXSHORT) {
            if (fixedcolormap == -1) {
                index = spryscale shr LIGHTSCALESHIFT

                if (index.toUInt() >= MAXLIGHTSCALE.toUInt())
                    index = MAXLIGHTSCALE - 1

                dc_colormap = walllights[index]
            }

            sprtopscreen = centeryfrac - FixedMul(dc_texturemid, spryscale)
            dc_iscale = (0xffffffffu / spryscale.toUInt()).toInt()

            // draw the texture
            // col = (column_t *)(
            //     (byte *)R_GetColumn(texnum,maskedtexturecol[dc_x]) -3);
            col = R_GetColumn(texnum,
                maskedtexturecol[maskedtexturecol_base + dc_x].toInt()) - 3

            R_DrawMaskedColumn(GETCOL_DATA, col)
            maskedtexturecol[maskedtexturecol_base + dc_x] = MAXSHORT.toShort()
        }
        spryscale += rw_scalestep
        dc_x++
    }
}

//
// R_RenderSegLoop
// Draws zero, one, or two textures (and possibly a masked
//  texture) for walls.
// Can draw or mark the starting pixel of floor and ceiling
//  textures.
// CALLED: CORE LOOPING ROUTINE.
//
private const val HEIGHTBITS = 12
private const val HEIGHTUNIT = 1 shl HEIGHTBITS

internal fun DoomEngineCore.R_RenderSegLoop() {
    var angle: angle_t
    var index: Int  // unsigned in C
    var yl: Int
    var yh: Int
    var mid: Int
    var texturecolumn: fixed_t = 0  // shut up compiler warning
    var top: Int
    var bottom: Int

    while (rw_x < rw_stopx) {
        // mark floor / ceiling areas
        yl = (topfrac + HEIGHTUNIT - 1) shr HEIGHTBITS

        // no space above wall?
        if (yl < ceilingclip[rw_x] + 1)
            yl = ceilingclip[rw_x] + 1

        if (markceiling) {
            top = ceilingclip[rw_x] + 1
            bottom = yl - 1

            if (bottom >= floorclip[rw_x])
                bottom = floorclip[rw_x] - 1

            if (top <= bottom) {
                visplanes[ceilingplane].top[rw_x] = top
                visplanes[ceilingplane].bottom[rw_x] = bottom
            }
        }

        yh = bottomfrac shr HEIGHTBITS

        if (yh >= floorclip[rw_x])
            yh = floorclip[rw_x] - 1

        if (markfloor) {
            top = yh + 1
            bottom = floorclip[rw_x] - 1
            if (top <= ceilingclip[rw_x])
                top = ceilingclip[rw_x] + 1
            if (top <= bottom) {
                visplanes[floorplane].top[rw_x] = top
                visplanes[floorplane].bottom[rw_x] = bottom
            }
        }

        // texturecolumn and lighting are independent of wall tiers
        if (segtextured) {
            // calculate texture offset
            angle = (rw_centerangle + xtoviewangle[rw_x].toUInt()) shr ANGLETOFINESHIFT
            texturecolumn = rw_offset - FixedMul(finetangent[angle.toInt()], rw_distance)
            texturecolumn = texturecolumn shr FRACBITS
            // calculate lighting
            index = rw_scale shr LIGHTSCALESHIFT

            if (index.toUInt() >= MAXLIGHTSCALE.toUInt())
                index = MAXLIGHTSCALE - 1

            dc_colormap = walllights[index]
            dc_x = rw_x
            dc_iscale = (0xffffffffu / rw_scale.toUInt()).toInt()
        }

        // draw the wall tiers
        if (midtexture != 0) {
            // single sided line
            dc_yl = yl
            dc_yh = yh
            dc_texturemid = rw_midtexturemid
            dc_source_ofs = R_GetColumn(midtexture, texturecolumn)
            dc_source = GETCOL_DATA
            colfunc()
            ceilingclip[rw_x] = viewheight.toShort()
            floorclip[rw_x] = -1
        } else {
            // two sided line
            if (toptexture != 0) {
                // top wall
                mid = pixhigh shr HEIGHTBITS
                pixhigh += pixhighstep

                if (mid >= floorclip[rw_x])
                    mid = floorclip[rw_x] - 1

                if (mid >= yl) {
                    dc_yl = yl
                    dc_yh = mid
                    dc_texturemid = rw_toptexturemid
                    dc_source_ofs = R_GetColumn(toptexture, texturecolumn)
                    dc_source = GETCOL_DATA
                    colfunc()
                    ceilingclip[rw_x] = mid.toShort()
                } else
                    ceilingclip[rw_x] = (yl - 1).toShort()
            } else {
                // no top wall
                if (markceiling)
                    ceilingclip[rw_x] = (yl - 1).toShort()
            }

            if (bottomtexture != 0) {
                // bottom wall
                mid = (pixlow + HEIGHTUNIT - 1) shr HEIGHTBITS
                pixlow += pixlowstep

                // no space above wall?
                if (mid <= ceilingclip[rw_x])
                    mid = ceilingclip[rw_x] + 1

                if (mid <= yh) {
                    dc_yl = mid
                    dc_yh = yh
                    dc_texturemid = rw_bottomtexturemid
                    dc_source_ofs = R_GetColumn(bottomtexture, texturecolumn)
                    dc_source = GETCOL_DATA
                    colfunc()
                    floorclip[rw_x] = mid.toShort()
                } else
                    floorclip[rw_x] = (yh + 1).toShort()
            } else {
                // no bottom wall
                if (markfloor)
                    floorclip[rw_x] = (yh + 1).toShort()
            }

            if (maskedtexture) {
                // save texturecol
                //  for backdrawing of masked mid texture
                maskedtexturecol[maskedtexturecol_base + rw_x] = texturecolumn.toShort()
            }
        }

        rw_scale += rw_scalestep
        topfrac += topstep
        bottomfrac += bottomstep
        rw_x++
    }
}

//
// R_StoreWallRange
// A wall segment will be drawn
//  between start and stop pixels (inclusive).
//
internal fun DoomEngineCore.R_StoreWallRange(start: Int, stop: Int) {
    val hyp: fixed_t
    var sineval: fixed_t
    val distangle: angle_t
    var offsetangle: angle_t
    var vtop: fixed_t
    var lightnum: Int

    // don't overflow and crash
    if (ds_p == MAXDRAWSEGS)  // ds_p == &drawsegs[MAXDRAWSEGS]
        return

    // #ifdef RANGECHECK
    if (start >= viewwidth || start > stop)
        I_Error("Bad R_RenderWallRange: $start to $stop")
    // #endif

    sidedef = curline!!.sidedef
    linedef = curline!!.linedef

    // mark the segment as visible for auto map
    linedef!!.flags = linedef!!.flags or ML_MAPPED

    // calculate rw_distance for scale calculation
    rw_normalangle = curline!!.angle + ANG90
    // offsetangle = abs(rw_normalangle-rw_angle1);
    offsetangle = abs((rw_normalangle - rw_angle1.toUInt()).toInt()).toUInt()

    if (offsetangle > ANG90)
        offsetangle = ANG90

    distangle = ANG90 - offsetangle
    hyp = R_PointToDist(curline!!.v1.x, curline!!.v1.y)
    sineval = finesine[(distangle shr ANGLETOFINESHIFT).toInt()]
    rw_distance = FixedMul(hyp, sineval)

    rw_x = start
    drawsegs[ds_p].x1 = rw_x  // ds_p->x1 = rw_x = start
    drawsegs[ds_p].x2 = stop
    drawsegs[ds_p].curline = curline
    rw_stopx = stop + 1

    // calculate scale at both ends and step
    rw_scale = R_ScaleFromGlobalAngle(viewangle + xtoviewangle[start].toUInt())
    drawsegs[ds_p].scale1 = rw_scale

    if (stop > start) {
        drawsegs[ds_p].scale2 = R_ScaleFromGlobalAngle(viewangle + xtoviewangle[stop].toUInt())
        rw_scalestep = (drawsegs[ds_p].scale2 - rw_scale) / (stop - start)
        drawsegs[ds_p].scalestep = rw_scalestep
    } else {
        // UNUSED: try to fix the stretched line bug
        // #if 0
        // if (rw_distance < FRACUNIT/2) { ... }
        // #endif
        drawsegs[ds_p].scale2 = drawsegs[ds_p].scale1
    }

    // calculate texture boundaries
    //  and decide if floor / ceiling marks are needed
    worldtop = frontsector!!.ceilingheight - viewz
    worldbottom = frontsector!!.floorheight - viewz

    // midtexture = toptexture = bottomtexture = maskedtexture = 0;
    midtexture = 0
    toptexture = 0
    bottomtexture = 0
    maskedtexture = false
    drawsegs[ds_p].maskedtexturecol = -1  // NULL

    if (backsector == null) {
        // single sided line
        midtexture = texturetranslation[sidedef!!.midtexture]
        // a single sided line is terminal, so it must mark ends
        markfloor = true
        markceiling = true
        if ((linedef!!.flags and ML_DONTPEGBOTTOM) != 0) {
            vtop = frontsector!!.floorheight +
                textureheight[sidedef!!.midtexture]
            // bottom of texture at bottom
            rw_midtexturemid = vtop - viewz
        } else {
            // top of texture at top
            rw_midtexturemid = worldtop
        }
        rw_midtexturemid += sidedef!!.rowoffset

        drawsegs[ds_p].silhouette = SIL_BOTH
        // ds_p->sprtopclip = screenheightarray;
        drawsegs[ds_p].sprtopclip = R_SnapshotClipArray(screenheightarray, start)
        // ds_p->sprbottomclip = negonearray;
        drawsegs[ds_p].sprbottomclip = R_SnapshotClipArray(negonearray, start)
        drawsegs[ds_p].bsilheight = MAXINT
        drawsegs[ds_p].tsilheight = MININT
    } else {
        // two sided line
        drawsegs[ds_p].sprtopclip = -1     // NULL
        drawsegs[ds_p].sprbottomclip = -1  // NULL
        drawsegs[ds_p].silhouette = 0

        if (frontsector!!.floorheight > backsector!!.floorheight) {
            drawsegs[ds_p].silhouette = SIL_BOTTOM
            drawsegs[ds_p].bsilheight = frontsector!!.floorheight
        } else if (backsector!!.floorheight > viewz) {
            drawsegs[ds_p].silhouette = SIL_BOTTOM
            drawsegs[ds_p].bsilheight = MAXINT
            // ds_p->sprbottomclip = negonearray;
        }

        if (frontsector!!.ceilingheight < backsector!!.ceilingheight) {
            drawsegs[ds_p].silhouette = drawsegs[ds_p].silhouette or SIL_TOP
            drawsegs[ds_p].tsilheight = frontsector!!.ceilingheight
        } else if (backsector!!.ceilingheight < viewz) {
            drawsegs[ds_p].silhouette = drawsegs[ds_p].silhouette or SIL_TOP
            drawsegs[ds_p].tsilheight = MININT
            // ds_p->sprtopclip = screenheightarray;
        }

        if (backsector!!.ceilingheight <= frontsector!!.floorheight) {
            // ds_p->sprbottomclip = negonearray;
            drawsegs[ds_p].sprbottomclip = R_SnapshotClipArray(negonearray, start)
            drawsegs[ds_p].bsilheight = MAXINT
            drawsegs[ds_p].silhouette = drawsegs[ds_p].silhouette or SIL_BOTTOM
        }

        if (backsector!!.floorheight >= frontsector!!.ceilingheight) {
            // ds_p->sprtopclip = screenheightarray;
            drawsegs[ds_p].sprtopclip = R_SnapshotClipArray(screenheightarray, start)
            drawsegs[ds_p].tsilheight = MININT
            drawsegs[ds_p].silhouette = drawsegs[ds_p].silhouette or SIL_TOP
        }

        worldhigh = backsector!!.ceilingheight - viewz
        worldlow = backsector!!.floorheight - viewz

        // hack to allow height changes in outdoor areas
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
            // same plane on both sides
            markfloor = false
        }

        if (worldhigh != worldtop
            || backsector!!.ceilingpic != frontsector!!.ceilingpic
            || backsector!!.lightlevel != frontsector!!.lightlevel
        ) {
            markceiling = true
        } else {
            // same plane on both sides
            markceiling = false
        }

        if (backsector!!.ceilingheight <= frontsector!!.floorheight
            || backsector!!.floorheight >= frontsector!!.ceilingheight
        ) {
            // closed door
            markceiling = true
            markfloor = true
        }

        if (worldhigh < worldtop) {
            // top texture
            toptexture = texturetranslation[sidedef!!.toptexture]
            if ((linedef!!.flags and ML_DONTPEGTOP) != 0) {
                // top of texture at top
                rw_toptexturemid = worldtop
            } else {
                vtop = backsector!!.ceilingheight +
                    textureheight[sidedef!!.toptexture]

                // bottom of texture
                rw_toptexturemid = vtop - viewz
            }
        }
        if (worldlow > worldbottom) {
            // bottom texture
            bottomtexture = texturetranslation[sidedef!!.bottomtexture]

            if ((linedef!!.flags and ML_DONTPEGBOTTOM) != 0) {
                // bottom of texture at bottom
                // top of texture at top
                rw_bottomtexturemid = worldtop
            } else {
                // top of texture at top
                rw_bottomtexturemid = worldlow
            }
        }
        rw_toptexturemid += sidedef!!.rowoffset
        rw_bottomtexturemid += sidedef!!.rowoffset

        // allocate space for masked texture tables
        if (sidedef!!.midtexture != 0) {
            // masked midtexture
            maskedtexture = true
            // ds_p->maskedtexturecol = maskedtexturecol = lastopening - rw_x;
            maskedtexturecol = openings
            maskedtexturecol_base = lastopening - rw_x
            drawsegs[ds_p].maskedtexturecol = maskedtexturecol_base
            lastopening += rw_stopx - rw_x
        }
    }

    // calculate rw_offset (only needed for textured lines)
    // segtextured = midtexture | toptexture | bottomtexture | maskedtexture;
    segtextured = midtexture != 0 || toptexture != 0 || bottomtexture != 0 || maskedtexture

    if (segtextured) {
        offsetangle = rw_normalangle - rw_angle1.toUInt()

        if (offsetangle > ANG180)
            offsetangle = 0u - offsetangle  // -offsetangle

        if (offsetangle > ANG90)
            offsetangle = ANG90

        sineval = finesine[(offsetangle shr ANGLETOFINESHIFT).toInt()]
        rw_offset = FixedMul(hyp, sineval)

        if (rw_normalangle - rw_angle1.toUInt() < ANG180)
            rw_offset = -rw_offset

        rw_offset += sidedef!!.textureoffset + curline!!.offset
        rw_centerangle = ANG90 + viewangle - rw_normalangle

        // calculate light table
        //  use different light tables
        //  for horizontal / vertical / diagonal
        // OPTIMIZE: get rid of LIGHTSEGSHIFT globally
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

    // if a floor / ceiling plane is on the wrong side
    //  of the view plane, it is definitely invisible
    //  and doesn't need to be marked.

    if (frontsector!!.floorheight >= viewz) {
        // above view plane
        markfloor = false
    }

    if (frontsector!!.ceilingheight <= viewz
        && frontsector!!.ceilingpic != skyflatnum
    ) {
        // below view plane
        markceiling = false
    }

    // calculate incremental stepping values for texture edges
    worldtop = worldtop shr 4
    worldbottom = worldbottom shr 4

    topstep = -FixedMul(rw_scalestep, worldtop)
    topfrac = (centeryfrac shr 4) - FixedMul(worldtop, rw_scale)

    bottomstep = -FixedMul(rw_scalestep, worldbottom)
    bottomfrac = (centeryfrac shr 4) - FixedMul(worldbottom, rw_scale)

    if (backsector != null) {
        worldhigh = worldhigh shr 4
        worldlow = worldlow shr 4

        if (worldhigh < worldtop) {
            pixhigh = (centeryfrac shr 4) - FixedMul(worldhigh, rw_scale)
            pixhighstep = -FixedMul(rw_scalestep, worldhigh)
        }

        if (worldlow > worldbottom) {
            pixlow = (centeryfrac shr 4) - FixedMul(worldlow, rw_scale)
            pixlowstep = -FixedMul(rw_scalestep, worldlow)
        }
    }

    // render it
    if (markceiling)
        ceilingplane = R_CheckPlane(ceilingplane, rw_x, rw_stopx - 1)

    if (markfloor)
        floorplane = R_CheckPlane(floorplane, rw_x, rw_stopx - 1)

    R_RenderSegLoop()

    // save sprite clipping info
    if (((drawsegs[ds_p].silhouette and SIL_TOP) != 0 || maskedtexture)
        && drawsegs[ds_p].sprtopclip == -1
    ) {
        // memcpy (lastopening, ceilingclip+start, 2*(rw_stopx-start));
        ceilingclip.copyInto(openings, lastopening, start, rw_stopx)
        drawsegs[ds_p].sprtopclip = lastopening - start
        lastopening += rw_stopx - start
    }

    if (((drawsegs[ds_p].silhouette and SIL_BOTTOM) != 0 || maskedtexture)
        && drawsegs[ds_p].sprbottomclip == -1
    ) {
        // memcpy (lastopening, floorclip+start, 2*(rw_stopx-start));
        floorclip.copyInto(openings, lastopening, start, rw_stopx)
        drawsegs[ds_p].sprbottomclip = lastopening - start
        lastopening += rw_stopx - start
    }

    if (maskedtexture && (drawsegs[ds_p].silhouette and SIL_TOP) == 0) {
        drawsegs[ds_p].silhouette = drawsegs[ds_p].silhouette or SIL_TOP
        drawsegs[ds_p].tsilheight = MININT
    }
    if (maskedtexture && (drawsegs[ds_p].silhouette and SIL_BOTTOM) == 0) {
        drawsegs[ds_p].silhouette = drawsegs[ds_p].silhouette or SIL_BOTTOM
        drawsegs[ds_p].bsilheight = MAXINT
    }
    ds_p++
}
