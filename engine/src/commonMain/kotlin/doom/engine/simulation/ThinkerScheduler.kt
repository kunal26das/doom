// Thinker traversal adapted from linuxdoom-1.10 p_tick.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.simulation

import doom.engine.thinker_t

/** Owns insertion order and deferred removal of the world's intrusive thinker list. */
internal class ThinkerScheduler {
    /** Borrowed for savegame traversal; only this scheduler changes list links. */
    val sentinel: thinker_t = thinker_t()

    init { reset() }

    fun reset() {
        sentinel.next = sentinel
        sentinel.prev = sentinel
    }

    fun add(thinker: thinker_t) {
        sentinel.prev!!.next = thinker
        thinker.next = sentinel
        thinker.prev = sentinel.prev
        sentinel.prev = thinker
    }

    /** Removal becomes structural when traversal reaches this thinker, never during a callback. */
    fun remove(thinker: thinker_t) {
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
            // Read after the callback: a newly appended thinker runs during this same tic.
            thinker = thinker.next!!
        }
    }
}
