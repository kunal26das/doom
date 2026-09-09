package doom.engine.runtime

import kotlin.concurrent.Volatile

/** Pollable session signal; quitting never requires retaining a host callback. */
internal class QuitSignal {
    @Volatile var requested = false
        private set

    fun request() { requested = true }
}
