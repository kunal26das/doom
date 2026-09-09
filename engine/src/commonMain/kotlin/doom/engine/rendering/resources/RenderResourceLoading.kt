
package doom.engine.rendering.resources

import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.demoplayback
import doom.engine.geometry.FRACBITS
import doom.engine.rendering.numsprites
import doom.engine.rendering.patchColumnOfs
import doom.engine.rendering.patchLeftOffset
import doom.engine.rendering.patchTopOffset
import doom.engine.rendering.patchWidth
import doom.engine.rendering.skytexture
import doom.engine.rendering.sprites
import doom.engine.resources.wCacheLumpName
import doom.engine.resources.wCacheLumpNum
import doom.engine.resources.wCheckNumForName
import doom.engine.resources.wGetNumForName
import doom.engine.resources.wLumpLength
import doom.engine.resources.i16
import doom.engine.resources.i32
import doom.engine.resources.lumpinfo
import doom.engine.resources.str
import doom.engine.resources.u8
import doom.engine.simulation.Thinker
import doom.engine.simulation.thinkercap
import doom.engine.world.numsectors
import doom.engine.world.numsides
import doom.engine.world.sectors
import doom.engine.world.sides




internal var DoomEngineCore.firstflat
    get() = stateTextureResource.firstflat
    set(value) { stateTextureResource.firstflat = value }
internal var DoomEngineCore.lastflat
    get() = stateTextureResource.lastflat
    set(value) { stateTextureResource.lastflat = value }
internal var DoomEngineCore.numflats
    get() = stateTextureResource.numflats
    set(value) { stateTextureResource.numflats = value }

internal var DoomEngineCore.firstpatch
    get() = stateTextureResource.firstpatch
    set(value) { stateTextureResource.firstpatch = value }
internal var DoomEngineCore.lastpatch
    get() = stateTextureResource.lastpatch
    set(value) { stateTextureResource.lastpatch = value }
internal var DoomEngineCore.numpatches
    get() = stateTextureResource.numpatches
    set(value) { stateTextureResource.numpatches = value }

internal var DoomEngineCore.firstspritelump
    get() = stateTextureResource.firstspritelump
    set(value) { stateTextureResource.firstspritelump = value }
internal var DoomEngineCore.lastspritelump
    get() = stateTextureResource.lastspritelump
    set(value) { stateTextureResource.lastspritelump = value }
internal var DoomEngineCore.numspritelumps
    get() = stateTextureResource.numspritelumps
    set(value) { stateTextureResource.numspritelumps = value }

internal var DoomEngineCore.numtextures
    get() = stateTextureResource.numtextures
    set(value) { stateTextureResource.numtextures = value }
internal var DoomEngineCore.textures: Array<Texture?>
    get() = stateTextureResource.textures
    set(value) { stateTextureResource.textures = value }

internal var DoomEngineCore.texturewidthmask: IntArray
    get() = stateTextureResource.texturewidthmask
    set(value) { stateTextureResource.texturewidthmask = value }

internal var DoomEngineCore.textureheight: IntArray
    get() = stateTextureResource.textureheight
    set(value) { stateTextureResource.textureheight = value }
internal var DoomEngineCore.texturecompositesize: IntArray
    get() = stateTextureResource.texturecompositesize
    set(value) { stateTextureResource.texturecompositesize = value }
internal var DoomEngineCore.texturecolumnlump: Array<ShortArray>
    get() = stateTextureResource.texturecolumnlump
    set(value) { stateTextureResource.texturecolumnlump = value }
internal var DoomEngineCore.texturecolumnofs: Array<IntArray>
    get() = stateTextureResource.texturecolumnofs
    set(value) { stateTextureResource.texturecolumnofs = value }
internal var DoomEngineCore.texturecomposite: Array<ByteArray?>
    get() = stateTextureResource.texturecomposite
    set(value) { stateTextureResource.texturecomposite = value }

internal var DoomEngineCore.flattranslation: IntArray
    get() = stateTextureResource.flattranslation
    set(value) { stateTextureResource.flattranslation = value }
internal var DoomEngineCore.texturetranslation: IntArray
    get() = stateTextureResource.texturetranslation
    set(value) { stateTextureResource.texturetranslation = value }

internal var DoomEngineCore.spritewidth: IntArray
    get() = stateTextureResource.spritewidth
    set(value) { stateTextureResource.spritewidth = value }
internal var DoomEngineCore.spriteoffset: IntArray
    get() = stateTextureResource.spriteoffset
    set(value) { stateTextureResource.spriteoffset = value }
internal var DoomEngineCore.spritetopoffset: IntArray
    get() = stateTextureResource.spritetopoffset
    set(value) { stateTextureResource.spritetopoffset = value }

internal var DoomEngineCore.colormaps: ByteArray
    get() = stateTextureResource.colormaps
    set(value) { stateTextureResource.colormaps = value }

internal var DoomEngineCore.getcolDATA: ByteArray
    get() = stateTextureResource.getcolDATA
    set(value) { stateTextureResource.getcolDATA = value }


internal fun DoomEngineCore.rDrawColumnInCache(
    patch: ByteArray, patchofs: Int,
    cache: ByteArray, cacheofs: Int,
    originy: Int,
    cacheheight: Int,
) {
    var ofs = patchofs

    while (patch.u8(ofs) != 0xff) {
        val source = ofs + 3
        var count = patch.u8(ofs + 1)
        var position = originy + patch.u8(ofs)

        if (position < 0) {
            count += position
            position = 0
        }

        if (position + count > cacheheight)
            count = cacheheight - position

        if (count > 0)
            patch.copyInto(cache, cacheofs + position, source, source + count)

        ofs = ofs + patch.u8(ofs + 1) + 4
    }
}

internal fun DoomEngineCore.rGenerateComposite(texnum: Int) {
    val texture = textures[texnum]!!

    val block = ByteArray(texturecompositesize[texnum])
    texturecomposite[texnum] = block

    val collump = texturecolumnlump[texnum]
    val colofs = texturecolumnofs[texnum]

    for (i in 0 until texture.patchcount) {
        val patch = texture.patches[i]
        val realpatch = wCacheLumpNum(patch.patch)
        val x1 = patch.originx
        var x2 = x1 + patchWidth(realpatch)

        var x = if (x1 < 0) 0 else x1

        if (x2 > texture.width)
            x2 = texture.width

        while (x < x2) {
            if (collump[x] >= 0) {
                x++
                continue
            }

            val patchcol = patchColumnOfs(realpatch, x - x1)
            rDrawColumnInCache(
                realpatch, patchcol,
                block, colofs[x],
                patch.originy,
                texture.height,
            )
            x++
        }
    }

}

internal fun DoomEngineCore.rGenerateLookup(texnum: Int) {
    val texture = textures[texnum]!!

    texturecomposite[texnum] = null

    texturecompositesize[texnum] = 0
    val collump = texturecolumnlump[texnum]
    val colofs = texturecolumnofs[texnum]

    val patchcount = ByteArray(texture.width)

    for (i in 0 until texture.patchcount) {
        val patch = texture.patches[i]
        val realpatch = wCacheLumpNum(patch.patch)
        val x1 = patch.originx
        var x2 = x1 + patchWidth(realpatch)

        var x = if (x1 < 0) 0 else x1

        if (x2 > texture.width)
            x2 = texture.width
        while (x < x2) {
            patchcount[x]++
            collump[x] = patch.patch.toShort()
            colofs[x] = (patchColumnOfs(realpatch, x - x1) + 3) and 0xFFFF
            x++
        }
    }

    for (x in 0 until texture.width) {
        if ((patchcount[x].toInt() and 0xFF) == 0) {
            println("R_GenerateLookup: column without a patch (${texture.name})")
            return
        }

        if ((patchcount[x].toInt() and 0xFF) > 1) {
            collump[x] = -1
            colofs[x] = texturecompositesize[texnum]

            if (texturecompositesize[texnum] > 0x10000 - texture.height) {
                iError("R_GenerateLookup: texture $texnum is >64k")
            }

            texturecompositesize[texnum] += texture.height
        }
    }
}

internal fun DoomEngineCore.rGetColumn(tex: Int, col: Int): Int {
    val c = col and texturewidthmask[tex]
    val lump = texturecolumnlump[tex][c].toInt()
    val ofs = texturecolumnofs[tex][c]

    if (lump > 0) {
        getcolDATA = wCacheLumpNum(lump)
        return ofs
    }

    if (texturecomposite[tex] == null)
        rGenerateComposite(tex)

    getcolDATA = texturecomposite[tex]!!
    return ofs
}

internal fun DoomEngineCore.rInitTextures() {
    val names = wCacheLumpName("PNAMES")
    val nummappatches = names.i32(0)
    val patchlookup = IntArray(nummappatches)

    for (i in 0 until nummappatches) {
        val name = names.str(4 + i * 8, 8)
        patchlookup[i] = wCheckNumForName(name)
    }

    val maptex1 = wCacheLumpName("TEXTURE1")
    var maptex = maptex1
    val numtextures1 = maptex.i32(0)
    var maxoff = wLumpLength(wGetNumForName("TEXTURE1"))

    val maptex2: ByteArray?
    val numtextures2: Int
    val maxoff2: Int

    if (wCheckNumForName("TEXTURE2") != -1) {
        maptex2 = wCacheLumpName("TEXTURE2")
        numtextures2 = maptex2.i32(0)
        maxoff2 = wLumpLength(wGetNumForName("TEXTURE2"))
    } else {
        maptex2 = null
        numtextures2 = 0
        maxoff2 = 0
    }
    numtextures = numtextures1 + numtextures2

    textures = arrayOfNulls(numtextures)
    texturecolumnlump = Array(numtextures) { ShortArray(0) }
    texturecolumnofs = Array(numtextures) { IntArray(0) }
    texturecomposite = arrayOfNulls(numtextures)
    texturecompositesize = IntArray(numtextures)
    texturewidthmask = IntArray(numtextures)
    textureheight = IntArray(numtextures)

    var totalwidth = 0

    val temp1 = wGetNumForName("S_START")
    val temp2 = wGetNumForName("S_END") - 1
    val temp3 = ((temp2 - temp1 + 63) / 64) + ((numtextures + 63) / 64)
    print("[")
    for (i in 0 until temp3)
        print(" ")
    print("         ]")
    for (i in 0 until temp3)
        print("\b")
    print("\b\b\b\b\b\b\b\b\b\b")

    var directory = 4

    for (i in 0 until numtextures) {
        if ((i and 63) == 0)
            print(".")

        if (i == numtextures1) {
            maptex = maptex2!!
            maxoff = maxoff2
            directory = 4
        }

        val offset = maptex.i32(directory)

        if (offset > maxoff)
            iError("R_InitTextures: bad texture directory")

        val texture = Texture()
        textures[i] = texture

        texture.width = maptex.i16(offset + 12)
        texture.height = maptex.i16(offset + 14)
        texture.patchcount = maptex.i16(offset + 20)

        texture.name = maptex.str(offset, 8)

        texture.patches = Array(texture.patchcount) { TexturePatch() }
        for (j in 0 until texture.patchcount) {
            val mpatch = offset + 22 + j * 10
            val patch = texture.patches[j]
            patch.originx = maptex.i16(mpatch + 0)
            patch.originy = maptex.i16(mpatch + 2)
            patch.patch = patchlookup[maptex.i16(mpatch + 4)]
            if (patch.patch == -1) {
                iError("R_InitTextures: Missing patch in texture ${texture.name}")
            }
        }
        texturecolumnlump[i] = ShortArray(texture.width)
        texturecolumnofs[i] = IntArray(texture.width)

        var j = 1
        while (j * 2 <= texture.width)
            j = j shl 1

        texturewidthmask[i] = j - 1
        textureheight[i] = texture.height shl FRACBITS

        totalwidth += texture.width

        directory += 4
    }

    for (i in 0 until numtextures)
        rGenerateLookup(i)

    texturetranslation = IntArray(numtextures + 1)

    for (i in 0 until numtextures)
        texturetranslation[i] = i
}

internal fun DoomEngineCore.rInitFlats() {
    firstflat = wGetNumForName("F_START") + 1
    lastflat = wGetNumForName("F_END") - 1
    numflats = lastflat - firstflat + 1

    flattranslation = IntArray(numflats + 1)

    for (i in 0 until numflats)
        flattranslation[i] = i
}

internal fun DoomEngineCore.rInitSpriteLumps() {
    firstspritelump = wGetNumForName("S_START") + 1
    lastspritelump = wGetNumForName("S_END") - 1

    numspritelumps = lastspritelump - firstspritelump + 1
    spritewidth = IntArray(numspritelumps)
    spriteoffset = IntArray(numspritelumps)
    spritetopoffset = IntArray(numspritelumps)

    for (i in 0 until numspritelumps) {
        if ((i and 63) == 0)
            print(".")

        val patch = wCacheLumpNum(firstspritelump + i)
        spritewidth[i] = patchWidth(patch) shl FRACBITS
        spriteoffset[i] = patchLeftOffset(patch) shl FRACBITS
        spritetopoffset[i] = patchTopOffset(patch) shl FRACBITS
    }
}

internal fun DoomEngineCore.rInitColormaps() {
    val lump = wGetNumForName("COLORMAP")
    colormaps = wCacheLumpNum(lump)
}

internal fun DoomEngineCore.rInitData() {
    rInitTextures()
    print("\nInitTextures")
    rInitFlats()
    print("\nInitFlats")
    rInitSpriteLumps()
    print("\nInitSprites")
    rInitColormaps()
    print("\nInitColormaps")
}

internal fun DoomEngineCore.rFlatNumForName(name: String): Int {
    val i = wCheckNumForName(name)

    if (i == -1) {
        iError("R_FlatNumForName: $name not found")
    }
    return i - firstflat
}

internal fun DoomEngineCore.rCheckTextureNumForName(name: String): Int {
    if (name[0] == '-')
        return 0

    for (i in 0 until numtextures)
        if (textures[i]!!.name.equals(name, ignoreCase = true))
            return i

    return -1
}

internal fun DoomEngineCore.rTextureNumForName(name: String): Int {
    val i = rCheckTextureNumForName(name)

    if (i == -1) {
        iError("R_TextureNumForName: $name not found")
    }
    return i
}

internal var DoomEngineCore.flatmemory
    get() = stateTextureResource.flatmemory
    set(value) { stateTextureResource.flatmemory = value }
internal var DoomEngineCore.texturememory
    get() = stateTextureResource.texturememory
    set(value) { stateTextureResource.texturememory = value }
internal var DoomEngineCore.spritememory
    get() = stateTextureResource.spritememory
    set(value) { stateTextureResource.spritememory = value }

internal fun DoomEngineCore.rPrecacheLevel() {
    if (demoplayback)
        return

    val flatpresent = ByteArray(numflats)

    for (i in 0 until numsectors) {
        flatpresent[sectors[i].floorpic] = 1
        flatpresent[sectors[i].ceilingpic] = 1
    }

    flatmemory = 0

    for (i in 0 until numflats) {
        if (flatpresent[i].toInt() != 0) {
            val lump = firstflat + i
            flatmemory += lumpinfo[lump].size
            wCacheLumpNum(lump)
        }
    }

    val texturepresent = ByteArray(numtextures)

    for (i in 0 until numsides) {
        texturepresent[sides[i].toptexture] = 1
        texturepresent[sides[i].midtexture] = 1
        texturepresent[sides[i].bottomtexture] = 1
    }

    texturepresent[skytexture] = 1

    texturememory = 0
    for (i in 0 until numtextures) {
        if (texturepresent[i].toInt() == 0)
            continue

        val texture = textures[i]!!

        for (j in 0 until texture.patchcount) {
            val lump = texture.patches[j].patch
            texturememory += lumpinfo[lump].size
            wCacheLumpNum(lump)
        }
    }

    val spritepresent = ByteArray(numsprites)

    var th: Thinker? = thinkercap.next
    while (th !== thinkercap) {
        if (th is Actor)
            spritepresent[th.sprite] = 1
        th = th!!.next
    }

    spritememory = 0
    for (i in 0 until numsprites) {
        if (spritepresent[i].toInt() == 0)
            continue

        for (j in 0 until sprites[i].numframes) {
            val sf = sprites[i].spriteframes[j]!!
            for (k in 0 until 8) {
                val lump = firstspritelump + sf.lump[k]
                spritememory += lumpinfo[lump].size
                wCacheLumpNum(lump)
            }
        }
    }
}
