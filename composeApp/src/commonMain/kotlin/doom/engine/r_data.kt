// Port of linuxdoom-1.10 r_data.c -- Preparation of data for rendering,
// generation of lookups, caching, retrieval by name.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "MagicNumber", "ktlint")

package doom.engine

//
// Graphics.
// DOOM graphics for walls and sprites
// is stored in vertical runs of opaque pixels (posts).
// A column is composed of zero or more posts,
// a patch or sprite is composed of zero or more columns.
//

//
// Texture definition.
// Each texture is composed of one or more patches,
// with patches being lumps stored in the WAD.
// The lumps are referenced by number, and patched
// into the rectangular texture space using origin
// and possibly other attributes.
//
// mappatch_t is read straight from the TEXTURE1/TEXTURE2 lump bytes:
//   short originx   +0
//   short originy   +2
//   short patch     +4
//   short stepdir   +6
//   short colormap  +8
// (10 bytes per entry)

//
// Texture definition.
// A DOOM wall texture is a list of patches
// which are to be combined in a predefined order.
//
// maptexture_t is read straight from the TEXTURE1/TEXTURE2 lump bytes:
//   char  name[8]           +0
//   boolean masked          +8   (4 bytes)
//   short width             +12
//   short height            +14
//   void** columndirectory  +16  // OBSOLETE (4 bytes)
//   short patchcount        +20
//   mappatch_t patches[]    +22

// A single patch from a texture definition,
//  basically a rectangular area within
//  the texture rectangle.
class texpatch_t {
    // Block origin (allways UL),
    // which has allready accounted
    // for the internal origin of the patch.
    var originx = 0
    var originy = 0
    var patch = 0
}

// A maptexturedef_t describes a rectangular texture,
//  which is composed of one or more mappatch_t structures
//  that arrange graphic patches.
class texture_t {
    // Keep name for switch changing, etc.
    var name: String = ""
    var width = 0
    var height = 0

    // All the patches[patchcount]
    //  are drawn back to front into the cached texture.
    var patchcount = 0
    var patches: Array<texpatch_t> = emptyArray()
}

var firstflat = 0
var lastflat = 0
var numflats = 0

var firstpatch = 0
var lastpatch = 0
var numpatches = 0

var firstspritelump = 0
var lastspritelump = 0
var numspritelumps = 0

var numtextures = 0
var textures: Array<texture_t?> = emptyArray()

var texturewidthmask: IntArray = IntArray(0)

// needed for texture pegging
var textureheight: IntArray = IntArray(0)  // fixed_t[]
var texturecompositesize: IntArray = IntArray(0)
var texturecolumnlump: Array<ShortArray> = emptyArray()
var texturecolumnofs: Array<IntArray> = emptyArray()  // unsigned short in C
var texturecomposite: Array<ByteArray?> = emptyArray()

// for global animation
var flattranslation: IntArray = IntArray(0)
var texturetranslation: IntArray = IntArray(0)

// needed for pre rendering
var spritewidth: IntArray = IntArray(0)      // fixed_t[]
var spriteoffset: IntArray = IntArray(0)     // fixed_t[]
var spritetopoffset: IntArray = IntArray(0)  // fixed_t[]

// lighttable_t* colormaps: every C lighttable_t* becomes an Int byte offset
// into this array (-1 == NULL). Pixel mapping: colormaps[offset + pixelbyte].
var colormaps: ByteArray = ByteArray(0)

// R_GetColumn contract: the ByteArray holding the column whose byte offset
// R_GetColumn just returned (either a cached patch lump or a composite).
var GETCOL_DATA: ByteArray = ByteArray(0)

//
// MAPTEXTURE_T CACHING
// When a texture is first needed,
//  it counts the number of composite columns
//  required in the texture and allocates space
//  for a column directory and any new columns.
// The directory will simply point inside other patches
//  if there is only one patch in a given column,
//  but any columns with multiple patches
//  will have new column_ts generated.
//

//
// R_DrawColumnInCache
// Clip and draw a column
//  from a patch into a cached post.
//
fun R_DrawColumnInCache(
    patch: ByteArray, patchofs: Int,  // column_t* patch
    cache: ByteArray, cacheofs: Int,  // byte* cache
    originy: Int,
    cacheheight: Int,
) {
    var ofs = patchofs

    // dest = (byte *)cache + 3;  (set but never used in vanilla)
    val dest = cacheofs + 3

    while (patch.u8(ofs) != 0xff) {  // patch->topdelta != 0xff
        val source = ofs + 3         // (byte *)patch + 3
        var count = patch.u8(ofs + 1)              // patch->length
        var position = originy + patch.u8(ofs)     // originy + patch->topdelta

        if (position < 0) {
            count += position
            position = 0
        }

        if (position + count > cacheheight)
            count = cacheheight - position

        if (count > 0)
            patch.copyInto(cache, cacheofs + position, source, source + count)

        ofs = ofs + patch.u8(ofs + 1) + 4  // patch = (column_t *)((byte *)patch + patch->length + 4)
    }
}

//
// R_GenerateComposite
// Using the texture definition,
//  the composite texture is created from the patches,
//  and each column is cached.
//
fun R_GenerateComposite(texnum: Int) {
    val texture = textures[texnum]!!

    val block = ByteArray(texturecompositesize[texnum])
    texturecomposite[texnum] = block

    val collump = texturecolumnlump[texnum]
    val colofs = texturecolumnofs[texnum]

    // Composite the columns together.
    for (i in 0 until texture.patchcount) {
        val patch = texture.patches[i]
        val realpatch = W_CacheLumpNum(patch.patch)
        val x1 = patch.originx
        var x2 = x1 + patchWidth(realpatch)

        var x = if (x1 < 0) 0 else x1

        if (x2 > texture.width)
            x2 = texture.width

        while (x < x2) {
            // Column does not have multiple patches?
            if (collump[x] >= 0) {
                x++
                continue
            }

            val patchcol = patchColumnOfs(realpatch, x - x1)
            R_DrawColumnInCache(
                realpatch, patchcol,
                block, colofs[x],
                patch.originy,
                texture.height,
            )
            x++
        }
    }

    // Now that the texture has been built in column cache,
    //  it is purgable from zone memory. (Z_ChangeTag deleted.)
}

//
// R_GenerateLookup
//
fun R_GenerateLookup(texnum: Int) {
    val texture = textures[texnum]!!

    // Composited texture not created yet.
    texturecomposite[texnum] = null

    texturecompositesize[texnum] = 0
    val collump = texturecolumnlump[texnum]
    val colofs = texturecolumnofs[texnum]

    // Now count the number of columns
    //  that are covered by more than one patch.
    // Fill in the lump / offset, so columns
    //  with only a single patch are all done.
    val patchcount = ByteArray(texture.width)  // patchcount[texture->width]

    for (i in 0 until texture.patchcount) {
        val patch = texture.patches[i]
        val realpatch = W_CacheLumpNum(patch.patch)
        val x1 = patch.originx
        var x2 = x1 + patchWidth(realpatch)

        var x = if (x1 < 0) 0 else x1

        if (x2 > texture.width)
            x2 = texture.width
        while (x < x2) {
            patchcount[x]++
            collump[x] = patch.patch.toShort()
            colofs[x] = (patchColumnOfs(realpatch, x - x1) + 3) and 0xFFFF  // unsigned short in C
            x++
        }
    }

    for (x in 0 until texture.width) {
        if ((patchcount[x].toInt() and 0xFF) == 0) {
            println("R_GenerateLookup: column without a patch (${texture.name})")
            return
        }
        // I_Error ("R_GenerateLookup: column without a patch");

        if ((patchcount[x].toInt() and 0xFF) > 1) {
            // Use the cached block.
            collump[x] = -1
            colofs[x] = texturecompositesize[texnum]

            if (texturecompositesize[texnum] > 0x10000 - texture.height) {
                I_Error("R_GenerateLookup: texture $texnum is >64k")
            }

            texturecompositesize[texnum] += texture.height
        }
    }
}

//
// R_GetColumn
// Sets GETCOL_DATA to the ByteArray holding the column and returns the
// column's byte offset in it (C returned byte*).
//
fun R_GetColumn(tex: Int, col: Int): Int {
    val c = col and texturewidthmask[tex]
    val lump = texturecolumnlump[tex][c].toInt()
    val ofs = texturecolumnofs[tex][c]

    if (lump > 0) {
        GETCOL_DATA = W_CacheLumpNum(lump)
        return ofs
    }

    if (texturecomposite[tex] == null)
        R_GenerateComposite(tex)

    GETCOL_DATA = texturecomposite[tex]!!
    return ofs
}

//
// R_InitTextures
// Initializes the texture list
//  with the textures from the world map.
//
fun R_InitTextures() {
    // Load the patch names from pnames.lmp.
    val names = W_CacheLumpName("PNAMES")
    val nummappatches = names.i32(0)
    val patchlookup = IntArray(nummappatches)

    for (i in 0 until nummappatches) {
        val name = names.str(4 + i * 8, 8)
        patchlookup[i] = W_CheckNumForName(name)
    }

    // Load the map texture definitions from textures.lmp.
    // The data is contained in one or two lumps,
    //  TEXTURE1 for shareware, plus TEXTURE2 for commercial.
    val maptex1 = W_CacheLumpName("TEXTURE1")
    var maptex = maptex1
    val numtextures1 = maptex.i32(0)
    var maxoff = W_LumpLength(W_GetNumForName("TEXTURE1"))

    val maptex2: ByteArray?
    val numtextures2: Int
    val maxoff2: Int

    if (W_CheckNumForName("TEXTURE2") != -1) {
        maptex2 = W_CacheLumpName("TEXTURE2")
        numtextures2 = maptex2.i32(0)
        maxoff2 = W_LumpLength(W_GetNumForName("TEXTURE2"))
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

    //	Really complex printing shit...
    val temp1 = W_GetNumForName("S_START")  // P_???????
    val temp2 = W_GetNumForName("S_END") - 1
    val temp3 = ((temp2 - temp1 + 63) / 64) + ((numtextures + 63) / 64)
    print("[")
    for (i in 0 until temp3)
        print(" ")
    print("         ]")
    for (i in 0 until temp3)
        print("\b")
    print("\b\b\b\b\b\b\b\b\b\b")

    var directory = 4  // directory = maptex+1 (byte offset into maptex)

    for (i in 0 until numtextures) {
        if ((i and 63) == 0)
            print(".")

        if (i == numtextures1) {
            // Start looking in second texture file.
            maptex = maptex2!!
            maxoff = maxoff2
            directory = 4
        }

        val offset = maptex.i32(directory)

        if (offset > maxoff)
            I_Error("R_InitTextures: bad texture directory")

        // mtexture = (maptexture_t *)((byte *)maptex + offset)
        val texture = texture_t()
        textures[i] = texture

        texture.width = maptex.i16(offset + 12)   // SHORT(mtexture->width)
        texture.height = maptex.i16(offset + 14)  // SHORT(mtexture->height)
        texture.patchcount = maptex.i16(offset + 20)  // SHORT(mtexture->patchcount)

        texture.name = maptex.str(offset, 8)  // memcpy (texture->name, mtexture->name, 8)

        texture.patches = Array(texture.patchcount) { texpatch_t() }
        for (j in 0 until texture.patchcount) {
            val mpatch = offset + 22 + j * 10  // &mtexture->patches[j]
            val patch = texture.patches[j]
            patch.originx = maptex.i16(mpatch + 0)
            patch.originy = maptex.i16(mpatch + 2)
            patch.patch = patchlookup[maptex.i16(mpatch + 4)]
            if (patch.patch == -1) {
                I_Error("R_InitTextures: Missing patch in texture ${texture.name}")
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

    // Precalculate whatever possible.
    for (i in 0 until numtextures)
        R_GenerateLookup(i)

    // Create translation table for global animation.
    texturetranslation = IntArray(numtextures + 1)

    for (i in 0 until numtextures)
        texturetranslation[i] = i
}

//
// R_InitFlats
//
fun R_InitFlats() {
    firstflat = W_GetNumForName("F_START") + 1
    lastflat = W_GetNumForName("F_END") - 1
    numflats = lastflat - firstflat + 1

    // Create translation table for global animation.
    flattranslation = IntArray(numflats + 1)

    for (i in 0 until numflats)
        flattranslation[i] = i
}

//
// R_InitSpriteLumps
// Finds the width and hoffset of all sprites in the wad,
//  so the sprite does not need to be cached completely
//  just for having the header info ready during rendering.
//
fun R_InitSpriteLumps() {
    firstspritelump = W_GetNumForName("S_START") + 1
    lastspritelump = W_GetNumForName("S_END") - 1

    numspritelumps = lastspritelump - firstspritelump + 1
    spritewidth = IntArray(numspritelumps)
    spriteoffset = IntArray(numspritelumps)
    spritetopoffset = IntArray(numspritelumps)

    for (i in 0 until numspritelumps) {
        if ((i and 63) == 0)
            print(".")

        val patch = W_CacheLumpNum(firstspritelump + i)
        spritewidth[i] = patchWidth(patch) shl FRACBITS
        spriteoffset[i] = patchLeftOffset(patch) shl FRACBITS
        spritetopoffset[i] = patchTopOffset(patch) shl FRACBITS
    }
}

//
// R_InitColormaps
//
fun R_InitColormaps() {
    // Load in the light tables,
    //  256 byte align tables. (alignment trick deleted with Z_Malloc)
    val lump = W_GetNumForName("COLORMAP")
    colormaps = W_CacheLumpNum(lump)
}

//
// R_InitData
// Locates all the lumps
//  that will be used by all views
// Must be called after W_Init.
//
fun R_InitData() {
    R_InitTextures()
    print("\nInitTextures")
    R_InitFlats()
    print("\nInitFlats")
    R_InitSpriteLumps()
    print("\nInitSprites")
    R_InitColormaps()
    print("\nInitColormaps")
}

//
// R_FlatNumForName
// Retrieval, get a flat number for a flat name.
//
fun R_FlatNumForName(name: String): Int {
    val i = W_CheckNumForName(name)

    if (i == -1) {
        I_Error("R_FlatNumForName: $name not found")
    }
    return i - firstflat
}

//
// R_CheckTextureNumForName
// Check whether texture is available.
// Filter out NoTexture indicator.
//
fun R_CheckTextureNumForName(name: String): Int {
    // "NoTexture" marker.
    if (name[0] == '-')
        return 0

    for (i in 0 until numtextures)
        if (textures[i]!!.name.equals(name, ignoreCase = true))  // strncasecmp(...,8)
            return i

    return -1
}

//
// R_TextureNumForName
// Calls R_CheckTextureNumForName,
//  aborts with error message.
//
fun R_TextureNumForName(name: String): Int {
    val i = R_CheckTextureNumForName(name)

    if (i == -1) {
        I_Error("R_TextureNumForName: $name not found")
    }
    return i
}

//
// R_PrecacheLevel
// Preloads all relevant graphics for the level.
//
var flatmemory = 0
var texturememory = 0
var spritememory = 0

fun R_PrecacheLevel() {
    if (demoplayback)
        return

    // Precache flats.
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
            W_CacheLumpNum(lump)
        }
    }

    // Precache textures.
    val texturepresent = ByteArray(numtextures)

    for (i in 0 until numsides) {
        texturepresent[sides[i].toptexture] = 1
        texturepresent[sides[i].midtexture] = 1
        texturepresent[sides[i].bottomtexture] = 1
    }

    // Sky texture is always present.
    // Note that F_SKY1 is the name used to
    //  indicate a sky floor/ceiling as a flat,
    //  while the sky texture is stored like
    //  a wall texture, with an episode dependend
    //  name.
    texturepresent[skytexture] = 1

    texturememory = 0
    for (i in 0 until numtextures) {
        if (texturepresent[i].toInt() == 0)
            continue

        val texture = textures[i]!!

        for (j in 0 until texture.patchcount) {
            val lump = texture.patches[j].patch
            texturememory += lumpinfo[lump].size
            W_CacheLumpNum(lump)
        }
    }

    // Precache sprites.
    val spritepresent = ByteArray(numsprites)

    var th: thinker_t? = thinkercap.next
    while (th !== thinkercap) {
        if (th is mobj_t)  // th->function.acp1 == (actionf_p1)P_MobjThinker
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
                W_CacheLumpNum(lump)
            }
        }
    }
}
