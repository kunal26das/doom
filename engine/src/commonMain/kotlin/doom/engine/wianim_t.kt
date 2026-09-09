// Port of linuxdoom-1.10 wi_stuff.c/wi_stuff.h -- intermission screens.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

//
// Animation.
// There is another anim_t used in p_spec.
// (Named wianim_t here since p_spec.kt owns the public anim_t.)
//
internal class wianim_t(
    val type: Int,

    // period in tics between animations
    val period: Int,

    // number of animation frames
    val nanims: Int,

    // location of animation
    val loc: point_t,

    // ALWAYS: n/a,
    // RANDOM: period deviation (<256),
    // LEVEL: level
    val data1: Int = 0,

    // ALWAYS: n/a,
    // RANDOM: random base period,
    // LEVEL: n/a
    val data2: Int = 0,
) {
    // actual graphics for frames of animations
    val p = Array(3) { ByteArray(0) }

    // following must be initialized to zero before use!

    // next value of bcnt (used in conjunction with period)
    var nexttic = 0

    // last drawn animation frame
    var lastdrawn = 0

    // next frame number to animate
    var ctr = 0

    // used by RANDOM and LEVEL when animating
    var state = 0
}
