
package doom.engine.intermission

internal class IntermissionAnimation(
    val type: Int,

    val period: Int,

    val nanims: Int,

    val loc: IntermissionPoint,

    val data1: Int = 0,

    val data2: Int = 0,
) {
    val p = Array(3) { ByteArray(0) }


    var nexttic = 0

    var lastdrawn = 0

    var ctr = 0

    var state = 0
}
