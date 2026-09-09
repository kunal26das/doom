// Port of linuxdoom-1.10 info.h struct definitions (the data tables themselves
// are generated into gen/InfoGen.kt / gen/InfoConstsGen.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// Sprite frame flags (r_defs.h in vanilla, kept with states here).
internal const val FF_FULLBRIGHT = 0x8000  // flag in thing->frame
internal const val FF_FRAMEMASK = 0x7fff


internal val DoomEngineCore.actionMap
    get() = stateActionRegistry.actionMap

internal fun DoomEngineCore.registerAction(action: ActionF) {
    actionMap[action.name] = action
}

internal fun DoomEngineCore.InfoResolveActions() {
    for ((i, st) in states.withIndex()) {
        st.index = i
        if (st.actionName != null) {
            st.action = actionMap[st.actionName]
                ?: I_Error("InfoResolveActions: unregistered action ${st.actionName}")
        }
    }
}
