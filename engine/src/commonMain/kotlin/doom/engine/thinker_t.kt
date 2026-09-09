// Port of linuxdoom-1.10 d_think.h -- thinkers.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "ktlint")

package doom.engine

/**
 * Doubly linked list of actors/thinkers.
 * C stores an actionf_t union; NULL function means "just a list node" and
 * (actionf_v)(-1) marks a removed thinker. Here: `function == null` is the
 * plain node, `removed == true` is the -1 hack (set by P_RemoveThinker).
 * Where C compares `function.acp1 == (actionf_p1)P_MobjThinker`, test `is mobj_t`.
 */
internal open class thinker_t {
    var prev: thinker_t? = null
    var next: thinker_t? = null
    var function: ((thinker_t) -> Unit)? = null
    var removed = false
}
