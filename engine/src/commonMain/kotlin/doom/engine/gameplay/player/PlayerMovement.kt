
package doom.engine.gameplay.player

import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.actors.MF_JUSTATTACKED
import doom.engine.gameplay.actors.MF_NOCLIP
import doom.engine.gameplay.actors.MF_SHADOW
import doom.engine.gameplay.actors.pSetMobjState
import doom.engine.gameplay.actors.S_PLAY
import doom.engine.gameplay.actors.S_PLAY_RUN1
import doom.engine.gameplay.actors.states
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.PW_INFRARED
import doom.engine.gameplay.PW_INVISIBILITY
import doom.engine.gameplay.PW_INVULNERABILITY
import doom.engine.gameplay.PW_IRONFEET
import doom.engine.gameplay.PW_STRENGTH
import doom.engine.gameplay.SHAREWARE
import doom.engine.gameplay.weapons.pMovePsprites
import doom.engine.gameplay.WP_BFG
import doom.engine.gameplay.WP_CHAINSAW
import doom.engine.gameplay.WP_FIST
import doom.engine.gameplay.WP_PLASMA
import doom.engine.gameplay.WP_SHOTGUN
import doom.engine.gameplay.WP_SUPERSHOTGUN
import doom.engine.geometry.ANG180
import doom.engine.geometry.ANG90
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FINEANGLES
import doom.engine.geometry.FINEMASK
import doom.engine.geometry.FRACUNIT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.FixedGeometry
import doom.engine.geometry.fixedMul
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.finesine
import doom.engine.input.BT_CHANGE
import doom.engine.input.BT_SPECIAL
import doom.engine.input.BT_USE
import doom.engine.input.BT_WEAPONMASK
import doom.engine.input.BT_WEAPONSHIFT
import doom.engine.input.TicCommand
import doom.engine.simulation.leveltime
import doom.engine.world.VIEWHEIGHT
import doom.engine.world.collision.pUseLines
import doom.engine.world.specials.pPlayerInSpecialSector

internal const val INVERSECOLORMAP = 32


internal const val MAXBOB = 0x100000

internal var DoomEngineCore.onground
    get() = statePUser.onground
    set(value) { statePUser.onground = value }

internal fun DoomEngineCore.pThrust(player: Player, angleValue: BinaryAngle, move: FixedPoint) {
    val angle = (angleValue shr ANGLETOFINESHIFT).toInt()

    player.mo!!.momx += fixedMul(move, FineCosineTable[angle])
    player.mo!!.momy += fixedMul(move, finesine[angle])
}

internal fun DoomEngineCore.pCalcHeight(player: Player) {
    val angle: Int
    val bob: FixedPoint

    player.bob =
        fixedMul(player.mo!!.momx, player.mo!!.momx) +
        fixedMul(player.mo!!.momy, player.mo!!.momy)

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
    bob = fixedMul(player.bob / 2, finesine[angle])

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

internal fun DoomEngineCore.pMovePlayer(player: Player) {
    val cmd: TicCommand

    cmd = player.cmd

    player.mo!!.angle += (cmd.angleturn shl 16).toUInt()

    onground = (player.mo!!.z <= player.mo!!.floorz)

    if (cmd.forwardmove != 0 && onground)
        pThrust(player, player.mo!!.angle, cmd.forwardmove * 2048)

    if (cmd.sidemove != 0 && onground)
        pThrust(player, player.mo!!.angle - ANG90, cmd.sidemove * 2048)

    if ((cmd.forwardmove != 0 || cmd.sidemove != 0)
        && player.mo!!.state === states[S_PLAY]) {
        pSetMobjState(player.mo!!, S_PLAY_RUN1)
    }
}

internal val DoomEngineCore.ang5: BinaryAngle
    get() = statePUser.ang5

internal fun DoomEngineCore.pDeathThink(player: Player) {
    pMovePsprites(player)

    if (player.viewheight > 6 * FRACUNIT)
        player.viewheight -= FRACUNIT

    if (player.viewheight < 6 * FRACUNIT)
        player.viewheight = 6 * FRACUNIT

    player.deltaviewheight = 0
    onground = (player.mo!!.z <= player.mo!!.floorz)
    pCalcHeight(player)

    if (player.attacker != null && player.attacker !== player.mo) {
        val angle: BinaryAngle = FixedGeometry.angleBetween(player.mo!!.x,
            player.mo!!.y,
            player.attacker!!.x,
            player.attacker!!.y)

        val delta: BinaryAngle = angle - player.mo!!.angle

        if (delta < ang5 || delta > 0u - ang5) {
            player.mo!!.angle = angle

            if (player.damagecount != 0)
                player.damagecount--
        } else if (delta < ANG180)
            player.mo!!.angle += ang5
        else
            player.mo!!.angle -= ang5
    } else if (player.damagecount != 0)
        player.damagecount--

    if ((player.cmd.buttons and BT_USE) != 0)
        player.playerstate = PST_REBORN
}

internal fun DoomEngineCore.pPlayerThink(player: Player) {
    val cmd: TicCommand
    var newweapon: Int

    if ((player.cheats and CF_NOCLIP) != 0)
        player.mo!!.flags = player.mo!!.flags or MF_NOCLIP
    else
        player.mo!!.flags = player.mo!!.flags and MF_NOCLIP.inv()

    cmd = player.cmd
    if ((player.mo!!.flags and MF_JUSTATTACKED) != 0) {
        cmd.angleturn = 0
        cmd.forwardmove = 0xc800 / 512
        cmd.sidemove = 0
        player.mo!!.flags = player.mo!!.flags and MF_JUSTATTACKED.inv()
    }

    if (player.playerstate == PST_DEAD) {
        pDeathThink(player)
        return
    }

    if (player.mo!!.reactiontime != 0)
        player.mo!!.reactiontime--
    else
        pMovePlayer(player)

    pCalcHeight(player)

    if (player.mo!!.subsector!!.sector!!.special != 0)
        pPlayerInSpecialSector(player)


    if ((cmd.buttons and BT_SPECIAL) != 0)
        cmd.buttons = 0

    if ((cmd.buttons and BT_CHANGE) != 0) {
        newweapon = (cmd.buttons and BT_WEAPONMASK) shr BT_WEAPONSHIFT

        if (newweapon == WP_FIST
            && player.weaponowned[WP_CHAINSAW]
            && !(player.readyweapon == WP_CHAINSAW
                && player.powers[PW_STRENGTH] != 0)) {
            newweapon = WP_CHAINSAW
        }

        if ((gamemode == COMMERCIAL)
            && newweapon == WP_SHOTGUN
            && player.weaponowned[WP_SUPERSHOTGUN]
            && player.readyweapon != WP_SUPERSHOTGUN) {
            newweapon = WP_SUPERSHOTGUN
        }

        if (player.weaponowned[newweapon]
            && newweapon != player.readyweapon) {
            if ((newweapon != WP_PLASMA
                    && newweapon != WP_BFG)
                || (gamemode != SHAREWARE)) {
                player.pendingweapon = newweapon
            }
        }
    }

    if ((cmd.buttons and BT_USE) != 0) {
        if (!player.usedown) {
            pUseLines(player)
            player.usedown = true
        }
    } else
        player.usedown = false

    pMovePsprites(player)


    if (player.powers[PW_STRENGTH] != 0)
        player.powers[PW_STRENGTH]++

    if (player.powers[PW_INVULNERABILITY] != 0)
        player.powers[PW_INVULNERABILITY]--

    if (player.powers[PW_INVISIBILITY] != 0) {
        player.powers[PW_INVISIBILITY]--
        if (player.powers[PW_INVISIBILITY] == 0)
            player.mo!!.flags = player.mo!!.flags and MF_SHADOW.inv()
    }

    if (player.powers[PW_INFRARED] != 0)
        player.powers[PW_INFRARED]--

    if (player.powers[PW_IRONFEET] != 0)
        player.powers[PW_IRONFEET]--

    if (player.damagecount != 0)
        player.damagecount--

    if (player.bonuscount != 0)
        player.bonuscount--

    if (player.powers[PW_INVULNERABILITY] != 0) {
        if (player.powers[PW_INVULNERABILITY] > 4 * 32
            || (player.powers[PW_INVULNERABILITY] and 8) != 0)
            player.fixedcolormap = INVERSECOLORMAP
        else
            player.fixedcolormap = 0
    } else if (player.powers[PW_INFRARED] != 0) {
        if (player.powers[PW_INFRARED] > 4 * 32
            || (player.powers[PW_INFRARED] and 8) != 0) {
            player.fixedcolormap = 1
        } else
            player.fixedcolormap = 0
    } else
        player.fixedcolormap = 0
}
