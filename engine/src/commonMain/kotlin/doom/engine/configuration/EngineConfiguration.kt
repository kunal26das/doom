// Config compatibility derived from linuxdoom-1.10 m_misc.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.configuration

import doom.engine.DoomStorage

/** Owns default application, config syntax and persistence through a narrow storage port. */
internal class EngineConfiguration(
    settings: List<ConfigurationSetting>,
    private val storage: DoomStorage,
) {
    private val settings = settings.toList()
    private var fileName = ""
    private var loaded = false

    fun load(fileName: String) {
        loaded = true
        settings.forEach { it.reset() }
        this.fileName = fileName
        val bytes = storage.read(fileName) ?: return
        for (line in bytes.decodeToString().split('\n')) {
            val trimmed = line.trim { it == ' ' || it == '\t' || it == '\r' }
            val separator = trimmed.indexOfFirst { it == ' ' || it == '\t' }
            if (separator < 0) continue
            val name = trimmed.substring(0, separator)
            val value = trimmed.substring(separator).trimStart(' ', '\t')
            if (value.isEmpty()) continue
            settings.firstOrNull { it.name == name }?.readValue(value)
        }
    }

    fun save() {
        val text = buildString {
            if (loaded) settings.forEach { setting ->
                append(setting.name).append("\t\t").append(setting.writeValue()).append('\n')
            }
        }
        storage.write(fileName, text.encodeToByteArray())
    }
}
