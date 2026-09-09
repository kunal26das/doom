package doom.engine

public data class DoomMetrics(
    public val gametic: Int,
    public val leveltime: Int,
    public val gameState: Int,
    public val episode: Int,
    public val map: Int,
)
