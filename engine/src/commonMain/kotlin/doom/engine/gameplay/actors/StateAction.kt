
package doom.engine.gameplay.actors

import doom.engine.gameplay.player.Player
import doom.engine.gameplay.weapons.WeaponSprite

internal class StateAction(
    val name: String,
    val mobjFun: ((Actor) -> Unit)? = null,
    val pspFun: ((Player, WeaponSprite) -> Unit)? = null,
)
