// Config compatibility derived from linuxdoom-1.10 m_misc.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.configuration

/** One typed setting binding; its value remains owned by the consuming subsystem. */
internal sealed class ConfigurationSetting(val name: String) {
    abstract fun reset()
    abstract fun readValue(value: String)
    abstract fun writeValue(): String
}
