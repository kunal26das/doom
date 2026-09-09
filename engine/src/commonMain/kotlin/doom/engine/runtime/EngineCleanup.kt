package doom.engine.runtime

/** Runs all cleanup operations while preserving the first failure. */
internal class EngineCleanup(private var failure: Throwable? = null) {
    fun attempt(action: () -> Unit) {
        try { action() } catch (error: Throwable) {
            val original = failure
            if (original == null) failure = error
            else if (original !== error) original.addSuppressed(error)
        }
    }
    fun throwIfFailed() { failure?.let { throw it } }
}
