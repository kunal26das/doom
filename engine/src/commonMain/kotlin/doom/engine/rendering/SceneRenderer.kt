package doom.engine.rendering

/**
 * Owns render-pass order. Checkpoints let the frame coordinator preserve DOOM's
 * input polling cadence without making rendering depend on a game scheduler.
 */
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
