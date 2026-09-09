
package doom.engine.world

import doom.engine.geometry.FixedPoint

internal class MapSide {
    var textureoffset: FixedPoint = 0

    var rowoffset: FixedPoint = 0

    var toptexture = 0
    var bottomtexture = 0
    var midtexture = 0

    var sector: Sector? = null

    var index = 0
}
