package doom.engine

/** A monotonic clock expressed in the engine's 35 Hz tics. */
public fun interface DoomClock { public fun ticks(): Int }
