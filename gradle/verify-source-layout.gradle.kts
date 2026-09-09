import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyKotlinSourceLayout : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val kotlinSources: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val diagnosticConfigurationFiles: ConfigurableFileCollection

    @get:Internal
    abstract val sourceRoot: DirectoryProperty

    @TaskAction
    fun verify() {
        val rootDir = sourceRoot.get().asFile
        val sources = kotlinSources.files.sortedBy { it.path }
        val upperCamelCase = Regex("[A-Z][A-Za-z0-9]*")
        val platformSuffixes = setOf("android", "ios", "jvm", "wasm", "wasmJs", "js", "native")
        val violations = sources.flatMap { file ->
            val code = kotlinCodeOnly(file.readText())
            val types = namedKotlinTypes(code)
            val packages = declaredKotlinPackages(code)
            val path = file.relativeTo(rootDir).invariantSeparatorsPath
            val nameParts = file.nameWithoutExtension.split('.')
            val baseName = nameParts.first()
            val platformSuffix = nameParts.getOrNull(1)
            val validSuffix = nameParts.size == 1 ||
                (nameParts.size == 2 && platformSuffix != null && platformSuffix in platformSuffixes)
            buildList {
                addAll(suppressionViolations(file.readText(), path, code))
                if (path.startsWith("engine/")) {
                    addAll(engineNamingViolations(code, path))
                }
                if (!upperCamelCase.matches(baseName) || !validSuffix) {
                    add("$path: filename must use UpperCamelCase ASCII letters/digits, such as GameSession.kt; " +
                        "optional platform suffixes: ${platformSuffixes.joinToString { ".$it.kt" }}")
                }
                if (types.size > 1) {
                    add("$path: contains ${types.size} named types: " +
                        types.joinToString { "${it.first} (line ${it.second})" })
                }
                types.forEach { (name, line) ->
                    if (!upperCamelCase.matches(name)) {
                        add("$path:$line: type '$name' must use an UpperCamelCase ASCII name (letters/digits, no underscores)")
                    }
                }
                if (types.size == 1 && baseName != types.single().first) {
                    val (name, line) = types.single()
                    val suffix = if (validSuffix && platformSuffix != null) ".$platformSuffix" else ""
                    add("$path:$line: $name must be in $name$suffix.kt")
                }

                if (!path.contains("/kotlin/")) {
                    add("$path: Kotlin sources must be beneath a source-set kotlin/ directory so their package path is defined")
                } else {
                    val expectedPackage = path.substringAfter("/kotlin/").substringBeforeLast('/', "").replace('/', '.')
                    when {
                        packages.size > 1 -> add("$path: multiple package declarations: " +
                            packages.joinToString { "${it.first} (line ${it.second})" })
                        packages.isEmpty() && expectedPackage.isNotEmpty() ->
                            add("$path: missing package declaration; expected 'package $expectedPackage'")
                        packages.isNotEmpty() && packages.single().first != expectedPackage -> {
                            val (actualPackage, line) = packages.single()
                            val expected = if (expectedPackage.isEmpty()) "the default package (no package declaration)"
                            else "'package $expectedPackage'"
                            add("$path:$line: package '$actualPackage' does not match its directory; expected $expected")
                        }
                    }
                }
            }
        } + diagnosticConfigurationFiles.files.sortedBy { it.path }.flatMap { file ->
            val source = file.readText()
            val path = file.relativeTo(rootDir).invariantSeparatorsPath
            suppressionViolations(source, path, if (file.extension == "kts") kotlinCodeOnly(source) else source)
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Kotlin source layout violations:\n" + violations.joinToString("\n") +
                    "\nUse one UpperCamelCase type per matching file and keep package declarations aligned with kotlin/ directories. " +
                    "Companions, anonymous objects and classless files are allowed.",
            )
        }
        logger.lifecycle("Verified Kotlin source layout in ${sources.size} files.")
    }

    private fun suppressionViolations(source: String, path: String, code: String): List<String> = buildList {
        val annotations = Regex("@(?:file\\s*:\\s*)?(?:[A-Za-z_][A-Za-z0-9_]*\\.)*(?:Suppress|SuppressLint|SuppressWarnings)\\b")
        val comments = Regex("(?m)^\\s*(?://\\s*noinspection|#\\s*shellcheck\\s+disable|//\\s*(?:ktlint|detekt|eslint)-disable)\\b")
        (annotations.findAll(code) + comments.findAll(source)).forEach { match ->
            val line = 1 + source.take(match.range.first).count { it == '\n' }
            add("$path:$line: diagnostic suppressions are forbidden; fix the reported issue")
        }
        if (Regex("\\bsuppressWarnings\\s*(?:\\.set\\(\\s*true\\s*\\)|=\\s*true)").containsMatchIn(code)) {
            add("$path: compiler warnings must remain enabled")
        }
        val disabledArguments = listOf("nowarn", "Xsuppress-warning")
        if (disabledArguments.any { source.contains("-$it") } ||
            Regex("-Xwarning-level=[^\\s\\\"]+:disabled").containsMatchIn(source)
        ) {
            add("$path: compiler arguments must not disable diagnostics")
        }
        if (Regex("\\btools:(?:ignore|targetApi)\\s*=").containsMatchIn(source)) {
            add("$path: Android lint issues must be fixed without tools:ignore or tools:targetApi")
        }
        if (Regex("(?m)^\\s*org\\.gradle\\.warning\\.mode\\s*=\\s*none\\s*$").containsMatchIn(source)) {
            add("$path: Gradle warnings must remain enabled")
        }
    }

    private fun engineNamingViolations(code: String, path: String): List<String> {
        val identifier = "(?:`[^`\\r\\n]+`|[A-Za-z_][A-Za-z0-9_]*)"
        val functions = Regex("\\bfun\\s+(?:<[^>{}]+>\\s+)?(?:$identifier\\.)*($identifier)\\s*\\(")
        val properties = Regex("\\b(?:(const)\\s+)?(?:val|var)\\s+(?:$identifier\\.)*($identifier)")
        val parameters = Regex("[,(]\\s*(?:(?:crossinline|noinline|vararg)\\s+)?($identifier)\\s*:(?!:)")
        val lowerCamelCase = Regex("[a-z][A-Za-z0-9]*")
        val constantName = Regex("[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*")
        return buildList {
            functions.findAll(code).forEach { match ->
                val name = match.groupValues[1].removeSurrounding("`")
                if (!lowerCamelCase.matches(name)) {
                    val line = 1 + code.take(match.range.first).count { it == '\n' }
                    add("$path:$line: function '$name' must use lowerCamelCase")
                }
            }
            properties.findAll(code).forEach { match ->
                val name = match.groupValues[2].removeSurrounding("`")
                val isConstant = match.groupValues[1].isNotEmpty()
                if (name != "_" && !(if (isConstant) constantName else lowerCamelCase).matches(name)) {
                    val line = 1 + code.take(match.range.first).count { it == '\n' }
                    val convention = if (isConstant) "UPPER_SNAKE_CASE" else "lowerCamelCase"
                    add("$path:$line: ${if (isConstant) "constant" else "property"} '$name' must use $convention")
                }
            }
            parameters.findAll(code).forEach { match ->
                val name = match.groupValues[1].removeSurrounding("`")
                if (name != "_" && !lowerCamelCase.matches(name)) {
                    val line = 1 + code.take(match.range.first).count { it == '\n' }
                    add("$path:$line: parameter '$name' must use lowerCamelCase")
                }
            }
        }
    }

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

    private fun namedKotlinTypes(code: String): List<Pair<String, Int>> {
        val identifier = "[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{N}\\p{Mn}\\p{Mc}\\p{Pc}]*"
        val tokenPattern = Regex("`[^`\\r\\n]+`|$identifier|::|\\S")
        val namePattern = Regex("`[^`\\r\\n]+`|$identifier")
        val tokens = tokenPattern.findAll(code).toList()
        val declarationKeywords = setOf("class", "interface", "object", "typealias")
        return buildList {
            for (index in tokens.indices) {
                val keyword = tokens[index]
                if (keyword.value !in declarationKeywords) continue
                val previous = tokens.getOrNull(index - 1)?.value
                if (keyword.value == "class" && previous == "::") continue
                if (keyword.value == "object" && previous == "companion") continue
                val name = tokens.getOrNull(index + 1) ?: continue
                if (!namePattern.matches(name.value)) continue
                add(name.value.removeSurrounding("`") to (1 + code.take(keyword.range.first).count { it == '\n' }))
            }
        }
    }

    private fun declaredKotlinPackages(code: String): List<Pair<String, Int>> {
        val identifier = "(?:`[^`\\r\\n]+`|[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{N}\\p{Mn}\\p{Mc}\\p{Pc}]*)"
        val declaration = Regex("(?m)^[\\t ]*package\\s+($identifier(?:\\s*\\.\\s*$identifier)*)")
        return declaration.findAll(code).map { match ->
            val name = match.groupValues[1].split('.').joinToString(".") { it.trim().removeSurrounding("`") }
            name to (1 + code.take(match.range.first).count { it == '\n' })
        }.toList()
    }
}

tasks.register<VerifyKotlinSourceLayout>("verifyKotlinSourceLayout") {
    group = "verification"
    description = "Require one named Kotlin type per file, UpperCamelCase names and matching package directories."
    sourceRoot.set(rootProject.layout.projectDirectory)
    kotlinSources.from(fileTree(rootDir) {
        listOf("engine", "domain", "composeApp", "androidApp").forEach { module ->
            include("$module/src/**/*.kt")
        }
    })
    diagnosticConfigurationFiles.from(fileTree(rootDir) {
        include("*.gradle.kts", "gradle/*.gradle.kts", "*/build.gradle.kts", "gradle.properties", "gradlew")
        include("*/src/**/AndroidManifest.xml", "*/src/**/*.js", "scripts/**/*.mjs")
    })
}
