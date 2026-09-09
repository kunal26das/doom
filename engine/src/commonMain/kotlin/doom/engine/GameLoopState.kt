// Port of linuxdoom-1.10 d_main.c -- DOOM main program (D_DoomMain) and game
// loop (D_DoomLoop), plus functions to determine game mode (shareware,
// registered), parse command line parameters, configure game parameters
// (turbo), and call the startup functions.
//
// Host-driven restructure: the never-returning D_DoomLoop becomes
// D_DoomStep(), called by the host once per display frame, and D_Display's
// blocking wipe do/while becomes a wipeActive state machine (D_WipeStep).
// WAD files arrive as in-memory ByteArrays instead of file paths, so
// IdentifyVersion becomes D_IdentifyVersion (by lump presence, after
// W_InitMultipleFiles).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class GameLoopState {
    var devparm = false

    var nomonsters = false

    var respawnparm = false

    var fastparm = false

    var basedefault = "default.cfg"

    var singletics = false

    var modifiedgame = false

    var startskill = sk_medium

    var startepisode = 1

    var startmap = 1

    var autostart = false

    var advancedemo = false

    var wipegamestate = GS_DEMOSCREEN

    var viewactivestate = false

    var menuactivestate = false

    var inhelpscreensstate = false

    var fullscreen = false

    var oldgamestate = -1

    var borderdrawcount = 0

    var wipeActive = false

    var wipestart = 0

    var demosequence = 0

    var pagetic = 0

    var pagename = "TITLEPIC"
}
