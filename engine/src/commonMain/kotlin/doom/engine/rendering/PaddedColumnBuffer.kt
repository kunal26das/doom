
package doom.engine.rendering

import doom.engine.SCREENWIDTH

internal class PaddedColumnBuffer {
    private val a = IntArray(SCREENWIDTH + 2)
    operator fun get(i: Int): Int = a[i + 1]
    operator fun set(i: Int, v: Int) { a[i + 1] = v }
    fun fill(v: Int) = a.fill(v)
}
