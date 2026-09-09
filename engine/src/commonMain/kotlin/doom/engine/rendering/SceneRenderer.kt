package doom.engine.rendering

import doom.engine.gameplay.player.Player

internal class SceneRenderer<Player>(
    private val passes: ScenePasses<Player>,
    private val checkpoint: () -> Unit,
) {
    fun render(player: Player) {
        passes.prepare(player)
        checkpoint()
        passes.drawWorld()
        checkpoint()
        passes.drawPlanes()
        checkpoint()
        passes.drawMasked()
        checkpoint()
    }
}
