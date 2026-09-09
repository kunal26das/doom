
package doom.engine.resources

import doom.engine.core.iError

internal class WadArchive {
    private var entries: List<WadEntry> = emptyList()
    private var lookup: Map<String, Int> = emptyMap()
    private var cache: Array<ByteArray?> = emptyArray()

    var directory: List<LumpInfo> = emptyList()
        private set
    val size: Int get() = entries.size

    fun load(images: List<ByteArray>) {
        val loaded = ArrayList<WadEntry>()
        for (image in images) readDirectory(image, loaded)
        if (loaded.isEmpty()) iError("W_InitFiles: no files found")
        val names = HashMap<String, Int>()
        loaded.forEachIndexed { index, entry -> names[entry.info.name] = index }
        val descriptions = loaded.map { it.info }
        val metadata = object : AbstractList<LumpInfo>() {
            override val size: Int get() = descriptions.size
            override fun get(index: Int): LumpInfo = descriptions[index]
        }
        val loadedCache = arrayOfNulls<ByteArray>(loaded.size)

        entries = loaded
        lookup = names
        directory = metadata
        cache = loadedCache
    }

    fun find(name: String): Int = lookup[name.uppercase()] ?: -1

    fun requireIndex(name: String): Int = find(name).also {
        if (it == -1) iError("W_GetNumForName: $name not found!")
    }

    fun length(index: Int): Int = entry(index, "W_LumpLength").info.size

    fun read(index: Int): ByteArray {
        val entry = entry(index, "W_CacheLumpNum")
        return cache[index] ?: entry.image.copyOfRange(entry.position, entry.position + entry.info.size).also {
            cache[index] = it
        }
    }

    fun read(name: String): ByteArray = read(requireIndex(name))

    private fun entry(index: Int, operation: String): WadEntry {
        if (index !in entries.indices) {
            iError("$operation: invalid lump $index (count $size)")
        }
        return entries[index]
    }

    private fun readDirectory(image: ByteArray, target: MutableList<WadEntry>) {
        if (image.size < 12) iError("WAD header is truncated")
        val id = image.str(0, 4)
        if (id != "IWAD" && id != "PWAD") iError("Wad file doesn't have IWAD or PWAD id")
        val count = image.i32(4)
        val directoryOffset = image.i32(8)
        if (count < 0 || directoryOffset < 0 ||
            directoryOffset.toLong() + count.toLong() * 16 > image.size.toLong()) {
            iError("WAD directory is outside the file")
        }
        for (index in 0 until count) {
            val offset = directoryOffset + index * 16
            val name = image.str(offset + 8, 8).uppercase()
            val position = image.i32(offset)
            val length = image.i32(offset + 4)
            if (position < 0 || length < 0 || position.toLong() + length.toLong() > image.size.toLong()) {
                iError("WAD lump $index ($name) is outside the file")
            }
            target.add(WadEntry(LumpInfo(name, length), image, position))
        }
    }
}
