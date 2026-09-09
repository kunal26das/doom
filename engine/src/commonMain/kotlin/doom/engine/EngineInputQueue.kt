package doom.engine

import kotlinx.coroutines.channels.Channel

/**
 * Separates thread-safe host submission from owner-thread responder dispatch.
 * Pending events cannot wrap a fixed-size ring and silently lose key releases.
 */
internal class EngineInputQueue {
    private val incoming = Channel<event_t>(Channel.UNLIMITED)
    private val pending = ArrayDeque<event_t>()
    val pendingCount: Int get() = pending.size

    fun post(event: event_t) { incoming.trySend(event) }
    fun stage(event: event_t) { pending.addLast(event) }
    fun collect() {
        while (true) pending.addLast(incoming.tryReceive().getOrNull() ?: return)
    }
    fun dispatch(respond: (event_t) -> Unit) {
        while (pending.isNotEmpty()) respond(pending.removeFirst())
    }
    fun clearPending() { pending.clear() }
    fun clear() {
        while (incoming.tryReceive().isSuccess) { }
        pending.clear()
    }
    fun close() { incoming.close(); clear() }
}
