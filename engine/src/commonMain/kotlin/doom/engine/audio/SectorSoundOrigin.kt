
package doom.engine.audio

import doom.engine.geometry.FixedPoint

internal class SectorSoundOrigin(
    override var x: FixedPoint = 0,
    override var y: FixedPoint = 0,
    var z: FixedPoint = 0,
) : SoundOrigin
