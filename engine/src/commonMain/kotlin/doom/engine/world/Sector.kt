
package doom.engine.world

import doom.engine.audio.SectorSoundOrigin
import doom.engine.gameplay.actors.Actor
import doom.engine.geometry.FixedPoint
import doom.engine.simulation.Thinker

internal class Sector {
    var floorheight: FixedPoint = 0
    var ceilingheight: FixedPoint = 0
    var floorpic = 0
    var ceilingpic = 0
    var lightlevel = 0
    var special = 0
    var tag = 0

    var soundtraversed = 0

    var soundtarget: Actor? = null

    val blockbox = IntArray(4)

    val soundorg = SectorSoundOrigin()

    var validcount = 0

    var thinglist: Actor? = null

    var specialdata: Thinker? = null

    var linecount = 0
    var lines: Array<MapLine?> = emptyArray()

    var index = 0
}
