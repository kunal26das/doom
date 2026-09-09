package doom.engine.savegame

import doom.engine.DoomStorage

internal class SaveGameMemoryStorage : DoomStorage {
    private val files = mutableMapOf<String, ByteArray>()
    var ticks = 0
    var onSave: (() -> Unit)? = null
    var saveFailure: Throwable? = null
    var loadFailure: Throwable? = null

    override fun read(name: String): ByteArray? {
        if (name.endsWith(".dsg")) loadFailure?.let { throw it }
        return files[name]?.copyOf()
    }

    override fun write(name: String, data: ByteArray) {
        if (name.endsWith(".dsg")) saveFailure?.let { throw it }
        files[name] = data.copyOf()
        if (name.endsWith(".dsg")) onSave?.invoke()
    }
}
