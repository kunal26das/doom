
package doom.engine.capture

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.players
import doom.engine.rendering.iReadScreen
import doom.engine.rendering.screens
import doom.engine.resources.wCacheLumpName

internal fun DoomEngineCore.writePCXfile(
    filename: String,
    data: ByteArray,
    width: Int,
    height: Int,
    palette: ByteArray,
) {
    host.write(filename, screenshots.encode(data, width, height, palette))
}

internal fun DoomEngineCore.mScreenShot() {
    val linear = screens[2]
    iReadScreen(linear)
    val name = screenshots.nextFileName(::iError)
    writePCXfile(name, linear, SCREENWIDTH, SCREENHEIGHT, wCacheLumpName("PLAYPAL"))
    players[consoleplayer].message = "screen shot"
}
