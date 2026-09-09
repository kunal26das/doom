// WAD loading, directory lookup and cached lump reads.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

/**
 * Owns the validated directory, override lookup and lump cache for one engine.
 * Loading is transactional: rejection leaves the previous directory/cache usable.
 * Source images are borrowed read-only to avoid duplicating a complete IWAD per
 * engine. Callers must not modify them while this archive is in use.
 */
internal class WadArchive {
    private var entries: List<WadEntry> = emptyList()
    private var lookup: Map<String, Int> = emptyMap()
    private val cache = HashMap<Int, ByteArray>()

    var directory: List<LumpInfo> = emptyList()
        private set
    val size: Int get() = entries.size

    fun load(images: List<ByteArray>) {
        val loaded = ArrayList<WadEntry>()
        for (image in images) readDirectory(image, loaded)
        if (loaded.isEmpty()) I_Error("W_InitFiles: no files found")
        val names = HashMap<String, Int>()
        loaded.forEachIndexed { index, entry -> names[entry.info.name] = index }
        val descriptions = loaded.map { it.info }
        val metadata = object : AbstractList<LumpInfo>() {
            override val size: Int get() = descriptions.size
            override fun get(index: Int): LumpInfo = descriptions[index]
        }

        entries = loaded
        lookup = names
        directory = metadata
        cache.clear()
    }

    fun find(name: String): Int = lookup[name.uppercase()] ?: -1

    fun requireIndex(name: String): Int = find(name).also {
        if (it == -1) I_Error("W_GetNumForName: $name not found!")
    }

    fun length(index: Int): Int = entry(index, "W_LumpLength").info.size

    /** Cached bytes are borrowed by the engine; callers must not modify them. */
    fun read(index: Int): ByteArray {
        val entry = entry(index, "W_CacheLumpNum")
        return cache.getOrPut(index) {
            entry.image.copyOfRange(entry.position, entry.position + entry.info.size)
        }
    }

    fun read(name: String): ByteArray = read(requireIndex(name))

    private fun entry(index: Int, operation: String): WadEntry {
        if (index !in entries.indices) {
            I_Error("$operation: invalid lump $index (count $size)")
        }
        return entries[index]
    }

    private fun readDirectory(image: ByteArray, target: MutableList<WadEntry>) {
        if (image.size < 12) I_Error("WAD header is truncated")
        val id = image.str(0, 4)
        if (id != "IWAD" && id != "PWAD") I_Error("Wad file doesn't have IWAD or PWAD id")
        val count = image.i32(4)
        val directoryOffset = image.i32(8)
        if (count < 0 || directoryOffset < 0 ||
            directoryOffset.toLong() + count.toLong() * 16 > image.size.toLong()) {
            I_Error("WAD directory is outside the file")
        }
        for (index in 0 until count) {
            val offset = directoryOffset + index * 16
            val name = image.str(offset + 8, 8).uppercase()
            val position = image.i32(offset)
            val length = image.i32(offset + 4)
            if (position < 0 || length < 0 || position.toLong() + length.toLong() > image.size.toLong()) {
                I_Error("WAD lump $index ($name) is outside the file")
            }
            target.add(WadEntry(LumpInfo(name, length), image, position))
        }
    }
}
