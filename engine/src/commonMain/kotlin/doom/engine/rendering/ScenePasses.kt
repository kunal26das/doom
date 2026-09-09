package doom.engine.rendering

import doom.engine.gameplay.player.Player

internal interface ScenePasses<Player> {
    fun prepare(player: Player)
    fun drawWorld()
    fun drawPlanes()
    fun drawMasked()
}
