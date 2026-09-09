// Config compatibility derived from linuxdoom-1.10 m_misc.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.configuration

internal class StringSetting(
    name: String,
    private val read: () -> String,
    private val write: (String) -> Unit,
    private val defaultValue: String,
) : ConfigurationSetting(name) {
    override fun reset() = write(defaultValue)

    override fun readValue(value: String) {
        if (!value.startsWith('"')) return
        // Preserve the original config reader's treatment of an unfinished quote.
        write(if (value.length >= 2) value.substring(1, value.length - 1) else "")
    }

    override fun writeValue(): String = "\"${read()}\""
}
