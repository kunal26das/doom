package doom.engine.runtime

import kotlin.concurrent.Volatile

internal class QuitSignal {
    @Volatile var requested = false
        private set

    fun request() { requested = true }
}
