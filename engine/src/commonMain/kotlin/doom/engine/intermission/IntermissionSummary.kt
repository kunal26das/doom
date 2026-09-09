
package doom.engine.intermission

import doom.engine.gameplay.MAXPLAYERS

internal class IntermissionSummary {
    var epsd = 0
    var didsecret = false
    var last = 0
    var next = 0
    var maxkills = 0
    var maxitems = 0
    var maxsecret = 0
    var maxfrags = 0
    var partime = 0
    var pnum = 0
    val plyr = Array(MAXPLAYERS) { IntermissionPlayerStats() }
}
