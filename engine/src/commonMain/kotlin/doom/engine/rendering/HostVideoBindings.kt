
package doom.engine.rendering

import doom.engine.core.DoomEngineCore
import doom.engine.input.EngineEvent

internal fun DoomEngineCore.iPostEvent(ev: EngineEvent) { inputQueue.post(ev) }
internal fun DoomEngineCore.iInitGraphics() {}
internal fun DoomEngineCore.iShutdownGraphics() {}
internal fun DoomEngineCore.iStartFrame() {}
internal fun DoomEngineCore.iStartTic() { inputQueue.collect() }
internal fun DoomEngineCore.iUpdateNoBlit() {}

internal fun DoomEngineCore.iSetPalette(pal: ByteArray, offset: Int = 0) {
    videoOutput.setPalette(pal, offset, gammatable[usegamma])
}

internal fun DoomEngineCore.iFinishUpdate() { videoOutput.present(screens[0]) }
internal fun DoomEngineCore.iReadScreen(scr: ByteArray) { frameBuffers.readScreen(scr) }
