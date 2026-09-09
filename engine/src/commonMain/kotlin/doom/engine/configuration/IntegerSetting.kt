package doom.engine.configuration

internal class IntegerSetting(
    name: String,
    private val read: () -> Int,
    private val write: (Int) -> Unit,
    private val defaultValue: Int,
    private val range: IntRange? = null,
) : ConfigurationSetting(name) {
    override fun reset() = write(defaultValue)

    override fun readValue(value: String) {
        val parsed = if (value.startsWith("0x")) value.substring(2).toIntOrNull(16)
        else value.toIntOrNull()
        if (parsed != null && (range == null || parsed in range)) write(parsed)
    }

    override fun writeValue(): String = read().toString()
}
