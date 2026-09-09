// Argument compatibility derived from linuxdoom-1.10 m_argv.c.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
package doom.engine.configuration

/** Owns a session's argument snapshot and validation before subsystem startup. */
internal class StartupArguments(private val ticRate: Int) {
    private var tokens: List<String> = emptyList()
    val values: List<String> get() = tokens
    val size: Int get() = tokens.size

    fun replace(values: List<String>) { tokens = values.toList() }

    /** The executable name is excluded; zero means the option is absent. */
    fun indexOf(option: String): Int {
        for (index in 1 until tokens.size) {
            if (option.equals(tokens[index], ignoreCase = true)) return index
        }
        return 0
    }

    fun validate(commercialGame: Boolean, reject: (String) -> Nothing) {
        fun value(index: Int, option: String, offset: Int = 1): String {
            val argument = tokens.getOrNull(index + offset)
            if (argument.isNullOrEmpty() || argument.startsWith('-'))
                reject("$option requires ${if (offset == 2) "two values" else "a value"}")
            return argument
        }

        fun number(index: Int, option: String, range: IntRange, offset: Int = 1) {
            val argument = value(index, option, offset)
            val parsed = argument.toIntOrNull()
            if (parsed == null || parsed !in range)
                reject("Invalid $option value '$argument' (expected ${range.first}..${range.last})")
        }

        for (index in 1 until size) {
            val option = tokens[index].lowercase()
            when (option) {
                "-skill" -> number(index, option, 1..5)
                "-episode" -> number(index, option, 1..4)
                "-loadgame" -> number(index, option, 0..5)
                "-timer" -> number(index, option, 0..(Int.MAX_VALUE / (60 * ticRate)))
                "-maxdemo" -> number(index, option, 1..(Int.MAX_VALUE / 1024))
                "-config", "-playdemo", "-timedemo", "-record" -> value(index, option)
                "-warp" -> {
                    number(index, option, if (commercialGame) 1..32 else 1..4)
                    if (!commercialGame) number(index, option, 1..9, offset = 2)
                }
                "-turbo" -> {
                    val argument = tokens.getOrNull(index + 1)
                    if (argument != null && !argument.startsWith('-') && argument.toIntOrNull() == null)
                        reject("Invalid -turbo value '$argument'")
                }
            }
        }
    }
}
