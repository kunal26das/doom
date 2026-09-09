
package doom.engine.core

import doom.engine.gameplay.GS_DEMOSCREEN
import doom.engine.gameplay.SK_MEDIUM

internal class GameLoopState {
    var devparm = false

    var nomonsters = false

    var respawnparm = false

    var fastparm = false

    var basedefault = "default.cfg"

    var singletics = false

    var modifiedgame = false

    var startskill = SK_MEDIUM

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
