package doom.engine.input


import kotlinx.coroutines.channels.Channel

internal class EngineInputQueue {
    private val incoming = Channel<EngineEvent>(Channel.UNLIMITED)
    private val pending = ArrayDeque<EngineEvent>()
    val pendingCount: Int get() = pending.size

    fun post(event: EngineEvent) { incoming.trySend(event) }
    fun stage(event: EngineEvent) { pending.addLast(event) }
    fun collect() {
        while (true) pending.addLast(incoming.tryReceive().getOrNull() ?: return)
    }
    fun dispatch(respond: (EngineEvent) -> Unit) {
        while (pending.isNotEmpty()) respond(pending.removeFirst())
    }
    fun clearPending() { pending.clear() }
    fun clear() {
        while (incoming.tryReceive().isSuccess) { }
        pending.clear()
    }
    fun close() { incoming.close(); clear() }
}
