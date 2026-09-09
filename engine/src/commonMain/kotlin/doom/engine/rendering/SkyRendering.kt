
package doom.engine.rendering

import doom.engine.core.DoomEngineCore
import doom.engine.geometry.FRACUNIT

internal const val SKYFLATNAME = "F_SKY1"

internal const val ANGLETOSKYSHIFT = 22

internal var DoomEngineCore.skyflatnum
    get() = stateSkyRenderer.skyflatnum
    set(value) { stateSkyRenderer.skyflatnum = value }
internal var DoomEngineCore.skytexture
    get() = stateSkyRenderer.skytexture
    set(value) { stateSkyRenderer.skytexture = value }
internal var DoomEngineCore.skytexturemid
    get() = stateSkyRenderer.skytexturemid
    set(value) { stateSkyRenderer.skytexturemid = value }

internal fun DoomEngineCore.rInitSkyMap() {
    skytexturemid = 100 * FRACUNIT
}
