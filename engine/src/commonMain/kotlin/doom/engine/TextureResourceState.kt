// Port of linuxdoom-1.10 r_data.c -- Preparation of data for rendering,
// generation of lookups, caching, retrieval by name.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class TextureResourceState {
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

    var textureheight: IntArray = IntArray(0)

    var texturecompositesize: IntArray = IntArray(0)

    var texturecolumnlump: Array<ShortArray> = emptyArray()

    var texturecolumnofs: Array<IntArray> = emptyArray()

    var texturecomposite: Array<ByteArray?> = emptyArray()

    var flattranslation: IntArray = IntArray(0)

    var texturetranslation: IntArray = IntArray(0)

    var spritewidth: IntArray = IntArray(0)

    var spriteoffset: IntArray = IntArray(0)

    var spritetopoffset: IntArray = IntArray(0)

    var colormaps: ByteArray = ByteArray(0)

    var GETCOL_DATA: ByteArray = ByteArray(0)

    var flatmemory = 0

    var texturememory = 0

    var spritememory = 0
}
