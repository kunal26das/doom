package doom.engine

public data class DoomKeyInput(public val code: Int, public val pressed: Boolean) : DoomInput {
    init { require(code in 1..255) { "Key codes must be in 1..255" } }
}
