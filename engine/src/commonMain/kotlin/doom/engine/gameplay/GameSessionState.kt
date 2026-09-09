
package doom.engine.gameplay

import doom.engine.KEY_DOWNARROW
import doom.engine.KEY_LEFTARROW
import doom.engine.KEY_RALT
import doom.engine.KEY_RCTRL
import doom.engine.KEY_RIGHTARROW
import doom.engine.KEY_RSHIFT
import doom.engine.KEY_UPARROW
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.player.Player
import doom.engine.input.OffsetButtons
import doom.engine.input.TicCommand
import doom.engine.intermission.IntermissionSummary
import doom.engine.simulation.BACKUPTICS

internal class GameSessionState {
    var gameaction = GA_NOTHING

    var gamestate = GS_LEVEL

    var gameskill = SK_BABY

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

    val players by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { Player() } }

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

    var demoP = 0

    var demoend = 0

    var singledemo = false

    var precache = true

    val wminfo by lazy(LazyThreadSafetyMode.NONE) { IntermissionSummary() }

    val consistancy by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { ShortArray(BACKUPTICS) } }

    var savebuffer = ByteArray(0)

    var keyRight = KEY_RIGHTARROW

    var keyLeft = KEY_LEFTARROW

    var keyUp = KEY_UPARROW

    var keyDown = KEY_DOWNARROW

    var keyStrafeleft = ','.code

    var keyStraferight = '.'.code

    var keyFire = KEY_RCTRL

    var keyUse = ' '.code

    var keyStrafe = KEY_RALT

    var keySpeed = KEY_RSHIFT

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

    val bodyque by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<Actor>(BODYQUESIZE) }

    var bodyqueslot = 0

    val emptycmd by lazy(LazyThreadSafetyMode.NONE) { TicCommand() }

    val pars by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 30, 75, 120, 90, 165, 180, 180, 30, 165),
        intArrayOf(0, 90, 90, 90, 120, 90, 360, 240, 30, 170),
        intArrayOf(0, 90, 45, 90, 150, 90, 90, 165, 30, 135)
    ) }

    val cpars by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        30, 90, 120, 120, 90, 150, 120, 120, 270, 90,
        210, 150, 150, 150, 210, 150, 420, 150, 210, 150,
        240, 150, 180, 150, 150, 300, 330, 420, 300, 180,
        120, 30
    ) }

    var secretexit = false

    var savename = ""

    var dSkill = 0

    var dEpisode = 0

    var dMap = 0

    var defdemoname = ""
}
