package doom.engine

public data class DoomHost(
    public val clock: DoomClock? = null,
    public val storage: DoomStorage? = null,
    public val video: DoomVideo? = null,
    public val sound: ISoundDriver? = null,
)
