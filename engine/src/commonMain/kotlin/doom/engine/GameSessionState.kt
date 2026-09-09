// Port of linuxdoom-1.10 g_game.c -- game state, tic commands, demos, save/load.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class GameSessionState {
    var gameaction = ga_nothing

    var gamestate = GS_LEVEL

    var gameskill = sk_baby

    var respawnmonsters = false

    var gameepisode = 0

    var gamemap = 0

    var paused = false

    var sendpause = false

    var sendsave = false

    var usergame = false

    var timingdemo = false

    var nodrawers = false

    var noblit = false

    var starttime = 0

    var viewactive = false

    var deathmatch = 0

    var netgame = false

    val playeringame by lazy(LazyThreadSafetyMode.NONE) { BooleanArray(MAXPLAYERS) }

    val players by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { player_t() } }

    var consoleplayer = 0

    var displayplayer = 0

    var gametic = 0

    var levelstarttic = 0

    var totalkills = 0

    var totalitems = 0

    var totalsecret = 0

    var demoname = ""

    var demorecording = false

    var demoplayback = false

    var netdemo = false

    var demobuffer: ByteArray? = null

    var demo_p = 0

    var demoend = 0

    var singledemo = false

    var precache = true

    val wminfo by lazy(LazyThreadSafetyMode.NONE) { wbstartstruct_t() }

    val consistancy by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { ShortArray(BACKUPTICS) } }

    var savebuffer = ByteArray(0)

    var key_right = KEY_RIGHTARROW

    var key_left = KEY_LEFTARROW

    var key_up = KEY_UPARROW

    var key_down = KEY_DOWNARROW

    var key_strafeleft = ','.code

    var key_straferight = '.'.code

    var key_fire = KEY_RCTRL

    var key_use = ' '.code

    var key_strafe = KEY_RALT

    var key_speed = KEY_RSHIFT

    var mousebfire = 0

    var mousebstrafe = 1

    var mousebforward = 2

    var joybfire = 0

    var joybstrafe = 1

    var joybuse = 3

    var joybspeed = 2

    val forwardmove by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(0x19, 0x32) }

    val sidemove by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(0x18, 0x28) }

    val angleturn by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(640, 1280, 320) }

    val gamekeydown by lazy(LazyThreadSafetyMode.NONE) { BooleanArray(NUMKEYS) }

    var turnheld = 0

    val mousearray by lazy(LazyThreadSafetyMode.NONE) { BooleanArray(4) }

    val mousebuttons by lazy(LazyThreadSafetyMode.NONE) { OffsetButtons(mousearray) }

    var mousex = 0

    var mousey = 0

    var dclicktime = 0

    var dclickstate = 0

    var dclicks = 0

    var dclicktime2 = 0

    var dclickstate2 = 0

    var dclicks2 = 0

    var joyxmove = 0

    var joyymove = 0

    val joyarray by lazy(LazyThreadSafetyMode.NONE) { BooleanArray(5) }

    val joybuttons by lazy(LazyThreadSafetyMode.NONE) { OffsetButtons(joyarray) }

    var savegameslot = 0

    var savedescription = ""

    val bodyque by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<mobj_t>(BODYQUESIZE) }

    var bodyqueslot = 0

    val emptycmd by lazy(LazyThreadSafetyMode.NONE) { ticcmd_t() }

    val pars by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 30, 75, 120, 90, 165, 180, 180, 30, 165),
        intArrayOf(0, 90, 90, 90, 120, 90, 360, 240, 30, 170),
        intArrayOf(0, 90, 45, 90, 150, 90, 90, 165, 30, 135)
    ) }

    val cpars by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        30, 90, 120, 120, 90, 150, 120, 120, 270, 90,        //  1-10
        210, 150, 150, 150, 210, 150, 420, 150, 210, 150,    // 11-20
        240, 150, 180, 150, 150, 300, 330, 420, 300, 180,    // 21-30
        120, 30                                              // 31-32
    ) }

    var secretexit = false

    var savename = ""

    var d_skill = 0

    var d_episode = 0

    var d_map = 0

    var defdemoname = ""
}
