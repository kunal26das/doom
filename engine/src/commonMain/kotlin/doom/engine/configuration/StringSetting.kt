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
        write(if (value.length >= 2) value.substring(1, value.length - 1) else "")
    }

    override fun writeValue(): String = "\"${read()}\""
}
