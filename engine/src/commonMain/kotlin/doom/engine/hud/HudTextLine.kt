
package doom.engine.hud

internal class HudTextLine {
    var x = 0
    var y = 0

    var f: Array<ByteArray>? = null
    var sc = 0
    val l = CharArray(HU_MAXLINELENGTH + 1)
    var len = 0

    var needsupdate = 0
}
