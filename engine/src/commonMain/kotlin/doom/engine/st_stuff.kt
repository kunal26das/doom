// Port of linuxdoom-1.10 st_stuff.c/st_stuff.h -- status bar code.
// Does the face/direction indicator animatin.
// Does palette indicators as well (red pain/berserk, bright pickup)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

import doom.engine.geometry.FixedGeometry

//
// STATUS BAR DATA
//

// Size of statusbar.
// Now sensitive for scaling. (st_stuff.h)
internal const val ST_HEIGHT = 32  // 32*SCREEN_MUL
internal const val ST_WIDTH = SCREENWIDTH
internal const val ST_Y = SCREENHEIGHT - ST_HEIGHT

// States for status bar code. (st_stateenum_t)
internal const val AutomapState = 0
internal const val FirstPersonState = 1

// States for the chat code. (st_chatstateenum_t)
internal const val StartChatState = 0
internal const val WaitDestState = 1
internal const val GetChatState = 2

// Palette indices.
// For damage/bonus red-/gold-shifts
internal const val STARTREDPALS = 1
internal const val STARTBONUSPALS = 9
internal const val NUMREDPALS = 8
internal const val NUMBONUSPALS = 4
// Radiation suit, green shift.
internal const val RADIATIONPAL = 13

// N/256*100% probability
//  that the normal face state will change
internal const val ST_FACEPROBABILITY = 96

// For Responder
internal const val ST_TOGGLECHAT = KEY_ENTER

// Location of status bar
internal const val ST_X = 0
internal const val ST_X2 = 104

internal const val ST_FX = 143
internal const val ST_FY = 169

// Should be set to patch width
//  for tall numbers later on
// #define ST_TALLNUMWIDTH (tallnum[0]->width)

// Number of status faces.
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

// Location and size of statistics,
//  justified according to widget type.
// Problem is, within which space? STbar? Screen?
// Note: this could be read in by a lump.
//       Problem is, is the stuff rendered
//       into a buffer,
//       or into the frame buffer?

// AMMO number pos.
internal const val ST_AMMOWIDTH = 3
internal const val ST_AMMOX = 44
internal const val ST_AMMOY = 171

// HEALTH number pos.
internal const val ST_HEALTHWIDTH = 3
internal const val ST_HEALTHX = 90
internal const val ST_HEALTHY = 171

// Weapon pos.
internal const val ST_ARMSX = 111
internal const val ST_ARMSY = 172
internal const val ST_ARMSBGX = 104
internal const val ST_ARMSBGY = 168
internal const val ST_ARMSXSPACE = 12
internal const val ST_ARMSYSPACE = 10

// Frags pos.
internal const val ST_FRAGSX = 138
internal const val ST_FRAGSY = 171
internal const val ST_FRAGSWIDTH = 2

// ARMOR number pos.
internal const val ST_ARMORWIDTH = 3
internal const val ST_ARMORX = 221
internal const val ST_ARMORY = 171

// Key icon positions.
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

// Ammunition counter.
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

// Indicate maximum ammunition.
// Only needed because backpack exists.
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

// pistol
internal const val ST_WEAPON0X = 110
internal const val ST_WEAPON0Y = 172

// shotgun
internal const val ST_WEAPON1X = 122
internal const val ST_WEAPON1Y = 172

// chain gun
internal const val ST_WEAPON2X = 134
internal const val ST_WEAPON2Y = 172

// missile launcher
internal const val ST_WEAPON3X = 110
internal const val ST_WEAPON3Y = 181

// plasma gun
internal const val ST_WEAPON4X = 122
internal const val ST_WEAPON4Y = 181

// bfg
internal const val ST_WEAPON5X = 134
internal const val ST_WEAPON5Y = 181

// WPNS title
internal const val ST_WPNSX = 109
internal const val ST_WPNSY = 191

// DETH title
internal const val ST_DETHX = 109
internal const val ST_DETHY = 191

//Incoming messages window location
//UNUSED
// #define ST_MSGTEXTX	   (viewwindowx)
// #define ST_MSGTEXTY	   (viewwindowy+viewheight-18)
internal const val ST_MSGTEXTX = 0
internal const val ST_MSGTEXTY = 0
// Dimensions given in characters.
internal const val ST_MSGWIDTH = 52
// Or shall I say, in lines?
internal const val ST_MSGHEIGHT = 1

internal const val ST_OUTTEXTX = 0
internal const val ST_OUTTEXTY = 6

// Width, in characters again.
internal const val ST_OUTWIDTH = 52
// Height, in lines.
internal const val ST_OUTHEIGHT = 1

// #define ST_MAPWIDTH (strlen(mapnames[(gameepisode-1)*9+(gamemap-1)]))
// #define ST_MAPTITLEX (SCREENWIDTH - ST_MAPWIDTH * ST_CHATFONTWIDTH)

internal const val ST_MAPTITLEY = 0
internal const val ST_MAPHEIGHT = 1


private var DoomEngineCore.plyr: player_t
    get() = stateStatusBar.plyr
    set(value) { stateStatusBar.plyr = value }

private var DoomEngineCore.st_firsttime
    get() = stateStatusBar.st_firsttime
    set(value) { stateStatusBar.st_firsttime = value }

private var DoomEngineCore.veryfirsttime
    get() = stateStatusBar.veryfirsttime
    set(value) { stateStatusBar.veryfirsttime = value }

private var DoomEngineCore.lu_palette
    get() = stateStatusBar.lu_palette
    set(value) { stateStatusBar.lu_palette = value }

private var DoomEngineCore.st_clock
    get() = stateStatusBar.st_clock
    set(value) { stateStatusBar.st_clock = value }

private var DoomEngineCore.st_msgcounter
    get() = stateStatusBar.st_msgcounter
    set(value) { stateStatusBar.st_msgcounter = value }

private var DoomEngineCore.st_chatstate
    get() = stateStatusBar.st_chatstate
    set(value) { stateStatusBar.st_chatstate = value }

private var DoomEngineCore.st_gamestate
    get() = stateStatusBar.st_gamestate
    set(value) { stateStatusBar.st_gamestate = value }

private var DoomEngineCore.st_statusbaron
    get() = stateStatusBar.st_statusbaron
    set(value) { stateStatusBar.st_statusbaron = value }

private var DoomEngineCore.st_chat
    get() = stateStatusBar.st_chat
    set(value) { stateStatusBar.st_chat = value }

private var DoomEngineCore.st_oldchat
    get() = stateStatusBar.st_oldchat
    set(value) { stateStatusBar.st_oldchat = value }

private var DoomEngineCore.st_cursoron
    get() = stateStatusBar.st_cursoron
    set(value) { stateStatusBar.st_cursoron = value }

private var DoomEngineCore.st_notdeathmatch
    get() = stateStatusBar.st_notdeathmatch
    set(value) { stateStatusBar.st_notdeathmatch = value }

private var DoomEngineCore.st_armson
    get() = stateStatusBar.st_armson
    set(value) { stateStatusBar.st_armson = value }

private var DoomEngineCore.st_fragson
    get() = stateStatusBar.st_fragson
    set(value) { stateStatusBar.st_fragson = value }

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

private val DoomEngineCore.w_ready
    get() = stateStatusBar.w_ready

private val DoomEngineCore.w_frags
    get() = stateStatusBar.w_frags

private val DoomEngineCore.w_health
    get() = stateStatusBar.w_health

private val DoomEngineCore.w_armsbg
    get() = stateStatusBar.w_armsbg


private val DoomEngineCore.w_arms
    get() = stateStatusBar.w_arms

private val DoomEngineCore.w_faces
    get() = stateStatusBar.w_faces

private val DoomEngineCore.w_keyboxes
    get() = stateStatusBar.w_keyboxes

private val DoomEngineCore.w_armor
    get() = stateStatusBar.w_armor

private val DoomEngineCore.w_ammo
    get() = stateStatusBar.w_ammo

private val DoomEngineCore.w_maxammo
    get() = stateStatusBar.w_maxammo


private var DoomEngineCore.st_fragscount
    get() = stateStatusBar.st_fragscount
    set(value) { stateStatusBar.st_fragscount = value }

private var DoomEngineCore.st_oldhealth
    get() = stateStatusBar.st_oldhealth
    set(value) { stateStatusBar.st_oldhealth = value }

private val DoomEngineCore.oldweaponsowned
    get() = stateStatusBar.oldweaponsowned

private var DoomEngineCore.st_facecount
    get() = stateStatusBar.st_facecount
    set(value) { stateStatusBar.st_facecount = value }

private var DoomEngineCore.st_faceindex
    get() = stateStatusBar.st_faceindex
    set(value) { stateStatusBar.st_faceindex = value }

private val DoomEngineCore.keyboxes
    get() = stateStatusBar.keyboxes

private var DoomEngineCore.st_randomnumber
    get() = stateStatusBar.st_randomnumber
    set(value) { stateStatusBar.st_randomnumber = value }


internal val DoomEngineCore.cheat_mus_seq
    get() = stateStatusBar.cheat_mus_seq

internal val DoomEngineCore.cheat_choppers_seq
    get() = stateStatusBar.cheat_choppers_seq

internal val DoomEngineCore.cheat_god_seq
    get() = stateStatusBar.cheat_god_seq

internal val DoomEngineCore.cheat_ammo_seq
    get() = stateStatusBar.cheat_ammo_seq

internal val DoomEngineCore.cheat_ammonokey_seq
    get() = stateStatusBar.cheat_ammonokey_seq


internal val DoomEngineCore.cheat_noclip_seq
    get() = stateStatusBar.cheat_noclip_seq

internal val DoomEngineCore.cheat_commercial_noclip_seq
    get() = stateStatusBar.cheat_commercial_noclip_seq


internal val DoomEngineCore.cheat_powerup_seq
    get() = stateStatusBar.cheat_powerup_seq


internal val DoomEngineCore.cheat_clev_seq
    get() = stateStatusBar.cheat_clev_seq


internal val DoomEngineCore.cheat_mypos_seq
    get() = stateStatusBar.cheat_mypos_seq


internal val DoomEngineCore.cheat_mus
    get() = stateStatusBar.cheat_mus
internal val DoomEngineCore.cheat_god
    get() = stateStatusBar.cheat_god
internal val DoomEngineCore.cheat_ammo
    get() = stateStatusBar.cheat_ammo
internal val DoomEngineCore.cheat_ammonokey
    get() = stateStatusBar.cheat_ammonokey
internal val DoomEngineCore.cheat_noclip
    get() = stateStatusBar.cheat_noclip
internal val DoomEngineCore.cheat_commercial_noclip
    get() = stateStatusBar.cheat_commercial_noclip

internal val DoomEngineCore.cheat_powerup
    get() = stateStatusBar.cheat_powerup

internal val DoomEngineCore.cheat_choppers
    get() = stateStatusBar.cheat_choppers
internal val DoomEngineCore.cheat_clev
    get() = stateStatusBar.cheat_clev
internal val DoomEngineCore.cheat_mypos
    get() = stateStatusBar.cheat_mypos


// (C: extern char* mapnames[]; -- in hu_stuff.c, only used by the unused
//  ST_MAPWIDTH macro above.)


//
// STATUS BAR CODE
//

internal fun DoomEngineCore.ST_refreshBackground() {

    if (st_statusbaron) {
        V_DrawPatch(ST_X, 0, BG, sbar)

        if (netgame)
            V_DrawPatch(ST_FX, 0, BG, faceback)

        V_CopyRect(ST_X, 0, BG, ST_WIDTH, ST_HEIGHT, ST_X, ST_Y, FG)
    }

}


// Respond to keyboard input events,
//  intercept cheats.
internal fun DoomEngineCore.ST_Responder(ev: event_t): Boolean {

    // Filter automap on/off.
    if (ev.type == ev_keyup
        && (ev.data1 and 0xffff0000.toInt()) == AM_MSGHEADER
    ) {
        when (ev.data1) {
            AM_MSGENTERED -> {
                st_gamestate = AutomapState
                st_firsttime = true
            }

            AM_MSGEXITED -> {
                //	fprintf(stderr, "AM exited\n");
                st_gamestate = FirstPersonState
            }
        }
    }

    // if a user keypress...
    else if (ev.type == ev_keydown) {
        if (!netgame) {
            // b. - enabled for more debug fun.
            // if (gameskill != sk_nightmare) {

            // 'dqd' cheat for toggleable god mode
            if (cht_CheckCheat(cheat_god, ev.data1) != 0) {
                plyr.cheats = plyr.cheats xor CF_GODMODE
                if ((plyr.cheats and CF_GODMODE) != 0) {
                    if (plyr.mo != null)
                        plyr.mo!!.health = 100

                    plyr.health = 100
                    plyr.message = STSTR_DQDON
                } else
                    plyr.message = STSTR_DQDOFF
            }
            // 'fa' cheat for killer fucking arsenal
            else if (cht_CheckCheat(cheat_ammonokey, ev.data1) != 0) {
                plyr.armorpoints = 200
                plyr.armortype = 2

                for (i in 0 until NUMWEAPONS)
                    plyr.weaponowned[i] = true

                for (i in 0 until NUMAMMO)
                    plyr.ammo[i] = plyr.maxammo[i]

                plyr.message = STSTR_FAADDED
            }
            // 'kfa' cheat for key full ammo
            else if (cht_CheckCheat(cheat_ammo, ev.data1) != 0) {
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
            // 'mus' cheat for changing music
            else if (cht_CheckCheat(cheat_mus, ev.data1) != 0) {

                val buf = CharArray(3)
                val musnum: Int

                plyr.message = STSTR_MUS
                cht_GetParam(cheat_mus, buf)

                if (gamemode == commercial) {
                    musnum = mus_runnin + (buf[0] - '0') * 10 + (buf[1] - '0') - 1

                    if ((buf[0] - '0') * 10 + (buf[1] - '0') > 35)
                        plyr.message = STSTR_NOMUS
                    else
                        S_ChangeMusic(musnum, 1)
                } else {
                    musnum = mus_e1m1 + (buf[0] - '1') * 9 + (buf[1] - '1')

                    if ((buf[0] - '1') * 9 + (buf[1] - '1') > 31)
                        plyr.message = STSTR_NOMUS
                    else
                        S_ChangeMusic(musnum, 1)
                }
            }
            // Simplified, accepting both "noclip" and "idspispopd".
            // no clipping mode cheat
            else if (cht_CheckCheat(cheat_noclip, ev.data1) != 0
                || cht_CheckCheat(cheat_commercial_noclip, ev.data1) != 0
            ) {
                plyr.cheats = plyr.cheats xor CF_NOCLIP

                if ((plyr.cheats and CF_NOCLIP) != 0)
                    plyr.message = STSTR_NCON
                else
                    plyr.message = STSTR_NCOFF
            }
            // 'behold?' power-up cheats
            for (i in 0 until 6) {
                if (cht_CheckCheat(cheat_powerup[i], ev.data1) != 0) {
                    if (plyr.powers[i] == 0)
                        P_GivePower(plyr, i)
                    else if (i != pw_strength)
                        plyr.powers[i] = 1
                    else
                        plyr.powers[i] = 0

                    plyr.message = STSTR_BEHOLDX
                }
            }

            // 'behold' power-up menu
            if (cht_CheckCheat(cheat_powerup[6], ev.data1) != 0) {
                plyr.message = STSTR_BEHOLD
            }
            // 'choppers' invulnerability & chainsaw
            else if (cht_CheckCheat(cheat_choppers, ev.data1) != 0) {
                plyr.weaponowned[wp_chainsaw] = true
                plyr.powers[pw_invulnerability] = 1  // (C: = true)
                plyr.message = STSTR_CHOPPERS
            }
            // 'mypos' for player position
            else if (cht_CheckCheat(cheat_mypos, ev.data1) != 0) {
                // (C: sprintf(buf, "ang=0x%x;x,y=(0x%x,0x%x)", ...))
                val buf = "ang=0x${players[consoleplayer].mo!!.angle.toString(16)};" +
                    "x,y=(0x${players[consoleplayer].mo!!.x.toUInt().toString(16)}," +
                    "0x${players[consoleplayer].mo!!.y.toUInt().toString(16)})"
                plyr.message = buf
            }
        }

        // 'clev' change-level cheat
        if (cht_CheckCheat(cheat_clev, ev.data1) != 0) {
            val buf = CharArray(3)
            val epsd: Int
            val map: Int

            cht_GetParam(cheat_clev, buf)

            if (gamemode == commercial) {
                epsd = 0
                map = (buf[0] - '0') * 10 + (buf[1] - '0')
            } else {
                epsd = buf[0] - '0'
                map = buf[1] - '0'
            }

            // Catch invalid maps.
            if (epsd < 1)
                return false

            if (map < 1)
                return false

            // Ohmygod - this is not going to work.
            if (gamemode == retail
                && (epsd > 4 || map > 9)
            )
                return false

            if (gamemode == registered
                && (epsd > 3 || map > 9)
            )
                return false

            if (gamemode == shareware
                && (epsd > 1 || map > 9)
            )
                return false

            if (gamemode == commercial
                && (epsd > 1 || map > 34)
            )
                return false

            // So be it.
            plyr.message = STSTR_CLEV
            G_DeferedInitNew(gameskill, epsd, map)
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

internal fun DoomEngineCore.ST_calcPainOffset(): Int {
    val health: Int

    health = if (plyr.health > 100) 100 else plyr.health

    if (health != oldhealth) {
        lastcalc = ST_FACESTRIDE * (((100 - health) * ST_NUMPAINFACES) / 101)
        oldhealth = health
    }
    return lastcalc
}


//
// This is a not-very-pretty routine which handles
//  the face states and their timing.
// the precedence of expressions is:
//  dead > evil grin > turned head > straight ahead
//

private var DoomEngineCore.lastattackdown
    get() = stateStatusBar.lastattackdown
    set(value) { stateStatusBar.lastattackdown = value }
private var DoomEngineCore.priority
    get() = stateStatusBar.priority
    set(value) { stateStatusBar.priority = value }

internal fun DoomEngineCore.ST_updateFaceWidget() {

    if (priority < 10) {
        // dead
        if (plyr.health == 0) {
            priority = 9
            st_faceindex = ST_DEADFACE
            st_facecount = 1
        }
    }

    if (priority < 9) {
        if (plyr.bonuscount != 0) {
            // picking up bonus
            var doevilgrin = false

            for (i in 0 until NUMWEAPONS) {
                if (oldweaponsowned[i] != plyr.weaponowned[i]) {
                    doevilgrin = true
                    oldweaponsowned[i] = plyr.weaponowned[i]
                }
            }
            if (doevilgrin) {
                // evil grin if just picked up weapon
                priority = 8
                st_facecount = ST_EVILGRINCOUNT
                st_faceindex = ST_calcPainOffset() + ST_EVILGRINOFFSET
            }
        }

    }

    if (priority < 8) {
        if (plyr.damagecount != 0
            && plyr.attacker != null
            && plyr.attacker !== plyr.mo
        ) {
            // being attacked
            priority = 7

            if (plyr.health - st_oldhealth > ST_MUCHPAIN) {
                st_facecount = ST_TURNCOUNT
                st_faceindex = ST_calcPainOffset() + ST_OUCHOFFSET
            } else {
                val badguyangle: angle_t = FixedGeometry.angleBetween(
                    plyr.mo!!.x,
                    plyr.mo!!.y,
                    plyr.attacker!!.x,
                    plyr.attacker!!.y
                )
                val diffang: angle_t
                val i: Int

                if (badguyangle > plyr.mo!!.angle) {
                    // whether right or left
                    diffang = badguyangle - plyr.mo!!.angle
                    i = if (diffang > ANG180) 1 else 0
                } else {
                    // whether left or right
                    diffang = plyr.mo!!.angle - badguyangle
                    i = if (diffang <= ANG180) 1 else 0
                } // confusing, aint it?


                st_facecount = ST_TURNCOUNT
                st_faceindex = ST_calcPainOffset()

                if (diffang < ANG45) {
                    // head-on
                    st_faceindex += ST_RAMPAGEOFFSET
                } else if (i != 0) {
                    // turn face right
                    st_faceindex += ST_TURNOFFSET
                } else {
                    // turn face left
                    st_faceindex += ST_TURNOFFSET + 1
                }
            }
        }
    }

    if (priority < 7) {
        // getting hurt because of your own damn stupidity
        if (plyr.damagecount != 0) {
            if (plyr.health - st_oldhealth > ST_MUCHPAIN) {
                priority = 7
                st_facecount = ST_TURNCOUNT
                st_faceindex = ST_calcPainOffset() + ST_OUCHOFFSET
            } else {
                priority = 6
                st_facecount = ST_TURNCOUNT
                st_faceindex = ST_calcPainOffset() + ST_RAMPAGEOFFSET
            }

        }

    }

    if (priority < 6) {
        // rapid firing
        if (plyr.attackdown) {
            if (lastattackdown == -1)
                lastattackdown = ST_RAMPAGEDELAY
            else if (--lastattackdown == 0) {
                priority = 5
                st_faceindex = ST_calcPainOffset() + ST_RAMPAGEOFFSET
                st_facecount = 1
                lastattackdown = 1
            }
        } else
            lastattackdown = -1

    }

    if (priority < 5) {
        // invulnerability
        if ((plyr.cheats and CF_GODMODE) != 0
            || plyr.powers[pw_invulnerability] != 0
        ) {
            priority = 4

            st_faceindex = ST_GODFACE
            st_facecount = 1

        }

    }

    // look left or look right if the facecount has timed out
    if (st_facecount == 0) {
        st_faceindex = ST_calcPainOffset() + (st_randomnumber % 3)
        st_facecount = ST_STRAIGHTFACECOUNT
        priority = 0
    }

    st_facecount--

}

private var DoomEngineCore.largeammo
    get() = stateStatusBar.largeammo
    set(value) { stateStatusBar.largeammo = value }

internal fun DoomEngineCore.ST_updateWidgets() {

    // must redirect the pointer if the ready weapon has changed.
    //  if (w_ready.data != plyr->readyweapon)
    //  {
    if (weaponinfo[plyr.readyweapon].ammo == am_noammo)
        w_ready.num = { largeammo }
    else {
        val ammo = weaponinfo[plyr.readyweapon].ammo
        w_ready.num = { plyr.ammo[ammo] }
    }
    //{
    // static int tic=0;
    // static int dir=-1;
    // if (!(tic&15))
    //   plyr->ammo[weaponinfo[plyr->readyweapon].ammo]+=dir;
    // if (plyr->ammo[weaponinfo[plyr->readyweapon].ammo] == -100)
    //   dir = 1;
    // tic++;
    // }
    w_ready.data = plyr.readyweapon

    // if (*w_ready.on)
    //  STlib_updateNum(&w_ready, true);
    // refresh weapon change
    //  }

    // update keycard multiple widgets
    for (i in 0 until 3) {
        keyboxes[i] = if (plyr.cards[i]) i else -1

        if (plyr.cards[i + 3])
            keyboxes[i] = i + 3
    }

    // refresh everything if this is him coming back to life
    ST_updateFaceWidget()

    // used by the w_armsbg widget
    st_notdeathmatch = deathmatch == 0

    // used by w_arms[] widgets
    st_armson = st_statusbaron && deathmatch == 0

    // used by w_frags widget
    st_fragson = deathmatch != 0 && st_statusbaron
    st_fragscount = 0

    for (i in 0 until MAXPLAYERS) {
        if (i != consoleplayer)
            st_fragscount += plyr.frags[i]
        else
            st_fragscount -= plyr.frags[i]
    }

    // get rid of chat window if up because of message
    st_msgcounter--
    if (st_msgcounter == 0)
        st_chat = st_oldchat

}

internal fun DoomEngineCore.ST_Ticker() {

    st_clock++
    st_randomnumber = M_Random()
    ST_updateWidgets()
    st_oldhealth = plyr.health

}

private var DoomEngineCore.st_palette
    get() = stateStatusBar.st_palette
    set(value) { stateStatusBar.st_palette = value }

internal fun DoomEngineCore.ST_doPaletteStuff() {

    var palette: Int
    var cnt: Int
    val bzc: Int

    cnt = plyr.damagecount

    if (plyr.powers[pw_strength] != 0) {
        // slowly fade the berzerk out
        bzc = 12 - (plyr.powers[pw_strength] shr 6)

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
    } else if (plyr.powers[pw_ironfeet] > 4 * 32
        || (plyr.powers[pw_ironfeet] and 8) != 0
    )
        palette = RADIATIONPAL
    else
        palette = 0

    if (palette != st_palette) {
        st_palette = palette
        I_SetPalette(W_CacheLumpNum(lu_palette), palette * 768)
    }

}

internal fun DoomEngineCore.ST_drawWidgets(refresh: Boolean) {

    // used by w_arms[] widgets
    st_armson = st_statusbaron && deathmatch == 0

    // used by w_frags widget
    st_fragson = deathmatch != 0 && st_statusbaron

    STlib_updateNum(w_ready, refresh)

    for (i in 0 until 4) {
        STlib_updateNum(w_ammo[i], refresh)
        STlib_updateNum(w_maxammo[i], refresh)
    }

    STlib_updatePercent(w_health, refresh)
    STlib_updatePercent(w_armor, refresh)

    STlib_updateBinIcon(w_armsbg, refresh)

    for (i in 0 until 6)
        STlib_updateMultIcon(w_arms[i], refresh)

    STlib_updateMultIcon(w_faces, refresh)

    for (i in 0 until 3)
        STlib_updateMultIcon(w_keyboxes[i], refresh)

    STlib_updateNum(w_frags, refresh)

}

internal fun DoomEngineCore.ST_doRefresh() {

    st_firsttime = false

    // draw status bar background to off-screen buff
    ST_refreshBackground()

    // and refresh all widgets
    ST_drawWidgets(true)

}

internal fun DoomEngineCore.ST_diffDraw() {
    // update all widgets
    ST_drawWidgets(false)
}

internal fun DoomEngineCore.ST_Drawer(fullscreen: Boolean, refresh: Boolean) {

    st_statusbaron = !fullscreen || automapactive
    st_firsttime = st_firsttime || refresh

    // Do red-/gold-shifts from damage/items
    ST_doPaletteStuff()

    // If just after ST_Start(), refresh all
    if (st_firsttime) ST_doRefresh()
    // Otherwise, update as little as possible
    else ST_diffDraw()

}

internal fun DoomEngineCore.ST_loadGraphics() {

    var facenum: Int

    // Load the numbers, tall and short
    for (i in 0 until 10) {
        tallnum[i] = W_CacheLumpName("STTNUM$i")

        shortnum[i] = W_CacheLumpName("STYSNUM$i")
    }

    // Load percent key.
    //Note: why not load STMINUS here, too?
    tallpercent = W_CacheLumpName("STTPRCNT")

    // key cards
    for (i in 0 until NUMCARDS) {
        keys[i] = W_CacheLumpName("STKEYS$i")
    }

    // arms background
    armsbg = W_CacheLumpName("STARMS")

    // arms ownership widgets
    for (i in 0 until 6) {
        // gray #
        arms[i][0] = W_CacheLumpName("STGNUM${i + 2}")

        // yellow #
        arms[i][1] = shortnum[i + 2]
    }

    // face backgrounds for different color players
    faceback = W_CacheLumpName("STFB$consoleplayer")

    // status bar background bits
    sbar = W_CacheLumpName("STBAR")

    // face states
    facenum = 0
    for (i in 0 until ST_NUMPAINFACES) {
        for (j in 0 until ST_NUMSTRAIGHTFACES) {
            faces[facenum++] = W_CacheLumpName("STFST$i$j")
        }
        faces[facenum++] = W_CacheLumpName("STFTR${i}0")  // turn right
        faces[facenum++] = W_CacheLumpName("STFTL${i}0")  // turn left
        faces[facenum++] = W_CacheLumpName("STFOUCH$i")   // ouch!
        faces[facenum++] = W_CacheLumpName("STFEVL$i")    // evil grin ;)
        faces[facenum++] = W_CacheLumpName("STFKILL$i")   // pissed off
    }
    faces[facenum++] = W_CacheLumpName("STFGOD0")
    faces[facenum++] = W_CacheLumpName("STFDEAD0")

}

internal fun DoomEngineCore.ST_loadData() {
    lu_palette = W_GetNumForName("PLAYPAL")
    ST_loadGraphics()
}

internal fun DoomEngineCore.ST_unloadGraphics() {

    // (C: Z_ChangeTag(..., PU_CACHE) on every cached patch --
    //  lumps stay cached in W_CacheLumpNum here, nothing to do.)

    // Note: nobody ain't seen no unloading
    //   of stminus yet. Dude.


}

internal fun DoomEngineCore.ST_unloadData() {
    ST_unloadGraphics()
}

internal fun DoomEngineCore.ST_initData() {

    st_firsttime = true
    plyr = players[consoleplayer]

    st_clock = 0
    st_chatstate = StartChatState
    st_gamestate = FirstPersonState

    st_statusbaron = true
    st_chat = false; st_oldchat = st_chat
    st_cursoron = false

    st_faceindex = 0
    st_palette = -1

    st_oldhealth = -1

    for (i in 0 until NUMWEAPONS)
        oldweaponsowned[i] = plyr.weaponowned[i]

    for (i in 0 until 3)
        keyboxes[i] = -1

    STlib_init()

}


internal fun DoomEngineCore.ST_createWidgets() {

    // ready weapon ammo
    // (C: &plyr->ammo[weaponinfo[plyr->readyweapon].ammo], bound at create
    //  time; ST_updateWidgets redirects it before any draw.)
    val readyammo = weaponinfo[plyr.readyweapon].ammo
    STlib_initNum(
        w_ready,
        ST_AMMOX,
        ST_AMMOY,
        tallnum,
        { plyr.ammo[readyammo] },
        { st_statusbaron },
        ST_AMMOWIDTH
    )

    // the last weapon type
    w_ready.data = plyr.readyweapon

    // health percentage
    STlib_initPercent(
        w_health,
        ST_HEALTHX,
        ST_HEALTHY,
        tallnum,
        { plyr.health },
        { st_statusbaron },
        tallpercent
    )

    // arms background
    STlib_initBinIcon(
        w_armsbg,
        ST_ARMSBGX,
        ST_ARMSBGY,
        armsbg,
        { st_notdeathmatch },
        { st_statusbaron }
    )

    // weapons owned
    for (i in 0 until 6) {
        STlib_initMultIcon(
            w_arms[i],
            ST_ARMSX + (i % 3) * ST_ARMSXSPACE,
            ST_ARMSY + (i / 3) * ST_ARMSYSPACE,
            arms[i],
            { if (plyr.weaponowned[i + 1]) 1 else 0 },  // (C: (int *) &plyr->weaponowned[i+1])
            { st_armson }
        )
    }

    // frags sum
    STlib_initNum(
        w_frags,
        ST_FRAGSX,
        ST_FRAGSY,
        tallnum,
        { st_fragscount },
        { st_fragson },
        ST_FRAGSWIDTH
    )

    // faces
    STlib_initMultIcon(
        w_faces,
        ST_FACESX,
        ST_FACESY,
        faces,
        { st_faceindex },
        { st_statusbaron }
    )

    // armor percentage - should be colored later
    STlib_initPercent(
        w_armor,
        ST_ARMORX,
        ST_ARMORY,
        tallnum,
        { plyr.armorpoints },
        { st_statusbaron }, tallpercent
    )

    // keyboxes 0-2
    STlib_initMultIcon(
        w_keyboxes[0],
        ST_KEY0X,
        ST_KEY0Y,
        keys,
        { keyboxes[0] },
        { st_statusbaron }
    )

    STlib_initMultIcon(
        w_keyboxes[1],
        ST_KEY1X,
        ST_KEY1Y,
        keys,
        { keyboxes[1] },
        { st_statusbaron }
    )

    STlib_initMultIcon(
        w_keyboxes[2],
        ST_KEY2X,
        ST_KEY2Y,
        keys,
        { keyboxes[2] },
        { st_statusbaron }
    )

    // ammo count (all four kinds)
    STlib_initNum(
        w_ammo[0],
        ST_AMMO0X,
        ST_AMMO0Y,
        shortnum,
        { plyr.ammo[0] },
        { st_statusbaron },
        ST_AMMO0WIDTH
    )

    STlib_initNum(
        w_ammo[1],
        ST_AMMO1X,
        ST_AMMO1Y,
        shortnum,
        { plyr.ammo[1] },
        { st_statusbaron },
        ST_AMMO1WIDTH
    )

    STlib_initNum(
        w_ammo[2],
        ST_AMMO2X,
        ST_AMMO2Y,
        shortnum,
        { plyr.ammo[2] },
        { st_statusbaron },
        ST_AMMO2WIDTH
    )

    STlib_initNum(
        w_ammo[3],
        ST_AMMO3X,
        ST_AMMO3Y,
        shortnum,
        { plyr.ammo[3] },
        { st_statusbaron },
        ST_AMMO3WIDTH
    )

    // max ammo count (all four kinds)
    STlib_initNum(
        w_maxammo[0],
        ST_MAXAMMO0X,
        ST_MAXAMMO0Y,
        shortnum,
        { plyr.maxammo[0] },
        { st_statusbaron },
        ST_MAXAMMO0WIDTH
    )

    STlib_initNum(
        w_maxammo[1],
        ST_MAXAMMO1X,
        ST_MAXAMMO1Y,
        shortnum,
        { plyr.maxammo[1] },
        { st_statusbaron },
        ST_MAXAMMO1WIDTH
    )

    STlib_initNum(
        w_maxammo[2],
        ST_MAXAMMO2X,
        ST_MAXAMMO2Y,
        shortnum,
        { plyr.maxammo[2] },
        { st_statusbaron },
        ST_MAXAMMO2WIDTH
    )

    STlib_initNum(
        w_maxammo[3],
        ST_MAXAMMO3X,
        ST_MAXAMMO3Y,
        shortnum,
        { plyr.maxammo[3] },
        { st_statusbaron },
        ST_MAXAMMO3WIDTH
    )

}

private var DoomEngineCore.st_stopped
    get() = stateStatusBar.st_stopped
    set(value) { stateStatusBar.st_stopped = value }


internal fun DoomEngineCore.ST_Start() {

    if (!st_stopped)
        ST_Stop()

    ST_initData()
    ST_createWidgets()
    st_stopped = false

}

internal fun DoomEngineCore.ST_Stop() {
    if (st_stopped)
        return

    I_SetPalette(W_CacheLumpNum(lu_palette))

    st_stopped = true
}

internal fun DoomEngineCore.ST_Init() {
    veryfirsttime = 0
    ST_loadData()
    // (C: screens[4] = (byte *) Z_Malloc(ST_WIDTH*ST_HEIGHT, PU_STATIC, 0);
    //  screens[4] is statically allocated full 320x200 in v_video.kt.)
}
