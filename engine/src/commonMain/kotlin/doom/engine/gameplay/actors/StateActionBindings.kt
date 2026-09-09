
package doom.engine.gameplay.actors

import doom.engine.core.DoomEngineCore
import doom.engine.core.iError

internal const val FF_FULLBRIGHT = 0x8000
internal const val FF_FRAMEMASK = 0x7fff

internal val DoomEngineCore.actionMap
    get() = stateActionRegistry.actionMap

internal fun DoomEngineCore.registerAction(action: StateAction) {
    actionMap[action.name] = action
}

internal fun DoomEngineCore.infoResolveActions() {
    for ((i, st) in states.withIndex()) {
        st.index = i
        if (st.actionName != null) {
            st.action = actionMap[st.actionName]
                ?: iError("InfoResolveActions: unregistered action ${st.actionName}")
        }
    }
}
