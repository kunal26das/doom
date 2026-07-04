// Port of linuxdoom-1.10 p_user.c -- player related stuff.
// Bobbing POV/weapon, movement. Pending weapon.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// Index of the special effects (INVUL inverse) map.
const val INVERSECOLORMAP = 32

//
// Movement.
//

// 16 pixels of bob
const val MAXBOB = 0x100000

var onground = false

//
// P_Thrust
// Moves the given origin along a given angle.
//
fun P_Thrust(player: player_t, angle: angle_t, move: fixed_t) {
    val angle = (angle shr ANGLETOFINESHIFT).toInt()

    player.mo!!.momx += FixedMul(move, finecosine[angle])
    player.mo!!.momy += FixedMul(move, finesine[angle])
}

//
// P_CalcHeight
// Calculate the walking / running height adjustment
//
fun P_CalcHeight(player: player_t) {
    val angle: Int
    val bob: fixed_t

    // Regular movement bobbing
    // (needs to be calculated for gun swing
    // even if not on ground)
    // OPTIMIZE: tablify angle
    // Note: a LUT allows for effects
    //  like a ramp with low health.
    player.bob =
        FixedMul(player.mo!!.momx, player.mo!!.momx) +
        FixedMul(player.mo!!.momy, player.mo!!.momy)

    player.bob = player.bob shr 2

    if (player.bob > MAXBOB)
        player.bob = MAXBOB

    if ((player.cheats and CF_NOMOMENTUM) != 0 || !onground) {
        player.viewz = player.mo!!.z + VIEWHEIGHT

        if (player.viewz > player.mo!!.ceilingz - 4 * FRACUNIT)
            player.viewz = player.mo!!.ceilingz - 4 * FRACUNIT

        player.viewz = player.mo!!.z + player.viewheight
        return
    }

    angle = (FINEANGLES / 20 * leveltime) and FINEMASK
    bob = FixedMul(player.bob / 2, finesine[angle])

    // move viewheight
    if (player.playerstate == PST_LIVE) {
        player.viewheight += player.deltaviewheight

        if (player.viewheight > VIEWHEIGHT) {
            player.viewheight = VIEWHEIGHT
            player.deltaviewheight = 0
        }

        if (player.viewheight < VIEWHEIGHT / 2) {
            player.viewheight = VIEWHEIGHT / 2
            if (player.deltaviewheight <= 0)
                player.deltaviewheight = 1
        }

        if (player.deltaviewheight != 0) {
            player.deltaviewheight += FRACUNIT / 4
            if (player.deltaviewheight == 0)
                player.deltaviewheight = 1
        }
    }
    player.viewz = player.mo!!.z + player.viewheight + bob

    if (player.viewz > player.mo!!.ceilingz - 4 * FRACUNIT)
        player.viewz = player.mo!!.ceilingz - 4 * FRACUNIT
}

//
// P_MovePlayer
//
fun P_MovePlayer(player: player_t) {
    val cmd: ticcmd_t

    cmd = player.cmd

    player.mo!!.angle += (cmd.angleturn shl 16).toUInt()

    // Do not let the player control movement
    //  if not onground.
    onground = (player.mo!!.z <= player.mo!!.floorz)

    if (cmd.forwardmove != 0 && onground)
        P_Thrust(player, player.mo!!.angle, cmd.forwardmove * 2048)

    if (cmd.sidemove != 0 && onground)
        P_Thrust(player, player.mo!!.angle - ANG90, cmd.sidemove * 2048)

    if ((cmd.forwardmove != 0 || cmd.sidemove != 0)
        && player.mo!!.state === states[S_PLAY]) {
        P_SetMobjState(player.mo!!, S_PLAY_RUN1)
    }
}

//
// P_DeathThink
// Fall on your face when dying.
// Decrease POV height to floor height.
//
val ANG5: angle_t = ANG90 / 18u

fun P_DeathThink(player: player_t) {
    P_MovePsprites(player)

    // fall to the ground
    if (player.viewheight > 6 * FRACUNIT)
        player.viewheight -= FRACUNIT

    if (player.viewheight < 6 * FRACUNIT)
        player.viewheight = 6 * FRACUNIT

    player.deltaviewheight = 0
    onground = (player.mo!!.z <= player.mo!!.floorz)
    P_CalcHeight(player)

    if (player.attacker != null && player.attacker !== player.mo) {
        val angle: angle_t = R_PointToAngle2(player.mo!!.x,
            player.mo!!.y,
            player.attacker!!.x,
            player.attacker!!.y)

        val delta: angle_t = angle - player.mo!!.angle

        if (delta < ANG5 || delta > 0u - ANG5) {
            // Looking at killer,
            //  so fade damage flash down.
            player.mo!!.angle = angle

            if (player.damagecount != 0)
                player.damagecount--
        } else if (delta < ANG180)
            player.mo!!.angle += ANG5
        else
            player.mo!!.angle -= ANG5
    } else if (player.damagecount != 0)
        player.damagecount--

    if ((player.cmd.buttons and BT_USE) != 0)
        player.playerstate = PST_REBORN
}

//
// P_PlayerThink
//
fun P_PlayerThink(player: player_t) {
    val cmd: ticcmd_t
    var newweapon: Int  // weapontype_t

    // fixme: do this in the cheat code
    if ((player.cheats and CF_NOCLIP) != 0)
        player.mo!!.flags = player.mo!!.flags or MF_NOCLIP
    else
        player.mo!!.flags = player.mo!!.flags and MF_NOCLIP.inv()

    // chain saw run forward
    cmd = player.cmd
    if ((player.mo!!.flags and MF_JUSTATTACKED) != 0) {
        cmd.angleturn = 0
        cmd.forwardmove = 0xc800 / 512
        cmd.sidemove = 0
        player.mo!!.flags = player.mo!!.flags and MF_JUSTATTACKED.inv()
    }

    if (player.playerstate == PST_DEAD) {
        P_DeathThink(player)
        return
    }

    // Move around.
    // Reactiontime is used to prevent movement
    //  for a bit after a teleport.
    if (player.mo!!.reactiontime != 0)
        player.mo!!.reactiontime--
    else
        P_MovePlayer(player)

    P_CalcHeight(player)

    if (player.mo!!.subsector!!.sector!!.special != 0)
        P_PlayerInSpecialSector(player)

    // Check for weapon change.

    // A special event has no other buttons.
    if ((cmd.buttons and BT_SPECIAL) != 0)
        cmd.buttons = 0

    if ((cmd.buttons and BT_CHANGE) != 0) {
        // The actual changing of the weapon is done
        //  when the weapon psprite can do it
        //  (read: not in the middle of an attack).
        newweapon = (cmd.buttons and BT_WEAPONMASK) shr BT_WEAPONSHIFT

        if (newweapon == wp_fist
            && player.weaponowned[wp_chainsaw]
            && !(player.readyweapon == wp_chainsaw
                && player.powers[pw_strength] != 0)) {
            newweapon = wp_chainsaw
        }

        if ((gamemode == commercial)
            && newweapon == wp_shotgun
            && player.weaponowned[wp_supershotgun]
            && player.readyweapon != wp_supershotgun) {
            newweapon = wp_supershotgun
        }

        if (player.weaponowned[newweapon]
            && newweapon != player.readyweapon) {
            // Do not go to plasma or BFG in shareware,
            //  even if cheated.
            if ((newweapon != wp_plasma
                    && newweapon != wp_bfg)
                || (gamemode != shareware)) {
                player.pendingweapon = newweapon
            }
        }
    }

    // check for use
    if ((cmd.buttons and BT_USE) != 0) {
        if (!player.usedown) {
            P_UseLines(player)
            player.usedown = true
        }
    } else
        player.usedown = false

    // cycle psprites
    P_MovePsprites(player)

    // Counters, time dependend power ups.

    // Strength counts up to diminish fade.
    if (player.powers[pw_strength] != 0)
        player.powers[pw_strength]++

    if (player.powers[pw_invulnerability] != 0)
        player.powers[pw_invulnerability]--

    if (player.powers[pw_invisibility] != 0) {
        player.powers[pw_invisibility]--
        if (player.powers[pw_invisibility] == 0)
            player.mo!!.flags = player.mo!!.flags and MF_SHADOW.inv()
    }

    if (player.powers[pw_infrared] != 0)
        player.powers[pw_infrared]--

    if (player.powers[pw_ironfeet] != 0)
        player.powers[pw_ironfeet]--

    if (player.damagecount != 0)
        player.damagecount--

    if (player.bonuscount != 0)
        player.bonuscount--

    // Handling colormaps.
    if (player.powers[pw_invulnerability] != 0) {
        if (player.powers[pw_invulnerability] > 4 * 32
            || (player.powers[pw_invulnerability] and 8) != 0)
            player.fixedcolormap = INVERSECOLORMAP
        else
            player.fixedcolormap = 0
    } else if (player.powers[pw_infrared] != 0) {
        if (player.powers[pw_infrared] > 4 * 32
            || (player.powers[pw_infrared] and 8) != 0) {
            // almost full bright
            player.fixedcolormap = 1
        } else
            player.fixedcolormap = 0
    } else
        player.fixedcolormap = 0
}
