
package doom.engine.gameplay.player

import doom.engine.geometry.ANG90
import doom.engine.geometry.BinaryAngle

internal class PlayerMovementState {
    var onground = false

    val ang5: BinaryAngle by lazy(LazyThreadSafetyMode.NONE) { ANG90 / 18u }
}
