
package doom.engine.audio

import doom.engine.geometry.FixedPoint

internal interface SoundOrigin {
    val x: FixedPoint
    val y: FixedPoint
}
