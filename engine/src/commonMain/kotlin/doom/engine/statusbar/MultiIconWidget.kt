
package doom.engine.statusbar

internal class MultiIconWidget {
    var x = 0
    var y = 0

    var oldinum = 0

    var inum: () -> Int = { 0 }

    var on: () -> Boolean = { false }

    var p: Array<ByteArray> = emptyArray()

    var data = 0
}
