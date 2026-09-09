
package doom.engine.gameplay.weapons

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_BFG
import doom.engine.audio.SFX_DSHTGN
import doom.engine.audio.SFX_PISTOL
import doom.engine.audio.SFX_PUNCH
import doom.engine.audio.SFX_SAWFUL
import doom.engine.audio.SFX_SAWHIT
import doom.engine.audio.SFX_SAWIDL
import doom.engine.audio.SFX_SAWUP
import doom.engine.audio.SFX_SHOTGN
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.actors.MF_JUSTATTACKED
import doom.engine.gameplay.actors.MT_BFG
import doom.engine.gameplay.actors.MT_EXTRABFG
import doom.engine.gameplay.actors.MT_PLASMA
import doom.engine.gameplay.actors.MT_ROCKET
import doom.engine.gameplay.actors.pNoiseAlert
import doom.engine.gameplay.actors.pSetMobjState
import doom.engine.gameplay.actors.pSpawnMobj
import doom.engine.gameplay.actors.pSpawnPlayerMissile
import doom.engine.gameplay.actors.S_CHAIN1
import doom.engine.gameplay.actors.S_NULL
import doom.engine.gameplay.actors.S_PLAY
import doom.engine.gameplay.actors.S_PLAY_ATK1
import doom.engine.gameplay.actors.S_PLAY_ATK2
import doom.engine.gameplay.actors.S_SAW
import doom.engine.gameplay.actors.StateAction
import doom.engine.gameplay.actors.StateDefinition
import doom.engine.gameplay.actors.registerAction
import doom.engine.gameplay.actors.states
import doom.engine.gameplay.AM_CELL
import doom.engine.gameplay.AM_CLIP
import doom.engine.gameplay.AM_MISL
import doom.engine.gameplay.AM_NOAMMO
import doom.engine.gameplay.AM_SHELL
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.interactions.pDamageMobj
import doom.engine.gameplay.player.NUMPSPRITES
import doom.engine.gameplay.player.PST_DEAD
import doom.engine.gameplay.player.Player
import doom.engine.gameplay.player.PS_FLASH
import doom.engine.gameplay.player.PS_WEAPON
import doom.engine.gameplay.PW_STRENGTH
import doom.engine.gameplay.SHAREWARE
import doom.engine.gameplay.WP_BFG
import doom.engine.gameplay.WP_CHAINGUN
import doom.engine.gameplay.WP_CHAINSAW
import doom.engine.gameplay.WP_FIST
import doom.engine.gameplay.WP_MISSILE
import doom.engine.gameplay.WP_NOCHANGE
import doom.engine.gameplay.WP_PISTOL
import doom.engine.gameplay.WP_PLASMA
import doom.engine.gameplay.WP_SHOTGUN
import doom.engine.gameplay.WP_SUPERSHOTGUN
import doom.engine.geometry.ANG180
import doom.engine.geometry.ANG90
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FINEANGLES
import doom.engine.geometry.FINEMASK
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.FixedGeometry
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.input.BT_ATTACK
import doom.engine.simulation.pRandom
import doom.engine.simulation.leveltime
import doom.engine.world.MELEERANGE
import doom.engine.world.MISSILERANGE
import doom.engine.world.collision.pAimLineAttack
import doom.engine.world.collision.pLineAttack
import doom.engine.world.collision.linetarget

internal const val LOWERSPEED = FRACUNIT * 6
internal const val RAISESPEED = FRACUNIT * 6

internal const val WEAPONBOTTOM = 128 * FRACUNIT
internal const val WEAPONTOP = 32 * FRACUNIT

internal const val BFGCELLS = 40

internal fun DoomEngineCore.pSetPsprite(player: Player, position: Int, stateIndex: Int) {
    var stnum = stateIndex
    val psp: WeaponSprite
    var state: StateDefinition

    psp = player.psprites[position]

    do {
        if (stnum == 0) {
            psp.state = null
            break
        }

        state = states[stnum]
        psp.state = state
        psp.tics = state.tics

        if (state.misc1 != 0) {
            psp.sx = state.misc1 shl FRACBITS
            psp.sy = state.misc2 shl FRACBITS
        }

        val acp2 = state.action?.pspFun
        if (acp2 != null) {
            acp2(player, psp)
            if (psp.state == null)
                break
        }

        stnum = psp.state!!.nextstate

    } while (psp.tics == 0)
}

internal var DoomEngineCore.swingx: FixedPoint
    get() = stateWeaponSprite.swingx
    set(value) { stateWeaponSprite.swingx = value }
internal var DoomEngineCore.swingy: FixedPoint
    get() = stateWeaponSprite.swingy
    set(value) { stateWeaponSprite.swingy = value }

internal fun DoomEngineCore.pCalcSwing(player: Player) {
    val swing: FixedPoint
    var angle: Int


    swing = player.bob

    angle = (FINEANGLES / 70 * leveltime) and FINEMASK
    swingx = fixedMul(swing, finesine[angle])

    angle = (FINEANGLES / 70 * leveltime + FINEANGLES / 2) and FINEMASK
    swingy = -fixedMul(swingx, finesine[angle])
}

internal fun DoomEngineCore.pBringUpWeapon(player: Player) {
    val newstate: Int

    if (player.pendingweapon == WP_NOCHANGE)
        player.pendingweapon = player.readyweapon

    if (player.pendingweapon == WP_CHAINSAW)
        sStartSound(player.mo, SFX_SAWUP)

    newstate = weaponinfo[player.pendingweapon].upstate

    player.pendingweapon = WP_NOCHANGE
    player.psprites[PS_WEAPON].sy = WEAPONBOTTOM

    pSetPsprite(player, PS_WEAPON, newstate)
}

internal fun DoomEngineCore.pCheckAmmo(player: Player): Boolean {
    val ammo: Int
    val count: Int

    ammo = weaponinfo[player.readyweapon].ammo

    if (player.readyweapon == WP_BFG)
        count = BFGCELLS
    else if (player.readyweapon == WP_SUPERSHOTGUN)
        count = 2
    else
        count = 1

    if (ammo == AM_NOAMMO || player.ammo[ammo] >= count)
        return true

    do {
        if (player.weaponowned[WP_PLASMA]
            && player.ammo[AM_CELL] != 0
            && (gamemode != SHAREWARE)) {
            player.pendingweapon = WP_PLASMA
        } else if (player.weaponowned[WP_SUPERSHOTGUN]
            && player.ammo[AM_SHELL] > 2
            && (gamemode == COMMERCIAL)) {
            player.pendingweapon = WP_SUPERSHOTGUN
        } else if (player.weaponowned[WP_CHAINGUN]
            && player.ammo[AM_CLIP] != 0) {
            player.pendingweapon = WP_CHAINGUN
        } else if (player.weaponowned[WP_SHOTGUN]
            && player.ammo[AM_SHELL] != 0) {
            player.pendingweapon = WP_SHOTGUN
        } else if (player.ammo[AM_CLIP] != 0) {
            player.pendingweapon = WP_PISTOL
        } else if (player.weaponowned[WP_CHAINSAW]) {
            player.pendingweapon = WP_CHAINSAW
        } else if (player.weaponowned[WP_MISSILE]
            && player.ammo[AM_MISL] != 0) {
            player.pendingweapon = WP_MISSILE
        } else if (player.weaponowned[WP_BFG]
            && player.ammo[AM_CELL] > 40
            && (gamemode != SHAREWARE)) {
            player.pendingweapon = WP_BFG
        } else {
            player.pendingweapon = WP_FIST
        }

    } while (player.pendingweapon == WP_NOCHANGE)

    pSetPsprite(player,
        PS_WEAPON,
        weaponinfo[player.readyweapon].downstate)

    return false
}

internal fun DoomEngineCore.pFireWeapon(player: Player) {
    val newstate: Int

    if (!pCheckAmmo(player))
        return

    pSetMobjState(player.mo!!, S_PLAY_ATK1)
    newstate = weaponinfo[player.readyweapon].atkstate
    pSetPsprite(player, PS_WEAPON, newstate)
    pNoiseAlert(player.mo!!, player.mo!!)
}

internal fun DoomEngineCore.pDropWeapon(player: Player) {
    pSetPsprite(player,
        PS_WEAPON,
        weaponinfo[player.readyweapon].downstate)
}

internal fun DoomEngineCore.aWeaponReady(player: Player, psp: WeaponSprite) {
    val newstate: Int
    var angle: Int

    if (player.mo!!.state === states[S_PLAY_ATK1]
        || player.mo!!.state === states[S_PLAY_ATK2]) {
        pSetMobjState(player.mo!!, S_PLAY)
    }

    if (player.readyweapon == WP_CHAINSAW
        && psp.state === states[S_SAW]) {
        sStartSound(player.mo, SFX_SAWIDL)
    }

    if (player.pendingweapon != WP_NOCHANGE || player.health == 0) {
        newstate = weaponinfo[player.readyweapon].downstate
        pSetPsprite(player, PS_WEAPON, newstate)
        return
    }

    if ((player.cmd.buttons and BT_ATTACK) != 0) {
        if (!player.attackdown
            || (player.readyweapon != WP_MISSILE
                && player.readyweapon != WP_BFG)) {
            player.attackdown = true
            pFireWeapon(player)
            return
        }
    } else
        player.attackdown = false

    angle = (128 * leveltime) and FINEMASK
    psp.sx = FRACUNIT + fixedMul(player.bob, FineCosineTable[angle])
    angle = angle and (FINEANGLES / 2 - 1)
    psp.sy = WEAPONTOP + fixedMul(player.bob, finesine[angle])
}

internal fun DoomEngineCore.aReFire(player: Player) {
    if ((player.cmd.buttons and BT_ATTACK) != 0
        && player.pendingweapon == WP_NOCHANGE
        && player.health != 0) {
        player.refire++
        pFireWeapon(player)
    } else {
        player.refire = 0
        pCheckAmmo(player)
    }
}

internal fun DoomEngineCore.aCheckReload(player: Player) {
    pCheckAmmo(player)
}

internal fun DoomEngineCore.aLower(player: Player, psp: WeaponSprite) {
    psp.sy += LOWERSPEED

    if (psp.sy < WEAPONBOTTOM)
        return

    if (player.playerstate == PST_DEAD) {
        psp.sy = WEAPONBOTTOM

        return
    }

    if (player.health == 0) {
        pSetPsprite(player, PS_WEAPON, S_NULL)
        return
    }

    player.readyweapon = player.pendingweapon

    pBringUpWeapon(player)
}

internal fun DoomEngineCore.aRaise(player: Player, psp: WeaponSprite) {
    val newstate: Int

    psp.sy -= RAISESPEED

    if (psp.sy > WEAPONTOP)
        return

    psp.sy = WEAPONTOP

    newstate = weaponinfo[player.readyweapon].readystate

    pSetPsprite(player, PS_WEAPON, newstate)
}

internal fun DoomEngineCore.aGunFlash(player: Player) {
    pSetMobjState(player.mo!!, S_PLAY_ATK2)
    pSetPsprite(player, PS_FLASH, weaponinfo[player.readyweapon].flashstate)
}


internal fun DoomEngineCore.aPunch(player: Player) {
    var angle: BinaryAngle
    var damage: Int
    val slope: Int

    damage = (pRandom() % 10 + 1) shl 1

    if (player.powers[PW_STRENGTH] != 0)
        damage *= 10

    angle = player.mo!!.angle
    angle += ((pRandom() - pRandom()) shl 18).toUInt()
    slope = pAimLineAttack(player.mo!!, angle, MELEERANGE)
    pLineAttack(player.mo!!, angle, MELEERANGE, slope, damage)

    if (linetarget != null) {
        sStartSound(player.mo, SFX_PUNCH)
        player.mo!!.angle = FixedGeometry.angleBetween(player.mo!!.x,
            player.mo!!.y,
            linetarget!!.x,
            linetarget!!.y)
    }
}

internal fun DoomEngineCore.aSaw(player: Player) {
    var angle: BinaryAngle
    val damage: Int
    val slope: Int

    damage = 2 * (pRandom() % 10 + 1)
    angle = player.mo!!.angle
    angle += ((pRandom() - pRandom()) shl 18).toUInt()

    slope = pAimLineAttack(player.mo!!, angle, MELEERANGE + 1)
    pLineAttack(player.mo!!, angle, MELEERANGE + 1, slope, damage)

    if (linetarget == null) {
        sStartSound(player.mo, SFX_SAWFUL)
        return
    }
    sStartSound(player.mo, SFX_SAWHIT)

    angle = FixedGeometry.angleBetween(player.mo!!.x, player.mo!!.y,
        linetarget!!.x, linetarget!!.y)
    if (angle - player.mo!!.angle > ANG180) {
        if ((angle - player.mo!!.angle).toInt() < -(ANG90.toInt()) / 20)
            player.mo!!.angle = angle + ANG90 / 21u
        else
            player.mo!!.angle -= ANG90 / 20u
    } else {
        if (angle - player.mo!!.angle > ANG90 / 20u)
            player.mo!!.angle = angle - ANG90 / 21u
        else
            player.mo!!.angle += ANG90 / 20u
    }
    player.mo!!.flags = player.mo!!.flags or MF_JUSTATTACKED
}

internal fun DoomEngineCore.aFireMissile(player: Player) {
    player.ammo[weaponinfo[player.readyweapon].ammo]--
    pSpawnPlayerMissile(player.mo!!, MT_ROCKET)
}

internal fun DoomEngineCore.aFireBFG(player: Player) {
    player.ammo[weaponinfo[player.readyweapon].ammo] -= BFGCELLS
    pSpawnPlayerMissile(player.mo!!, MT_BFG)
}

internal fun DoomEngineCore.aFirePlasma(player: Player) {
    player.ammo[weaponinfo[player.readyweapon].ammo]--

    pSetPsprite(player,
        PS_FLASH,
        weaponinfo[player.readyweapon].flashstate + (pRandom() and 1))

    pSpawnPlayerMissile(player.mo!!, MT_PLASMA)
}

internal var DoomEngineCore.bulletslope: FixedPoint
    get() = stateWeaponSprite.bulletslope
    set(value) { stateWeaponSprite.bulletslope = value }

internal fun DoomEngineCore.pBulletSlope(mo: Actor) {
    var an: BinaryAngle

    an = mo.angle
    bulletslope = pAimLineAttack(mo, an, 16 * 64 * FRACUNIT)

    if (linetarget == null) {
        an += (1 shl 26).toUInt()
        bulletslope = pAimLineAttack(mo, an, 16 * 64 * FRACUNIT)
        if (linetarget == null) {
            an -= (2 shl 26).toUInt()
            bulletslope = pAimLineAttack(mo, an, 16 * 64 * FRACUNIT)
        }
    }
}

internal fun DoomEngineCore.pGunShot(mo: Actor, accurate: Boolean) {
    var angle: BinaryAngle
    val damage: Int

    damage = 5 * (pRandom() % 3 + 1)
    angle = mo.angle

    if (!accurate)
        angle += ((pRandom() - pRandom()) shl 18).toUInt()

    pLineAttack(mo, angle, MISSILERANGE, bulletslope, damage)
}

internal fun DoomEngineCore.aFirePistol(player: Player) {
    sStartSound(player.mo, SFX_PISTOL)

    pSetMobjState(player.mo!!, S_PLAY_ATK2)
    player.ammo[weaponinfo[player.readyweapon].ammo]--

    pSetPsprite(player,
        PS_FLASH,
        weaponinfo[player.readyweapon].flashstate)

    pBulletSlope(player.mo!!)
    pGunShot(player.mo!!, player.refire == 0)
}

internal fun DoomEngineCore.aFireShotgun(player: Player) {
    var i: Int

    sStartSound(player.mo, SFX_SHOTGN)
    pSetMobjState(player.mo!!, S_PLAY_ATK2)

    player.ammo[weaponinfo[player.readyweapon].ammo]--

    pSetPsprite(player,
        PS_FLASH,
        weaponinfo[player.readyweapon].flashstate)

    pBulletSlope(player.mo!!)

    i = 0
    while (i < 7) {
        pGunShot(player.mo!!, false)
        i++
    }
}

internal fun DoomEngineCore.aFireShotgun2(player: Player) {
    var i: Int
    var angle: BinaryAngle
    var damage: Int

    sStartSound(player.mo, SFX_DSHTGN)
    pSetMobjState(player.mo!!, S_PLAY_ATK2)

    player.ammo[weaponinfo[player.readyweapon].ammo] -= 2

    pSetPsprite(player,
        PS_FLASH,
        weaponinfo[player.readyweapon].flashstate)

    pBulletSlope(player.mo!!)

    i = 0
    while (i < 20) {
        damage = 5 * (pRandom() % 3 + 1)
        angle = player.mo!!.angle
        angle += ((pRandom() - pRandom()) shl 19).toUInt()
        pLineAttack(player.mo!!,
            angle,
            MISSILERANGE,
            bulletslope + ((pRandom() - pRandom()) shl 5), damage)
        i++
    }
}

internal fun DoomEngineCore.aFireCGun(player: Player, psp: WeaponSprite) {
    sStartSound(player.mo, SFX_PISTOL)

    if (player.ammo[weaponinfo[player.readyweapon].ammo] == 0)
        return

    pSetMobjState(player.mo!!, S_PLAY_ATK2)
    player.ammo[weaponinfo[player.readyweapon].ammo]--

    pSetPsprite(player,
        PS_FLASH,
        weaponinfo[player.readyweapon].flashstate
            + psp.state!!.index
            - S_CHAIN1)

    pBulletSlope(player.mo!!)

    pGunShot(player.mo!!, player.refire == 0)
}

internal fun DoomEngineCore.aLight0(player: Player) {
    player.extralight = 0
}

internal fun DoomEngineCore.aLight1(player: Player) {
    player.extralight = 1
}

internal fun DoomEngineCore.aLight2(player: Player) {
    player.extralight = 2
}

internal fun DoomEngineCore.aBFGSpray(mo: Actor) {
    var i: Int
    var j: Int
    var damage: Int
    var an: BinaryAngle

    i = 0
    while (i < 40) {
        an = mo.angle - ANG90 / 2u + ANG90 / 40u * i.toUInt()

        pAimLineAttack(mo.target!!, an, 16 * 64 * FRACUNIT)

        if (linetarget == null) {
            i++
            continue
        }

        pSpawnMobj(linetarget!!.x,
            linetarget!!.y,
            linetarget!!.z + (linetarget!!.height shr 2),
            MT_EXTRABFG)

        damage = 0
        j = 0
        while (j < 15) {
            damage += (pRandom() and 7) + 1
            j++
        }

        pDamageMobj(linetarget!!, mo.target, mo.target, damage)
        i++
    }
}

internal fun DoomEngineCore.aBFGsound(player: Player) {
    sStartSound(player.mo, SFX_BFG)
}

internal fun DoomEngineCore.pSetupPsprites(player: Player) {
    var i: Int

    i = 0
    while (i < NUMPSPRITES) {
        player.psprites[i].state = null
        i++
    }

    player.pendingweapon = player.readyweapon
    pBringUpWeapon(player)
}

internal fun DoomEngineCore.pMovePsprites(player: Player) {
    var i: Int
    var psp: WeaponSprite
    var state: StateDefinition?

    i = 0
    while (i < NUMPSPRITES) {
        psp = player.psprites[i]
        state = psp.state
        if (state != null) {

            if (psp.tics != -1) {
                psp.tics--
                if (psp.tics == 0)
                    pSetPsprite(player, i, psp.state!!.nextstate)
            }
        }
        i++
    }

    player.psprites[PS_FLASH].sx = player.psprites[PS_WEAPON].sx
    player.psprites[PS_FLASH].sy = player.psprites[PS_WEAPON].sy
}

internal fun DoomEngineCore.pRegisterPsprActions() {
    registerAction(StateAction("A_WeaponReady", pspFun = { player, psp -> aWeaponReady(player, psp) }))
    registerAction(StateAction("A_ReFire", pspFun = { player, _ -> aReFire(player) }))
    registerAction(StateAction("A_CheckReload", pspFun = { player, _ -> aCheckReload(player) }))
    registerAction(StateAction("A_Lower", pspFun = { player, psp -> aLower(player, psp) }))
    registerAction(StateAction("A_Raise", pspFun = { player, psp -> aRaise(player, psp) }))
    registerAction(StateAction("A_GunFlash", pspFun = { player, _ -> aGunFlash(player) }))
    registerAction(StateAction("A_Punch", pspFun = { player, _ -> aPunch(player) }))
    registerAction(StateAction("A_Saw", pspFun = { player, _ -> aSaw(player) }))
    registerAction(StateAction("A_FireMissile", pspFun = { player, _ -> aFireMissile(player) }))
    registerAction(StateAction("A_FireBFG", pspFun = { player, _ -> aFireBFG(player) }))
    registerAction(StateAction("A_FirePlasma", pspFun = { player, _ -> aFirePlasma(player) }))
    registerAction(StateAction("A_FirePistol", pspFun = { player, _ -> aFirePistol(player) }))
    registerAction(StateAction("A_FireShotgun", pspFun = { player, _ -> aFireShotgun(player) }))
    registerAction(StateAction("A_FireShotgun2", pspFun = { player, _ -> aFireShotgun2(player) }))
    registerAction(StateAction("A_FireCGun", pspFun = { player, psp -> aFireCGun(player, psp) }))
    registerAction(StateAction("A_Light0", pspFun = { player, _ -> aLight0(player) }))
    registerAction(StateAction("A_Light1", pspFun = { player, _ -> aLight1(player) }))
    registerAction(StateAction("A_Light2", pspFun = { player, _ -> aLight2(player) }))
    registerAction(StateAction("A_BFGSpray", mobjFun = { aBFGSpray(it) }))
    registerAction(StateAction("A_BFGsound", pspFun = { player, _ -> aBFGsound(player) }))
}
