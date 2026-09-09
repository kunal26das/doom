
package doom.engine.gameplay.actors

internal class ActionRegistryState {
    val actionMap by lazy(LazyThreadSafetyMode.NONE) { HashMap<String, StateAction>() }
}
