
package doom.engine.gameplay.weapons

import doom.engine.gameplay.actors.StateDefinition
import doom.engine.geometry.FixedPoint

internal class WeaponSprite {
    var state: StateDefinition? = null
    var tics = 0
    var sx: FixedPoint = 0
    var sy: FixedPoint = 0
}
