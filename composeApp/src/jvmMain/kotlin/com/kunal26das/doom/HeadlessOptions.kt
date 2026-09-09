package com.kunal26das.doom

import doom.engine.TICRATE
import java.io.File

internal data class HeadlessOptions(val wad: File?, val output: File?, val ticks: Int, val demo: String?) {
    companion object {
        fun parse(args: List<String>): HeadlessOptions {
            var wad: File? = null
            var output: File? = File("build/frames")
            var ticks = 80 * TICRATE
            var demo: String? = null
            val positional = ArrayList<String>()
            var index = 0
            fun value(option: String): String {
                val result = args.getOrNull(++index)
                require(!result.isNullOrBlank() && !result.startsWith("--")) { "$option requires a value" }
                return result
            }
            fun positiveInt(value: String, name: String): Int = value.toIntOrNull()
                ?.takeIf { it > 0 } ?: error("$name must be a positive integer")
            while (index < args.size) {
                when (val option = args[index]) {
                    "--wad" -> wad = File(value(option))
                    "--output" -> output = File(value(option))
                    "--ticks" -> ticks = positiveInt(value(option), option)
                    "--demo" -> demo = value(option)
                    "--no-frames" -> output = null
                    else -> {
                        require(!option.startsWith("--")) { "Unknown option $option" }
                        positional.add(option)
                    }
                }
                index++
            }
            require(positional.size <= 3) { "Expected [outDir] [gameSeconds] [wadPath]" }
            positional.getOrNull(0)?.let { output = File(it) }
            positional.getOrNull(1)?.let {
                val seconds = positiveInt(it, "gameSeconds")
                require(seconds <= Int.MAX_VALUE / TICRATE) { "gameSeconds is too large" }
                ticks = seconds * TICRATE
            }
            positional.getOrNull(2)?.let { wad = File(it) }
            return HeadlessOptions(wad, output, ticks, demo)
        }
    }
}
