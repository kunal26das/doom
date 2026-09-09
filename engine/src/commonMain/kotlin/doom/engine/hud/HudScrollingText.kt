
package doom.engine.hud

internal class HudScrollingText {
    val l = Array(HU_MAXLINES) { HudTextLine() }
    var h = 0
    var cl = 0

    var on: () -> Boolean = { false }
    var laston = false
}
