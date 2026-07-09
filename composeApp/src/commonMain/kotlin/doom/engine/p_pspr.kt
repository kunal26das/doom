// Port of linuxdoom-1.10 p_pspr.c -- weapon sprite animation, weapon objects.
// Action functions for weapons.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

const val LOWERSPEED = FRACUNIT * 6
const val RAISESPEED = FRACUNIT * 6

const val WEAPONBOTTOM = 128 * FRACUNIT
const val WEAPONTOP = 32 * FRACUNIT

// plasma cells for a bfg attack
const val BFGCELLS = 40

//
// P_SetPsprite
//
fun P_SetPsprite(player: player_t, position: Int, stnum: Int) {
    var stnum = stnum
    val psp: pspdef_t
    var state: state_t

    psp = player.psprites[position]

    do {
        if (stnum == 0) {
            // object removed itself
            psp.state = null
            break
        }

        state = states[stnum]
        psp.state = state
        psp.tics = state.tics  // could be 0

        if (state.misc1 != 0) {
            // coordinate set
            psp.sx = state.misc1 shl FRACBITS
            psp.sy = state.misc2 shl FRACBITS
        }

        // Call action routine.
        // Modified handling.
        val acp2 = state.action?.pspFun
        if (acp2 != null) {
            acp2(player, psp)
            if (psp.state == null)
                break
        }

        stnum = psp.state!!.nextstate

    } while (psp.tics == 0)
    // an initial state of 0 could cycle through
}

//
// P_CalcSwing
//
var swingx: fixed_t = 0
var swingy: fixed_t = 0

fun P_CalcSwing(player: player_t) {
    val swing: fixed_t
    var angle: Int

    // OPTIMIZE: tablify this.
    // A LUT would allow for different modes,
    //  and add flexibility.

    swing = player.bob

    angle = (FINEANGLES / 70 * leveltime) and FINEMASK
    swingx = FixedMul(swing, finesine[angle])

    angle = (FINEANGLES / 70 * leveltime + FINEANGLES / 2) and FINEMASK
    swingy = -FixedMul(swingx, finesine[angle])
}

//
// P_BringUpWeapon
// Starts bringing the pending weapon up
// from the bottom of the screen.
// Uses player
//
fun P_BringUpWeapon(player: player_t) {
    val newstate: Int

    if (player.pendingweapon == wp_nochange)
        player.pendingweapon = player.readyweapon

    if (player.pendingweapon == wp_chainsaw)
        S_StartSound(player.mo, sfx_sawup)

    newstate = weaponinfo[player.pendingweapon].upstate

    player.pendingweapon = wp_nochange
    player.psprites[ps_weapon].sy = WEAPONBOTTOM

    P_SetPsprite(player, ps_weapon, newstate)
}

//
// P_CheckAmmo
// Returns true if there is enough ammo to shoot.
// If not, selects the next weapon to use.
//
fun P_CheckAmmo(player: player_t): Boolean {
    val ammo: Int  // ammotype_t
    val count: Int

    ammo = weaponinfo[player.readyweapon].ammo

    // Minimal amount for one shot varies.
    if (player.readyweapon == wp_bfg)
        count = BFGCELLS
    else if (player.readyweapon == wp_supershotgun)
        count = 2  // Double barrel.
    else
        count = 1  // Regular.

    // Some do not need ammunition anyway.
    // Return if current ammunition sufficient.
    if (ammo == am_noammo || player.ammo[ammo] >= count)
        return true

    // Out of ammo, pick a weapon to change to.
    // Preferences are set here.
    do {
        if (player.weaponowned[wp_plasma]
            && player.ammo[am_cell] != 0
            && (gamemode != shareware)) {
            player.pendingweapon = wp_plasma
        } else if (player.weaponowned[wp_supershotgun]
            && player.ammo[am_shell] > 2
            && (gamemode == commercial)) {
            player.pendingweapon = wp_supershotgun
        } else if (player.weaponowned[wp_chaingun]
            && player.ammo[am_clip] != 0) {
            player.pendingweapon = wp_chaingun
        } else if (player.weaponowned[wp_shotgun]
            && player.ammo[am_shell] != 0) {
            player.pendingweapon = wp_shotgun
        } else if (player.ammo[am_clip] != 0) {
            player.pendingweapon = wp_pistol
        } else if (player.weaponowned[wp_chainsaw]) {
            player.pendingweapon = wp_chainsaw
        } else if (player.weaponowned[wp_missile]
            && player.ammo[am_misl] != 0) {
            player.pendingweapon = wp_missile
        } else if (player.weaponowned[wp_bfg]
            && player.ammo[am_cell] > 40
            && (gamemode != shareware)) {
            player.pendingweapon = wp_bfg
        } else {
            // If everything fails.
            player.pendingweapon = wp_fist
        }

    } while (player.pendingweapon == wp_nochange)

    // Now set appropriate weapon overlay.
    P_SetPsprite(player,
        ps_weapon,
        weaponinfo[player.readyweapon].downstate)

    return false
}

//
// P_FireWeapon.
//
fun P_FireWeapon(player: player_t) {
    val newstate: Int

    if (!P_CheckAmmo(player))
        return

    P_SetMobjState(player.mo!!, S_PLAY_ATK1)
    newstate = weaponinfo[player.readyweapon].atkstate
    P_SetPsprite(player, ps_weapon, newstate)
    P_NoiseAlert(player.mo!!, player.mo!!)
}

//
// P_DropWeapon
// Player died, so put the weapon away.
//
fun P_DropWeapon(player: player_t) {
    P_SetPsprite(player,
        ps_weapon,
        weaponinfo[player.readyweapon].downstate)
}

//
// A_WeaponReady
// The player can fire the weapon
// or change to another weapon at this time.
// Follows after getting weapon up,
// or after previous attack/fire sequence.
//
fun A_WeaponReady(player: player_t, psp: pspdef_t) {
    val newstate: Int
    var angle: Int

    // get out of attack state
    if (player.mo!!.state === states[S_PLAY_ATK1]
        || player.mo!!.state === states[S_PLAY_ATK2]) {
        P_SetMobjState(player.mo!!, S_PLAY)
    }

    if (player.readyweapon == wp_chainsaw
        && psp.state === states[S_SAW]) {
        S_StartSound(player.mo, sfx_sawidl)
    }

    // check for change
    //  if player is dead, put the weapon away
    if (player.pendingweapon != wp_nochange || player.health == 0) {
        // change weapon
        //  (pending weapon should allready be validated)
        newstate = weaponinfo[player.readyweapon].downstate
        P_SetPsprite(player, ps_weapon, newstate)
        return
    }

    // check for fire
    //  the missile launcher and bfg do not auto fire
    if ((player.cmd.buttons and BT_ATTACK) != 0) {
        if (!player.attackdown
            || (player.readyweapon != wp_missile
                && player.readyweapon != wp_bfg)) {
            player.attackdown = true
            P_FireWeapon(player)
            return
        }
    } else
        player.attackdown = false

    // bob the weapon based on movement speed
    angle = (128 * leveltime) and FINEMASK
    psp.sx = FRACUNIT + FixedMul(player.bob, finecosine[angle])
    angle = angle and (FINEANGLES / 2 - 1)
    psp.sy = WEAPONTOP + FixedMul(player.bob, finesine[angle])
}

//
// A_ReFire
// The player can re-fire the weapon
// without lowering it entirely.
//
fun A_ReFire(player: player_t, psp: pspdef_t) {
    // check for fire
    //  (if a weaponchange is pending, let it go through instead)
    if ((player.cmd.buttons and BT_ATTACK) != 0
        && player.pendingweapon == wp_nochange
        && player.health != 0) {
        player.refire++
        P_FireWeapon(player)
    } else {
        player.refire = 0
        P_CheckAmmo(player)
    }
}

fun A_CheckReload(player: player_t, psp: pspdef_t) {
    P_CheckAmmo(player)
    // (C: #if 0
    //  if (player->ammo[am_shell]<2)
    //      P_SetPsprite (player, ps_weapon, S_DSNR1);
    //  #endif)
}

//
// A_Lower
// Lowers current weapon,
//  and changes weapon at bottom.
//
fun A_Lower(player: player_t, psp: pspdef_t) {
    psp.sy += LOWERSPEED

    // Is already down.
    if (psp.sy < WEAPONBOTTOM)
        return

    // Player is dead.
    if (player.playerstate == PST_DEAD) {
        psp.sy = WEAPONBOTTOM

        // don't bring weapon back up
        return
    }

    // The old weapon has been lowered off the screen,
    // so change the weapon and start raising it
    if (player.health == 0) {
        // Player is dead, so keep the weapon off screen.
        P_SetPsprite(player, ps_weapon, S_NULL)
        return
    }

    player.readyweapon = player.pendingweapon

    P_BringUpWeapon(player)
}

//
// A_Raise
//
fun A_Raise(player: player_t, psp: pspdef_t) {
    val newstate: Int

    psp.sy -= RAISESPEED

    if (psp.sy > WEAPONTOP)
        return

    psp.sy = WEAPONTOP

    // The weapon has been raised all the way,
    //  so change to the ready state.
    newstate = weaponinfo[player.readyweapon].readystate

    P_SetPsprite(player, ps_weapon, newstate)
}

//
// A_GunFlash
//
fun A_GunFlash(player: player_t, psp: pspdef_t) {
    P_SetMobjState(player.mo!!, S_PLAY_ATK2)
    P_SetPsprite(player, ps_flash, weaponinfo[player.readyweapon].flashstate)
}

//
// WEAPON ATTACKS
//

//
// A_Punch
//
fun A_Punch(player: player_t, psp: pspdef_t) {
    var angle: angle_t
    var damage: Int
    val slope: Int

    damage = (P_Random() % 10 + 1) shl 1

    if (player.powers[pw_strength] != 0)
        damage *= 10

    angle = player.mo!!.angle
    angle += ((P_Random() - P_Random()) shl 18).toUInt()
    slope = P_AimLineAttack(player.mo!!, angle, MELEERANGE)
    P_LineAttack(player.mo!!, angle, MELEERANGE, slope, damage)

    // turn to face target
    if (linetarget != null) {
        S_StartSound(player.mo, sfx_punch)
        player.mo!!.angle = R_PointToAngle2(player.mo!!.x,
            player.mo!!.y,
            linetarget!!.x,
            linetarget!!.y)
    }
}

//
// A_Saw
//
fun A_Saw(player: player_t, psp: pspdef_t) {
    var angle: angle_t
    val damage: Int
    val slope: Int

    damage = 2 * (P_Random() % 10 + 1)
    angle = player.mo!!.angle
    angle += ((P_Random() - P_Random()) shl 18).toUInt()

    // use meleerange + 1 se the puff doesn't skip the flash
    slope = P_AimLineAttack(player.mo!!, angle, MELEERANGE + 1)
    P_LineAttack(player.mo!!, angle, MELEERANGE + 1, slope, damage)

    if (linetarget == null) {
        S_StartSound(player.mo, sfx_sawful)
        return
    }
    S_StartSound(player.mo, sfx_sawhit)

    // turn to face target
    angle = R_PointToAngle2(player.mo!!.x, player.mo!!.y,
        linetarget!!.x, linetarget!!.y)
    if (angle - player.mo!!.angle > ANG180) {
        // (C: angle - player->mo->angle < -ANG90/20 -- the int constant is
        //  promoted to unsigned; within this branch that is exactly the
        //  signed comparison below, per chocolate-doom.)
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

//
// A_FireMissile
//
fun A_FireMissile(player: player_t, psp: pspdef_t) {
    player.ammo[weaponinfo[player.readyweapon].ammo]--
    P_SpawnPlayerMissile(player.mo!!, MT_ROCKET)
}

//
// A_FireBFG
//
fun A_FireBFG(player: player_t, psp: pspdef_t) {
    player.ammo[weaponinfo[player.readyweapon].ammo] -= BFGCELLS
    P_SpawnPlayerMissile(player.mo!!, MT_BFG)
}

//
// A_FirePlasma
//
fun A_FirePlasma(player: player_t, psp: pspdef_t) {
    player.ammo[weaponinfo[player.readyweapon].ammo]--

    P_SetPsprite(player,
        ps_flash,
        weaponinfo[player.readyweapon].flashstate + (P_Random() and 1))

    P_SpawnPlayerMissile(player.mo!!, MT_PLASMA)
}

//
// P_BulletSlope
// Sets a slope so a near miss is at aproximately
// the height of the intended target
//
var bulletslope: fixed_t = 0

fun P_BulletSlope(mo: mobj_t) {
    var an: angle_t

    // see which target is to be aimed at
    an = mo.angle
    bulletslope = P_AimLineAttack(mo, an, 16 * 64 * FRACUNIT)

    if (linetarget == null) {
        an += (1 shl 26).toUInt()
        bulletslope = P_AimLineAttack(mo, an, 16 * 64 * FRACUNIT)
        if (linetarget == null) {
            an -= (2 shl 26).toUInt()
            bulletslope = P_AimLineAttack(mo, an, 16 * 64 * FRACUNIT)
        }
    }
}

//
// P_GunShot
//
fun P_GunShot(mo: mobj_t, accurate: Boolean) {
    var angle: angle_t
    val damage: Int

    damage = 5 * (P_Random() % 3 + 1)
    angle = mo.angle

    if (!accurate)
        angle += ((P_Random() - P_Random()) shl 18).toUInt()

    P_LineAttack(mo, angle, MISSILERANGE, bulletslope, damage)
}

//
// A_FirePistol
//
fun A_FirePistol(player: player_t, psp: pspdef_t) {
    S_StartSound(player.mo, sfx_pistol)

    P_SetMobjState(player.mo!!, S_PLAY_ATK2)
    player.ammo[weaponinfo[player.readyweapon].ammo]--

    P_SetPsprite(player,
        ps_flash,
        weaponinfo[player.readyweapon].flashstate)

    P_BulletSlope(player.mo!!)
    P_GunShot(player.mo!!, player.refire == 0)
}

//
// A_FireShotgun
//
fun A_FireShotgun(player: player_t, psp: pspdef_t) {
    var i: Int

    S_StartSound(player.mo, sfx_shotgn)
    P_SetMobjState(player.mo!!, S_PLAY_ATK2)

    player.ammo[weaponinfo[player.readyweapon].ammo]--

    P_SetPsprite(player,
        ps_flash,
        weaponinfo[player.readyweapon].flashstate)

    P_BulletSlope(player.mo!!)

    i = 0
    while (i < 7) {
        P_GunShot(player.mo!!, false)
        i++
    }
}

//
// A_FireShotgun2
//
fun A_FireShotgun2(player: player_t, psp: pspdef_t) {
    var i: Int
    var angle: angle_t
    var damage: Int

    S_StartSound(player.mo, sfx_dshtgn)
    P_SetMobjState(player.mo!!, S_PLAY_ATK2)

    player.ammo[weaponinfo[player.readyweapon].ammo] -= 2

    P_SetPsprite(player,
        ps_flash,
        weaponinfo[player.readyweapon].flashstate)

    P_BulletSlope(player.mo!!)

    i = 0
    while (i < 20) {
        damage = 5 * (P_Random() % 3 + 1)
        angle = player.mo!!.angle
        angle += ((P_Random() - P_Random()) shl 19).toUInt()
        P_LineAttack(player.mo!!,
            angle,
            MISSILERANGE,
            bulletslope + ((P_Random() - P_Random()) shl 5), damage)
        i++
    }
}

//
// A_FireCGun
//
fun A_FireCGun(player: player_t, psp: pspdef_t) {
    S_StartSound(player.mo, sfx_pistol)

    if (player.ammo[weaponinfo[player.readyweapon].ammo] == 0)
        return

    P_SetMobjState(player.mo!!, S_PLAY_ATK2)
    player.ammo[weaponinfo[player.readyweapon].ammo]--

    // (C: flashstate + psp->state - &states[S_CHAIN1])
    P_SetPsprite(player,
        ps_flash,
        weaponinfo[player.readyweapon].flashstate
            + psp.state!!.index
            - S_CHAIN1)

    P_BulletSlope(player.mo!!)

    P_GunShot(player.mo!!, player.refire == 0)
}

//
// ?
//
fun A_Light0(player: player_t, psp: pspdef_t) {
    player.extralight = 0
}

fun A_Light1(player: player_t, psp: pspdef_t) {
    player.extralight = 1
}

fun A_Light2(player: player_t, psp: pspdef_t) {
    player.extralight = 2
}

//
// A_BFGSpray
// Spawn a BFG explosion on every monster in view
//
fun A_BFGSpray(mo: mobj_t) {
    var i: Int
    var j: Int
    var damage: Int
    var an: angle_t

    // offset angles from its attack angle
    i = 0
    while (i < 40) {
        an = mo.angle - ANG90 / 2u + ANG90 / 40u * i.toUInt()

        // mo->target is the originator (player)
        //  of the missile
        P_AimLineAttack(mo.target!!, an, 16 * 64 * FRACUNIT)

        if (linetarget == null) {
            i++
            continue
        }

        P_SpawnMobj(linetarget!!.x,
            linetarget!!.y,
            linetarget!!.z + (linetarget!!.height shr 2),
            MT_EXTRABFG)

        damage = 0
        j = 0
        while (j < 15) {
            damage += (P_Random() and 7) + 1
            j++
        }

        P_DamageMobj(linetarget!!, mo.target, mo.target, damage)
        i++
    }
}

//
// A_BFGsound
//
fun A_BFGsound(player: player_t, psp: pspdef_t) {
    S_StartSound(player.mo, sfx_bfg)
}

//
// P_SetupPsprites
// Called at start of level for each player.
//
fun P_SetupPsprites(player: player_t) {
    var i: Int

    // remove all psprites
    i = 0
    while (i < NUMPSPRITES) {
        player.psprites[i].state = null
        i++
    }

    // spawn the gun
    player.pendingweapon = player.readyweapon
    P_BringUpWeapon(player)
}

//
// P_MovePsprites
// Called every tic by player thinking routine.
//
fun P_MovePsprites(player: player_t) {
    var i: Int
    var psp: pspdef_t
    var state: state_t?

    i = 0
    while (i < NUMPSPRITES) {
        psp = player.psprites[i]
        // a null state means not active
        state = psp.state
        if (state != null) {
            // drop tic count and possibly change state

            // a -1 tic count never changes
            if (psp.tics != -1) {
                psp.tics--
                if (psp.tics == 0)
                    P_SetPsprite(player, i, psp.state!!.nextstate)
            }
        }
        i++
    }

    player.psprites[ps_flash].sx = player.psprites[ps_weapon].sx
    player.psprites[ps_flash].sy = player.psprites[ps_weapon].sy
}

//
// Register every action function this file defines in the actionMap
// (called by D_DoomMain before InfoResolveActions).
//
fun P_RegisterPsprActions() {
    registerAction(ActionF("A_WeaponReady", pspFun = { player, psp -> A_WeaponReady(player, psp) }))
    registerAction(ActionF("A_ReFire", pspFun = { player, psp -> A_ReFire(player, psp) }))
    registerAction(ActionF("A_CheckReload", pspFun = { player, psp -> A_CheckReload(player, psp) }))
    registerAction(ActionF("A_Lower", pspFun = { player, psp -> A_Lower(player, psp) }))
    registerAction(ActionF("A_Raise", pspFun = { player, psp -> A_Raise(player, psp) }))
    registerAction(ActionF("A_GunFlash", pspFun = { player, psp -> A_GunFlash(player, psp) }))
    registerAction(ActionF("A_Punch", pspFun = { player, psp -> A_Punch(player, psp) }))
    registerAction(ActionF("A_Saw", pspFun = { player, psp -> A_Saw(player, psp) }))
    registerAction(ActionF("A_FireMissile", pspFun = { player, psp -> A_FireMissile(player, psp) }))
    registerAction(ActionF("A_FireBFG", pspFun = { player, psp -> A_FireBFG(player, psp) }))
    registerAction(ActionF("A_FirePlasma", pspFun = { player, psp -> A_FirePlasma(player, psp) }))
    registerAction(ActionF("A_FirePistol", pspFun = { player, psp -> A_FirePistol(player, psp) }))
    registerAction(ActionF("A_FireShotgun", pspFun = { player, psp -> A_FireShotgun(player, psp) }))
    registerAction(ActionF("A_FireShotgun2", pspFun = { player, psp -> A_FireShotgun2(player, psp) }))
    registerAction(ActionF("A_FireCGun", pspFun = { player, psp -> A_FireCGun(player, psp) }))
    registerAction(ActionF("A_Light0", pspFun = { player, psp -> A_Light0(player, psp) }))
    registerAction(ActionF("A_Light1", pspFun = { player, psp -> A_Light1(player, psp) }))
    registerAction(ActionF("A_Light2", pspFun = { player, psp -> A_Light2(player, psp) }))
    registerAction(ActionF("A_BFGSpray", mobjFun = { A_BFGSpray(it) }))
    registerAction(ActionF("A_BFGsound", pspFun = { player, psp -> A_BFGsound(player, psp) }))
}
