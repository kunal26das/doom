
package doom.engine.geometry

internal object FineCosineTable {
    operator fun get(i: Int): Int = finesine[i + 2048]
}
