// Port of linuxdoom-1.10 p_inter.c -- handling interactions (i.e., collisions).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

const val BONUSADD = 6

// a weapon is found with two clip loads,
// a big item has five clip loads
val maxammo = intArrayOf(200, 50, 300, 50)
val clipammo = intArrayOf(10, 4, 20, 1)

//
// GET STUFF
//

//
// P_GiveAmmo
// Num is the number of clip loads,
// not the individual count (0= 1/2 clip).
// Returns false if the ammo can't be picked up at all
//
fun P_GiveAmmo(player: player_t, ammo: Int, num: Int): Boolean {
    var num = num
    val oldammo: Int

    if (ammo == am_noammo)
        return false

    if (ammo < 0 || ammo > NUMAMMO)
        I_Error("P_GiveAmmo: bad type $ammo")

    if (player.ammo[ammo] == player.maxammo[ammo])
        return false

    if (num != 0)
        num *= clipammo[ammo]
    else
        num = clipammo[ammo] / 2

    if (gameskill == sk_baby
        || gameskill == sk_nightmare) {
        // give double ammo in trainer mode,
        // you'll need in nightmare
        num = num shl 1
    }

    oldammo = player.ammo[ammo]
    player.ammo[ammo] += num

    if (player.ammo[ammo] > player.maxammo[ammo])
        player.ammo[ammo] = player.maxammo[ammo]

    // If non zero ammo,
    // don't change up weapons,
    // player was lower on purpose.
    if (oldammo != 0)
        return true

    // We were down to zero,
    // so select a new weapon.
    // Preferences are not user selectable.
    when (ammo) {
        am_clip -> {
            if (player.readyweapon == wp_fist) {
                if (player.weaponowned[wp_chaingun])
                    player.pendingweapon = wp_chaingun
                else
                    player.pendingweapon = wp_pistol
            }
        }

        am_shell -> {
            if (player.readyweapon == wp_fist
                || player.readyweapon == wp_pistol) {
                if (player.weaponowned[wp_shotgun])
                    player.pendingweapon = wp_shotgun
            }
        }

        am_cell -> {
            if (player.readyweapon == wp_fist
                || player.readyweapon == wp_pistol) {
                if (player.weaponowned[wp_plasma])
                    player.pendingweapon = wp_plasma
            }
        }

        am_misl -> {
            // (C falls through into the empty default here.)
            if (player.readyweapon == wp_fist) {
                if (player.weaponowned[wp_missile])
                    player.pendingweapon = wp_missile
            }
        }

        else -> {}
    }

    return true
}

//
// P_GiveWeapon
// The weapon name may have a MF_DROPPED flag ored in.
//
fun P_GiveWeapon(player: player_t, weapon: Int, dropped: Boolean): Boolean {
    val gaveammo: Boolean
    val gaveweapon: Boolean

    if (netgame
        && (deathmatch != 2)
        && !dropped) {
        // leave placed weapons forever on net games
        if (player.weaponowned[weapon])
            return false

        player.bonuscount += BONUSADD
        player.weaponowned[weapon] = true

        if (deathmatch != 0)
            P_GiveAmmo(player, weaponinfo[weapon].ammo, 5)
        else
            P_GiveAmmo(player, weaponinfo[weapon].ammo, 2)
        player.pendingweapon = weapon

        if (player === players[consoleplayer])
            S_StartSound(null, sfx_wpnup)
        return false
    }

    if (weaponinfo[weapon].ammo != am_noammo) {
        // give one clip with a dropped weapon,
        // two clips with a found weapon
        if (dropped)
            gaveammo = P_GiveAmmo(player, weaponinfo[weapon].ammo, 1)
        else
            gaveammo = P_GiveAmmo(player, weaponinfo[weapon].ammo, 2)
    } else
        gaveammo = false

    if (player.weaponowned[weapon])
        gaveweapon = false
    else {
        gaveweapon = true
        player.weaponowned[weapon] = true
        player.pendingweapon = weapon
    }

    return (gaveweapon || gaveammo)
}

//
// P_GiveBody
// Returns false if the body isn't needed at all
//
fun P_GiveBody(player: player_t, num: Int): Boolean {
    if (player.health >= MAXHEALTH)
        return false

    player.health += num
    if (player.health > MAXHEALTH)
        player.health = MAXHEALTH
    player.mo!!.health = player.health

    return true
}

//
// P_GiveArmor
// Returns false if the armor is worse
// than the current armor.
//
fun P_GiveArmor(player: player_t, armortype: Int): Boolean {
    val hits: Int

    hits = armortype * 100
    if (player.armorpoints >= hits)
        return false  // don't pick up

    player.armortype = armortype
    player.armorpoints = hits

    return true
}

//
// P_GiveCard
//
fun P_GiveCard(player: player_t, card: Int) {
    if (player.cards[card])
        return

    player.bonuscount = BONUSADD
    player.cards[card] = true
}

//
// P_GivePower
//
fun P_GivePower(player: player_t, power: Int /*powertype_t*/): Boolean {
    if (power == pw_invulnerability) {
        player.powers[power] = INVULNTICS
        return true
    }

    if (power == pw_invisibility) {
        player.powers[power] = INVISTICS
        player.mo!!.flags = player.mo!!.flags or MF_SHADOW
        return true
    }

    if (power == pw_infrared) {
        player.powers[power] = INFRATICS
        return true
    }

    if (power == pw_ironfeet) {
        player.powers[power] = IRONTICS
        return true
    }

    if (power == pw_strength) {
        P_GiveBody(player, 100)
        player.powers[power] = 1
        return true
    }

    if (player.powers[power] != 0)
        return false  // already got it

    player.powers[power] = 1
    return true
}

//
// P_TouchSpecialThing
//
fun P_TouchSpecialThing(special: mobj_t, toucher: mobj_t) {
    val player: player_t
    var i: Int
    val delta: fixed_t
    var sound: Int

    delta = special.z - toucher.z

    if (delta > toucher.height
        || delta < -8 * FRACUNIT) {
        // out of reach
        return
    }

    sound = sfx_itemup
    player = toucher.player!!

    // Dead thing touching.
    // Can happen with a sliding player corpse.
    if (toucher.health <= 0)
        return

    // Identify by sprite.
    when (special.sprite) {
        // armor
        SPR_ARM1 -> {
            if (!P_GiveArmor(player, 1))
                return
            player.message = GOTARMOR
        }

        SPR_ARM2 -> {
            if (!P_GiveArmor(player, 2))
                return
            player.message = GOTMEGA
        }

        // bonus items
        SPR_BON1 -> {
            player.health++  // can go over 100%
            if (player.health > 200)
                player.health = 200
            player.mo!!.health = player.health
            player.message = GOTHTHBONUS
        }

        SPR_BON2 -> {
            player.armorpoints++  // can go over 100%
            if (player.armorpoints > 200)
                player.armorpoints = 200
            if (player.armortype == 0)
                player.armortype = 1
            player.message = GOTARMBONUS
        }

        SPR_SOUL -> {
            player.health += 100
            if (player.health > 200)
                player.health = 200
            player.mo!!.health = player.health
            player.message = GOTSUPER
            sound = sfx_getpow
        }

        SPR_MEGA -> {
            if (gamemode != commercial)
                return
            player.health = 200
            player.mo!!.health = player.health
            P_GiveArmor(player, 2)
            player.message = GOTMSPHERE
            sound = sfx_getpow
        }

        // cards
        // leave cards for everyone
        SPR_BKEY -> {
            if (!player.cards[it_bluecard])
                player.message = GOTBLUECARD
            P_GiveCard(player, it_bluecard)
            if (netgame)  // C: if (!netgame) break; return;
                return
        }

        SPR_YKEY -> {
            if (!player.cards[it_yellowcard])
                player.message = GOTYELWCARD
            P_GiveCard(player, it_yellowcard)
            if (netgame)  // C: if (!netgame) break; return;
                return
        }

        SPR_RKEY -> {
            if (!player.cards[it_redcard])
                player.message = GOTREDCARD
            P_GiveCard(player, it_redcard)
            if (netgame)  // C: if (!netgame) break; return;
                return
        }

        SPR_BSKU -> {
            if (!player.cards[it_blueskull])
                player.message = GOTBLUESKUL
            P_GiveCard(player, it_blueskull)
            if (netgame)  // C: if (!netgame) break; return;
                return
        }

        SPR_YSKU -> {
            if (!player.cards[it_yellowskull])
                player.message = GOTYELWSKUL
            P_GiveCard(player, it_yellowskull)
            if (netgame)  // C: if (!netgame) break; return;
                return
        }

        SPR_RSKU -> {
            if (!player.cards[it_redskull])
                player.message = GOTREDSKULL
            P_GiveCard(player, it_redskull)
            if (netgame)  // C: if (!netgame) break; return;
                return
        }

        // medikits, heals
        SPR_STIM -> {
            if (!P_GiveBody(player, 10))
                return
            player.message = GOTSTIM
        }

        SPR_MEDI -> {
            if (!P_GiveBody(player, 25))
                return

            if (player.health < 25)
                player.message = GOTMEDINEED
            else
                player.message = GOTMEDIKIT
        }

        // power ups
        SPR_PINV -> {
            if (!P_GivePower(player, pw_invulnerability))
                return
            player.message = GOTINVUL
            sound = sfx_getpow
        }

        SPR_PSTR -> {
            if (!P_GivePower(player, pw_strength))
                return
            player.message = GOTBERSERK
            if (player.readyweapon != wp_fist)
                player.pendingweapon = wp_fist
            sound = sfx_getpow
        }

        SPR_PINS -> {
            if (!P_GivePower(player, pw_invisibility))
                return
            player.message = GOTINVIS
            sound = sfx_getpow
        }

        SPR_SUIT -> {
            if (!P_GivePower(player, pw_ironfeet))
                return
            player.message = GOTSUIT
            sound = sfx_getpow
        }

        SPR_PMAP -> {
            if (!P_GivePower(player, pw_allmap))
                return
            player.message = GOTMAP
            sound = sfx_getpow
        }

        SPR_PVIS -> {
            if (!P_GivePower(player, pw_infrared))
                return
            player.message = GOTVISOR
            sound = sfx_getpow
        }

        // ammo
        SPR_CLIP -> {
            if ((special.flags and MF_DROPPED) != 0) {
                if (!P_GiveAmmo(player, am_clip, 0))
                    return
            } else {
                if (!P_GiveAmmo(player, am_clip, 1))
                    return
            }
            player.message = GOTCLIP
        }

        SPR_AMMO -> {
            if (!P_GiveAmmo(player, am_clip, 5))
                return
            player.message = GOTCLIPBOX
        }

        SPR_ROCK -> {
            if (!P_GiveAmmo(player, am_misl, 1))
                return
            player.message = GOTROCKET
        }

        SPR_BROK -> {
            if (!P_GiveAmmo(player, am_misl, 5))
                return
            player.message = GOTROCKBOX
        }

        SPR_CELL -> {
            if (!P_GiveAmmo(player, am_cell, 1))
                return
            player.message = GOTCELL
        }

        SPR_CELP -> {
            if (!P_GiveAmmo(player, am_cell, 5))
                return
            player.message = GOTCELLBOX
        }

        SPR_SHEL -> {
            if (!P_GiveAmmo(player, am_shell, 1))
                return
            player.message = GOTSHELLS
        }

        SPR_SBOX -> {
            if (!P_GiveAmmo(player, am_shell, 5))
                return
            player.message = GOTSHELLBOX
        }

        SPR_BPAK -> {
            if (!player.backpack) {
                i = 0
                while (i < NUMAMMO) {
                    player.maxammo[i] *= 2
                    i++
                }
                player.backpack = true
            }
            i = 0
            while (i < NUMAMMO) {
                P_GiveAmmo(player, i, 1)
                i++
            }
            player.message = GOTBACKPACK
        }

        // weapons
        SPR_BFUG -> {
            if (!P_GiveWeapon(player, wp_bfg, false))
                return
            player.message = GOTBFG9000
            sound = sfx_wpnup
        }

        SPR_MGUN -> {
            if (!P_GiveWeapon(player, wp_chaingun, (special.flags and MF_DROPPED) != 0))
                return
            player.message = GOTCHAINGUN
            sound = sfx_wpnup
        }

        SPR_CSAW -> {
            if (!P_GiveWeapon(player, wp_chainsaw, false))
                return
            player.message = GOTCHAINSAW
            sound = sfx_wpnup
        }

        SPR_LAUN -> {
            if (!P_GiveWeapon(player, wp_missile, false))
                return
            player.message = GOTLAUNCHER
            sound = sfx_wpnup
        }

        SPR_PLAS -> {
            if (!P_GiveWeapon(player, wp_plasma, false))
                return
            player.message = GOTPLASMA
            sound = sfx_wpnup
        }

        SPR_SHOT -> {
            if (!P_GiveWeapon(player, wp_shotgun, (special.flags and MF_DROPPED) != 0))
                return
            player.message = GOTSHOTGUN
            sound = sfx_wpnup
        }

        SPR_SGN2 -> {
            if (!P_GiveWeapon(player, wp_supershotgun, (special.flags and MF_DROPPED) != 0))
                return
            player.message = GOTSHOTGUN2
            sound = sfx_wpnup
        }

        else ->
            I_Error("P_SpecialThing: Unknown gettable thing")
    }

    if ((special.flags and MF_COUNTITEM) != 0)
        player.itemcount++
    P_RemoveMobj(special)
    player.bonuscount += BONUSADD
    if (player === players[consoleplayer])
        S_StartSound(null, sound)
}

//
// KillMobj
//
fun P_KillMobj(source: mobj_t?, target: mobj_t) {
    val item: Int  // mobjtype_t
    val mo: mobj_t

    target.flags = target.flags and (MF_SHOOTABLE or MF_FLOAT or MF_SKULLFLY).inv()

    if (target.type != MT_SKULL)
        target.flags = target.flags and MF_NOGRAVITY.inv()

    target.flags = target.flags or (MF_CORPSE or MF_DROPOFF)
    target.height = target.height shr 2

    if (source != null && source.player != null) {
        // count for intermission
        if ((target.flags and MF_COUNTKILL) != 0)
            source.player!!.killcount++

        if (target.player != null)
            source.player!!.frags[players.indexOf(target.player!!)]++  // C: target->player-players
    } else if (!netgame && (target.flags and MF_COUNTKILL) != 0) {
        // count all monster deaths,
        // even those caused by other monsters
        players[0].killcount++
    }

    if (target.player != null) {
        // count environment kills against you
        if (source == null)
            target.player!!.frags[players.indexOf(target.player!!)]++  // C: target->player-players

        target.flags = target.flags and MF_SOLID.inv()
        target.player!!.playerstate = PST_DEAD
        P_DropWeapon(target.player!!)

        if (target.player === players[consoleplayer]
            && automapactive) {
            // don't die in auto map,
            // switch view prior to dying
            AM_Stop()
        }
    }

    if (target.health < -target.info!!.spawnhealth
        && target.info!!.xdeathstate != 0) {
        P_SetMobjState(target, target.info!!.xdeathstate)
    } else
        P_SetMobjState(target, target.info!!.deathstate)
    target.tics -= P_Random() and 3

    if (target.tics < 1)
        target.tics = 1

    //	I_StartSound (&actor->r, actor->info->deathsound);

    // Drop stuff.
    // This determines the kind of object spawned
    // during the death frame of a thing.
    when (target.type) {
        MT_WOLFSS,
        MT_POSSESSED ->
            item = MT_CLIP

        MT_SHOTGUY ->
            item = MT_SHOTGUN

        MT_CHAINGUY ->
            item = MT_CHAINGUN

        else ->
            return
    }

    mo = P_SpawnMobj(target.x, target.y, ONFLOORZ, item)
    mo.flags = mo.flags or MF_DROPPED  // special versions of items
}

//
// P_DamageMobj
// Damages both enemies and players
// "inflictor" is the thing that caused the damage
//  creature or missile, can be NULL (slime, etc)
// "source" is the thing to target after taking damage
//  creature or NULL
// Source and inflictor are the same for melee attacks.
// Source can be NULL for slime, barrel explosions
// and other environmental stuff.
//
fun P_DamageMobj(target: mobj_t, inflictor: mobj_t?, source: mobj_t?, damage: Int) {
    var damage = damage
    var ang: angle_t  // C: unsigned
    var saved: Int
    val player: player_t?
    var thrust: fixed_t
    val temp: Int

    if ((target.flags and MF_SHOOTABLE) == 0)
        return  // shouldn't happen...

    if (target.health <= 0)
        return

    if ((target.flags and MF_SKULLFLY) != 0) {
        target.momx = 0
        target.momy = 0
        target.momz = 0
    }

    player = target.player
    if (player != null && gameskill == sk_baby)
        damage = damage shr 1  // take half damage in trainer mode

    // Some close combat weapons should not
    // inflict thrust and push the victim out of reach,
    // thus kick away unless using the chainsaw.
    if (inflictor != null
        && (target.flags and MF_NOCLIP) == 0
        && (source == null
            || source.player == null
            || source.player!!.readyweapon != wp_chainsaw)) {
        ang = R_PointToAngle2(inflictor.x,
            inflictor.y,
            target.x,
            target.y)

        thrust = damage * (FRACUNIT shr 3) * 100 / target.info!!.mass

        // make fall forwards sometimes
        if (damage < 40
            && damage > target.health
            && target.z - inflictor.z > 64 * FRACUNIT
            && (P_Random() and 1) != 0) {
            ang += ANG180
            thrust *= 4
        }

        ang = ang shr ANGLETOFINESHIFT
        target.momx += FixedMul(thrust, finecosine[ang.toInt()])
        target.momy += FixedMul(thrust, finesine[ang.toInt()])
    }

    // player specific
    if (player != null) {
        // end of game hell hack
        if (target.subsector!!.sector!!.special == 11
            && damage >= target.health) {
            damage = target.health - 1
        }

        // Below certain threshold,
        // ignore damage in GOD mode, or with INVUL power.
        if (damage < 1000
            && ((player.cheats and CF_GODMODE) != 0
                || player.powers[pw_invulnerability] != 0)) {
            return
        }

        if (player.armortype != 0) {
            if (player.armortype == 1)
                saved = damage / 3
            else
                saved = damage / 2

            if (player.armorpoints <= saved) {
                // armor is used up
                saved = player.armorpoints
                player.armortype = 0
            }
            player.armorpoints -= saved
            damage -= saved
        }
        player.health -= damage  // mirror mobj health here for Dave
        if (player.health < 0)
            player.health = 0

        player.attacker = source
        player.damagecount += damage  // add damage after armor / invuln

        if (player.damagecount > 100)
            player.damagecount = 100  // teleport stomp does 10k points...

        temp = if (damage < 100) damage else 100

        if (player === players[consoleplayer])
            I_Tactile(40, 10, 40 + temp * 2)
    }

    // do the damage
    target.health -= damage
    if (target.health <= 0) {
        P_KillMobj(source, target)
        return
    }

    if ((P_Random() < target.info!!.painchance)
        && (target.flags and MF_SKULLFLY) == 0) {
        target.flags = target.flags or MF_JUSTHIT  // fight back!

        P_SetMobjState(target, target.info!!.painstate)
    }

    target.reactiontime = 0  // we're awake now...

    if ((target.threshold == 0 || target.type == MT_VILE)
        && source != null && source !== target
        && source.type != MT_VILE) {
        // if not intent on another player,
        // chase after this one
        target.target = source
        target.threshold = BASETHRESHOLD
        if (target.state === states[target.info!!.spawnstate]
            && target.info!!.seestate != S_NULL)
            P_SetMobjState(target, target.info!!.seestate)
    }
}
