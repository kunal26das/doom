
package doom.engine.simulation

internal open class Thinker {
    var prev: Thinker? = null
    var next: Thinker? = null
    var function: ((Thinker) -> Unit)? = null
    var removed = false
}
