
package doom.engine.gameplay.interactions

internal class ItemInteractionState {
    val maxammo by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(200, 50, 300, 50) }

    val clipammo by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(10, 4, 20, 1) }
}
