
package doom.engine.world

import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.actors.Actor
import doom.engine.geometry.FixedPoint

internal class WorldGeometryState {
    var numvertexes = 0

    var vertexes: Array<MapVertex> = emptyArray()

    var numsegs = 0

    var segs: Array<MapSegment> = emptyArray()

    var numsectors = 0

    var sectors: Array<Sector> = emptyArray()

    var numsubsectors = 0

    var subsectors: Array<Subsector> = emptyArray()

    var numnodes = 0

    var nodes: Array<BspNode> = emptyArray()

    var numlines = 0

    var lines: Array<MapLine> = emptyArray()

    var numsides = 0

    var sides: Array<MapSide> = emptyArray()

    var bmapwidth = 0

    var bmapheight = 0

    var blockmap = 0

    var blockmaplump: IntArray = IntArray(0)

    var bmaporgx: FixedPoint = 0

    var bmaporgy: FixedPoint = 0

    var blocklinks: Array<Actor?> = emptyArray()

    var rejectmatrix: ByteArray = ByteArray(0)

    val deathmatchstarts by lazy(LazyThreadSafetyMode.NONE) { Array(MAX_DEATHMATCH_STARTS) { MapThingSpawn() } }

    var deathmatchP = 0

    val playerstarts by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { MapThingSpawn() } }
}
