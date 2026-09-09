package doom.engine.rendering

/** Rendering capabilities, independent of the simulation's player representation. */
internal interface ScenePasses<Player> {
    fun prepare(player: Player)
    fun drawWorld()
    fun drawPlanes()
    fun drawMasked()
}
