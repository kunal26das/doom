
package doom.engine.statusbar

import doom.engine.KEY_ENTER
import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.TICRATE
import doom.engine.audio.sChangeMusic
import doom.engine.audio.MUS_E1M1
import doom.engine.audio.MUS_RUNNIN
import doom.engine.automap.AM_MSGENTERED
import doom.engine.automap.AM_MSGEXITED
import doom.engine.automap.AM_MSGHEADER
import doom.engine.automap.automapactive
import doom.engine.cheats.chtCheckCheat
import doom.engine.cheats.chtGetParam
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.gDeferedInitNew
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.NUMAMMO
import doom.engine.gameplay.NUMCARDS
import doom.engine.gameplay.NUMWEAPONS
import doom.engine.gameplay.AM_NOAMMO
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.deathmatch
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.gameskill
import doom.engine.gameplay.interactions.pGivePower
import doom.engine.gameplay.netgame
import doom.engine.gameplay.player.CF_GODMODE
import doom.engine.gameplay.player.CF_NOCLIP
import doom.engine.gameplay.player.Player
import doom.engine.gameplay.players
import doom.engine.gameplay.PW_INVULNERABILITY
import doom.engine.gameplay.PW_IRONFEET
import doom.engine.gameplay.PW_STRENGTH
import doom.engine.gameplay.REGISTERED
import doom.engine.gameplay.RETAIL
import doom.engine.gameplay.SHAREWARE
import doom.engine.gameplay.weapons.weaponinfo
import doom.engine.gameplay.WP_CHAINSAW
import doom.engine.geometry.ANG180
import doom.engine.geometry.ANG45
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FixedGeometry
import doom.engine.input.EngineEvent
import doom.engine.input.EV_KEYDOWN
import doom.engine.input.EV_KEYUP
import doom.engine.rendering.iSetPalette
import doom.engine.rendering.vCopyRect
import doom.engine.rendering.vDrawPatch
import doom.engine.resources.STSTR_BEHOLD
import doom.engine.resources.STSTR_BEHOLDX
import doom.engine.resources.STSTR_CHOPPERS
import doom.engine.resources.STSTR_CLEV
import doom.engine.resources.STSTR_DQDOFF
import doom.engine.resources.STSTR_DQDON
import doom.engine.resources.STSTR_FAADDED
import doom.engine.resources.STSTR_KFAADDED
import doom.engine.resources.STSTR_MUS
import doom.engine.resources.STSTR_NCOFF
import doom.engine.resources.STSTR_NCON
import doom.engine.resources.STSTR_NOMUS
import doom.engine.resources.wCacheLumpName
import doom.engine.resources.wCacheLumpNum
import doom.engine.resources.wGetNumForName
import doom.engine.simulation.mRandom


internal const val ST_HEIGHT = 32
internal const val ST_WIDTH = SCREENWIDTH
internal const val ST_Y = SCREENHEIGHT - ST_HEIGHT

internal const val AUTOMAP_STATE = 0
internal const val FIRST_PERSON_STATE = 1

internal const val START_CHAT_STATE = 0
internal const val WAIT_DEST_STATE = 1
internal const val GET_CHAT_STATE = 2

internal const val STARTREDPALS = 1
internal const val STARTBONUSPALS = 9
internal const val NUMREDPALS = 8
internal const val NUMBONUSPALS = 4
internal const val RADIATIONPAL = 13

internal const val ST_FACEPROBABILITY = 96

internal const val ST_TOGGLECHAT = KEY_ENTER

internal const val ST_X = 0
internal const val ST_X2 = 104

internal const val ST_FX = 143
internal const val ST_FY = 169


internal const val ST_NUMPAINFACES = 5
internal const val ST_NUMSTRAIGHTFACES = 3
internal const val ST_NUMTURNFACES = 2
internal const val ST_NUMSPECIALFACES = 3

internal const val ST_FACESTRIDE =
    ST_NUMSTRAIGHTFACES + ST_NUMTURNFACES + ST_NUMSPECIALFACES

internal const val ST_NUMEXTRAFACES = 2

internal const val ST_NUMFACES =
    ST_FACESTRIDE * ST_NUMPAINFACES + ST_NUMEXTRAFACES

internal const val ST_TURNOFFSET = ST_NUMSTRAIGHTFACES
internal const val ST_OUCHOFFSET = ST_TURNOFFSET + ST_NUMTURNFACES
internal const val ST_EVILGRINOFFSET = ST_OUCHOFFSET + 1
internal const val ST_RAMPAGEOFFSET = ST_EVILGRINOFFSET + 1
internal const val ST_GODFACE = ST_NUMPAINFACES * ST_FACESTRIDE
internal const val ST_DEADFACE = ST_GODFACE + 1

internal const val ST_FACESX = 143
internal const val ST_FACESY = 168

internal const val ST_EVILGRINCOUNT = 2 * TICRATE
internal const val ST_STRAIGHTFACECOUNT = TICRATE / 2
internal const val ST_TURNCOUNT = 1 * TICRATE
internal const val ST_OUCHCOUNT = 1 * TICRATE
internal const val ST_RAMPAGEDELAY = 2 * TICRATE

internal const val ST_MUCHPAIN = 20


internal const val ST_AMMOWIDTH = 3
internal const val ST_AMMOX = 44
internal const val ST_AMMOY = 171

internal const val ST_HEALTHWIDTH = 3
internal const val ST_HEALTHX = 90
internal const val ST_HEALTHY = 171

internal const val ST_ARMSX = 111
internal const val ST_ARMSY = 172
internal const val ST_ARMSBGX = 104
internal const val ST_ARMSBGY = 168
internal const val ST_ARMSXSPACE = 12
internal const val ST_ARMSYSPACE = 10

internal const val ST_FRAGSX = 138
internal const val ST_FRAGSY = 171
internal const val ST_FRAGSWIDTH = 2

internal const val ST_ARMORWIDTH = 3
internal const val ST_ARMORX = 221
internal const val ST_ARMORY = 171

internal const val ST_KEY0WIDTH = 8
internal const val ST_KEY0HEIGHT = 5
internal const val ST_KEY0X = 239
internal const val ST_KEY0Y = 171
internal const val ST_KEY1WIDTH = ST_KEY0WIDTH
internal const val ST_KEY1X = 239
internal const val ST_KEY1Y = 181
internal const val ST_KEY2WIDTH = ST_KEY0WIDTH
internal const val ST_KEY2X = 239
internal const val ST_KEY2Y = 191

internal const val ST_AMMO0WIDTH = 3
internal const val ST_AMMO0HEIGHT = 6
internal const val ST_AMMO0X = 288
internal const val ST_AMMO0Y = 173
internal const val ST_AMMO1WIDTH = ST_AMMO0WIDTH
internal const val ST_AMMO1X = 288
internal const val ST_AMMO1Y = 179
internal const val ST_AMMO2WIDTH = ST_AMMO0WIDTH
internal const val ST_AMMO2X = 288
internal const val ST_AMMO2Y = 191
internal const val ST_AMMO3WIDTH = ST_AMMO0WIDTH
internal const val ST_AMMO3X = 288
internal const val ST_AMMO3Y = 185

internal const val ST_MAXAMMO0WIDTH = 3
internal const val ST_MAXAMMO0HEIGHT = 5
internal const val ST_MAXAMMO0X = 314
internal const val ST_MAXAMMO0Y = 173
internal const val ST_MAXAMMO1WIDTH = ST_MAXAMMO0WIDTH
internal const val ST_MAXAMMO1X = 314
internal const val ST_MAXAMMO1Y = 179
internal const val ST_MAXAMMO2WIDTH = ST_MAXAMMO0WIDTH
internal const val ST_MAXAMMO2X = 314
internal const val ST_MAXAMMO2Y = 191
internal const val ST_MAXAMMO3WIDTH = ST_MAXAMMO0WIDTH
internal const val ST_MAXAMMO3X = 314
internal const val ST_MAXAMMO3Y = 185

internal const val ST_WEAPON0X = 110
internal const val ST_WEAPON0Y = 172

internal const val ST_WEAPON1X = 122
internal const val ST_WEAPON1Y = 172

internal const val ST_WEAPON2X = 134
internal const val ST_WEAPON2Y = 172

internal const val ST_WEAPON3X = 110
internal const val ST_WEAPON3Y = 181

internal const val ST_WEAPON4X = 122
internal const val ST_WEAPON4Y = 181

internal const val ST_WEAPON5X = 134
internal const val ST_WEAPON5Y = 181

internal const val ST_WPNSX = 109
internal const val ST_WPNSY = 191

internal const val ST_DETHX = 109
internal const val ST_DETHY = 191

internal const val ST_MSGTEXTX = 0
internal const val ST_MSGTEXTY = 0
internal const val ST_MSGWIDTH = 52
internal const val ST_MSGHEIGHT = 1

internal const val ST_OUTTEXTX = 0
internal const val ST_OUTTEXTY = 6

internal const val ST_OUTWIDTH = 52
internal const val ST_OUTHEIGHT = 1


internal const val ST_MAPTITLEY = 0
internal const val ST_MAPHEIGHT = 1

private var DoomEngineCore.plyr: Player
    get() = stateStatusBar.plyr
    set(value) { stateStatusBar.plyr = value }

private var DoomEngineCore.stFirsttime
    get() = stateStatusBar.stFirsttime
    set(value) { stateStatusBar.stFirsttime = value }

private var DoomEngineCore.veryfirsttime
    get() = stateStatusBar.veryfirsttime
    set(value) { stateStatusBar.veryfirsttime = value }

private var DoomEngineCore.luPalette
    get() = stateStatusBar.luPalette
    set(value) { stateStatusBar.luPalette = value }

private var DoomEngineCore.stClock
    get() = stateStatusBar.stClock
    set(value) { stateStatusBar.stClock = value }

private var DoomEngineCore.stMsgcounter
    get() = stateStatusBar.stMsgcounter
    set(value) { stateStatusBar.stMsgcounter = value }

private var DoomEngineCore.stChatstate
    get() = stateStatusBar.stChatstate
    set(value) { stateStatusBar.stChatstate = value }

private var DoomEngineCore.stGamestate
    get() = stateStatusBar.stGamestate
    set(value) { stateStatusBar.stGamestate = value }

private var DoomEngineCore.stStatusbaron
    get() = stateStatusBar.stStatusbaron
    set(value) { stateStatusBar.stStatusbaron = value }

private var DoomEngineCore.stChat
    get() = stateStatusBar.stChat
    set(value) { stateStatusBar.stChat = value }

private var DoomEngineCore.stOldchat
    get() = stateStatusBar.stOldchat
    set(value) { stateStatusBar.stOldchat = value }

private var DoomEngineCore.stCursoron
    get() = stateStatusBar.stCursoron
    set(value) { stateStatusBar.stCursoron = value }

private var DoomEngineCore.stNotdeathmatch
    get() = stateStatusBar.stNotdeathmatch
    set(value) { stateStatusBar.stNotdeathmatch = value }

private var DoomEngineCore.stArmson
    get() = stateStatusBar.stArmson
    set(value) { stateStatusBar.stArmson = value }

private var DoomEngineCore.stFragson
    get() = stateStatusBar.stFragson
    set(value) { stateStatusBar.stFragson = value }

private var DoomEngineCore.sbar: ByteArray
    get() = stateStatusBar.sbar
    set(value) { stateStatusBar.sbar = value }

private val DoomEngineCore.tallnum
    get() = stateStatusBar.tallnum

private var DoomEngineCore.tallpercent: ByteArray
    get() = stateStatusBar.tallpercent
    set(value) { stateStatusBar.tallpercent = value }

private val DoomEngineCore.shortnum
    get() = stateStatusBar.shortnum

private val DoomEngineCore.keys
    get() = stateStatusBar.keys

private val DoomEngineCore.faces
    get() = stateStatusBar.faces

private var DoomEngineCore.faceback: ByteArray
    get() = stateStatusBar.faceback
    set(value) { stateStatusBar.faceback = value }

private var DoomEngineCore.armsbg: ByteArray
    get() = stateStatusBar.armsbg
    set(value) { stateStatusBar.armsbg = value }

private val DoomEngineCore.arms
    get() = stateStatusBar.arms

private val DoomEngineCore.wReady
    get() = stateStatusBar.wReady

private val DoomEngineCore.wFrags
    get() = stateStatusBar.wFrags

private val DoomEngineCore.wHealth
    get() = stateStatusBar.wHealth

private val DoomEngineCore.wArmsbg
    get() = stateStatusBar.wArmsbg

private val DoomEngineCore.wArms
    get() = stateStatusBar.wArms

private val DoomEngineCore.wFaces
    get() = stateStatusBar.wFaces

private val DoomEngineCore.wKeyboxes
    get() = stateStatusBar.wKeyboxes

private val DoomEngineCore.wArmor
    get() = stateStatusBar.wArmor

private val DoomEngineCore.wAmmo
    get() = stateStatusBar.wAmmo

private val DoomEngineCore.wMaxammo
    get() = stateStatusBar.wMaxammo

private var DoomEngineCore.stFragscount
    get() = stateStatusBar.stFragscount
    set(value) { stateStatusBar.stFragscount = value }

private var DoomEngineCore.stOldhealth
    get() = stateStatusBar.stOldhealth
    set(value) { stateStatusBar.stOldhealth = value }

private val DoomEngineCore.oldweaponsowned
    get() = stateStatusBar.oldweaponsowned

private var DoomEngineCore.stFacecount
    get() = stateStatusBar.stFacecount
    set(value) { stateStatusBar.stFacecount = value }

private var DoomEngineCore.stFaceindex
    get() = stateStatusBar.stFaceindex
    set(value) { stateStatusBar.stFaceindex = value }

private val DoomEngineCore.keyboxes
    get() = stateStatusBar.keyboxes

private var DoomEngineCore.stRandomnumber
    get() = stateStatusBar.stRandomnumber
    set(value) { stateStatusBar.stRandomnumber = value }

internal val DoomEngineCore.cheatMusSeq
    get() = stateStatusBar.cheatMusSeq

internal val DoomEngineCore.cheatChoppersSeq
    get() = stateStatusBar.cheatChoppersSeq

internal val DoomEngineCore.cheatGodSeq
    get() = stateStatusBar.cheatGodSeq

internal val DoomEngineCore.cheatAmmoSeq
    get() = stateStatusBar.cheatAmmoSeq

internal val DoomEngineCore.cheatAmmonokeySeq
    get() = stateStatusBar.cheatAmmonokeySeq

internal val DoomEngineCore.cheatNoclipSeq
    get() = stateStatusBar.cheatNoclipSeq

internal val DoomEngineCore.cheatCommercialNoclipSeq
    get() = stateStatusBar.cheatCommercialNoclipSeq

internal val DoomEngineCore.cheatPowerupSeq
    get() = stateStatusBar.cheatPowerupSeq

internal val DoomEngineCore.cheatClevSeq
    get() = stateStatusBar.cheatClevSeq

internal val DoomEngineCore.cheatMyposSeq
    get() = stateStatusBar.cheatMyposSeq

internal val DoomEngineCore.cheatMus
    get() = stateStatusBar.cheatMus
internal val DoomEngineCore.cheatGod
    get() = stateStatusBar.cheatGod
internal val DoomEngineCore.cheatAmmo
    get() = stateStatusBar.cheatAmmo
internal val DoomEngineCore.cheatAmmonokey
    get() = stateStatusBar.cheatAmmonokey
internal val DoomEngineCore.cheatNoclip
    get() = stateStatusBar.cheatNoclip
internal val DoomEngineCore.cheatCommercialNoclip
    get() = stateStatusBar.cheatCommercialNoclip

internal val DoomEngineCore.cheatPowerup
    get() = stateStatusBar.cheatPowerup

internal val DoomEngineCore.cheatChoppers
    get() = stateStatusBar.cheatChoppers
internal val DoomEngineCore.cheatClev
    get() = stateStatusBar.cheatClev
internal val DoomEngineCore.cheatMypos
    get() = stateStatusBar.cheatMypos



internal fun DoomEngineCore.stRefreshBackground() {

    if (stStatusbaron) {
        vDrawPatch(ST_X, 0, BG, sbar)

        if (netgame)
            vDrawPatch(ST_FX, 0, BG, faceback)

        vCopyRect(ST_X, 0, BG, ST_WIDTH, ST_HEIGHT, ST_X, ST_Y, FG)
    }

}

internal fun DoomEngineCore.stResponder(ev: EngineEvent): Boolean {

    if (ev.type == EV_KEYUP
        && (ev.data1 and 0xffff0000.toInt()) == AM_MSGHEADER
    ) {
        when (ev.data1) {
            AM_MSGENTERED -> {
                stGamestate = AUTOMAP_STATE
                stFirsttime = true
            }

            AM_MSGEXITED -> {
                stGamestate = FIRST_PERSON_STATE
            }
        }
    }

    else if (ev.type == EV_KEYDOWN) {
        if (!netgame) {

            if (chtCheckCheat(cheatGod, ev.data1) != 0) {
                plyr.cheats = plyr.cheats xor CF_GODMODE
                if ((plyr.cheats and CF_GODMODE) != 0) {
                    if (plyr.mo != null)
                        plyr.mo!!.health = 100

                    plyr.health = 100
                    plyr.message = STSTR_DQDON
                } else
                    plyr.message = STSTR_DQDOFF
            }
            else if (chtCheckCheat(cheatAmmonokey, ev.data1) != 0) {
                plyr.armorpoints = 200
                plyr.armortype = 2

                for (i in 0 until NUMWEAPONS)
                    plyr.weaponowned[i] = true

                for (i in 0 until NUMAMMO)
                    plyr.ammo[i] = plyr.maxammo[i]

                plyr.message = STSTR_FAADDED
            }
            else if (chtCheckCheat(cheatAmmo, ev.data1) != 0) {
                plyr.armorpoints = 200
                plyr.armortype = 2

                for (i in 0 until NUMWEAPONS)
                    plyr.weaponowned[i] = true

                for (i in 0 until NUMAMMO)
                    plyr.ammo[i] = plyr.maxammo[i]

                for (i in 0 until NUMCARDS)
                    plyr.cards[i] = true

                plyr.message = STSTR_KFAADDED
            }
            else if (chtCheckCheat(cheatMus, ev.data1) != 0) {

                val buf = CharArray(3)
                val musnum: Int

                plyr.message = STSTR_MUS
                chtGetParam(cheatMus, buf)

                if (gamemode == COMMERCIAL) {
                    musnum = MUS_RUNNIN + (buf[0] - '0') * 10 + (buf[1] - '0') - 1

                    if ((buf[0] - '0') * 10 + (buf[1] - '0') > 35)
                        plyr.message = STSTR_NOMUS
                    else
                        sChangeMusic(musnum, 1)
                } else {
                    musnum = MUS_E1M1 + (buf[0] - '1') * 9 + (buf[1] - '1')

                    if ((buf[0] - '1') * 9 + (buf[1] - '1') > 31)
                        plyr.message = STSTR_NOMUS
                    else
                        sChangeMusic(musnum, 1)
                }
            }
            else if (chtCheckCheat(cheatNoclip, ev.data1) != 0
                || chtCheckCheat(cheatCommercialNoclip, ev.data1) != 0
            ) {
                plyr.cheats = plyr.cheats xor CF_NOCLIP

                if ((plyr.cheats and CF_NOCLIP) != 0)
                    plyr.message = STSTR_NCON
                else
                    plyr.message = STSTR_NCOFF
            }
            for (i in 0 until 6) {
                if (chtCheckCheat(cheatPowerup[i], ev.data1) != 0) {
                    if (plyr.powers[i] == 0)
                        pGivePower(plyr, i)
                    else if (i != PW_STRENGTH)
                        plyr.powers[i] = 1
                    else
                        plyr.powers[i] = 0

                    plyr.message = STSTR_BEHOLDX
                }
            }

            if (chtCheckCheat(cheatPowerup[6], ev.data1) != 0) {
                plyr.message = STSTR_BEHOLD
            }
            else if (chtCheckCheat(cheatChoppers, ev.data1) != 0) {
                plyr.weaponowned[WP_CHAINSAW] = true
                plyr.powers[PW_INVULNERABILITY] = 1
                plyr.message = STSTR_CHOPPERS
            }
            else if (chtCheckCheat(cheatMypos, ev.data1) != 0) {
                val buf = "ang=0x${players[consoleplayer].mo!!.angle.toString(16)};" +
                    "x,y=(0x${players[consoleplayer].mo!!.x.toUInt().toString(16)}," +
                    "0x${players[consoleplayer].mo!!.y.toUInt().toString(16)})"
                plyr.message = buf
            }
        }

        if (chtCheckCheat(cheatClev, ev.data1) != 0) {
            val buf = CharArray(3)
            val epsd: Int
            val map: Int

            chtGetParam(cheatClev, buf)

            if (gamemode == COMMERCIAL) {
                epsd = 0
                map = (buf[0] - '0') * 10 + (buf[1] - '0')
            } else {
                epsd = buf[0] - '0'
                map = buf[1] - '0'
            }

            if (epsd < 1)
                return false

            if (map < 1)
                return false

            if (gamemode == RETAIL
                && (epsd > 4 || map > 9)
            )
                return false

            if (gamemode == REGISTERED
                && (epsd > 3 || map > 9)
            )
                return false

            if (gamemode == SHAREWARE
                && (epsd > 1 || map > 9)
            )
                return false

            if (gamemode == COMMERCIAL
                && (epsd > 1 || map > 34)
            )
                return false

            plyr.message = STSTR_CLEV
            gDeferedInitNew(gameskill, epsd, map)
        }
    }
    return false
}

private var DoomEngineCore.lastcalc
    get() = stateStatusBar.lastcalc
    set(value) { stateStatusBar.lastcalc = value }
private var DoomEngineCore.oldhealth
    get() = stateStatusBar.oldhealth
    set(value) { stateStatusBar.oldhealth = value }

internal fun DoomEngineCore.stCalcPainOffset(): Int {
    val health: Int

    health = if (plyr.health > 100) 100 else plyr.health

    if (health != oldhealth) {
        lastcalc = ST_FACESTRIDE * (((100 - health) * ST_NUMPAINFACES) / 101)
        oldhealth = health
    }
    return lastcalc
}


private var DoomEngineCore.lastattackdown
    get() = stateStatusBar.lastattackdown
    set(value) { stateStatusBar.lastattackdown = value }
private var DoomEngineCore.priority
    get() = stateStatusBar.priority
    set(value) { stateStatusBar.priority = value }

internal fun DoomEngineCore.stUpdateFaceWidget() {

    if (priority < 10) {
        if (plyr.health == 0) {
            priority = 9
            stFaceindex = ST_DEADFACE
            stFacecount = 1
        }
    }

    if (priority < 9) {
        if (plyr.bonuscount != 0) {
            var doevilgrin = false

            for (i in 0 until NUMWEAPONS) {
                if (oldweaponsowned[i] != plyr.weaponowned[i]) {
                    doevilgrin = true
                    oldweaponsowned[i] = plyr.weaponowned[i]
                }
            }
            if (doevilgrin) {
                priority = 8
                stFacecount = ST_EVILGRINCOUNT
                stFaceindex = stCalcPainOffset() + ST_EVILGRINOFFSET
            }
        }

    }

    if (priority < 8) {
        if (plyr.damagecount != 0
            && plyr.attacker != null
            && plyr.attacker !== plyr.mo
        ) {
            priority = 7

            if (plyr.health - stOldhealth > ST_MUCHPAIN) {
                stFacecount = ST_TURNCOUNT
                stFaceindex = stCalcPainOffset() + ST_OUCHOFFSET
            } else {
                val badguyangle: BinaryAngle = FixedGeometry.angleBetween(
                    plyr.mo!!.x,
                    plyr.mo!!.y,
                    plyr.attacker!!.x,
                    plyr.attacker!!.y
                )
                val diffang: BinaryAngle
                val i: Int

                if (badguyangle > plyr.mo!!.angle) {
                    diffang = badguyangle - plyr.mo!!.angle
                    i = if (diffang > ANG180) 1 else 0
                } else {
                    diffang = plyr.mo!!.angle - badguyangle
                    i = if (diffang <= ANG180) 1 else 0
                }

                stFacecount = ST_TURNCOUNT
                stFaceindex = stCalcPainOffset()

                if (diffang < ANG45) {
                    stFaceindex += ST_RAMPAGEOFFSET
                } else if (i != 0) {
                    stFaceindex += ST_TURNOFFSET
                } else {
                    stFaceindex += ST_TURNOFFSET + 1
                }
            }
        }
    }

    if (priority < 7) {
        if (plyr.damagecount != 0) {
            if (plyr.health - stOldhealth > ST_MUCHPAIN) {
                priority = 7
                stFacecount = ST_TURNCOUNT
                stFaceindex = stCalcPainOffset() + ST_OUCHOFFSET
            } else {
                priority = 6
                stFacecount = ST_TURNCOUNT
                stFaceindex = stCalcPainOffset() + ST_RAMPAGEOFFSET
            }

        }

    }

    if (priority < 6) {
        if (plyr.attackdown) {
            if (lastattackdown == -1)
                lastattackdown = ST_RAMPAGEDELAY
            else if (--lastattackdown == 0) {
                priority = 5
                stFaceindex = stCalcPainOffset() + ST_RAMPAGEOFFSET
                stFacecount = 1
                lastattackdown = 1
            }
        } else
            lastattackdown = -1

    }

    if (priority < 5) {
        if ((plyr.cheats and CF_GODMODE) != 0
            || plyr.powers[PW_INVULNERABILITY] != 0
        ) {
            priority = 4

            stFaceindex = ST_GODFACE
            stFacecount = 1

        }

    }

    if (stFacecount == 0) {
        stFaceindex = stCalcPainOffset() + (stRandomnumber % 3)
        stFacecount = ST_STRAIGHTFACECOUNT
        priority = 0
    }

    stFacecount--

}

private var DoomEngineCore.largeammo
    get() = stateStatusBar.largeammo
    set(value) { stateStatusBar.largeammo = value }

internal fun DoomEngineCore.stUpdateWidgets() {

    if (weaponinfo[plyr.readyweapon].ammo == AM_NOAMMO)
        wReady.num = { largeammo }
    else {
        val ammo = weaponinfo[plyr.readyweapon].ammo
        wReady.num = { plyr.ammo[ammo] }
    }
    wReady.data = plyr.readyweapon


    for (i in 0 until 3) {
        keyboxes[i] = if (plyr.cards[i]) i else -1

        if (plyr.cards[i + 3])
            keyboxes[i] = i + 3
    }

    stUpdateFaceWidget()

    stNotdeathmatch = deathmatch == 0

    stArmson = stStatusbaron && deathmatch == 0

    stFragson = deathmatch != 0 && stStatusbaron
    stFragscount = 0

    for (i in 0 until MAXPLAYERS) {
        if (i != consoleplayer)
            stFragscount += plyr.frags[i]
        else
            stFragscount -= plyr.frags[i]
    }

    stMsgcounter--
    if (stMsgcounter == 0)
        stChat = stOldchat

}

internal fun DoomEngineCore.stTicker() {

    stClock++
    stRandomnumber = mRandom()
    stUpdateWidgets()
    stOldhealth = plyr.health

}

private var DoomEngineCore.stPalette
    get() = stateStatusBar.stPalette
    set(value) { stateStatusBar.stPalette = value }

internal fun DoomEngineCore.stDoPaletteStuff() {

    var palette: Int
    var cnt: Int
    val bzc: Int

    cnt = plyr.damagecount

    if (plyr.powers[PW_STRENGTH] != 0) {
        bzc = 12 - (plyr.powers[PW_STRENGTH] shr 6)

        if (bzc > cnt)
            cnt = bzc
    }

    if (cnt != 0) {
        palette = (cnt + 7) shr 3

        if (palette >= NUMREDPALS)
            palette = NUMREDPALS - 1

        palette += STARTREDPALS
    } else if (plyr.bonuscount != 0) {
        palette = (plyr.bonuscount + 7) shr 3

        if (palette >= NUMBONUSPALS)
            palette = NUMBONUSPALS - 1

        palette += STARTBONUSPALS
    } else if (plyr.powers[PW_IRONFEET] > 4 * 32
        || (plyr.powers[PW_IRONFEET] and 8) != 0
    )
        palette = RADIATIONPAL
    else
        palette = 0

    if (palette != stPalette) {
        stPalette = palette
        iSetPalette(wCacheLumpNum(luPalette), palette * 768)
    }

}

internal fun DoomEngineCore.stDrawWidgets(refresh: Boolean) {

    stArmson = stStatusbaron && deathmatch == 0

    stFragson = deathmatch != 0 && stStatusbaron

    stLibUpdateNum(wReady)

    for (i in 0 until 4) {
        stLibUpdateNum(wAmmo[i])
        stLibUpdateNum(wMaxammo[i])
    }

    stLibUpdatePercent(wHealth, refresh)
    stLibUpdatePercent(wArmor, refresh)

    stLibUpdateBinIcon(wArmsbg, refresh)

    for (i in 0 until 6)
        stLibUpdateMultIcon(wArms[i], refresh)

    stLibUpdateMultIcon(wFaces, refresh)

    for (i in 0 until 3)
        stLibUpdateMultIcon(wKeyboxes[i], refresh)

    stLibUpdateNum(wFrags)

}

internal fun DoomEngineCore.stDoRefresh() {

    stFirsttime = false

    stRefreshBackground()

    stDrawWidgets(true)

}

internal fun DoomEngineCore.stDiffDraw() {
    stDrawWidgets(false)
}

internal fun DoomEngineCore.stDrawer(fullscreen: Boolean, refresh: Boolean) {

    stStatusbaron = !fullscreen || automapactive
    stFirsttime = stFirsttime || refresh

    stDoPaletteStuff()

    if (stFirsttime) stDoRefresh()
    else stDiffDraw()

}

internal fun DoomEngineCore.stLoadGraphics() {

    var facenum: Int

    for (i in 0 until 10) {
        tallnum[i] = wCacheLumpName("STTNUM$i")

        shortnum[i] = wCacheLumpName("STYSNUM$i")
    }

    tallpercent = wCacheLumpName("STTPRCNT")

    for (i in 0 until NUMCARDS) {
        keys[i] = wCacheLumpName("STKEYS$i")
    }

    armsbg = wCacheLumpName("STARMS")

    for (i in 0 until 6) {
        arms[i][0] = wCacheLumpName("STGNUM${i + 2}")

        arms[i][1] = shortnum[i + 2]
    }

    faceback = wCacheLumpName("STFB$consoleplayer")

    sbar = wCacheLumpName("STBAR")

    facenum = 0
    for (i in 0 until ST_NUMPAINFACES) {
        for (j in 0 until ST_NUMSTRAIGHTFACES) {
            faces[facenum++] = wCacheLumpName("STFST$i$j")
        }
        faces[facenum++] = wCacheLumpName("STFTR${i}0")
        faces[facenum++] = wCacheLumpName("STFTL${i}0")
        faces[facenum++] = wCacheLumpName("STFOUCH$i")
        faces[facenum++] = wCacheLumpName("STFEVL$i")
        faces[facenum++] = wCacheLumpName("STFKILL$i")
    }
    faces[facenum++] = wCacheLumpName("STFGOD0")
    faces[facenum] = wCacheLumpName("STFDEAD0")

}

internal fun DoomEngineCore.stLoadData() {
    luPalette = wGetNumForName("PLAYPAL")
    stLoadGraphics()
}

internal fun DoomEngineCore.stUnloadGraphics() {



}

internal fun DoomEngineCore.stUnloadData() {
    stUnloadGraphics()
}

internal fun DoomEngineCore.stInitData() {

    stFirsttime = true
    plyr = players[consoleplayer]

    stClock = 0
    stChatstate = START_CHAT_STATE
    stGamestate = FIRST_PERSON_STATE

    stStatusbaron = true
    stChat = false; stOldchat = stChat
    stCursoron = false

    stFaceindex = 0
    stPalette = -1

    stOldhealth = -1

    for (i in 0 until NUMWEAPONS)
        oldweaponsowned[i] = plyr.weaponowned[i]

    for (i in 0 until 3)
        keyboxes[i] = -1

    stLibInit()

}

internal fun DoomEngineCore.stCreateWidgets() {

    val readyammo = weaponinfo[plyr.readyweapon].ammo
    stLibInitNum(
        wReady,
        ST_AMMOX,
        ST_AMMOY,
        tallnum,
        { plyr.ammo[readyammo] },
        { stStatusbaron },
        ST_AMMOWIDTH
    )

    wReady.data = plyr.readyweapon

    stLibInitPercent(
        wHealth,
        ST_HEALTHX,
        ST_HEALTHY,
        tallnum,
        { plyr.health },
        { stStatusbaron },
        tallpercent
    )

    stLibInitBinIcon(
        wArmsbg,
        ST_ARMSBGX,
        ST_ARMSBGY,
        armsbg,
        { stNotdeathmatch },
        { stStatusbaron }
    )

    for (i in 0 until 6) {
        stLibInitMultIcon(
            wArms[i],
            ST_ARMSX + (i % 3) * ST_ARMSXSPACE,
            ST_ARMSY + (i / 3) * ST_ARMSYSPACE,
            arms[i],
            { if (plyr.weaponowned[i + 1]) 1 else 0 },
            { stArmson }
        )
    }

    stLibInitNum(
        wFrags,
        ST_FRAGSX,
        ST_FRAGSY,
        tallnum,
        { stFragscount },
        { stFragson },
        ST_FRAGSWIDTH
    )

    stLibInitMultIcon(
        wFaces,
        ST_FACESX,
        ST_FACESY,
        faces,
        { stFaceindex },
        { stStatusbaron }
    )

    stLibInitPercent(
        wArmor,
        ST_ARMORX,
        ST_ARMORY,
        tallnum,
        { plyr.armorpoints },
        { stStatusbaron }, tallpercent
    )

    stLibInitMultIcon(
        wKeyboxes[0],
        ST_KEY0X,
        ST_KEY0Y,
        keys,
        { keyboxes[0] },
        { stStatusbaron }
    )

    stLibInitMultIcon(
        wKeyboxes[1],
        ST_KEY1X,
        ST_KEY1Y,
        keys,
        { keyboxes[1] },
        { stStatusbaron }
    )

    stLibInitMultIcon(
        wKeyboxes[2],
        ST_KEY2X,
        ST_KEY2Y,
        keys,
        { keyboxes[2] },
        { stStatusbaron }
    )

    stLibInitNum(
        wAmmo[0],
        ST_AMMO0X,
        ST_AMMO0Y,
        shortnum,
        { plyr.ammo[0] },
        { stStatusbaron },
        ST_AMMO0WIDTH
    )

    stLibInitNum(
        wAmmo[1],
        ST_AMMO1X,
        ST_AMMO1Y,
        shortnum,
        { plyr.ammo[1] },
        { stStatusbaron },
        ST_AMMO1WIDTH
    )

    stLibInitNum(
        wAmmo[2],
        ST_AMMO2X,
        ST_AMMO2Y,
        shortnum,
        { plyr.ammo[2] },
        { stStatusbaron },
        ST_AMMO2WIDTH
    )

    stLibInitNum(
        wAmmo[3],
        ST_AMMO3X,
        ST_AMMO3Y,
        shortnum,
        { plyr.ammo[3] },
        { stStatusbaron },
        ST_AMMO3WIDTH
    )

    stLibInitNum(
        wMaxammo[0],
        ST_MAXAMMO0X,
        ST_MAXAMMO0Y,
        shortnum,
        { plyr.maxammo[0] },
        { stStatusbaron },
        ST_MAXAMMO0WIDTH
    )

    stLibInitNum(
        wMaxammo[1],
        ST_MAXAMMO1X,
        ST_MAXAMMO1Y,
        shortnum,
        { plyr.maxammo[1] },
        { stStatusbaron },
        ST_MAXAMMO1WIDTH
    )

    stLibInitNum(
        wMaxammo[2],
        ST_MAXAMMO2X,
        ST_MAXAMMO2Y,
        shortnum,
        { plyr.maxammo[2] },
        { stStatusbaron },
        ST_MAXAMMO2WIDTH
    )

    stLibInitNum(
        wMaxammo[3],
        ST_MAXAMMO3X,
        ST_MAXAMMO3Y,
        shortnum,
        { plyr.maxammo[3] },
        { stStatusbaron },
        ST_MAXAMMO3WIDTH
    )

}

private var DoomEngineCore.stStopped
    get() = stateStatusBar.stStopped
    set(value) { stateStatusBar.stStopped = value }

internal fun DoomEngineCore.stStart() {

    if (!stStopped)
        stStop()

    stInitData()
    stCreateWidgets()
    stStopped = false

}

internal fun DoomEngineCore.stStop() {
    if (stStopped)
        return

    iSetPalette(wCacheLumpNum(luPalette))

    stStopped = true
}

internal fun DoomEngineCore.stInit() {
    veryfirsttime = 0
    stLoadData()
}
