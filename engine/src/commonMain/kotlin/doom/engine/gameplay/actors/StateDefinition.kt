
package doom.engine.gameplay.actors

internal class StateDefinition(
    val sprite: Int,
    val frame: Int,
    var tics: Int,
    val actionName: String?,
    val nextstate: Int,
    val misc1: Int = 0,
    val misc2: Int = 0,
) {
    var action: StateAction? = null
    var index: Int = 0
}
