
package doom.engine.rendering

import doom.engine.SCREENHEIGHT
import doom.engine.SCREENWIDTH
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.gamemode
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FixedPoint
import doom.engine.rendering.resources.colormaps
import doom.engine.resources.wCacheLumpName

internal const val MAXWIDTH = 1120
internal const val MAXHEIGHT = 832

internal const val SBARHEIGHT = 32


internal var DoomEngineCore.viewimage: ByteArray
    get() = stateRasterizer.viewimage
    set(value) { stateRasterizer.viewimage = value }
internal var DoomEngineCore.viewwidth
    get() = stateRasterizer.viewwidth
    set(value) { stateRasterizer.viewwidth = value }
internal var DoomEngineCore.scaledviewwidth
    get() = stateRasterizer.scaledviewwidth
    set(value) { stateRasterizer.scaledviewwidth = value }
internal var DoomEngineCore.viewheight
    get() = stateRasterizer.viewheight
    set(value) { stateRasterizer.viewheight = value }
internal var DoomEngineCore.viewwindowx
    get() = stateRasterizer.viewwindowx
    set(value) { stateRasterizer.viewwindowx = value }
internal var DoomEngineCore.viewwindowy
    get() = stateRasterizer.viewwindowy
    set(value) { stateRasterizer.viewwindowy = value }

internal val DoomEngineCore.ylookup
    get() = stateRasterizer.ylookup
internal val DoomEngineCore.columnofs
    get() = stateRasterizer.columnofs

internal val DoomEngineCore.translations
    get() = stateRasterizer.translations

internal var DoomEngineCore.dcColormap
    get() = stateRasterizer.dcColormap
    set(value) { stateRasterizer.dcColormap = value }
internal var DoomEngineCore.dcX
    get() = stateRasterizer.dcX
    set(value) { stateRasterizer.dcX = value }
internal var DoomEngineCore.dcYl
    get() = stateRasterizer.dcYl
    set(value) { stateRasterizer.dcYl = value }
internal var DoomEngineCore.dcYh
    get() = stateRasterizer.dcYh
    set(value) { stateRasterizer.dcYh = value }
internal var DoomEngineCore.dcIscale: FixedPoint
    get() = stateRasterizer.dcIscale
    set(value) { stateRasterizer.dcIscale = value }
internal var DoomEngineCore.dcTexturemid: FixedPoint
    get() = stateRasterizer.dcTexturemid
    set(value) { stateRasterizer.dcTexturemid = value }

internal var DoomEngineCore.dcSource: ByteArray
    get() = stateRasterizer.dcSource
    set(value) { stateRasterizer.dcSource = value }
internal var DoomEngineCore.dcSourceOfs
    get() = stateRasterizer.dcSourceOfs
    set(value) { stateRasterizer.dcSourceOfs = value }

internal var DoomEngineCore.dccount
    get() = stateRasterizer.dccount
    set(value) { stateRasterizer.dccount = value }

internal fun DoomEngineCore.rDrawColumn() {
    var count: Int
    var dest: Int
    var frac: FixedPoint
    val fracstep: FixedPoint

    count = dcYh - dcYl

    if (count < 0)
        return

    if (dcX.toUInt() >= SCREENWIDTH.toUInt()
        || dcYl < 0
        || dcYh >= SCREENHEIGHT
    )
        iError("R_DrawColumn: $dcYl to $dcYh at $dcX")

    dest = ylookup[dcYl] + columnofs[dcX]

    fracstep = dcIscale
    frac = dcTexturemid + (dcYl - centery) * fracstep

    do {
        screens[0][dest] =
            colormaps[dcColormap + dcSource.srcByte(dcSourceOfs + ((frac shr FRACBITS) and 127))]

        dest += SCREENWIDTH
        frac += fracstep

        count--
    } while (count >= 0)
}

internal fun DoomEngineCore.rDrawColumnLow() {
    var count: Int
    var dest: Int
    var dest2: Int
    var frac: FixedPoint
    val fracstep: FixedPoint

    count = dcYh - dcYl

    if (count < 0)
        return

    if (dcX.toUInt() >= SCREENWIDTH.toUInt()
        || dcYl < 0
        || dcYh >= SCREENHEIGHT
    ) {
        iError("R_DrawColumn: $dcYl to $dcYh at $dcX")
    }

    dcX = dcX shl 1

    dest = ylookup[dcYl] + columnofs[dcX]
    dest2 = ylookup[dcYl] + columnofs[dcX + 1]

    fracstep = dcIscale
    frac = dcTexturemid + (dcYl - centery) * fracstep

    do {
        val pixel =
            colormaps[dcColormap + dcSource.srcByte(dcSourceOfs + ((frac shr FRACBITS) and 127))]
        screens[0][dest2] = pixel
        screens[0][dest] = pixel
        dest += SCREENWIDTH
        dest2 += SCREENWIDTH
        frac += fracstep

        count--
    } while (count >= 0)
}

internal const val FUZZTABLE = 50
internal const val FUZZOFF = SCREENWIDTH

internal val DoomEngineCore.fuzzoffset
    get() = stateRasterizer.fuzzoffset

internal var DoomEngineCore.fuzzpos
    get() = stateRasterizer.fuzzpos
    set(value) { stateRasterizer.fuzzpos = value }

internal fun DoomEngineCore.rDrawFuzzColumn() {
    var count: Int
    var dest: Int
    var frac: FixedPoint
    val fracstep: FixedPoint

    if (dcYl == 0)
        dcYl = 1

    if (dcYh == viewheight - 1)
        dcYh = viewheight - 2

    count = dcYh - dcYl

    if (count < 0)
        return

    if (dcX.toUInt() >= SCREENWIDTH.toUInt()
        || dcYl < 0 || dcYh >= SCREENHEIGHT
    ) {
        iError("R_DrawFuzzColumn: $dcYl to $dcYh at $dcX")
    }


    dest = ylookup[dcYl] + columnofs[dcX]

    fracstep = dcIscale
    frac = dcTexturemid + (dcYl - centery) * fracstep

    do {
        screens[0][dest] =
            colormaps[6 * 256 + (screens[0][dest + fuzzoffset[fuzzpos]].toInt() and 0xFF)]

        fuzzpos++
        if (fuzzpos == FUZZTABLE)
            fuzzpos = 0

        dest += SCREENWIDTH

        frac += fracstep

        count--
    } while (count >= 0)
}

internal var DoomEngineCore.dcTranslation
    get() = stateRasterizer.dcTranslation
    set(value) { stateRasterizer.dcTranslation = value }
internal var DoomEngineCore.translationtables: ByteArray
    get() = stateRasterizer.translationtables
    set(value) { stateRasterizer.translationtables = value }

internal fun DoomEngineCore.rDrawTranslatedColumn() {
    var count: Int
    var dest: Int
    var frac: FixedPoint
    val fracstep: FixedPoint

    count = dcYh - dcYl
    if (count < 0)
        return

    if (dcX.toUInt() >= SCREENWIDTH.toUInt()
        || dcYl < 0
        || dcYh >= SCREENHEIGHT
    ) {
        iError("R_DrawColumn: $dcYl to $dcYh at $dcX")
    }


    dest = ylookup[dcYl] + columnofs[dcX]

    fracstep = dcIscale
    frac = dcTexturemid + (dcYl - centery) * fracstep

    do {
        screens[0][dest] = colormaps[dcColormap +
            (translationtables[dcTranslation +
                dcSource.srcByte(dcSourceOfs + (frac shr FRACBITS))].toInt() and 0xFF)]
        dest += SCREENWIDTH

        frac += fracstep

        count--
    } while (count >= 0)
}

internal fun DoomEngineCore.rInitTranslationTables() {
    translationtables = ByteArray(256 * 3)

    for (i in 0 until 256) {
        if (i >= 0x70 && i <= 0x7f) {
            translationtables[i] = (0x60 + (i and 0xf)).toByte()
            translationtables[i + 256] = (0x40 + (i and 0xf)).toByte()
            translationtables[i + 512] = (0x20 + (i and 0xf)).toByte()
        } else {
            translationtables[i] = i.toByte()
            translationtables[i + 256] = i.toByte()
            translationtables[i + 512] = i.toByte()
        }
    }
}

internal var DoomEngineCore.dsY
    get() = stateRasterizer.dsY
    set(value) { stateRasterizer.dsY = value }
internal var DoomEngineCore.dsX1
    get() = stateRasterizer.dsX1
    set(value) { stateRasterizer.dsX1 = value }
internal var DoomEngineCore.dsX2
    get() = stateRasterizer.dsX2
    set(value) { stateRasterizer.dsX2 = value }

internal var DoomEngineCore.dsColormap
    get() = stateRasterizer.dsColormap
    set(value) { stateRasterizer.dsColormap = value }

internal var DoomEngineCore.dsXfrac: FixedPoint
    get() = stateRasterizer.dsXfrac
    set(value) { stateRasterizer.dsXfrac = value }
internal var DoomEngineCore.dsYfrac: FixedPoint
    get() = stateRasterizer.dsYfrac
    set(value) { stateRasterizer.dsYfrac = value }
internal var DoomEngineCore.dsXstep: FixedPoint
    get() = stateRasterizer.dsXstep
    set(value) { stateRasterizer.dsXstep = value }
internal var DoomEngineCore.dsYstep: FixedPoint
    get() = stateRasterizer.dsYstep
    set(value) { stateRasterizer.dsYstep = value }

internal var DoomEngineCore.dsSource: ByteArray
    get() = stateRasterizer.dsSource
    set(value) { stateRasterizer.dsSource = value }
internal var DoomEngineCore.dsSourceOfs
    get() = stateRasterizer.dsSourceOfs
    set(value) { stateRasterizer.dsSourceOfs = value }

internal var DoomEngineCore.dscount
    get() = stateRasterizer.dscount
    set(value) { stateRasterizer.dscount = value }

internal fun DoomEngineCore.rDrawSpan() {
    var xfrac: FixedPoint
    var yfrac: FixedPoint
    var dest: Int
    var count: Int
    var spot: Int

    if (dsX2 < dsX1
        || dsX1 < 0
        || dsX2 >= SCREENWIDTH
        || dsY.toUInt() > SCREENHEIGHT.toUInt()
    ) {
        iError("R_DrawSpan: $dsX1 to $dsX2 at $dsY")
    }

    xfrac = dsXfrac
    yfrac = dsYfrac

    dest = ylookup[dsY] + columnofs[dsX1]

    count = dsX2 - dsX1

    do {
        spot = ((yfrac shr (16 - 6)) and (63 * 64)) + ((xfrac shr 16) and 63)

        screens[0][dest] =
            colormaps[dsColormap + dsSource.srcByte(dsSourceOfs + spot)]
        dest++

        xfrac += dsXstep
        yfrac += dsYstep

        count--
    } while (count >= 0)
}

internal fun DoomEngineCore.rDrawSpanLow() {
    var xfrac: FixedPoint
    var yfrac: FixedPoint
    var dest: Int
    var count: Int
    var spot: Int

    if (dsX2 < dsX1
        || dsX1 < 0
        || dsX2 >= SCREENWIDTH
        || dsY.toUInt() > SCREENHEIGHT.toUInt()
    ) {
        iError("R_DrawSpan: $dsX1 to $dsX2 at $dsY")
    }

    xfrac = dsXfrac
    yfrac = dsYfrac

    dsX1 = dsX1 shl 1
    dsX2 = dsX2 shl 1

    dest = ylookup[dsY] + columnofs[dsX1]

    count = dsX2 - dsX1
    do {
        spot = ((yfrac shr (16 - 6)) and (63 * 64)) + ((xfrac shr 16) and 63)
        screens[0][dest] =
            colormaps[dsColormap + dsSource.srcByte(dsSourceOfs + spot)]
        dest++
        screens[0][dest] =
            colormaps[dsColormap + dsSource.srcByte(dsSourceOfs + spot)]
        dest++

        xfrac += dsXstep
        yfrac += dsYstep

        count--
    } while (count >= 0)
}

internal fun DoomEngineCore.rInitBuffer(width: Int, height: Int) {
    viewwindowx = (SCREENWIDTH - width) shr 1

    for (i in 0 until width)
        columnofs[i] = viewwindowx + i

    if (width == SCREENWIDTH)
        viewwindowy = 0
    else
        viewwindowy = (SCREENHEIGHT - SBARHEIGHT - height) shr 1

    for (i in 0 until height)
        ylookup[i] = (i + viewwindowy) * SCREENWIDTH
}

internal fun DoomEngineCore.rFillBackScreen() {
    val src: ByteArray
    var dest: Int
    var x: Int
    var y: Int
    var patch: ByteArray

    val name1 = "FLOOR7_2"

    val name2 = "GRNROCK"

    val name: String

    if (scaledviewwidth == 320)
        return

    if (gamemode == COMMERCIAL)
        name = name2
    else
        name = name1

    src = wCacheLumpName(name)
    dest = 0

    for (y in 0 until SCREENHEIGHT - SBARHEIGHT) {
        for (x in 0 until SCREENWIDTH / 64) {
            src.copyInto(screens[1], dest, (y and 63) shl 6, ((y and 63) shl 6) + 64)
            dest += 64
        }

        if ((SCREENWIDTH and 63) != 0) {
            src.copyInto(screens[1], dest, (y and 63) shl 6, ((y and 63) shl 6) + (SCREENWIDTH and 63))
            dest += (SCREENWIDTH and 63)
        }
    }

    patch = wCacheLumpName("brdr_t")

    x = 0
    while (x < scaledviewwidth) {
        vDrawPatch(viewwindowx + x, viewwindowy - 8, 1, patch)
        x += 8
    }
    patch = wCacheLumpName("brdr_b")

    x = 0
    while (x < scaledviewwidth) {
        vDrawPatch(viewwindowx + x, viewwindowy + viewheight, 1, patch)
        x += 8
    }
    patch = wCacheLumpName("brdr_l")

    y = 0
    while (y < viewheight) {
        vDrawPatch(viewwindowx - 8, viewwindowy + y, 1, patch)
        y += 8
    }
    patch = wCacheLumpName("brdr_r")

    y = 0
    while (y < viewheight) {
        vDrawPatch(viewwindowx + scaledviewwidth, viewwindowy + y, 1, patch)
        y += 8
    }

    vDrawPatch(viewwindowx - 8,
        viewwindowy - 8,
        1,
        wCacheLumpName("brdr_tl"))

    vDrawPatch(viewwindowx + scaledviewwidth,
        viewwindowy - 8,
        1,
        wCacheLumpName("brdr_tr"))

    vDrawPatch(viewwindowx - 8,
        viewwindowy + viewheight,
        1,
        wCacheLumpName("brdr_bl"))

    vDrawPatch(viewwindowx + scaledviewwidth,
        viewwindowy + viewheight,
        1,
        wCacheLumpName("brdr_br"))
}

internal fun DoomEngineCore.rVideoErase(ofs: Int, count: Int) {
    screens[1].copyInto(screens[0], ofs, ofs, ofs + count)
}

internal fun DoomEngineCore.rDrawViewBorder() {
    val top: Int
    var side: Int
    var ofs: Int

    if (scaledviewwidth == SCREENWIDTH)
        return

    top = ((SCREENHEIGHT - SBARHEIGHT) - viewheight) / 2
    side = (SCREENWIDTH - scaledviewwidth) / 2

    rVideoErase(0, top * SCREENWIDTH + side)

    ofs = (viewheight + top) * SCREENWIDTH - side
    rVideoErase(ofs, top * SCREENWIDTH + side)

    ofs = top * SCREENWIDTH + SCREENWIDTH - side
    side = side shl 1

    for (i in 1 until viewheight) {
        rVideoErase(ofs, side)
        ofs += SCREENWIDTH
    }

    vMarkRect(0, 0, SCREENWIDTH, SCREENHEIGHT - SBARHEIGHT)
}

internal fun ByteArray.srcByte(i: Int): Int {
    val ci = if (i < 0) 0 else if (i >= size) size - 1 else i
    return this[ci].toInt() and 0xFF
}
