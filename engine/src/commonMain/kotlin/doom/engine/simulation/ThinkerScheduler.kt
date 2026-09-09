package doom.engine.simulation

internal class ThinkerScheduler {
    val sentinel: Thinker = Thinker()

    init { reset() }

    fun reset() {
        sentinel.next = sentinel
        sentinel.prev = sentinel
    }

    fun add(thinker: Thinker) {
        sentinel.prev!!.next = thinker
        thinker.next = sentinel
        thinker.prev = sentinel.prev
        sentinel.prev = thinker
    }

    fun remove(thinker: Thinker) {
        thinker.removed = true
    }

    fun run() {
        var thinker = sentinel.next!!
        while (thinker !== sentinel) {
            if (thinker.removed) {
                thinker.next!!.prev = thinker.prev
                thinker.prev!!.next = thinker.next
            } else {
                thinker.function?.invoke(thinker)
            }
            thinker = thinker.next!!
        }
    }
}
