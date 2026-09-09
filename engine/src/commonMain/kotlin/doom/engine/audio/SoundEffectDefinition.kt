
package doom.engine.audio

internal class SoundEffectDefinition(
    val name: String,
    val singularity: Boolean,
    var priority: Int,
    val link: Int,
    val pitch: Int,
    val volume: Int,
) {
    var usefulness: Int = 0
    var lumpnum: Int = -1
}
