
package doom.engine.cheats

internal class CheatDecoderState {
    var firsttime = 1

    val cheatXlateTable by lazy(LazyThreadSafetyMode.NONE) { IntArray(256) }
}
