
package doom.engine.audio

internal class MusicTrack(
    val name: String,
) {
    var lumpnum: Int = -1
    var data: ByteArray? = null
    var handle: Int = 0
}
