
package doom.engine.hud

internal class HudInputText {
    val l = HudTextLine()

    var lm = 0

    var on: () -> Boolean = { false }
    var laston = false
}
