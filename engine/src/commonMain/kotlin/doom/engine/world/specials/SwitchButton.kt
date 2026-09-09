
package doom.engine.world.specials

import doom.engine.audio.SectorSoundOrigin
import doom.engine.world.MapLine

internal class SwitchButton {
    var line: MapLine? = null
    var where = 0
    var btexture = 0
    var btimer = 0
    var soundorg: SectorSoundOrigin? = null
}
