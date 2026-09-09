package doom.engine.configuration

internal sealed class ConfigurationSetting(val name: String) {
    abstract fun reset()
    abstract fun readValue(value: String)
    abstract fun writeValue(): String
}
