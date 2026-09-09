
package doom.engine.savegame

import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.NUMAMMO
import doom.engine.gameplay.NUMCARDS
import doom.engine.gameplay.NUMPOWERS
import doom.engine.gameplay.NUMWEAPONS
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.actors.pMobjThinker
import doom.engine.gameplay.actors.pRemoveMobj
import doom.engine.gameplay.actors.mobjinfo
import doom.engine.gameplay.actors.states
import doom.engine.gameplay.player.NUMPSPRITES
import doom.engine.gameplay.player.Player
import doom.engine.gameplay.playeringame
import doom.engine.gameplay.players
import doom.engine.gameplay.savebuffer
import doom.engine.gameplay.weapons.WeaponSprite
import doom.engine.geometry.FRACBITS
import doom.engine.input.TicCommand
import doom.engine.resources.u8
import doom.engine.simulation.pAddThinker
import doom.engine.simulation.pInitThinkers
import doom.engine.simulation.Thinker
import doom.engine.simulation.thinkercap
import doom.engine.world.MapSide
import doom.engine.world.MapThingSpawn
import doom.engine.world.collision.pSetThingPosition
import doom.engine.world.lighting.GlowingLight
import doom.engine.world.lighting.LightFlash
import doom.engine.world.lighting.StrobeLight
import doom.engine.world.lighting.tGlow
import doom.engine.world.lighting.tLightFlash
import doom.engine.world.lighting.tStrobeFlash
import doom.engine.world.lines
import doom.engine.world.movers.CeilingMover
import doom.engine.world.movers.FloorMover
import doom.engine.world.movers.MAXCEILINGS
import doom.engine.world.movers.pAddActiveCeiling
import doom.engine.world.movers.pAddActivePlat
import doom.engine.world.movers.PlatformMover
import doom.engine.world.movers.tMoveCeiling
import doom.engine.world.movers.tMoveFloor
import doom.engine.world.movers.tPlatRaise
import doom.engine.world.movers.tVerticalDoor
import doom.engine.world.movers.VerticalDoor
import doom.engine.world.movers.activeceilings
import doom.engine.world.movers.activeplats
import doom.engine.world.numlines
import doom.engine.world.numsectors
import doom.engine.world.sectors
import doom.engine.world.sides

internal var DoomEngineCore.saveP
    get() = statePSaveg.saveP
    set(value) { statePSaveg.saveP = value }

internal fun DoomEngineCore.padsavep() {
}

private val DoomEngineCore.savegRestoredAction: (Thinker) -> Unit
    get() = statePSaveg.savegRestoredAction


private fun DoomEngineCore.savegRead8(): Int {
    val result = savebuffer.u8(saveP)
    saveP++
    return result
}

private fun DoomEngineCore.savegWrite8(value: Int) {
    savebuffer[saveP] = value.toByte()
    saveP++
}

private fun DoomEngineCore.savegRead16(): Int {
    var result: Int

    result = savegRead8()
    result = result or (savegRead8() shl 8)

    return result.toShort().toInt()
}

private fun DoomEngineCore.savegWrite16(value: Int) {
    savegWrite8(value and 0xff)
    savegWrite8((value shr 8) and 0xff)
}

private fun DoomEngineCore.savegRead32(): Int {
    var result: Int

    result = savegRead8()
    result = result or (savegRead8() shl 8)
    result = result or (savegRead8() shl 16)
    result = result or (savegRead8() shl 24)

    return result
}

private fun DoomEngineCore.savegWrite32(value: Int) {
    savegWrite8(value and 0xff)
    savegWrite8((value shr 8) and 0xff)
    savegWrite8((value shr 16) and 0xff)
    savegWrite8((value shr 24) and 0xff)
}


private fun DoomEngineCore.savegReadp(): Int {
    return savegRead32()
}

private fun DoomEngineCore.savegWritep(p: Any?) {
    savegWrite32(if (p != null) 1 else 0)
}


private fun DoomEngineCore.savegReadEnum(): Int = savegRead32()

private fun DoomEngineCore.savegWriteEnum(value: Int) = savegWrite32(value)



private fun DoomEngineCore.savegReadMapthingT(str: MapThingSpawn) {
    str.x = savegRead16()

    str.y = savegRead16()

    str.angle = savegRead16()

    str.type = savegRead16()

    str.options = savegRead16()
}

private fun DoomEngineCore.savegWriteMapthingT(str: MapThingSpawn?) {
    savegWrite16(str?.x ?: 0)

    savegWrite16(str?.y ?: 0)

    savegWrite16(str?.angle ?: 0)

    savegWrite16(str?.type ?: 0)

    savegWrite16(str?.options ?: 0)
}


private fun DoomEngineCore.savegReadActionfT(str: Thinker) {
    str.function = if (savegReadp() != 0) savegRestoredAction else null
}

private fun DoomEngineCore.savegWriteActionfT(str: Thinker) {
    savegWritep(str.function)
}


private fun DoomEngineCore.savegReadThinkT(str: Thinker) = savegReadActionfT(str)

private fun DoomEngineCore.savegWriteThinkT(str: Thinker) = savegWriteActionfT(str)


private fun DoomEngineCore.savegReadThinkerT(str: Thinker) {
    savegReadp()

    savegReadp()

    savegReadThinkT(str)
}

private fun DoomEngineCore.savegWriteThinkerT(str: Thinker) {
    savegWritep(str.prev)

    savegWritep(str.next)

    savegWriteThinkT(str)
}


private fun DoomEngineCore.savegReadMobjT(str: Actor) {
    val pl: Int

    savegReadThinkerT(str)

    str.x = savegRead32()

    str.y = savegRead32()

    str.z = savegRead32()

    savegReadp()

    savegReadp()

    str.angle = savegRead32().toUInt()

    str.sprite = savegReadEnum()

    str.frame = savegRead32()

    savegReadp()

    savegReadp()

    savegReadp()

    str.floorz = savegRead32()

    str.ceilingz = savegRead32()

    str.radius = savegRead32()

    str.height = savegRead32()

    str.momx = savegRead32()

    str.momy = savegRead32()

    str.momz = savegRead32()

    str.validcount = savegRead32()

    str.type = savegReadEnum()

    savegReadp()

    str.tics = savegRead32()

    str.state = states[savegRead32()]

    str.flags = savegRead32()

    str.health = savegRead32()

    str.movedir = savegRead32()

    str.movecount = savegRead32()

    savegReadp()

    str.reactiontime = savegRead32()

    str.threshold = savegRead32()

    pl = savegRead32()

    if (pl > 0) {
        str.player = players[pl - 1]
        str.player!!.mo = str
    } else {
        str.player = null
    }

    str.lastlook = savegRead32()

    str.spawnpoint = MapThingSpawn()
    savegReadMapthingT(str.spawnpoint!!)

    savegReadp()
}

private fun DoomEngineCore.savegWriteMobjT(str: Actor) {
    savegWriteThinkerT(str)

    savegWrite32(str.x)

    savegWrite32(str.y)

    savegWrite32(str.z)

    savegWritep(str.snext)

    savegWritep(str.sprev)

    savegWrite32(str.angle.toInt())

    savegWriteEnum(str.sprite)

    savegWrite32(str.frame)

    savegWritep(str.bnext)

    savegWritep(str.bprev)

    savegWritep(str.subsector)

    savegWrite32(str.floorz)

    savegWrite32(str.ceilingz)

    savegWrite32(str.radius)

    savegWrite32(str.height)

    savegWrite32(str.momx)

    savegWrite32(str.momy)

    savegWrite32(str.momz)

    savegWrite32(str.validcount)

    savegWriteEnum(str.type)

    savegWritep(str.info)

    savegWrite32(str.tics)

    savegWrite32(str.state!!.index)

    savegWrite32(str.flags)

    savegWrite32(str.health)

    savegWrite32(str.movedir)

    savegWrite32(str.movecount)

    savegWritep(str.target)

    savegWrite32(str.reactiontime)

    savegWrite32(str.threshold)

    if (str.player != null) {
        savegWrite32(players.indexOf(str.player!!) + 1)
    } else {
        savegWrite32(0)
    }

    savegWrite32(str.lastlook)

    savegWriteMapthingT(str.spawnpoint)

    savegWritep(str.tracer)
}


private fun DoomEngineCore.savegReadTiccmdT(str: TicCommand) {
    str.forwardmove = savegRead8().toByte().toInt()

    str.sidemove = savegRead8().toByte().toInt()

    str.angleturn = savegRead16()

    str.consistancy = savegRead16()

    str.chatchar = savegRead8()

    str.buttons = savegRead8()
}

private fun DoomEngineCore.savegWriteTiccmdT(str: TicCommand) {
    savegWrite8(str.forwardmove)

    savegWrite8(str.sidemove)

    savegWrite16(str.angleturn)

    savegWrite16(str.consistancy)

    savegWrite8(str.chatchar)

    savegWrite8(str.buttons)
}


private fun DoomEngineCore.savegReadPspdefT(str: WeaponSprite) {
    val state: Int

    state = savegRead32()

    if (state > 0) {
        str.state = states[state]
    } else {
        str.state = null
    }

    str.tics = savegRead32()

    str.sx = savegRead32()

    str.sy = savegRead32()
}

private fun DoomEngineCore.savegWritePspdefT(str: WeaponSprite) {
    if (str.state != null) {
        savegWrite32(str.state!!.index)
    } else {
        savegWrite32(0)
    }

    savegWrite32(str.tics)

    savegWrite32(str.sx)

    savegWrite32(str.sy)
}


private fun DoomEngineCore.savegReadPlayerT(str: Player) {
    var i: Int

    savegReadp()
    str.mo = null

    str.playerstate = savegReadEnum()

    savegReadTiccmdT(str.cmd)

    str.viewz = savegRead32()

    str.viewheight = savegRead32()

    str.deltaviewheight = savegRead32()

    str.bob = savegRead32()

    str.health = savegRead32()

    str.armorpoints = savegRead32()

    str.armortype = savegRead32()

    i = 0
    while (i < NUMPOWERS) {
        str.powers[i] = savegRead32()
        i++
    }

    i = 0
    while (i < NUMCARDS) {
        str.cards[i] = savegRead32() != 0
        i++
    }

    str.backpack = savegRead32() != 0

    i = 0
    while (i < MAXPLAYERS) {
        str.frags[i] = savegRead32()
        i++
    }

    str.readyweapon = savegReadEnum()

    str.pendingweapon = savegReadEnum()

    i = 0
    while (i < NUMWEAPONS) {
        str.weaponowned[i] = savegRead32() != 0
        i++
    }

    i = 0
    while (i < NUMAMMO) {
        str.ammo[i] = savegRead32()
        i++
    }

    i = 0
    while (i < NUMAMMO) {
        str.maxammo[i] = savegRead32()
        i++
    }

    str.attackdown = savegRead32() != 0

    str.usedown = savegRead32() != 0

    str.cheats = savegRead32()

    str.refire = savegRead32()

    str.killcount = savegRead32()

    str.itemcount = savegRead32()

    str.secretcount = savegRead32()

    savegReadp()
    str.message = null

    str.damagecount = savegRead32()

    str.bonuscount = savegRead32()

    savegReadp()
    str.attacker = null

    str.extralight = savegRead32()

    str.fixedcolormap = savegRead32()

    str.colormap = savegRead32()

    i = 0
    while (i < NUMPSPRITES) {
        savegReadPspdefT(str.psprites[i])
        i++
    }

    str.didsecret = savegRead32() != 0
}

private fun DoomEngineCore.savegWritePlayerT(str: Player) {
    var i: Int

    savegWritep(str.mo)

    savegWriteEnum(str.playerstate)

    savegWriteTiccmdT(str.cmd)

    savegWrite32(str.viewz)

    savegWrite32(str.viewheight)

    savegWrite32(str.deltaviewheight)

    savegWrite32(str.bob)

    savegWrite32(str.health)

    savegWrite32(str.armorpoints)

    savegWrite32(str.armortype)

    i = 0
    while (i < NUMPOWERS) {
        savegWrite32(str.powers[i])
        i++
    }

    i = 0
    while (i < NUMCARDS) {
        savegWrite32(if (str.cards[i]) 1 else 0)
        i++
    }

    savegWrite32(if (str.backpack) 1 else 0)

    i = 0
    while (i < MAXPLAYERS) {
        savegWrite32(str.frags[i])
        i++
    }

    savegWriteEnum(str.readyweapon)

    savegWriteEnum(str.pendingweapon)

    i = 0
    while (i < NUMWEAPONS) {
        savegWrite32(if (str.weaponowned[i]) 1 else 0)
        i++
    }

    i = 0
    while (i < NUMAMMO) {
        savegWrite32(str.ammo[i])
        i++
    }

    i = 0
    while (i < NUMAMMO) {
        savegWrite32(str.maxammo[i])
        i++
    }

    savegWrite32(if (str.attackdown) 1 else 0)

    savegWrite32(if (str.usedown) 1 else 0)

    savegWrite32(str.cheats)

    savegWrite32(str.refire)

    savegWrite32(str.killcount)

    savegWrite32(str.itemcount)

    savegWrite32(str.secretcount)

    savegWritep(str.message)

    savegWrite32(str.damagecount)

    savegWrite32(str.bonuscount)

    savegWritep(str.attacker)

    savegWrite32(str.extralight)

    savegWrite32(str.fixedcolormap)

    savegWrite32(str.colormap)

    i = 0
    while (i < NUMPSPRITES) {
        savegWritePspdefT(str.psprites[i])
        i++
    }

    savegWrite32(if (str.didsecret) 1 else 0)
}


private fun DoomEngineCore.savegReadCeilingT(str: CeilingMover) {
    val sector: Int

    savegReadThinkerT(str)

    str.type = savegReadEnum()

    sector = savegRead32()
    str.sector = sectors[sector]

    str.bottomheight = savegRead32()

    str.topheight = savegRead32()

    str.speed = savegRead32()

    str.crush = savegRead32() != 0

    str.direction = savegRead32()

    str.tag = savegRead32()

    str.olddirection = savegRead32()
}

private fun DoomEngineCore.savegWriteCeilingT(str: CeilingMover) {
    savegWriteThinkerT(str)

    savegWriteEnum(str.type)

    savegWrite32(str.sector!!.index)

    savegWrite32(str.bottomheight)

    savegWrite32(str.topheight)

    savegWrite32(str.speed)

    savegWrite32(if (str.crush) 1 else 0)

    savegWrite32(str.direction)

    savegWrite32(str.tag)

    savegWrite32(str.olddirection)
}


private fun DoomEngineCore.savegReadVldoorT(str: VerticalDoor) {
    val sector: Int

    savegReadThinkerT(str)

    str.type = savegReadEnum()

    sector = savegRead32()
    str.sector = sectors[sector]

    str.topheight = savegRead32()

    str.speed = savegRead32()

    str.direction = savegRead32()

    str.topwait = savegRead32()

    str.topcountdown = savegRead32()
}

private fun DoomEngineCore.savegWriteVldoorT(str: VerticalDoor) {
    savegWriteThinkerT(str)

    savegWriteEnum(str.type)

    savegWrite32(str.sector!!.index)

    savegWrite32(str.topheight)

    savegWrite32(str.speed)

    savegWrite32(str.direction)

    savegWrite32(str.topwait)

    savegWrite32(str.topcountdown)
}


private fun DoomEngineCore.savegReadFloormoveT(str: FloorMover) {
    val sector: Int

    savegReadThinkerT(str)

    str.type = savegReadEnum()

    str.crush = savegRead32() != 0

    sector = savegRead32()
    str.sector = sectors[sector]

    str.direction = savegRead32()

    str.newspecial = savegRead32()

    str.texture = savegRead16()

    str.floordestheight = savegRead32()

    str.speed = savegRead32()
}

private fun DoomEngineCore.savegWriteFloormoveT(str: FloorMover) {
    savegWriteThinkerT(str)

    savegWriteEnum(str.type)

    savegWrite32(if (str.crush) 1 else 0)

    savegWrite32(str.sector!!.index)

    savegWrite32(str.direction)

    savegWrite32(str.newspecial)

    savegWrite16(str.texture)

    savegWrite32(str.floordestheight)

    savegWrite32(str.speed)
}


private fun DoomEngineCore.savegReadPlatT(str: PlatformMover) {
    val sector: Int

    savegReadThinkerT(str)

    sector = savegRead32()
    str.sector = sectors[sector]

    str.speed = savegRead32()

    str.low = savegRead32()

    str.high = savegRead32()

    str.wait = savegRead32()

    str.count = savegRead32()

    str.status = savegReadEnum()

    str.oldstatus = savegReadEnum()

    str.crush = savegRead32() != 0

    str.tag = savegRead32()

    str.type = savegReadEnum()
}

private fun DoomEngineCore.savegWritePlatT(str: PlatformMover) {
    savegWriteThinkerT(str)

    savegWrite32(str.sector!!.index)

    savegWrite32(str.speed)

    savegWrite32(str.low)

    savegWrite32(str.high)

    savegWrite32(str.wait)

    savegWrite32(str.count)

    savegWriteEnum(str.status)

    savegWriteEnum(str.oldstatus)

    savegWrite32(if (str.crush) 1 else 0)

    savegWrite32(str.tag)

    savegWriteEnum(str.type)
}


private fun DoomEngineCore.savegReadLightflashT(str: LightFlash) {
    val sector: Int

    savegReadThinkerT(str)

    sector = savegRead32()
    str.sector = sectors[sector]

    str.count = savegRead32()

    str.maxlight = savegRead32()

    str.minlight = savegRead32()

    str.maxtime = savegRead32()

    str.mintime = savegRead32()
}

private fun DoomEngineCore.savegWriteLightflashT(str: LightFlash) {
    savegWriteThinkerT(str)

    savegWrite32(str.sector!!.index)

    savegWrite32(str.count)

    savegWrite32(str.maxlight)

    savegWrite32(str.minlight)

    savegWrite32(str.maxtime)

    savegWrite32(str.mintime)
}


private fun DoomEngineCore.savegReadStrobeT(str: StrobeLight) {
    val sector: Int

    savegReadThinkerT(str)

    sector = savegRead32()
    str.sector = sectors[sector]

    str.count = savegRead32()

    str.minlight = savegRead32()

    str.maxlight = savegRead32()

    str.darktime = savegRead32()

    str.brighttime = savegRead32()
}

private fun DoomEngineCore.savegWriteStrobeT(str: StrobeLight) {
    savegWriteThinkerT(str)

    savegWrite32(str.sector!!.index)

    savegWrite32(str.count)

    savegWrite32(str.minlight)

    savegWrite32(str.maxlight)

    savegWrite32(str.darktime)

    savegWrite32(str.brighttime)
}


private fun DoomEngineCore.savegReadGlowT(str: GlowingLight) {
    val sector: Int

    savegReadThinkerT(str)

    sector = savegRead32()
    str.sector = sectors[sector]

    str.minlight = savegRead32()

    str.maxlight = savegRead32()

    str.direction = savegRead32()
}

private fun DoomEngineCore.savegWriteGlowT(str: GlowingLight) {
    savegWriteThinkerT(str)

    savegWrite32(str.sector!!.index)

    savegWrite32(str.minlight)

    savegWrite32(str.maxlight)

    savegWrite32(str.direction)
}

internal fun DoomEngineCore.pArchivePlayers() {
    var i: Int

    i = 0
    while (i < MAXPLAYERS) {
        if (!playeringame[i]) {
            i++
            continue
        }

        padsavep()

        savegWritePlayerT(players[i])
        i++
    }
}

internal fun DoomEngineCore.pUnArchivePlayers() {
    var i: Int

    i = 0
    while (i < MAXPLAYERS) {
        if (!playeringame[i]) {
            i++
            continue
        }

        padsavep()

        savegReadPlayerT(players[i])

        players[i].mo = null
        players[i].message = null
        players[i].attacker = null
        i++
    }
}

internal fun DoomEngineCore.pArchiveWorld() {
    var i: Int
    var j: Int
    var si: MapSide

    i = 0
    while (i < numsectors) {
        val sec = sectors[i]
        savegWrite16(sec.floorheight shr FRACBITS)
        savegWrite16(sec.ceilingheight shr FRACBITS)
        savegWrite16(sec.floorpic)
        savegWrite16(sec.ceilingpic)
        savegWrite16(sec.lightlevel)
        savegWrite16(sec.special)
        savegWrite16(sec.tag)
        i++
    }

    i = 0
    while (i < numlines) {
        val li = lines[i]
        savegWrite16(li.flags)
        savegWrite16(li.special)
        savegWrite16(li.tag)
        j = 0
        while (j < 2) {
            if (li.sidenum[j] == -1) {
                j++
                continue
            }

            si = sides[li.sidenum[j]]

            savegWrite16(si.textureoffset shr FRACBITS)
            savegWrite16(si.rowoffset shr FRACBITS)
            savegWrite16(si.toptexture)
            savegWrite16(si.bottomtexture)
            savegWrite16(si.midtexture)
            j++
        }
        i++
    }
}

internal fun DoomEngineCore.pUnArchiveWorld() {
    var i: Int
    var j: Int
    var si: MapSide

    i = 0
    while (i < numsectors) {
        val sec = sectors[i]
        sec.floorheight = savegRead16() shl FRACBITS
        sec.ceilingheight = savegRead16() shl FRACBITS
        sec.floorpic = savegRead16()
        sec.ceilingpic = savegRead16()
        sec.lightlevel = savegRead16()
        sec.special = savegRead16()
        sec.tag = savegRead16()
        sec.specialdata = null
        sec.soundtarget = null
        i++
    }

    i = 0
    while (i < numlines) {
        val li = lines[i]
        li.flags = savegRead16()
        li.special = savegRead16()
        li.tag = savegRead16()
        j = 0
        while (j < 2) {
            if (li.sidenum[j] == -1) {
                j++
                continue
            }
            si = sides[li.sidenum[j]]
            si.textureoffset = savegRead16() shl FRACBITS
            si.rowoffset = savegRead16() shl FRACBITS
            si.toptexture = savegRead16()
            si.bottomtexture = savegRead16()
            si.midtexture = savegRead16()
            j++
        }
        i++
    }
}

private const val TC_END = 0
private const val TC_MOBJ = 1

internal fun DoomEngineCore.pArchiveThinkers() {
    var th: Thinker

    th = thinkercap.next!!
    while (th !== thinkercap) {
        if (th is Actor && !th.removed) {
            savegWrite8(TC_MOBJ)
            padsavep()
            savegWriteMobjT(th)

            th = th.next!!
            continue
        }

        th = th.next!!
    }

    savegWrite8(TC_END)
}

internal fun DoomEngineCore.pUnArchiveThinkers() {
    var tclass: Int
    var currentthinker: Thinker
    var next: Thinker
    var mobj: Actor

    currentthinker = thinkercap.next!!
    while (currentthinker !== thinkercap) {
        next = currentthinker.next!!

        if (currentthinker is Actor && !currentthinker.removed)
            pRemoveMobj(currentthinker)

        currentthinker = next
    }
    pInitThinkers()

    while (true) {
        tclass = savegRead8()
        when (tclass) {
            TC_END ->
                return

            TC_MOBJ -> {
                padsavep()
                mobj = Actor()
                savegReadMobjT(mobj)

                mobj.target = null
                mobj.tracer = null
                pSetThingPosition(mobj)
                mobj.info = mobjinfo[mobj.type]
                mobj.floorz = mobj.subsector!!.sector!!.floorheight
                mobj.ceilingz = mobj.subsector!!.sector!!.ceilingheight
                mobj.function = { th -> pMobjThinker(th as Actor) }
                pAddThinker(mobj)
            }

            else ->
                iError("Unknown tclass $tclass in savegame")
        }
    }
}

private const val TC_CEILING = 0
private const val TC_DOOR = 1
private const val TC_FLOOR = 2
private const val TC_PLAT = 3
private const val TC_FLASH = 4
private const val TC_STROBE = 5
private const val TC_GLOW = 6
private const val TC_ENDSPECIALS = 7

internal fun DoomEngineCore.pArchiveSpecials() {
    var th: Thinker
    var i: Int

    th = thinkercap.next!!
    while (th !== thinkercap) {
        if (th.removed) {
            th = th.next!!
            continue
        }

        if (th.function == null) {
            i = 0
            while (i < MAXCEILINGS) {
                if (activeceilings[i] === th)
                    break
                i++
            }

            if (i < MAXCEILINGS) {
                savegWrite8(TC_CEILING)
                padsavep()
                savegWriteCeilingT(th as CeilingMover)
            } else if (th is PlatformMover && activeplats.contains(th)) {
                savegWrite8(TC_PLAT)
                padsavep()
                savegWritePlatT(th)
            }
            th = th.next!!
            continue
        }

        if (th is CeilingMover) {
            savegWrite8(TC_CEILING)
            padsavep()
            savegWriteCeilingT(th)
            th = th.next!!
            continue
        }

        if (th is VerticalDoor) {
            savegWrite8(TC_DOOR)
            padsavep()
            savegWriteVldoorT(th)
            th = th.next!!
            continue
        }

        if (th is FloorMover) {
            savegWrite8(TC_FLOOR)
            padsavep()
            savegWriteFloormoveT(th)
            th = th.next!!
            continue
        }

        if (th is PlatformMover) {
            savegWrite8(TC_PLAT)
            padsavep()
            savegWritePlatT(th)
            th = th.next!!
            continue
        }

        if (th is LightFlash) {
            savegWrite8(TC_FLASH)
            padsavep()
            savegWriteLightflashT(th)
            th = th.next!!
            continue
        }

        if (th is StrobeLight) {
            savegWrite8(TC_STROBE)
            padsavep()
            savegWriteStrobeT(th)
            th = th.next!!
            continue
        }

        if (th is GlowingLight) {
            savegWrite8(TC_GLOW)
            padsavep()
            savegWriteGlowT(th)
            th = th.next!!
            continue
        }

        th = th.next!!
    }

    savegWrite8(TC_ENDSPECIALS)
}

internal fun DoomEngineCore.pUnArchiveSpecials() {
    var tclass: Int
    var ceiling: CeilingMover
    var door: VerticalDoor
    var floor: FloorMover
    var plat: PlatformMover
    var flash: LightFlash
    var strobe: StrobeLight
    var glow: GlowingLight

    while (true) {
        tclass = savegRead8()

        when (tclass) {
            TC_ENDSPECIALS ->
                return

            TC_CEILING -> {
                padsavep()
                ceiling = CeilingMover()
                savegReadCeilingT(ceiling)
                ceiling.sector!!.specialdata = ceiling

                if (ceiling.function != null)
                    ceiling.function = { th -> tMoveCeiling(th as CeilingMover) }

                pAddThinker(ceiling)
                pAddActiveCeiling(ceiling)
            }

            TC_DOOR -> {
                padsavep()
                door = VerticalDoor()
                savegReadVldoorT(door)
                door.sector!!.specialdata = door
                door.function = { th -> tVerticalDoor(th as VerticalDoor) }
                pAddThinker(door)
            }

            TC_FLOOR -> {
                padsavep()
                floor = FloorMover()
                savegReadFloormoveT(floor)
                floor.sector!!.specialdata = floor
                floor.function = { th -> tMoveFloor(th as FloorMover) }
                pAddThinker(floor)
            }

            TC_PLAT -> {
                padsavep()
                plat = PlatformMover()
                savegReadPlatT(plat)
                plat.sector!!.specialdata = plat

                if (plat.function != null)
                    plat.function = { th -> tPlatRaise(th as PlatformMover) }

                pAddThinker(plat)
                pAddActivePlat(plat)
            }

            TC_FLASH -> {
                padsavep()
                flash = LightFlash()
                savegReadLightflashT(flash)
                flash.function = { th -> tLightFlash(th as LightFlash) }
                pAddThinker(flash)
            }

            TC_STROBE -> {
                padsavep()
                strobe = StrobeLight()
                savegReadStrobeT(strobe)
                strobe.function = { th -> tStrobeFlash(th as StrobeLight) }
                pAddThinker(strobe)
            }

            TC_GLOW -> {
                padsavep()
                glow = GlowingLight()
                savegReadGlowT(glow)
                glow.function = { th -> tGlow(th as GlowingLight) }
                pAddThinker(glow)
            }

            else ->
                iError("P_UnarchiveSpecials:Unknown tclass $tclass in savegame")
        }
    }
}
