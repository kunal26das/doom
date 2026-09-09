
package doom.engine.world.collision

import doom.engine.gameplay.actors.Actor
import doom.engine.geometry.FixedPoint
import doom.engine.world.MapLine

internal class CollisionState {
    val tmbbox by lazy(LazyThreadSafetyMode.NONE) { IntArray(4) }

    var tmthing: Actor? = null

    var tmflags = 0

    var tmx: FixedPoint = 0

    var tmy: FixedPoint = 0

    var floatok = false

    var tmfloorz: FixedPoint = 0

    var tmceilingz: FixedPoint = 0

    var tmdropoffz: FixedPoint = 0

    var ceilingline: MapLine? = null

    val spechit by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<MapLine>(MAXSPECIALCROSS) }

    var numspechit = 0

    var bestslidefrac: FixedPoint = 0

    var secondslidefrac: FixedPoint = 0

    var bestslideline: MapLine? = null

    var secondslideline: MapLine? = null

    var slidemo: Actor? = null

    var tmxmove: FixedPoint = 0

    var tmymove: FixedPoint = 0

    var linetarget: Actor? = null

    var shootthing: Actor? = null

    var shootz: FixedPoint = 0

    var laDamage = 0

    var attackrange: FixedPoint = 0

    var aimslope: FixedPoint = 0

    var usething: Actor? = null

    var bombsource: Actor? = null

    var bombspot: Actor? = null

    var bombdamage = 0

    var crushchange = false

    var nofit = false
}
