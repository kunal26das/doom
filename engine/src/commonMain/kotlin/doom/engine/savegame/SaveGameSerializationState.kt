
package doom.engine.savegame

import doom.engine.simulation.Thinker

internal class SaveGameSerializationState {
    var saveP = 0

    val savegRestoredAction: (Thinker) -> Unit by lazy(LazyThreadSafetyMode.NONE) { { } }
}
