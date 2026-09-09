// Port of linuxdoom-1.10 info.h struct definitions (the data tables themselves
// are generated into gen/InfoGen.kt / gen/InfoConstsGen.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

internal class mobjinfo_t(
    val doomednum: Int,
    val spawnstate: Int,
    val spawnhealth: Int,
    val seestate: Int,
    val seesound: Int,
    val reactiontime: Int,
    val attacksound: Int,
    val painstate: Int,
    val painchance: Int,
    val painsound: Int,
    val meleestate: Int,
    val missilestate: Int,
    val deathstate: Int,
    val xdeathstate: Int,
    val deathsound: Int,
    var speed: Int,        // mutated by -fast (G_InitNew)
    val radius: fixed_t,
    val height: fixed_t,
    val mass: Int,
    val damage: Int,
    val activesound: Int,
    val flags: Int,
    val raisestate: Int,
)
