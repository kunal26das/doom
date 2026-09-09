
package doom.engine.hud

import doom.engine.SCREENWIDTH
import doom.engine.core.DoomEngineCore
import doom.engine.rendering.vDrawPatch
import doom.engine.rendering.vDrawPatchDirect
import doom.engine.rendering.patchWidth

private fun DoomEngineCore.toupper(c: Int): Int = if (c >= 'a'.code && c <= 'z'.code) c - 32 else c

internal fun DoomEngineCore.mDrawText(startX: Int, y: Int, direct: Boolean, string: String): Int {
    var x = startX
    var pos = 0

    while (pos < string.length) {
        val c = toupper(string[pos].code) - HU_FONTSTART
        pos++
        if (c < 0 || c > HU_FONTSIZE) {
            x += 4
            continue
        }

        val w = patchWidth(huFont[c])
        if (x + w > SCREENWIDTH)
            break
        if (direct)
            vDrawPatchDirect(x, y, 0, huFont[c])
        else
            vDrawPatch(x, y, 0, huFont[c])
        x += w
    }

    return x
}
