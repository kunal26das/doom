// KMP replacement for linuxdoom-1.10 i_video.c -- frame output and input events.
// The engine renders palettized 320x200 into screens[0]; I_FinishUpdate expands
// it through the current palette into ARGB and hands it to the host app.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

import kotlinx.coroutines.channels.Channel

/** Host app sink: receives a 320*200 ARGB frame each I_FinishUpdate. */
var I_VideoSink: ((IntArray) -> Unit)? = null

/** Input events from the host UI thread(s), drained on the game loop by I_StartTic. */
val eventChannel = Channel<event_t>(Channel.UNLIMITED)

fun I_PostEvent(ev: event_t) {
    eventChannel.trySend(ev)
}

private val palette = IntArray(256)          // current ARGB palette (gamma applied)
private val frame = IntArray(SCREENWIDTH * SCREENHEIGHT)

fun I_InitGraphics() {}

fun I_ShutdownGraphics() {}

fun I_StartFrame() {}

/** called by D_DoomLoop, called before processing each tic in a frame. */
fun I_StartTic() {
    while (true) {
        val ev = eventChannel.tryReceive().getOrNull() ?: break
        D_PostEvent(ev)
    }
}

/** called by D_DoomLoop, called before processing any tics in a frame. */
fun I_UpdateNoBlit() {}

/** takes full 8 bit values (from PLAYPAL), applies gammatable[usegamma]. */
fun I_SetPalette(pal: ByteArray, offset: Int = 0) {
    val gt = gammatable[usegamma]
    for (i in 0 until 256) {
        val r = gt[pal.u8(offset + i * 3)]
        val g = gt[pal.u8(offset + i * 3 + 1)]
        val b = gt[pal.u8(offset + i * 3 + 2)]
        palette[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
}

fun I_FinishUpdate() {
    val src = screens[0]
    for (i in 0 until SCREENWIDTH * SCREENHEIGHT) {
        frame[i] = palette[src[i].toInt() and 0xFF]
    }
    I_VideoSink?.invoke(frame)
}

fun I_WaitVBL(count: Int) {}

fun I_ReadScreen(scr: ByteArray) {
    screens[0].copyInto(scr)
}
