package doom.engine.input

internal class OffsetButtons(private val storage: BooleanArray) {
    operator fun get(index: Int): Boolean = storage[index + 1]
    operator fun set(index: Int, value: Boolean) { storage[index + 1] = value }
}
