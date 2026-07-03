// Port of linuxdoom-1.10 info.h struct definitions (the data tables themselves
// are generated into gen/InfoGen.kt / gen/InfoConstsGen.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// Sprite frame flags (r_defs.h in vanilla, kept with states here).
const val FF_FULLBRIGHT = 0x8000  // flag in thing->frame
const val FF_FRAMEMASK = 0x7fff

/**
 * State action. Vanilla's actionf_t union: either an (mobj_t*) action used by
 * map objects, or a (player_t*, pspdef_t*) action used by player weapon sprites.
 */
class ActionF(
    val name: String,
    val mobjFun: ((mobj_t) -> Unit)? = null,
    val pspFun: ((player_t, pspdef_t) -> Unit)? = null,
)

class state_t(
    val sprite: Int,       // spritenum_t
    val frame: Int,        // may include FF_FULLBRIGHT
    var tics: Int,         // mutated by -fast (G_InitNew)
    val actionName: String?,
    val nextstate: Int,    // statenum_t
    val misc1: Int = 0,
    val misc2: Int = 0,
) {
    var action: ActionF? = null  // resolved from actionName by InfoResolveActions
    var index: Int = 0           // == statenum; C computes (st - states)
}

class mobjinfo_t(
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

/**
 * Registry of A_* action functions, populated by p_enemy.kt / p_pspr.kt /
 * p_mobj.kt via registerAction(). InfoResolveActions() is called once at
 * startup (D_DoomMain) to bind states[i].action.
 */
val actionMap = HashMap<String, ActionF>()

fun registerAction(action: ActionF) {
    actionMap[action.name] = action
}

fun InfoResolveActions() {
    for ((i, st) in states.withIndex()) {
        st.index = i
        if (st.actionName != null) {
            st.action = actionMap[st.actionName]
                ?: I_Error("InfoResolveActions: unregistered action ${st.actionName}")
        }
    }
}
