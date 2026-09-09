package doom.engine

public data class DoomJoystickInput(
    public val deltaX: Int,
    public val deltaY: Int,
    public val buttons: Int = 0,
) : DoomInput
