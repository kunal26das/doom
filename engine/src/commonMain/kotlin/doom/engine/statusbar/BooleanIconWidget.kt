
package doom.engine.statusbar

internal class BooleanIconWidget {
    var x = 0
    var y = 0

    var oldval = false

    var `val`: () -> Boolean = { false }

    var on: () -> Boolean = { false }

    var p: ByteArray = ByteArray(0)
    var data = 0
}
