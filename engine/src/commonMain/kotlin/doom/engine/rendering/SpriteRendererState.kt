
package doom.engine.rendering

import doom.engine.SCREENWIDTH
import doom.engine.geometry.FixedPoint
import doom.engine.rendering.resources.SpriteDefinition
import doom.engine.rendering.resources.SpriteFrame

internal class SpriteRendererState {
    var pspritescale: FixedPoint = 0

    var pspriteiscale: FixedPoint = 0

    var spritelights: IntArray = IntArray(0)

    val negonearray by lazy(LazyThreadSafetyMode.NONE) { ShortArray(SCREENWIDTH) }

    val screenheightarray by lazy(LazyThreadSafetyMode.NONE) { ShortArray(SCREENWIDTH) }

    val spriteBottomClip by lazy(LazyThreadSafetyMode.NONE) { ShortArray(SCREENWIDTH) }

    val spriteTopClip by lazy(LazyThreadSafetyMode.NONE) { ShortArray(SCREENWIDTH) }

    var sprites: Array<SpriteDefinition> = emptyArray()

    var numsprites = 0

    val sprtemp by lazy(LazyThreadSafetyMode.NONE) { Array(29) { SpriteFrame() } }

    var maxframe = 0

    var spritename: String = ""

    val vissprites by lazy(LazyThreadSafetyMode.NONE) { Array(MAXVISSPRITES) { VisibleSprite() } }

    var visspriteP = 0

    var newvissprite = 0

    val overflowsprite by lazy(LazyThreadSafetyMode.NONE) { VisibleSprite() }

    var mfloorclip: ShortArray = ShortArray(0)

    var mfloorclipBase = 0

    var mceilingclip: ShortArray = ShortArray(0)

    var mceilingclipBase = 0

    var spryscale: FixedPoint = 0

    var sprtopscreen: FixedPoint = 0

    val vsprsortedhead by lazy(LazyThreadSafetyMode.NONE) { VisibleSprite() }
}
