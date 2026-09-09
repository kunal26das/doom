// Compatibility entry points for host video output and input collection.
// Original code (C) 1993-1996 id Software, Inc., GNU GPL v2.
@file:Suppress("FunctionName", "unused", "UNUSED_PARAMETER")

package doom.engine

internal fun DoomEngineCore.I_PostEvent(ev: event_t) { inputQueue.post(ev) }
internal fun DoomEngineCore.I_InitGraphics() {}
internal fun DoomEngineCore.I_ShutdownGraphics() {}
internal fun DoomEngineCore.I_StartFrame() {}
internal fun DoomEngineCore.I_StartTic() { inputQueue.collect() }
internal fun DoomEngineCore.I_UpdateNoBlit() {}

internal fun DoomEngineCore.I_SetPalette(pal: ByteArray, offset: Int = 0) {
    videoOutput.setPalette(pal, offset, gammatable[usegamma])
}

internal fun DoomEngineCore.I_FinishUpdate() { videoOutput.present(screens[0]) }
internal fun DoomEngineCore.I_WaitVBL(count: Int) {}
internal fun DoomEngineCore.I_ReadScreen(scr: ByteArray) { frameBuffers.readScreen(scr) }
