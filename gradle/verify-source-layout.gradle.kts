import org.gradle.api.GradleException
import org.gradle.api.tasks.PathSensitivity

/** Keeps offsets/newlines intact while hiding text that cannot declare a Kotlin type. */
private fun kotlinCodeOnly(source: String): String = object {
    private val masked = source.toCharArray()

    fun read(): String {
        scanCode(0, inTemplate = false)
        return masked.concatToString()
    }

    private fun mask(start: Int, end: Int) {
        for (index in start until end) {
            if (masked[index] != '\n' && masked[index] != '\r') masked[index] = ' '
        }
    }

    private fun scanCode(start: Int, inTemplate: Boolean): Int {
        var position = start
        var depth = 0
        while (position < source.length) {
            val end = when {
                source.startsWith("//", position) -> skipLineComment(position)
                source.startsWith("/*", position) -> skipBlockComment(position)
                source[position] == '\'' -> skipCharacter(position)
                source[position] == '"' -> {
                    position = maskString(position)
                    continue
                }
                source[position] == '$' -> {
                    val quote = afterDollars(position)
                    if (quote < source.length && source[quote] == '"') {
                        mask(position, quote)
                        position = maskString(quote, quote - position)
                        continue
                    }
                    position
                }
                source[position] == '`' -> {
                    position = skipBacktick(position)
                    continue
                }
                source[position] == '{' -> { depth++; position }
                source[position] == '}' -> {
                    if (inTemplate && depth == 0) return position
                    depth--
                    position
                }
                else -> position
            }
            if (end > position) {
                mask(position, end)
                position = end
            } else position++
        }
        return source.length
    }

    private fun skipLineComment(start: Int): Int {
        var position = start + 2
        while (position < source.length && source[position] != '\n' && source[position] != '\r') position++
        return position
    }

    private fun skipBlockComment(start: Int): Int {
        var position = start + 2
        var depth = 1
        while (position < source.length) {
            when {
                source.startsWith("/*", position) -> { depth++; position += 2 }
                source.startsWith("*/", position) -> {
                    depth--
                    position += 2
                    if (depth == 0) return position
                }
                else -> position++
            }
        }
        return source.length
    }

    private fun skipCharacter(start: Int): Int {
        var position = start + 1
        while (position < source.length) {
            when (source[position]) {
                '\\' -> position = minOf(position + 2, source.length)
                '\'' -> return position + 1
                else -> position++
            }
        }
        return source.length
    }

    private fun skipBacktick(start: Int): Int {
        var position = start + 1
        while (position < source.length && source[position] != '`') position++
        return minOf(position + 1, source.length)
    }

    private fun afterDollars(start: Int): Int {
        var position = start
        while (position < source.length && source[position] == '$') position++
        return position
    }

    private fun maskString(start: Int, interpolationDollars: Int = 1): Int {
        val raw = source.startsWith("\"\"\"", start)
        var position = start + if (raw) 3 else 1
        mask(start, position)
        while (position < source.length) {
            when {
                !raw && source[position] == '\\' -> {
                    val end = minOf(position + 2, source.length)
                    mask(position, end)
                    position = end
                }
                raw && source.startsWith("\"\"\"", position) -> {
                    // A run of extra quotes immediately before the delimiter belongs to the raw string.
                    val closing = position
                    while (position < source.length && source[position] == '"') position++
                    mask(closing, position)
                    return position
                }
                !raw && source[position] == '"' -> {
                    mask(position, position + 1)
                    return position + 1
                }
                source[position] == '$' -> {
                    val afterPrefix = afterDollars(position)
                    if (afterPrefix - position >= interpolationDollars &&
                        afterPrefix < source.length && source[afterPrefix] == '{'
                    ) {
                        mask(position, afterPrefix + 1)
                        // Templates contain real code: count its local types, but hide its own literals/comments.
                        val closing = scanCode(afterPrefix + 1, inTemplate = true)
                        position = minOf(closing + 1, source.length)
                        mask(closing, position)
                    } else {
                        mask(position, afterPrefix)
                        position = afterPrefix
                    }
                }
                else -> {
                    mask(position, position + 1)
                    position++
                }
            }
        }
        return source.length
    }
}.read()

private fun namedKotlinTypes(source: String): List<Pair<String, Int>> {
    val code = kotlinCodeOnly(source)
    val identifier = "[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{N}\\p{Mn}\\p{Mc}\\p{Pc}]*"
    val tokenPattern = Regex("`[^`\\r\\n]+`|$identifier|::|\\S")
    val namePattern = Regex("`[^`\\r\\n]+`|$identifier")
    val tokens = tokenPattern.findAll(code).toList()
    return buildList {
        for (index in tokens.indices) {
            val keyword = tokens[index]
            if (keyword.value !in setOf("class", "interface", "object", "typealias")) continue
            val previous = tokens.getOrNull(index - 1)?.value
            if (keyword.value == "class" && previous == "::") continue
            if (keyword.value == "object" && previous == "companion") continue
            val name = tokens.getOrNull(index + 1) ?: continue
            // Anonymous object expressions are followed by ':' or '{', never a type name.
            if (!namePattern.matches(name.value)) continue
            add(name.value.removeSurrounding("`") to (1 + code.take(keyword.range.first).count { it == '\n' }))
        }
    }
}

val kotlinLayoutSources = fileTree(rootDir) {
    listOf("engine", "domain", "composeApp", "androidApp").forEach { module ->
        include("$module/src/**/*.kt")
    }
}

tasks.register("verifyKotlinSourceLayout") {
    group = "verification"
    description = "Require one named Kotlin type per file with its matching type name."
    inputs.files(kotlinLayoutSources)
        .withPropertyName("kotlinSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    doLast {
        val sources = kotlinLayoutSources.files.sortedBy { it.path }
        val violations = sources.mapNotNull { file ->
            val types = namedKotlinTypes(file.readText())
            val path = file.relativeTo(rootDir).invariantSeparatorsPath
            when {
                types.size > 1 -> "$path: contains ${types.size} named types: " +
                    types.joinToString { "${it.first} (line ${it.second})" }
                types.size == 1 && file.nameWithoutExtension != types.single().first -> {
                    val type = types.single()
                    "$path:${type.second}: ${type.first} must be in ${type.first}.kt"
                }
                else -> null
            }
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Kotlin source layout violations:\n" + violations.joinToString("\n") +
                    "\nMove each named type into TypeName.kt. Companions, anonymous objects and classless files are allowed.",
            )
        }
        logger.lifecycle("Verified Kotlin source layout in ${sources.size} files.")
    }
}
