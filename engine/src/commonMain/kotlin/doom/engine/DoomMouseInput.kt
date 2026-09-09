package doom.engine

public data class DoomMouseInput(
    public val deltaX: Int,
    public val deltaY: Int,
    public val buttons: Int = 0,
) : DoomInput
