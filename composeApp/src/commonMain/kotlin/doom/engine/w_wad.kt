// Port of linuxdoom-1.10 w_wad.c -- WAD file handling.
// Files are in-memory ByteArrays (loaded by the host app from resources or
// user-provided storage); lump reads slice them.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

class lumpinfo_t(
    val name: String,      // uppercase, max 8 chars
    val wad: ByteArray,
    val position: Int,
    val size: Int,
)

var lumpinfo: ArrayList<lumpinfo_t> = ArrayList()
val numlumps: Int get() = lumpinfo.size

private val lumpcache = HashMap<Int, ByteArray>()

/**
 * Pass a list of complete WAD images (IWAD first, then PWADs).
 * Lump names are stored uppercase; all searches are case-insensitive.
 */
fun W_InitMultipleFiles(files: List<ByteArray>) {
    lumpinfo = ArrayList()
    lumpcache.clear()
    for (wad in files) W_AddFile(wad)
    if (numlumps == 0) I_Error("W_InitFiles: no files found")
}

private fun W_AddFile(wad: ByteArray) {
    val id = wad.str(0, 4)
    if (id != "IWAD" && id != "PWAD") I_Error("Wad file doesn't have IWAD or PWAD id")
    val count = wad.i32(4)
    val infotableofs = wad.i32(8)
    for (i in 0 until count) {
        val ofs = infotableofs + i * 16
        lumpinfo.add(
            lumpinfo_t(
                name = wad.str(ofs + 8, 8).uppercase(),
                wad = wad,
                position = wad.i32(ofs),
                size = wad.i32(ofs + 4),
            )
        )
    }
}

/** Returns -1 if name not found. Scans backwards so later wads override. */
fun W_CheckNumForName(name: String): Int {
    val upper = name.uppercase()
    for (i in lumpinfo.indices.reversed()) {
        if (lumpinfo[i].name == upper) return i
    }
    return -1
}

/** Calls W_CheckNumForName, but bombs out if not found. */
fun W_GetNumForName(name: String): Int {
    val i = W_CheckNumForName(name)
    if (i == -1) I_Error("W_GetNumForName: $name not found!")
    return i
}

/** Returns the buffer size needed to load the given lump. */
fun W_LumpLength(lump: Int): Int {
    if (lump >= numlumps) I_Error("W_LumpLength: $lump >= numlumps")
    return lumpinfo[lump].size
}

fun W_CacheLumpNum(lump: Int): ByteArray {
    if (lump >= numlumps) I_Error("W_CacheLumpNum: $lump >= numlumps")
    return lumpcache.getOrPut(lump) {
        val l = lumpinfo[lump]
        l.wad.copyOfRange(l.position, l.position + l.size)
    }
}

fun W_CacheLumpName(name: String): ByteArray = W_CacheLumpNum(W_GetNumForName(name))
