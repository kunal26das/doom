// Port of linuxdoom-1.10 r_things.c -- refresh of things, i.e. objects
// represented by sprites.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VALUE", "UNUSED_VARIABLE",
    "NAME_SHADOWING", "MagicNumber", "ktlint")

package doom.engine

import kotlin.math.abs

private const val MINZ = FRACUNIT * 4
private const val BASEYCENTER = 100

internal const val MAXVISSPRITES = 128

//void R_DrawColumn (void);
//void R_DrawFuzzColumn (void);


internal var DoomEngineCore.pspritescale: fixed_t
    get() = stateSpriteRenderer.pspritescale
    set(value) { stateSpriteRenderer.pspritescale = value }
internal var DoomEngineCore.pspriteiscale: fixed_t
    get() = stateSpriteRenderer.pspriteiscale
    set(value) { stateSpriteRenderer.pspriteiscale = value }

internal var DoomEngineCore.spritelights: IntArray
    get() = stateSpriteRenderer.spritelights
    set(value) { stateSpriteRenderer.spritelights = value }

internal val DoomEngineCore.negonearray
    get() = stateSpriteRenderer.negonearray
internal val DoomEngineCore.screenheightarray
    get() = stateSpriteRenderer.screenheightarray

//
// INITIALIZATION FUNCTIONS
//

internal var DoomEngineCore.sprites: Array<spritedef_t>
    get() = stateSpriteRenderer.sprites
    set(value) { stateSpriteRenderer.sprites = value }
internal var DoomEngineCore.numsprites
    get() = stateSpriteRenderer.numsprites
    set(value) { stateSpriteRenderer.numsprites = value }

internal val DoomEngineCore.sprtemp
    get() = stateSpriteRenderer.sprtemp
internal var DoomEngineCore.maxframe
    get() = stateSpriteRenderer.maxframe
    set(value) { stateSpriteRenderer.maxframe = value }
internal var DoomEngineCore.spritename: String
    get() = stateSpriteRenderer.spritename
    set(value) { stateSpriteRenderer.spritename = value }

// C reads lump name chars past the NUL terminator as 0 (names are 8 bytes,
// NUL padded; our lumpinfo names are the NUL-truncated strings).
private fun DoomEngineCore.R_NameChar(name: String, i: Int): Int =
    if (i < name.length) name[i].code else 0

//
// R_InstallSpriteLump
// Local function for R_InitSprites.
//
internal fun DoomEngineCore.R_InstallSpriteLump(
    lump: Int,
    frame: Int,       // unsigned in C
    rotation: Int,    // unsigned in C
    flipped: Boolean,
) {
    var rotation = rotation
    var r: Int

    if (frame.toUInt() >= 29u || rotation.toUInt() > 8u)
        I_Error("R_InstallSpriteLump: Bad frame characters in lump $lump")

    if (frame > maxframe)
        maxframe = frame

    if (rotation == 0) {
        // the lump should be used for all rotations
        if (sprtemp[frame].rotate == 0)
            I_Error("R_InitSprites: Sprite $spritename frame ${'A' + frame} has " +
                "multip rot=0 lump")

        if (sprtemp[frame].rotate == 1)
            I_Error("R_InitSprites: Sprite $spritename frame ${'A' + frame} has rotations " +
                "and a rot=0 lump")

        sprtemp[frame].rotate = 0
        r = 0
        while (r < 8) {
            sprtemp[frame].lump[r] = lump - firstspritelump
            sprtemp[frame].flip[r] = if (flipped) 1 else 0
            r++
        }
        return
    }

    // the lump is only used for one rotation
    if (sprtemp[frame].rotate == 0)
        I_Error("R_InitSprites: Sprite $spritename frame ${'A' + frame} has rotations " +
            "and a rot=0 lump")

    sprtemp[frame].rotate = 1

    // make 0 based
    rotation--
    if (sprtemp[frame].lump[rotation] != -1)
        I_Error("R_InitSprites: Sprite $spritename : ${'A' + frame} : ${'1' + rotation} " +
            "has two lumps mapped to it")

    sprtemp[frame].lump[rotation] = lump - firstspritelump
    sprtemp[frame].flip[rotation] = if (flipped) 1 else 0
}

//
// R_InitSpriteDefs
// Pass a null terminated list of sprite names
//  (4 chars exactly) to be used.
// Builds the sprite rotation matrixes to account
//  for horizontally flipped sprites.
// Will report an error if the lumps are inconsistant.
// Only called at startup.
//
// Sprite lump names are 4 characters for the actor,
//  a letter for the frame, and a number for the rotation.
// A sprite that is flippable will have an additional
//  letter/number appended.
// The rotation character can be 0 to signify no rotations.
//
internal fun DoomEngineCore.R_InitSpriteDefs(namelist: Array<String>) {
    var frame: Int
    var rotation: Int

    // count the number of sprite names
    numsprites = namelist.size

    if (numsprites == 0)
        return

    sprites = Array(numsprites) { spritedef_t() }

    val start = firstspritelump - 1
    val end = lastspritelump + 1

    // scan all the lump names for each of the names,
    //  noting the highest frame letter.
    // Just compare 4 characters as ints
    for (i in 0 until numsprites) {
        spritename = namelist[i]
        // memset (sprtemp,-1, sizeof(sprtemp));
        for (f in 0 until 29) {
            sprtemp[f].rotate = -1
            sprtemp[f].lump.fill(-1)
            sprtemp[f].flip.fill(-1)
        }

        maxframe = -1
        val intname = namelist[i]  // *(int *)namelist[i] -- first 4 chars

        // scan the lumps,
        //  filling in the frames for whatever is found
        for (l in start + 1 until end) {
            val name = lumpinfo[l].name
            if (name.regionMatches(0, intname, 0, 4)) {
                frame = R_NameChar(name, 4) - 'A'.code
                rotation = R_NameChar(name, 5) - '0'.code

                val patched: Int
                if (modifiedgame)
                    patched = W_GetNumForName(name)
                else
                    patched = l

                R_InstallSpriteLump(patched, frame, rotation, false)

                if (R_NameChar(name, 6) != 0) {
                    frame = R_NameChar(name, 6) - 'A'.code
                    rotation = R_NameChar(name, 7) - '0'.code
                    R_InstallSpriteLump(l, frame, rotation, true)
                }
            }
        }

        // check the frames that were found for completeness
        if (maxframe == -1) {
            sprites[i].numframes = 0
            continue
        }

        maxframe++

        frame = 0
        while (frame < maxframe) {
            when (sprtemp[frame].rotate) {
                -1 -> {
                    // no rotations were found for that frame at all
                    I_Error("R_InitSprites: No patches found " +
                        "for ${namelist[i]} frame ${'A' + frame}")
                }
                0 -> {
                    // only the first rotation is needed
                }
                1 -> {
                    // must have all 8 frames
                    rotation = 0
                    while (rotation < 8) {
                        if (sprtemp[frame].lump[rotation] == -1)
                            I_Error("R_InitSprites: Sprite ${namelist[i]} frame ${'A' + frame} " +
                                "is missing rotations")
                        rotation++
                    }
                }
            }
            frame++
        }

        // allocate space for the frames present and copy sprtemp to it
        sprites[i].numframes = maxframe
        sprites[i].spriteframes = Array<spriteframe_t?>(maxframe) { f ->
            // memcpy (sprites[i].spriteframes, sprtemp, maxframe*sizeof(spriteframe_t));
            val copy = spriteframe_t()
            copy.rotate = sprtemp[f].rotate
            sprtemp[f].lump.copyInto(copy.lump)
            sprtemp[f].flip.copyInto(copy.flip)
            copy
        }
    }
}

internal val DoomEngineCore.vissprites
    get() = stateSpriteRenderer.vissprites
internal var DoomEngineCore.vissprite_p
    get() = stateSpriteRenderer.vissprite_p
    set(value) { stateSpriteRenderer.vissprite_p = value }
internal var DoomEngineCore.newvissprite
    get() = stateSpriteRenderer.newvissprite
    set(value) { stateSpriteRenderer.newvissprite = value }

//
// R_InitSprites
// Called at program start.
//
internal fun DoomEngineCore.R_InitSprites(namelist: Array<String>) {
    for (i in 0 until SCREENWIDTH) {
        negonearray[i] = -1
    }

    R_InitSpriteDefs(namelist)
}

//
// R_ClearSprites
// Called at frame start.
//
internal fun DoomEngineCore.R_ClearSprites() {
    vissprite_p = 0  // vissprite_p = vissprites
}

internal val DoomEngineCore.overflowsprite
    get() = stateSpriteRenderer.overflowsprite

internal fun DoomEngineCore.R_NewVisSprite(): vissprite_t {
    if (vissprite_p == MAXVISSPRITES)
        return overflowsprite

    vissprite_p++
    return vissprites[vissprite_p - 1]
}

internal var DoomEngineCore.mfloorclip: ShortArray
    get() = stateSpriteRenderer.mfloorclip
    set(value) { stateSpriteRenderer.mfloorclip = value }
internal var DoomEngineCore.mfloorclip_base
    get() = stateSpriteRenderer.mfloorclip_base
    set(value) { stateSpriteRenderer.mfloorclip_base = value }
internal var DoomEngineCore.mceilingclip: ShortArray
    get() = stateSpriteRenderer.mceilingclip
    set(value) { stateSpriteRenderer.mceilingclip = value }
internal var DoomEngineCore.mceilingclip_base
    get() = stateSpriteRenderer.mceilingclip_base
    set(value) { stateSpriteRenderer.mceilingclip_base = value }

internal var DoomEngineCore.spryscale: fixed_t
    get() = stateSpriteRenderer.spryscale
    set(value) { stateSpriteRenderer.spryscale = value }
internal var DoomEngineCore.sprtopscreen: fixed_t
    get() = stateSpriteRenderer.sprtopscreen
    set(value) { stateSpriteRenderer.sprtopscreen = value }

internal fun DoomEngineCore.R_DrawMaskedColumn(column: ByteArray, ofs: Int) {
    var column_ofs = ofs
    var topscreen: Int
    var bottomscreen: Int
    val basetexturemid: fixed_t

    basetexturemid = dc_texturemid

    while (column.u8(column_ofs) != 0xff) {  // column->topdelta != 0xff
        // calculate unclipped screen coordinates
        //  for post
        topscreen = sprtopscreen + spryscale * column.u8(column_ofs)          // column->topdelta
        bottomscreen = topscreen + spryscale * column.u8(column_ofs + 1)      // column->length

        dc_yl = (topscreen + FRACUNIT - 1) shr FRACBITS
        dc_yh = (bottomscreen - 1) shr FRACBITS

        if (dc_yh >= mfloorclip[mfloorclip_base + dc_x])
            dc_yh = mfloorclip[mfloorclip_base + dc_x] - 1
        if (dc_yl <= mceilingclip[mceilingclip_base + dc_x])
            dc_yl = mceilingclip[mceilingclip_base + dc_x] + 1

        if (dc_yl <= dc_yh) {
            dc_source = column               // dc_source = (byte *)column + 3
            dc_source_ofs = column_ofs + 3
            dc_texturemid = basetexturemid - (column.u8(column_ofs) shl FRACBITS)
            // dc_source = (byte *)column + 3 - column->topdelta;

            // Drawn by either R_DrawColumn
            //  or (SHADOW) R_DrawFuzzColumn.
            colfunc()
        }
        // column = (column_t *)(  (byte *)column + column->length + 4);
        column_ofs = column_ofs + column.u8(column_ofs + 1) + 4
    }

    dc_texturemid = basetexturemid
}

//
// R_DrawVisSprite
//  mfloorclip and mceilingclip should also be set.
//
internal fun DoomEngineCore.R_DrawVisSprite(vis: vissprite_t, x1: Int, x2: Int) {
    var texturecolumn: Int
    var frac: fixed_t
    val patch: ByteArray

    patch = W_CacheLumpNum(vis.patch + firstspritelump)

    dc_colormap = vis.colormap

    if (dc_colormap == -1) {
        // NULL colormap = shadow draw
        colfunc = fuzzcolfunc
    } else if ((vis.mobjflags and MF_TRANSLATION) != 0) {
        colfunc = { R_DrawTranslatedColumn() }
        // dc_translation = translationtables - 256 + (...): byte offset into
        // translationtables.
        dc_translation = ((vis.mobjflags and MF_TRANSLATION) shr (MF_TRANSSHIFT - 8)) - 256
    }

    dc_iscale = abs(vis.xiscale) shr detailshift
    dc_texturemid = vis.texturemid
    frac = vis.startfrac
    spryscale = vis.scale
    sprtopscreen = centeryfrac - FixedMul(dc_texturemid, spryscale)

    dc_x = vis.x1
    while (dc_x <= vis.x2) {
        texturecolumn = frac shr FRACBITS
        // #ifdef RANGECHECK
        if (texturecolumn < 0 || texturecolumn >= patchWidth(patch))
            I_Error("R_DrawSpriteRange: bad texturecolumn")
        // #endif
        // column = (column_t *)((byte *)patch + LONG(patch->columnofs[texturecolumn]));
        R_DrawMaskedColumn(patch, patchColumnOfs(patch, texturecolumn))

        dc_x++
        frac += vis.xiscale
    }

    colfunc = basecolfunc
}

//
// R_ProjectSprite
// Generates a vissprite for a thing
//  if it might be visible.
//
internal fun DoomEngineCore.R_ProjectSprite(thing: mobj_t) {
    var tr_x: fixed_t
    var tr_y: fixed_t

    var gxt: fixed_t
    var gyt: fixed_t

    var tx: fixed_t
    var tz: fixed_t

    var xscale: fixed_t

    var x1: Int
    var x2: Int

    val sprdef: spritedef_t
    val sprframe: spriteframe_t

    var lump: Int

    var rot: Int
    var flip: Boolean

    var index: Int

    val vis: vissprite_t

    var ang: angle_t
    var iscale: fixed_t

    // transform the origin point
    tr_x = thing.x - viewx
    tr_y = thing.y - viewy

    gxt = FixedMul(tr_x, viewcos)
    gyt = -FixedMul(tr_y, viewsin)

    tz = gxt - gyt

    // thing is behind view plane?
    if (tz < MINZ)
        return

    xscale = FixedDiv(projection, tz)

    gxt = -FixedMul(tr_x, viewsin)
    gyt = FixedMul(tr_y, viewcos)
    tx = -(gyt + gxt)

    // too far off the side?
    if (abs(tx) > (tz shl 2))
        return

    // decide which patch to use for sprite relative to player
    // #ifdef RANGECHECK
    if (thing.sprite.toUInt() >= numsprites.toUInt())
        I_Error("R_ProjectSprite: invalid sprite number ${thing.sprite} ")
    // #endif
    sprdef = sprites[thing.sprite]
    // #ifdef RANGECHECK
    if ((thing.frame and FF_FRAMEMASK) >= sprdef.numframes)
        I_Error("R_ProjectSprite: invalid sprite frame ${thing.sprite} : ${thing.frame} ")
    // #endif
    sprframe = sprdef.spriteframes[thing.frame and FF_FRAMEMASK]!!

    if (sprframe.rotate != 0) {
        // choose a different rotation based on player view
        ang = R_PointToAngle(thing.x, thing.y)
        rot = ((ang - thing.angle + ANG45 / 2u * 9u) shr 29).toInt()
        lump = sprframe.lump[rot]
        flip = sprframe.flip[rot] != 0
    } else {
        // use single rotation for all views
        lump = sprframe.lump[0]
        flip = sprframe.flip[0] != 0
    }

    // calculate edges of the shape
    tx -= spriteoffset[lump]
    x1 = (centerxfrac + FixedMul(tx, xscale)) shr FRACBITS

    // off the right side?
    if (x1 > viewwidth)
        return

    tx += spritewidth[lump]
    x2 = ((centerxfrac + FixedMul(tx, xscale)) shr FRACBITS) - 1

    // off the left side
    if (x2 < 0)
        return

    // store information in a vissprite
    vis = R_NewVisSprite()
    vis.mobjflags = thing.flags
    vis.scale = xscale shl detailshift
    vis.gx = thing.x
    vis.gy = thing.y
    vis.gz = thing.z
    vis.gzt = thing.z + spritetopoffset[lump]
    vis.texturemid = vis.gzt - viewz
    vis.x1 = if (x1 < 0) 0 else x1
    vis.x2 = if (x2 >= viewwidth) viewwidth - 1 else x2
    iscale = FixedDiv(FRACUNIT, xscale)

    if (flip) {
        vis.startfrac = spritewidth[lump] - 1
        vis.xiscale = -iscale
    } else {
        vis.startfrac = 0
        vis.xiscale = iscale
    }

    if (vis.x1 > x1)
        vis.startfrac += vis.xiscale * (vis.x1 - x1)
    vis.patch = lump

    // get light level
    if ((thing.flags and MF_SHADOW) != 0) {
        // shadow draw
        vis.colormap = -1  // NULL
    } else if (fixedcolormap != -1) {
        // fixed map
        vis.colormap = fixedcolormap
    } else if ((thing.frame and FF_FULLBRIGHT) != 0) {
        // full bright
        vis.colormap = 0  // colormaps
    } else {
        // diminished light
        index = xscale shr (LIGHTSCALESHIFT - detailshift)

        if (index >= MAXLIGHTSCALE)
            index = MAXLIGHTSCALE - 1

        vis.colormap = spritelights[index]
    }
}

//
// R_AddSprites
// During BSP traversal, this adds sprites by sector.
//
internal fun DoomEngineCore.R_AddSprites(sec: sector_t) {
    var thing: mobj_t?
    val lightnum: Int

    // BSP is traversed by subsector.
    // A sector might have been split into several
    //  subsectors during BSP building.
    // Thus we check whether its already added.
    if (sec.validcount == validcount)
        return

    // Well, now it will be done.
    sec.validcount = validcount

    lightnum = (sec.lightlevel shr LIGHTSEGSHIFT) + extralight

    if (lightnum < 0)
        spritelights = scalelight[0]
    else if (lightnum >= LIGHTLEVELS)
        spritelights = scalelight[LIGHTLEVELS - 1]
    else
        spritelights = scalelight[lightnum]

    // Handle all things in sector.
    thing = sec.thinglist
    while (thing != null) {
        R_ProjectSprite(thing)
        thing = thing.snext
    }
}

//
// R_DrawPSprite
//
internal fun DoomEngineCore.R_DrawPSprite(psp: pspdef_t) {
    var tx: fixed_t
    var x1: Int
    var x2: Int
    val sprdef: spritedef_t
    val sprframe: spriteframe_t
    val lump: Int
    val flip: Boolean
    val vis: vissprite_t
    val avis = vissprite_t()

    // decide which patch to use
    // #ifdef RANGECHECK
    if (psp.state!!.sprite.toUInt() >= numsprites.toUInt())
        I_Error("R_ProjectSprite: invalid sprite number ${psp.state!!.sprite} ")
    // #endif
    sprdef = sprites[psp.state!!.sprite]
    // #ifdef RANGECHECK
    if ((psp.state!!.frame and FF_FRAMEMASK) >= sprdef.numframes)
        I_Error("R_ProjectSprite: invalid sprite frame ${psp.state!!.sprite} : ${psp.state!!.frame} ")
    // #endif
    sprframe = sprdef.spriteframes[psp.state!!.frame and FF_FRAMEMASK]!!

    lump = sprframe.lump[0]
    flip = sprframe.flip[0] != 0

    // calculate edges of the shape
    tx = psp.sx - 160 * FRACUNIT

    tx -= spriteoffset[lump]
    x1 = (centerxfrac + FixedMul(tx, pspritescale)) shr FRACBITS

    // off the right side
    if (x1 > viewwidth)
        return

    tx += spritewidth[lump]
    x2 = ((centerxfrac + FixedMul(tx, pspritescale)) shr FRACBITS) - 1

    // off the left side
    if (x2 < 0)
        return

    // store information in a vissprite
    vis = avis
    vis.mobjflags = 0
    vis.texturemid = (BASEYCENTER shl FRACBITS) + FRACUNIT / 2 - (psp.sy - spritetopoffset[lump])
    vis.x1 = if (x1 < 0) 0 else x1
    vis.x2 = if (x2 >= viewwidth) viewwidth - 1 else x2
    vis.scale = pspritescale shl detailshift

    if (flip) {
        vis.xiscale = -pspriteiscale
        vis.startfrac = spritewidth[lump] - 1
    } else {
        vis.xiscale = pspriteiscale
        vis.startfrac = 0
    }

    if (vis.x1 > x1)
        vis.startfrac += vis.xiscale * (vis.x1 - x1)

    vis.patch = lump

    if (viewplayer!!.powers[pw_invisibility] > 4 * 32
        || (viewplayer!!.powers[pw_invisibility] and 8) != 0
    ) {
        // shadow draw
        vis.colormap = -1  // NULL
    } else if (fixedcolormap != -1) {
        // fixed color
        vis.colormap = fixedcolormap
    } else if ((psp.state!!.frame and FF_FULLBRIGHT) != 0) {
        // full bright
        vis.colormap = 0  // colormaps
    } else {
        // local light
        vis.colormap = spritelights[MAXLIGHTSCALE - 1]
    }

    R_DrawVisSprite(vis, vis.x1, vis.x2)
}

//
// R_DrawPlayerSprites
//
internal fun DoomEngineCore.R_DrawPlayerSprites() {
    var i: Int
    val lightnum: Int

    // get light level
    lightnum =
        (viewplayer!!.mo!!.subsector!!.sector!!.lightlevel shr LIGHTSEGSHIFT) + extralight

    if (lightnum < 0)
        spritelights = scalelight[0]
    else if (lightnum >= LIGHTLEVELS)
        spritelights = scalelight[LIGHTLEVELS - 1]
    else
        spritelights = scalelight[lightnum]

    // clip to screen bounds
    mfloorclip = screenheightarray
    mfloorclip_base = 0
    mceilingclip = negonearray
    mceilingclip_base = 0

    // add all active psprites
    i = 0
    while (i < NUMPSPRITES) {
        val psp = viewplayer!!.psprites[i]
        if (psp.state != null)
            R_DrawPSprite(psp)
        i++
    }
}

internal val DoomEngineCore.vsprsortedhead
    get() = stateSpriteRenderer.vsprsortedhead

internal fun DoomEngineCore.R_SortVisSprites() {
    var i: Int
    val count: Int
    var ds: vissprite_t?
    var best: vissprite_t? = null  // C leaves best uninitialized ("shut up the compiler warning")
    val unsorted = vissprite_t()
    var bestscale: fixed_t

    count = vissprite_p  // vissprite_p - vissprites

    unsorted.next = unsorted
    unsorted.prev = unsorted

    if (count == 0)
        return

    // for (ds=vissprites ; ds<vissprite_p ; ds++)
    //     { ds->next = ds+1; ds->prev = ds-1; }
    // (the out-of-range links at both ends are overwritten just below)
    for (j in 0 until count) {
        if (j + 1 < count) vissprites[j].next = vissprites[j + 1]
        if (j > 0) vissprites[j].prev = vissprites[j - 1]
    }

    vissprites[0].prev = unsorted
    unsorted.next = vissprites[0]
    vissprites[count - 1].next = unsorted   // (vissprite_p-1)->next = &unsorted
    unsorted.prev = vissprites[count - 1]

    // pull the vissprites out by scale
    //best = 0;		// shut up the compiler warning
    vsprsortedhead.next = vsprsortedhead
    vsprsortedhead.prev = vsprsortedhead
    i = 0
    while (i < count) {
        bestscale = MAXINT
        ds = unsorted.next
        while (ds !== unsorted) {
            if (ds!!.scale < bestscale) {
                bestscale = ds.scale
                best = ds
            }
            ds = ds.next
        }
        best!!.next!!.prev = best.prev
        best.prev!!.next = best.next
        best.next = vsprsortedhead
        best.prev = vsprsortedhead.prev
        vsprsortedhead.prev!!.next = best
        vsprsortedhead.prev = best
        i++
    }
}

//
// R_DrawSprite
//
internal fun DoomEngineCore.R_DrawSprite(spr: vissprite_t) {
    val clipbot = ShortArray(SCREENWIDTH)
    val cliptop = ShortArray(SCREENWIDTH)
    var x: Int
    var r1: Int
    var r2: Int
    var scale: fixed_t
    var lowscale: fixed_t
    var silhouette: Int

    x = spr.x1
    while (x <= spr.x2) {
        clipbot[x] = -2
        cliptop[x] = -2
        x++
    }

    // Scan drawsegs from end to start for obscuring segs.
    // The first drawseg that has a greater scale
    //  is the clip seg.
    for (dsi in ds_p - 1 downTo 0) {  // for (ds=ds_p-1 ; ds >= drawsegs ; ds--)
        val ds = drawsegs[dsi]

        // determine if the drawseg obscures the sprite
        if (ds.x1 > spr.x2
            || ds.x2 < spr.x1
            || (ds.silhouette == 0
                && ds.maskedtexturecol == -1)
        ) {
            // does not cover sprite
            continue
        }

        r1 = if (ds.x1 < spr.x1) spr.x1 else ds.x1
        r2 = if (ds.x2 > spr.x2) spr.x2 else ds.x2

        if (ds.scale1 > ds.scale2) {
            lowscale = ds.scale2
            scale = ds.scale1
        } else {
            lowscale = ds.scale1
            scale = ds.scale2
        }

        if (scale < spr.scale
            || (lowscale < spr.scale
                && R_PointOnSegSide(spr.gx, spr.gy, ds.curline!!) == 0)
        ) {
            // masked mid texture?
            if (ds.maskedtexturecol != -1)
                R_RenderMaskedSegRange(ds, r1, r2)
            // seg is behind sprite
            continue
        }

        // clip this piece of the sprite
        silhouette = ds.silhouette

        if (spr.gz >= ds.bsilheight)
            silhouette = silhouette and SIL_BOTTOM.inv()

        if (spr.gzt <= ds.tsilheight)
            silhouette = silhouette and SIL_TOP.inv()

        if (silhouette == 1) {
            // bottom sil
            x = r1
            while (x <= r2) {
                if (clipbot[x].toInt() == -2)
                    clipbot[x] = openings[ds.sprbottomclip + x]
                x++
            }
        } else if (silhouette == 2) {
            // top sil
            x = r1
            while (x <= r2) {
                if (cliptop[x].toInt() == -2)
                    cliptop[x] = openings[ds.sprtopclip + x]
                x++
            }
        } else if (silhouette == 3) {
            // both
            x = r1
            while (x <= r2) {
                if (clipbot[x].toInt() == -2)
                    clipbot[x] = openings[ds.sprbottomclip + x]
                if (cliptop[x].toInt() == -2)
                    cliptop[x] = openings[ds.sprtopclip + x]
                x++
            }
        }
    }

    // all clipping has been performed, so draw the sprite

    // check for unclipped columns
    x = spr.x1
    while (x <= spr.x2) {
        if (clipbot[x].toInt() == -2)
            clipbot[x] = viewheight.toShort()

        if (cliptop[x].toInt() == -2)
            cliptop[x] = -1
        x++
    }

    mfloorclip = clipbot
    mfloorclip_base = 0
    mceilingclip = cliptop
    mceilingclip_base = 0
    R_DrawVisSprite(spr, spr.x1, spr.x2)
}

//
// R_DrawMasked
//
internal fun DoomEngineCore.R_DrawMasked() {
    var spr: vissprite_t?

    R_SortVisSprites()

    if (vissprite_p > 0) {  // vissprite_p > vissprites
        // draw all vissprites back to front
        spr = vsprsortedhead.next
        while (spr !== vsprsortedhead) {
            R_DrawSprite(spr!!)
            spr = spr.next
        }
    }

    // render any remaining masked mid textures
    for (dsi in ds_p - 1 downTo 0) {  // for (ds=ds_p-1 ; ds >= drawsegs ; ds--)
        val ds = drawsegs[dsi]
        if (ds.maskedtexturecol != -1)
            R_RenderMaskedSegRange(ds, ds.x1, ds.x2)
    }

    // draw the psprites on top of everything
    //  but does not draw on side views
    if (viewangleoffset == 0)
        R_DrawPlayerSprites()
}
