package doom.engine.configuration

import doom.engine.DoomStorage

internal class EngineConfigurationMemoryStorage : DoomStorage {
    var content: ByteArray? = null
    var lastRead: String? = null
    var lastWrite: String? = null
    override fun read(name: String): ByteArray? { lastRead = name; return content }
    override fun write(name: String, data: ByteArray) { lastWrite = name; content = data }
}
