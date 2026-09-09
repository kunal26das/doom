
package doom.engine.gameplay.interactions

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_GETPOW
import doom.engine.audio.SFX_ITEMUP
import doom.engine.audio.SFX_WPNUP
import doom.engine.automap.amStop
import doom.engine.automap.automapactive
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.INFRATICS
import doom.engine.gameplay.INVISTICS
import doom.engine.gameplay.INVULNTICS
import doom.engine.gameplay.IRONTICS
import doom.engine.gameplay.NUMAMMO
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.actors.MF_CORPSE
import doom.engine.gameplay.actors.MF_COUNTITEM
import doom.engine.gameplay.actors.MF_COUNTKILL
import doom.engine.gameplay.actors.MF_DROPOFF
import doom.engine.gameplay.actors.MF_DROPPED
import doom.engine.gameplay.actors.MF_FLOAT
import doom.engine.gameplay.actors.MF_JUSTHIT
import doom.engine.gameplay.actors.MF_NOCLIP
import doom.engine.gameplay.actors.MF_NOGRAVITY
import doom.engine.gameplay.actors.MF_SHADOW
import doom.engine.gameplay.actors.MF_SHOOTABLE
import doom.engine.gameplay.actors.MF_SKULLFLY
import doom.engine.gameplay.actors.MF_SOLID
import doom.engine.gameplay.actors.MT_CHAINGUN
import doom.engine.gameplay.actors.MT_CHAINGUY
import doom.engine.gameplay.actors.MT_CLIP
import doom.engine.gameplay.actors.MT_POSSESSED
import doom.engine.gameplay.actors.MT_SHOTGUN
import doom.engine.gameplay.actors.MT_SHOTGUY
import doom.engine.gameplay.actors.MT_SKULL
import doom.engine.gameplay.actors.MT_VILE
import doom.engine.gameplay.actors.MT_WOLFSS
import doom.engine.gameplay.actors.pRemoveMobj
import doom.engine.gameplay.actors.pSetMobjState
import doom.engine.gameplay.actors.pSpawnMobj
import doom.engine.gameplay.actors.SPR_AMMO
import doom.engine.gameplay.actors.SPR_ARM1
import doom.engine.gameplay.actors.SPR_ARM2
import doom.engine.gameplay.actors.SPR_BFUG
import doom.engine.gameplay.actors.SPR_BKEY
import doom.engine.gameplay.actors.SPR_BON1
import doom.engine.gameplay.actors.SPR_BON2
import doom.engine.gameplay.actors.SPR_BPAK
import doom.engine.gameplay.actors.SPR_BROK
import doom.engine.gameplay.actors.SPR_BSKU
import doom.engine.gameplay.actors.SPR_CELL
import doom.engine.gameplay.actors.SPR_CELP
import doom.engine.gameplay.actors.SPR_CLIP
import doom.engine.gameplay.actors.SPR_CSAW
import doom.engine.gameplay.actors.SPR_LAUN
import doom.engine.gameplay.actors.SPR_MEDI
import doom.engine.gameplay.actors.SPR_MEGA
import doom.engine.gameplay.actors.SPR_MGUN
import doom.engine.gameplay.actors.SPR_PINS
import doom.engine.gameplay.actors.SPR_PINV
import doom.engine.gameplay.actors.SPR_PLAS
import doom.engine.gameplay.actors.SPR_PMAP
import doom.engine.gameplay.actors.SPR_PSTR
import doom.engine.gameplay.actors.SPR_PVIS
import doom.engine.gameplay.actors.SPR_RKEY
import doom.engine.gameplay.actors.SPR_ROCK
import doom.engine.gameplay.actors.SPR_RSKU
import doom.engine.gameplay.actors.SPR_SBOX
import doom.engine.gameplay.actors.SPR_SGN2
import doom.engine.gameplay.actors.SPR_SHEL
import doom.engine.gameplay.actors.SPR_SHOT
import doom.engine.gameplay.actors.SPR_SOUL
import doom.engine.gameplay.actors.SPR_STIM
import doom.engine.gameplay.actors.SPR_SUIT
import doom.engine.gameplay.actors.SPR_YKEY
import doom.engine.gameplay.actors.SPR_YSKU
import doom.engine.gameplay.actors.S_NULL
import doom.engine.gameplay.actors.states
import doom.engine.gameplay.AM_CELL
import doom.engine.gameplay.AM_CLIP
import doom.engine.gameplay.AM_MISL
import doom.engine.gameplay.AM_NOAMMO
import doom.engine.gameplay.AM_SHELL
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.deathmatch
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.gameskill
import doom.engine.gameplay.IT_BLUECARD
import doom.engine.gameplay.IT_BLUESKULL
import doom.engine.gameplay.IT_REDCARD
import doom.engine.gameplay.IT_REDSKULL
import doom.engine.gameplay.IT_YELLOWCARD
import doom.engine.gameplay.IT_YELLOWSKULL
import doom.engine.gameplay.netgame
import doom.engine.gameplay.player.CF_GODMODE
import doom.engine.gameplay.player.PST_DEAD
import doom.engine.gameplay.player.Player
import doom.engine.gameplay.players
import doom.engine.gameplay.PW_ALLMAP
import doom.engine.gameplay.PW_INFRARED
import doom.engine.gameplay.PW_INVISIBILITY
import doom.engine.gameplay.PW_INVULNERABILITY
import doom.engine.gameplay.PW_IRONFEET
import doom.engine.gameplay.PW_STRENGTH
import doom.engine.gameplay.SK_BABY
import doom.engine.gameplay.SK_NIGHTMARE
import doom.engine.gameplay.weapons.pDropWeapon
import doom.engine.gameplay.weapons.weaponinfo
import doom.engine.gameplay.WP_BFG
import doom.engine.gameplay.WP_CHAINGUN
import doom.engine.gameplay.WP_CHAINSAW
import doom.engine.gameplay.WP_FIST
import doom.engine.gameplay.WP_MISSILE
import doom.engine.gameplay.WP_PISTOL
import doom.engine.gameplay.WP_PLASMA
import doom.engine.gameplay.WP_SHOTGUN
import doom.engine.gameplay.WP_SUPERSHOTGUN
import doom.engine.geometry.ANG180
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.FixedGeometry
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.resources.GOTARMBONUS
import doom.engine.resources.GOTARMOR
import doom.engine.resources.GOTBACKPACK
import doom.engine.resources.GOTBERSERK
import doom.engine.resources.GOTBFG9000
import doom.engine.resources.GOTBLUECARD
import doom.engine.resources.GOTBLUESKUL
import doom.engine.resources.GOTCELL
import doom.engine.resources.GOTCELLBOX
import doom.engine.resources.GOTCHAINGUN
import doom.engine.resources.GOTCHAINSAW
import doom.engine.resources.GOTCLIP
import doom.engine.resources.GOTCLIPBOX
import doom.engine.resources.GOTHTHBONUS
import doom.engine.resources.GOTINVIS
import doom.engine.resources.GOTINVUL
import doom.engine.resources.GOTLAUNCHER
import doom.engine.resources.GOTMAP
import doom.engine.resources.GOTMEDIKIT
import doom.engine.resources.GOTMEDINEED
import doom.engine.resources.GOTMEGA
import doom.engine.resources.GOTMSPHERE
import doom.engine.resources.GOTPLASMA
import doom.engine.resources.GOTREDCARD
import doom.engine.resources.GOTREDSKULL
import doom.engine.resources.GOTROCKBOX
import doom.engine.resources.GOTROCKET
import doom.engine.resources.GOTSHELLBOX
import doom.engine.resources.GOTSHELLS
import doom.engine.resources.GOTSHOTGUN
import doom.engine.resources.GOTSHOTGUN2
import doom.engine.resources.GOTSTIM
import doom.engine.resources.GOTSUIT
import doom.engine.resources.GOTSUPER
import doom.engine.resources.GOTVISOR
import doom.engine.resources.GOTYELWCARD
import doom.engine.resources.GOTYELWSKUL
import doom.engine.simulation.pRandom
import doom.engine.world.BASETHRESHOLD
import doom.engine.world.MAXHEALTH
import doom.engine.world.ONFLOORZ

internal const val BONUSADD = 6

internal val DoomEngineCore.maxammo
    get() = statePInter.maxammo
internal val DoomEngineCore.clipammo
    get() = statePInter.clipammo


internal fun DoomEngineCore.pGiveAmmo(player: Player, ammo: Int, clipCount: Int): Boolean {
    var num = clipCount
    val oldammo: Int

    if (ammo == AM_NOAMMO)
        return false

    if (ammo < 0 || ammo > NUMAMMO)
        iError("P_GiveAmmo: bad type $ammo")

    if (player.ammo[ammo] == player.maxammo[ammo])
        return false

    if (num != 0)
        num *= clipammo[ammo]
    else
        num = clipammo[ammo] / 2

    if (gameskill == SK_BABY
        || gameskill == SK_NIGHTMARE) {
        num = num shl 1
    }

    oldammo = player.ammo[ammo]
    player.ammo[ammo] += num

    if (player.ammo[ammo] > player.maxammo[ammo])
        player.ammo[ammo] = player.maxammo[ammo]

    if (oldammo != 0)
        return true

    when (ammo) {
        AM_CLIP -> {
            if (player.readyweapon == WP_FIST) {
                if (player.weaponowned[WP_CHAINGUN])
                    player.pendingweapon = WP_CHAINGUN
                else
                    player.pendingweapon = WP_PISTOL
            }
        }

        AM_SHELL -> {
            if (player.readyweapon == WP_FIST
                || player.readyweapon == WP_PISTOL) {
                if (player.weaponowned[WP_SHOTGUN])
                    player.pendingweapon = WP_SHOTGUN
            }
        }

        AM_CELL -> {
            if (player.readyweapon == WP_FIST
                || player.readyweapon == WP_PISTOL) {
                if (player.weaponowned[WP_PLASMA])
                    player.pendingweapon = WP_PLASMA
            }
        }

        AM_MISL -> {
            if (player.readyweapon == WP_FIST) {
                if (player.weaponowned[WP_MISSILE])
                    player.pendingweapon = WP_MISSILE
            }
        }

        else -> {}
    }

    return true
}

internal fun DoomEngineCore.pGiveWeapon(player: Player, weapon: Int, dropped: Boolean): Boolean {
    val gaveammo: Boolean
    val gaveweapon: Boolean

    if (netgame
        && (deathmatch != 2)
        && !dropped) {
        if (player.weaponowned[weapon])
            return false

        player.bonuscount += BONUSADD
        player.weaponowned[weapon] = true

        if (deathmatch != 0)
            pGiveAmmo(player, weaponinfo[weapon].ammo, 5)
        else
            pGiveAmmo(player, weaponinfo[weapon].ammo, 2)
        player.pendingweapon = weapon

        if (player === players[consoleplayer])
            sStartSound(null, SFX_WPNUP)
        return false
    }

    if (weaponinfo[weapon].ammo != AM_NOAMMO) {
        if (dropped)
            gaveammo = pGiveAmmo(player, weaponinfo[weapon].ammo, 1)
        else
            gaveammo = pGiveAmmo(player, weaponinfo[weapon].ammo, 2)
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

internal fun DoomEngineCore.pGiveBody(player: Player, num: Int): Boolean {
    if (player.health >= MAXHEALTH)
        return false

    player.health += num
    if (player.health > MAXHEALTH)
        player.health = MAXHEALTH
    player.mo!!.health = player.health

    return true
}

internal fun DoomEngineCore.pGiveArmor(player: Player, armortype: Int): Boolean {
    val hits: Int

    hits = armortype * 100
    if (player.armorpoints >= hits)
        return false

    player.armortype = armortype
    player.armorpoints = hits

    return true
}

internal fun DoomEngineCore.pGiveCard(player: Player, card: Int) {
    if (player.cards[card])
        return

    player.bonuscount = BONUSADD
    player.cards[card] = true
}

internal fun DoomEngineCore.pGivePower(player: Player, power: Int  ): Boolean {
    if (power == PW_INVULNERABILITY) {
        player.powers[power] = INVULNTICS
        return true
    }

    if (power == PW_INVISIBILITY) {
        player.powers[power] = INVISTICS
        player.mo!!.flags = player.mo!!.flags or MF_SHADOW
        return true
    }

    if (power == PW_INFRARED) {
        player.powers[power] = INFRATICS
        return true
    }

    if (power == PW_IRONFEET) {
        player.powers[power] = IRONTICS
        return true
    }

    if (power == PW_STRENGTH) {
        pGiveBody(player, 100)
        player.powers[power] = 1
        return true
    }

    if (player.powers[power] != 0)
        return false

    player.powers[power] = 1
    return true
}

internal fun DoomEngineCore.pTouchSpecialThing(special: Actor, toucher: Actor) {
    val player: Player
    var i: Int
    val delta: FixedPoint
    var sound: Int

    delta = special.z - toucher.z

    if (delta > toucher.height
        || delta < -8 * FRACUNIT) {
        return
    }

    sound = SFX_ITEMUP
    player = toucher.player!!

    if (toucher.health <= 0)
        return

    when (special.sprite) {
        SPR_ARM1 -> {
            if (!pGiveArmor(player, 1))
                return
            player.message = GOTARMOR
        }

        SPR_ARM2 -> {
            if (!pGiveArmor(player, 2))
                return
            player.message = GOTMEGA
        }

        SPR_BON1 -> {
            player.health++
            if (player.health > 200)
                player.health = 200
            player.mo!!.health = player.health
            player.message = GOTHTHBONUS
        }

        SPR_BON2 -> {
            player.armorpoints++
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
            sound = SFX_GETPOW
        }

        SPR_MEGA -> {
            if (gamemode != COMMERCIAL)
                return
            player.health = 200
            player.mo!!.health = player.health
            pGiveArmor(player, 2)
            player.message = GOTMSPHERE
            sound = SFX_GETPOW
        }

        SPR_BKEY -> {
            if (!player.cards[IT_BLUECARD])
                player.message = GOTBLUECARD
            pGiveCard(player, IT_BLUECARD)
            if (netgame)
                return
        }

        SPR_YKEY -> {
            if (!player.cards[IT_YELLOWCARD])
                player.message = GOTYELWCARD
            pGiveCard(player, IT_YELLOWCARD)
            if (netgame)
                return
        }

        SPR_RKEY -> {
            if (!player.cards[IT_REDCARD])
                player.message = GOTREDCARD
            pGiveCard(player, IT_REDCARD)
            if (netgame)
                return
        }

        SPR_BSKU -> {
            if (!player.cards[IT_BLUESKULL])
                player.message = GOTBLUESKUL
            pGiveCard(player, IT_BLUESKULL)
            if (netgame)
                return
        }

        SPR_YSKU -> {
            if (!player.cards[IT_YELLOWSKULL])
                player.message = GOTYELWSKUL
            pGiveCard(player, IT_YELLOWSKULL)
            if (netgame)
                return
        }

        SPR_RSKU -> {
            if (!player.cards[IT_REDSKULL])
                player.message = GOTREDSKULL
            pGiveCard(player, IT_REDSKULL)
            if (netgame)
                return
        }

        SPR_STIM -> {
            if (!pGiveBody(player, 10))
                return
            player.message = GOTSTIM
        }

        SPR_MEDI -> {
            if (!pGiveBody(player, 25))
                return

            if (player.health < 25)
                player.message = GOTMEDINEED
            else
                player.message = GOTMEDIKIT
        }

        SPR_PINV -> {
            if (!pGivePower(player, PW_INVULNERABILITY))
                return
            player.message = GOTINVUL
            sound = SFX_GETPOW
        }

        SPR_PSTR -> {
            if (!pGivePower(player, PW_STRENGTH))
                return
            player.message = GOTBERSERK
            if (player.readyweapon != WP_FIST)
                player.pendingweapon = WP_FIST
            sound = SFX_GETPOW
        }

        SPR_PINS -> {
            if (!pGivePower(player, PW_INVISIBILITY))
                return
            player.message = GOTINVIS
            sound = SFX_GETPOW
        }

        SPR_SUIT -> {
            if (!pGivePower(player, PW_IRONFEET))
                return
            player.message = GOTSUIT
            sound = SFX_GETPOW
        }

        SPR_PMAP -> {
            if (!pGivePower(player, PW_ALLMAP))
                return
            player.message = GOTMAP
            sound = SFX_GETPOW
        }

        SPR_PVIS -> {
            if (!pGivePower(player, PW_INFRARED))
                return
            player.message = GOTVISOR
            sound = SFX_GETPOW
        }

        SPR_CLIP -> {
            if ((special.flags and MF_DROPPED) != 0) {
                if (!pGiveAmmo(player, AM_CLIP, 0))
                    return
            } else {
                if (!pGiveAmmo(player, AM_CLIP, 1))
                    return
            }
            player.message = GOTCLIP
        }

        SPR_AMMO -> {
            if (!pGiveAmmo(player, AM_CLIP, 5))
                return
            player.message = GOTCLIPBOX
        }

        SPR_ROCK -> {
            if (!pGiveAmmo(player, AM_MISL, 1))
                return
            player.message = GOTROCKET
        }

        SPR_BROK -> {
            if (!pGiveAmmo(player, AM_MISL, 5))
                return
            player.message = GOTROCKBOX
        }

        SPR_CELL -> {
            if (!pGiveAmmo(player, AM_CELL, 1))
                return
            player.message = GOTCELL
        }

        SPR_CELP -> {
            if (!pGiveAmmo(player, AM_CELL, 5))
                return
            player.message = GOTCELLBOX
        }

        SPR_SHEL -> {
            if (!pGiveAmmo(player, AM_SHELL, 1))
                return
            player.message = GOTSHELLS
        }

        SPR_SBOX -> {
            if (!pGiveAmmo(player, AM_SHELL, 5))
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
                pGiveAmmo(player, i, 1)
                i++
            }
            player.message = GOTBACKPACK
        }

        SPR_BFUG -> {
            if (!pGiveWeapon(player, WP_BFG, false))
                return
            player.message = GOTBFG9000
            sound = SFX_WPNUP
        }

        SPR_MGUN -> {
            if (!pGiveWeapon(player, WP_CHAINGUN, (special.flags and MF_DROPPED) != 0))
                return
            player.message = GOTCHAINGUN
            sound = SFX_WPNUP
        }

        SPR_CSAW -> {
            if (!pGiveWeapon(player, WP_CHAINSAW, false))
                return
            player.message = GOTCHAINSAW
            sound = SFX_WPNUP
        }

        SPR_LAUN -> {
            if (!pGiveWeapon(player, WP_MISSILE, false))
                return
            player.message = GOTLAUNCHER
            sound = SFX_WPNUP
        }

        SPR_PLAS -> {
            if (!pGiveWeapon(player, WP_PLASMA, false))
                return
            player.message = GOTPLASMA
            sound = SFX_WPNUP
        }

        SPR_SHOT -> {
            if (!pGiveWeapon(player, WP_SHOTGUN, (special.flags and MF_DROPPED) != 0))
                return
            player.message = GOTSHOTGUN
            sound = SFX_WPNUP
        }

        SPR_SGN2 -> {
            if (!pGiveWeapon(player, WP_SUPERSHOTGUN, (special.flags and MF_DROPPED) != 0))
                return
            player.message = GOTSHOTGUN2
            sound = SFX_WPNUP
        }

        else ->
            iError("P_SpecialThing: Unknown gettable thing")
    }

    if ((special.flags and MF_COUNTITEM) != 0)
        player.itemcount++
    pRemoveMobj(special)
    player.bonuscount += BONUSADD
    if (player === players[consoleplayer])
        sStartSound(null, sound)
}

internal fun DoomEngineCore.pKillMobj(source: Actor?, target: Actor) {
    val item: Int
    val mo: Actor

    target.flags = target.flags and (MF_SHOOTABLE or MF_FLOAT or MF_SKULLFLY).inv()

    if (target.type != MT_SKULL)
        target.flags = target.flags and MF_NOGRAVITY.inv()

    target.flags = target.flags or (MF_CORPSE or MF_DROPOFF)
    target.height = target.height shr 2

    if (source != null && source.player != null) {
        if ((target.flags and MF_COUNTKILL) != 0)
            source.player!!.killcount++

        if (target.player != null)
            source.player!!.frags[players.indexOf(target.player!!)]++
    } else if (!netgame && (target.flags and MF_COUNTKILL) != 0) {
        players[0].killcount++
    }

    if (target.player != null) {
        if (source == null)
            target.player!!.frags[players.indexOf(target.player!!)]++

        target.flags = target.flags and MF_SOLID.inv()
        target.player!!.playerstate = PST_DEAD
        pDropWeapon(target.player!!)

        if (target.player === players[consoleplayer]
            && automapactive) {
            amStop()
        }
    }

    if (target.health < -target.info!!.spawnhealth
        && target.info!!.xdeathstate != 0) {
        pSetMobjState(target, target.info!!.xdeathstate)
    } else
        pSetMobjState(target, target.info!!.deathstate)
    target.tics -= pRandom() and 3

    if (target.tics < 1)
        target.tics = 1


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

    mo = pSpawnMobj(target.x, target.y, ONFLOORZ, item)
    mo.flags = mo.flags or MF_DROPPED
}

internal fun DoomEngineCore.pDamageMobj(target: Actor, inflictor: Actor?, source: Actor?, initialDamage: Int) {
    var damage = initialDamage
    var ang: BinaryAngle
    var saved: Int
    val player: Player?
    var thrust: FixedPoint

    if ((target.flags and MF_SHOOTABLE) == 0)
        return

    if (target.health <= 0)
        return

    if ((target.flags and MF_SKULLFLY) != 0) {
        target.momx = 0
        target.momy = 0
        target.momz = 0
    }

    player = target.player
    if (player != null && gameskill == SK_BABY)
        damage = damage shr 1

    if (inflictor != null
        && (target.flags and MF_NOCLIP) == 0
        && (source == null
            || source.player == null
            || source.player!!.readyweapon != WP_CHAINSAW)) {
        ang = FixedGeometry.angleBetween(inflictor.x,
            inflictor.y,
            target.x,
            target.y)

        thrust = damage * (FRACUNIT shr 3) * 100 / target.info!!.mass

        if (damage < 40
            && damage > target.health
            && target.z - inflictor.z > 64 * FRACUNIT
            && (pRandom() and 1) != 0) {
            ang += ANG180
            thrust *= 4
        }

        ang = ang shr ANGLETOFINESHIFT
        target.momx += fixedMul(thrust, FineCosineTable[ang.toInt()])
        target.momy += fixedMul(thrust, finesine[ang.toInt()])
    }

    if (player != null) {
        if (target.subsector!!.sector!!.special == 11
            && damage >= target.health) {
            damage = target.health - 1
        }

        if (damage < 1000
            && ((player.cheats and CF_GODMODE) != 0
                || player.powers[PW_INVULNERABILITY] != 0)) {
            return
        }

        if (player.armortype != 0) {
            if (player.armortype == 1)
                saved = damage / 3
            else
                saved = damage / 2

            if (player.armorpoints <= saved) {
                saved = player.armorpoints
                player.armortype = 0
            }
            player.armorpoints -= saved
            damage -= saved
        }
        player.health -= damage
        if (player.health < 0)
            player.health = 0

        player.attacker = source
        player.damagecount += damage

        if (player.damagecount > 100)
            player.damagecount = 100
    }

    target.health -= damage
    if (target.health <= 0) {
        pKillMobj(source, target)
        return
    }

    if ((pRandom() < target.info!!.painchance)
        && (target.flags and MF_SKULLFLY) == 0) {
        target.flags = target.flags or MF_JUSTHIT

        pSetMobjState(target, target.info!!.painstate)
    }

    target.reactiontime = 0

    if ((target.threshold == 0 || target.type == MT_VILE)
        && source != null && source !== target
        && source.type != MT_VILE) {
        target.target = source
        target.threshold = BASETHRESHOLD
        if (target.state === states[target.info!!.spawnstate]
            && target.info!!.seestate != S_NULL)
            pSetMobjState(target, target.info!!.seestate)
    }
}
