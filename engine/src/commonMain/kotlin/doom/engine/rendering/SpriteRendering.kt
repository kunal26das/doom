
package doom.engine.rendering

import doom.engine.SCREENWIDTH
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.core.modifiedgame
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.actors.FF_FRAMEMASK
import doom.engine.gameplay.actors.FF_FULLBRIGHT
import doom.engine.gameplay.actors.MF_SHADOW
import doom.engine.gameplay.actors.MF_TRANSLATION
import doom.engine.gameplay.actors.MF_TRANSSHIFT
import doom.engine.gameplay.player.NUMPSPRITES
import doom.engine.gameplay.PW_INVISIBILITY
import doom.engine.gameplay.weapons.WeaponSprite
import doom.engine.geometry.ANG45
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.fixedDiv
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.rendering.resources.SpriteDefinition
import doom.engine.rendering.resources.SpriteFrame
import doom.engine.rendering.resources.firstspritelump
import doom.engine.rendering.resources.lastspritelump
import doom.engine.rendering.resources.spriteoffset
import doom.engine.rendering.resources.spritetopoffset
import doom.engine.rendering.resources.spritewidth
import doom.engine.resources.MAXINT
import doom.engine.resources.wCacheLumpNum
import doom.engine.resources.wGetNumForName
import doom.engine.resources.lumpinfo
import doom.engine.resources.u8
import doom.engine.world.Sector

import kotlin.math.abs

private const val MINZ = FRACUNIT * 4
private const val BASEYCENTER = 100

internal const val MAXVISSPRITES = 128


internal var DoomEngineCore.pspritescale: FixedPoint
    get() = stateSpriteRenderer.pspritescale
    set(value) { stateSpriteRenderer.pspritescale = value }
internal var DoomEngineCore.pspriteiscale: FixedPoint
    get() = stateSpriteRenderer.pspriteiscale
    set(value) { stateSpriteRenderer.pspriteiscale = value }

internal var DoomEngineCore.spritelights: IntArray
    get() = stateSpriteRenderer.spritelights
    set(value) { stateSpriteRenderer.spritelights = value }

internal val DoomEngineCore.negonearray
    get() = stateSpriteRenderer.negonearray
internal val DoomEngineCore.screenheightarray
    get() = stateSpriteRenderer.screenheightarray


internal var DoomEngineCore.sprites: Array<SpriteDefinition>
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

private fun DoomEngineCore.rNameChar(name: String, i: Int): Int =
    if (i < name.length) name[i].code else 0

internal fun DoomEngineCore.rInstallSpriteLump(
    lump: Int,
    frame: Int,
    spriteRotation: Int,
    flipped: Boolean,
) {
    var rotation = spriteRotation
    var r: Int

    if (frame.toUInt() >= 29u || rotation.toUInt() > 8u)
        iError("R_InstallSpriteLump: Bad frame characters in lump $lump")

    if (frame > maxframe)
        maxframe = frame

    if (rotation == 0) {
        if (sprtemp[frame].rotate == 0)
            iError("R_InitSprites: Sprite $spritename frame ${'A' + frame} has " +
                "multip rot=0 lump")

        if (sprtemp[frame].rotate == 1)
            iError("R_InitSprites: Sprite $spritename frame ${'A' + frame} has rotations " +
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

    if (sprtemp[frame].rotate == 0)
        iError("R_InitSprites: Sprite $spritename frame ${'A' + frame} has rotations " +
            "and a rot=0 lump")

    sprtemp[frame].rotate = 1

    rotation--
    if (sprtemp[frame].lump[rotation] != -1)
        iError("R_InitSprites: Sprite $spritename : ${'A' + frame} : ${'1' + rotation} " +
            "has two lumps mapped to it")

    sprtemp[frame].lump[rotation] = lump - firstspritelump
    sprtemp[frame].flip[rotation] = if (flipped) 1 else 0
}

internal fun DoomEngineCore.rInitSpriteDefs(namelist: Array<String>) {
    var frame: Int
    var rotation: Int

    numsprites = namelist.size

    if (numsprites == 0)
        return

    sprites = Array(numsprites) { SpriteDefinition() }

    val start = firstspritelump - 1
    val end = lastspritelump + 1

    for (i in 0 until numsprites) {
        spritename = namelist[i]
        for (f in 0 until 29) {
            sprtemp[f].rotate = -1
            sprtemp[f].lump.fill(-1)
            sprtemp[f].flip.fill(-1)
        }

        maxframe = -1
        val intname = namelist[i]

        for (l in start + 1 until end) {
            val name = lumpinfo[l].name
            if (name.regionMatches(0, intname, 0, 4)) {
                frame = rNameChar(name, 4) - 'A'.code
                rotation = rNameChar(name, 5) - '0'.code

                val patched: Int
                if (modifiedgame)
                    patched = wGetNumForName(name)
                else
                    patched = l

                rInstallSpriteLump(patched, frame, rotation, false)

                if (rNameChar(name, 6) != 0) {
                    frame = rNameChar(name, 6) - 'A'.code
                    rotation = rNameChar(name, 7) - '0'.code
                    rInstallSpriteLump(l, frame, rotation, true)
                }
            }
        }

        if (maxframe == -1) {
            sprites[i].numframes = 0
            continue
        }

        maxframe++

        frame = 0
        while (frame < maxframe) {
            when (sprtemp[frame].rotate) {
                -1 -> {
                    iError("R_InitSprites: No patches found " +
                        "for ${namelist[i]} frame ${'A' + frame}")
                }
                0 -> {
                }
                1 -> {
                    rotation = 0
                    while (rotation < 8) {
                        if (sprtemp[frame].lump[rotation] == -1)
                            iError("R_InitSprites: Sprite ${namelist[i]} frame ${'A' + frame} " +
                                "is missing rotations")
                        rotation++
                    }
                }
            }
            frame++
        }

        sprites[i].numframes = maxframe
        sprites[i].spriteframes = Array<SpriteFrame?>(maxframe) { f ->
            val copy = SpriteFrame()
            copy.rotate = sprtemp[f].rotate
            sprtemp[f].lump.copyInto(copy.lump)
            sprtemp[f].flip.copyInto(copy.flip)
            copy
        }
    }
}

internal val DoomEngineCore.vissprites
    get() = stateSpriteRenderer.vissprites
internal var DoomEngineCore.visspriteP
    get() = stateSpriteRenderer.visspriteP
    set(value) { stateSpriteRenderer.visspriteP = value }
internal var DoomEngineCore.newvissprite
    get() = stateSpriteRenderer.newvissprite
    set(value) { stateSpriteRenderer.newvissprite = value }

internal fun DoomEngineCore.rInitSprites(namelist: Array<String>) {
    for (i in 0 until SCREENWIDTH) {
        negonearray[i] = -1
    }

    rInitSpriteDefs(namelist)
}

internal fun DoomEngineCore.rClearSprites() {
    visspriteP = 0
}

internal val DoomEngineCore.overflowsprite
    get() = stateSpriteRenderer.overflowsprite

internal fun DoomEngineCore.rNewVisSprite(): VisibleSprite {
    if (visspriteP == MAXVISSPRITES)
        return overflowsprite

    visspriteP++
    return vissprites[visspriteP - 1]
}

internal var DoomEngineCore.mfloorclip: ShortArray
    get() = stateSpriteRenderer.mfloorclip
    set(value) { stateSpriteRenderer.mfloorclip = value }
internal var DoomEngineCore.mfloorclipBase
    get() = stateSpriteRenderer.mfloorclipBase
    set(value) { stateSpriteRenderer.mfloorclipBase = value }
internal var DoomEngineCore.mceilingclip: ShortArray
    get() = stateSpriteRenderer.mceilingclip
    set(value) { stateSpriteRenderer.mceilingclip = value }
internal var DoomEngineCore.mceilingclipBase
    get() = stateSpriteRenderer.mceilingclipBase
    set(value) { stateSpriteRenderer.mceilingclipBase = value }

internal var DoomEngineCore.spryscale: FixedPoint
    get() = stateSpriteRenderer.spryscale
    set(value) { stateSpriteRenderer.spryscale = value }
internal var DoomEngineCore.sprtopscreen: FixedPoint
    get() = stateSpriteRenderer.sprtopscreen
    set(value) { stateSpriteRenderer.sprtopscreen = value }

internal fun DoomEngineCore.rDrawMaskedColumn(column: ByteArray, ofs: Int) {
    var columnOfs = ofs
    var topscreen: Int
    var bottomscreen: Int
    val basetexturemid: FixedPoint

    basetexturemid = dcTexturemid

    while (column.u8(columnOfs) != 0xff) {
        topscreen = sprtopscreen + spryscale * column.u8(columnOfs)
        bottomscreen = topscreen + spryscale * column.u8(columnOfs + 1)

        dcYl = (topscreen + FRACUNIT - 1) shr FRACBITS
        dcYh = (bottomscreen - 1) shr FRACBITS

        if (dcYh >= mfloorclip[mfloorclipBase + dcX])
            dcYh = mfloorclip[mfloorclipBase + dcX] - 1
        if (dcYl <= mceilingclip[mceilingclipBase + dcX])
            dcYl = mceilingclip[mceilingclipBase + dcX] + 1

        if (dcYl <= dcYh) {
            dcSource = column
            dcSourceOfs = columnOfs + 3
            dcTexturemid = basetexturemid - (column.u8(columnOfs) shl FRACBITS)

            colfunc()
        }
        columnOfs = columnOfs + column.u8(columnOfs + 1) + 4
    }

    dcTexturemid = basetexturemid
}

internal fun DoomEngineCore.rDrawVisSprite(vis: VisibleSprite) {
    var texturecolumn: Int
    var frac: FixedPoint
    val patch: ByteArray

    patch = wCacheLumpNum(vis.patch + firstspritelump)

    dcColormap = vis.colormap

    if (dcColormap == -1) {
        colfunc = fuzzcolfunc
    } else if ((vis.mobjflags and MF_TRANSLATION) != 0) {
        colfunc = { rDrawTranslatedColumn() }
        dcTranslation = ((vis.mobjflags and MF_TRANSLATION) shr (MF_TRANSSHIFT - 8)) - 256
    }

    dcIscale = abs(vis.xiscale) shr detailshift
    dcTexturemid = vis.texturemid
    frac = vis.startfrac
    spryscale = vis.scale
    sprtopscreen = centeryfrac - fixedMul(dcTexturemid, spryscale)

    dcX = vis.x1
    while (dcX <= vis.x2) {
        texturecolumn = frac shr FRACBITS
        if (texturecolumn < 0 || texturecolumn >= patchWidth(patch))
            iError("R_DrawSpriteRange: bad texturecolumn")
        rDrawMaskedColumn(patch, patchColumnOfs(patch, texturecolumn))

        dcX++
        frac += vis.xiscale
    }

    colfunc = basecolfunc
}

internal fun DoomEngineCore.rProjectSprite(thing: Actor) {
    val trX: FixedPoint
    val trY: FixedPoint

    var gxt: FixedPoint
    var gyt: FixedPoint

    var tx: FixedPoint
    val tz: FixedPoint

    val xscale: FixedPoint

    val x1: Int
    val x2: Int

    val sprdef: SpriteDefinition
    val sprframe: SpriteFrame

    val lump: Int

    val rot: Int
    val flip: Boolean

    var index: Int

    val vis: VisibleSprite

    val ang: BinaryAngle
    val iscale: FixedPoint

    trX = thing.x - viewx
    trY = thing.y - viewy

    gxt = fixedMul(trX, viewcos)
    gyt = -fixedMul(trY, viewsin)

    tz = gxt - gyt

    if (tz < MINZ)
        return

    xscale = fixedDiv(projection, tz)

    gxt = -fixedMul(trX, viewsin)
    gyt = fixedMul(trY, viewcos)
    tx = -(gyt + gxt)

    if (abs(tx) > (tz shl 2))
        return

    if (thing.sprite.toUInt() >= numsprites.toUInt())
        iError("R_ProjectSprite: invalid sprite number ${thing.sprite} ")
    sprdef = sprites[thing.sprite]
    if ((thing.frame and FF_FRAMEMASK) >= sprdef.numframes)
        iError("R_ProjectSprite: invalid sprite frame ${thing.sprite} : ${thing.frame} ")
    sprframe = sprdef.spriteframes[thing.frame and FF_FRAMEMASK]!!

    if (sprframe.rotate != 0) {
        ang = rPointToAngle(thing.x, thing.y)
        rot = ((ang - thing.angle + ANG45 / 2u * 9u) shr 29).toInt()
        lump = sprframe.lump[rot]
        flip = sprframe.flip[rot] != 0
    } else {
        lump = sprframe.lump[0]
        flip = sprframe.flip[0] != 0
    }

    tx -= spriteoffset[lump]
    x1 = (centerxfrac + fixedMul(tx, xscale)) shr FRACBITS

    if (x1 > viewwidth)
        return

    tx += spritewidth[lump]
    x2 = ((centerxfrac + fixedMul(tx, xscale)) shr FRACBITS) - 1

    if (x2 < 0)
        return

    vis = rNewVisSprite()
    vis.mobjflags = thing.flags
    vis.scale = xscale shl detailshift
    vis.gx = thing.x
    vis.gy = thing.y
    vis.gz = thing.z
    vis.gzt = thing.z + spritetopoffset[lump]
    vis.texturemid = vis.gzt - viewz
    vis.x1 = if (x1 < 0) 0 else x1
    vis.x2 = if (x2 >= viewwidth) viewwidth - 1 else x2
    iscale = fixedDiv(FRACUNIT, xscale)

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

    if ((thing.flags and MF_SHADOW) != 0) {
        vis.colormap = -1
    } else if (fixedcolormap != -1) {
        vis.colormap = fixedcolormap
    } else if ((thing.frame and FF_FULLBRIGHT) != 0) {
        vis.colormap = 0
    } else {
        index = xscale shr (LIGHTSCALESHIFT - detailshift)

        if (index >= MAXLIGHTSCALE)
            index = MAXLIGHTSCALE - 1

        vis.colormap = spritelights[index]
    }
}

internal fun DoomEngineCore.rAddSprites(sec: Sector) {
    var thing: Actor?
    val lightnum: Int

    if (sec.validcount == validcount)
        return

    sec.validcount = validcount

    lightnum = (sec.lightlevel shr LIGHTSEGSHIFT) + extralight

    if (lightnum < 0)
        spritelights = scalelight[0]
    else if (lightnum >= LIGHTLEVELS)
        spritelights = scalelight[LIGHTLEVELS - 1]
    else
        spritelights = scalelight[lightnum]

    thing = sec.thinglist
    while (thing != null) {
        rProjectSprite(thing)
        thing = thing.snext
    }
}

internal fun DoomEngineCore.rDrawPSprite(psp: WeaponSprite) {
    var tx: FixedPoint
    val x1: Int
    val x2: Int
    val sprdef: SpriteDefinition
    val sprframe: SpriteFrame
    val lump: Int
    val flip: Boolean
    val vis: VisibleSprite
    val avis = VisibleSprite()

    if (psp.state!!.sprite.toUInt() >= numsprites.toUInt())
        iError("R_ProjectSprite: invalid sprite number ${psp.state!!.sprite} ")
    sprdef = sprites[psp.state!!.sprite]
    if ((psp.state!!.frame and FF_FRAMEMASK) >= sprdef.numframes)
        iError("R_ProjectSprite: invalid sprite frame ${psp.state!!.sprite} : ${psp.state!!.frame} ")
    sprframe = sprdef.spriteframes[psp.state!!.frame and FF_FRAMEMASK]!!

    lump = sprframe.lump[0]
    flip = sprframe.flip[0] != 0

    tx = psp.sx - 160 * FRACUNIT

    tx -= spriteoffset[lump]
    x1 = (centerxfrac + fixedMul(tx, pspritescale)) shr FRACBITS

    if (x1 > viewwidth)
        return

    tx += spritewidth[lump]
    x2 = ((centerxfrac + fixedMul(tx, pspritescale)) shr FRACBITS) - 1

    if (x2 < 0)
        return

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

    if (viewplayer!!.powers[PW_INVISIBILITY] > 4 * 32
        || (viewplayer!!.powers[PW_INVISIBILITY] and 8) != 0
    ) {
        vis.colormap = -1
    } else if (fixedcolormap != -1) {
        vis.colormap = fixedcolormap
    } else if ((psp.state!!.frame and FF_FULLBRIGHT) != 0) {
        vis.colormap = 0
    } else {
        vis.colormap = spritelights[MAXLIGHTSCALE - 1]
    }

    rDrawVisSprite(vis)
}

internal fun DoomEngineCore.rDrawPlayerSprites() {
    var i: Int
    val lightnum: Int

    lightnum =
        (viewplayer!!.mo!!.subsector!!.sector!!.lightlevel shr LIGHTSEGSHIFT) + extralight

    if (lightnum < 0)
        spritelights = scalelight[0]
    else if (lightnum >= LIGHTLEVELS)
        spritelights = scalelight[LIGHTLEVELS - 1]
    else
        spritelights = scalelight[lightnum]

    mfloorclip = screenheightarray
    mfloorclipBase = 0
    mceilingclip = negonearray
    mceilingclipBase = 0

    i = 0
    while (i < NUMPSPRITES) {
        val psp = viewplayer!!.psprites[i]
        if (psp.state != null)
            rDrawPSprite(psp)
        i++
    }
}

internal val DoomEngineCore.vsprsortedhead
    get() = stateSpriteRenderer.vsprsortedhead

internal fun DoomEngineCore.rSortVisSprites() {
    var i: Int
    val count: Int
    var ds: VisibleSprite?
    var best: VisibleSprite? = null
    val unsorted = VisibleSprite()
    var bestscale: FixedPoint

    count = visspriteP

    unsorted.next = unsorted
    unsorted.prev = unsorted

    if (count == 0)
        return

    for (j in 0 until count) {
        if (j + 1 < count) vissprites[j].next = vissprites[j + 1]
        if (j > 0) vissprites[j].prev = vissprites[j - 1]
    }

    vissprites[0].prev = unsorted
    unsorted.next = vissprites[0]
    vissprites[count - 1].next = unsorted
    unsorted.prev = vissprites[count - 1]

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

internal fun DoomEngineCore.rDrawSprite(spr: VisibleSprite) {
    val clipbot = stateSpriteRenderer.spriteBottomClip
    val cliptop = stateSpriteRenderer.spriteTopClip
    clipbot.fill(0)
    cliptop.fill(0)
    var x: Int
    var r1: Int
    var r2: Int
    var scale: FixedPoint
    var lowscale: FixedPoint
    var silhouette: Int

    x = spr.x1
    while (x <= spr.x2) {
        clipbot[x] = -2
        cliptop[x] = -2
        x++
    }

    for (dsi in dsP - 1 downTo 0) {
        val ds = drawsegs[dsi]

        if (ds.x1 > spr.x2
            || ds.x2 < spr.x1
            || (ds.silhouette == 0
                && ds.maskedtexturecol == -1)
        ) {
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
                && rPointOnSegSide(spr.gx, spr.gy, ds.curline!!) == 0)
        ) {
            if (ds.maskedtexturecol != -1)
                rRenderMaskedSegRange(ds, r1, r2)
            continue
        }

        silhouette = ds.silhouette

        if (spr.gz >= ds.bsilheight)
            silhouette = silhouette and SIL_BOTTOM.inv()

        if (spr.gzt <= ds.tsilheight)
            silhouette = silhouette and SIL_TOP.inv()

        if (silhouette == 1) {
            x = r1
            while (x <= r2) {
                if (clipbot[x].toInt() == -2)
                    clipbot[x] = openings[ds.sprbottomclip + x]
                x++
            }
        } else if (silhouette == 2) {
            x = r1
            while (x <= r2) {
                if (cliptop[x].toInt() == -2)
                    cliptop[x] = openings[ds.sprtopclip + x]
                x++
            }
        } else if (silhouette == 3) {
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


    x = spr.x1
    while (x <= spr.x2) {
        if (clipbot[x].toInt() == -2)
            clipbot[x] = viewheight.toShort()

        if (cliptop[x].toInt() == -2)
            cliptop[x] = -1
        x++
    }

    mfloorclip = clipbot
    mfloorclipBase = 0
    mceilingclip = cliptop
    mceilingclipBase = 0
    rDrawVisSprite(spr)
}

internal fun DoomEngineCore.rDrawMasked() {
    var spr: VisibleSprite?

    rSortVisSprites()

    if (visspriteP > 0) {
        spr = vsprsortedhead.next
        while (spr !== vsprsortedhead) {
            rDrawSprite(spr!!)
            spr = spr.next
        }
    }

    for (dsi in dsP - 1 downTo 0) {
        val ds = drawsegs[dsi]
        if (ds.maskedtexturecol != -1)
            rRenderMaskedSegRange(ds, ds.x1, ds.x2)
    }

    if (viewangleoffset == 0)
        rDrawPlayerSprites()
}
