
package doom.engine.rendering

import doom.engine.core.DoomEngineCore

internal val DoomEngineCore.screens: FrameBuffers get() = frameBuffers
internal val DoomEngineCore.dirtybox: IntArray get() = frameBuffers.dirtyRectangle()
internal var DoomEngineCore.usegamma: Int
    get() = videoOutput.gamma
    set(value) { videoOutput.setGamma(value) }

internal fun DoomEngineCore.patchWidth(p: ByteArray): Int = PatchFormat.width(p)
internal fun DoomEngineCore.patchHeight(p: ByteArray): Int = PatchFormat.height(p)
internal fun DoomEngineCore.patchLeftOffset(p: ByteArray): Int = PatchFormat.leftOffset(p)
internal fun DoomEngineCore.patchTopOffset(p: ByteArray): Int = PatchFormat.topOffset(p)
internal fun DoomEngineCore.patchColumnOfs(p: ByteArray, col: Int): Int = PatchFormat.columnOffset(p, col)

internal fun DoomEngineCore.vMarkRect(x: Int, y: Int, width: Int, height: Int) =
    frameBuffers.markRect(x, y, width, height)

internal fun DoomEngineCore.vCopyRect(
    srcx: Int, srcy: Int, srcscrn: Int,
    width: Int, height: Int,
    destx: Int, desty: Int, destscrn: Int,
) = frameBuffers.copyRect(srcx, srcy, srcscrn, width, height, destx, desty, destscrn)

internal fun DoomEngineCore.vDrawPatch(x0: Int, y0: Int, scrn: Int, patch: ByteArray) =
    frameBuffers.drawPatch(x0, y0, scrn, patch)

internal fun DoomEngineCore.vDrawPatchDirect(x: Int, y: Int, scrn: Int, patch: ByteArray) =
    frameBuffers.drawPatch(x, y, scrn, patch)

internal fun DoomEngineCore.vDrawBlock(x: Int, y: Int, scrn: Int, width: Int, height: Int, src: ByteArray) =
    frameBuffers.drawBlock(x, y, scrn, width, height, src)

internal fun DoomEngineCore.vGetBlock(x: Int, y: Int, scrn: Int, width: Int, height: Int, dest: ByteArray) =
    frameBuffers.getBlock(x, y, scrn, width, height, dest)

internal fun DoomEngineCore.vInit() {}
