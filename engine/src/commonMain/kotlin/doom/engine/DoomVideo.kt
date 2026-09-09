package doom.engine

/** The frame buffer is borrowed for this call; copy it before retaining it. */
public fun interface DoomVideo { public fun present(argb: IntArray): Unit }
