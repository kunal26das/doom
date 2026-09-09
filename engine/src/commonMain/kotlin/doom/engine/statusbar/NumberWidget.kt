
package doom.engine.statusbar

internal class NumberWidget {
    var x = 0
    var y = 0

    var width = 0

    var oldnum = 0

    var num: () -> Int = { 0 }

    var on: () -> Boolean = { false }

    var p: Array<ByteArray> = emptyArray()

    var data = 0
}
