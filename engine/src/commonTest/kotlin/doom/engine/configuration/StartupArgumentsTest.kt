package doom.engine.configuration


import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StartupArgumentsTest {
    @Test
    fun snapshotsExcludeExecutableAndMatchFirstOptionIgnoringCase() {
        val source = mutableListOf("-warp", "-SKILL", "3", "-skill", "4")
        val arguments = StartupArguments(35)
        arguments.replace(source)
        source[1] = "changed"
        assertEquals(1, arguments.indexOf("-skill"))
        assertEquals(0, arguments.indexOf("-warp"))
        assertEquals(5, arguments.size)
        assertEquals("-SKILL", arguments.values[1])
    }

    @Test
    fun commercialAndEpisodeMapNumbersHaveDifferentBounds() {
        assertValid(true, "-warp", "32")
        assertInvalid(false, "-warp requires two values", "-warp", "1")
        assertValid(false, "-warp", "4", "9")
        assertInvalid(true, "Invalid -warp value '33' (expected 1..32)", "-warp", "33")
        assertInvalid(false, "Invalid -warp value '10' (expected 1..9)", "-warp", "1", "10")
    }

    @Test
    fun requiredArgumentsRejectEmptyValuesAndOtherFlags() {
        assertInvalid(false, "-config requires a value", "-config", "-skill", "3")
        assertInvalid(false, "-record requires a value", "-record", "")
        assertInvalid(false, "Invalid -skill value '6' (expected 1..5)", "-skill", "6")
        assertInvalid(false, "-loadgame requires a value", "-loadgame", "-1")
    }

    @Test
    fun optionalTurboAndAllocationBoundsKeepTheirOriginalRules() {
        assertValid(false, "-turbo", "-nomonsters")
        assertValid(false, "-turbo", "0")
        assertValid(false, "-turbo", "9999")
        assertInvalid(false, "Invalid -turbo value 'fast'", "-turbo", "fast")
        val timerLimit = Int.MAX_VALUE / (60 * 35)
        assertValid(false, "-timer", timerLimit.toString(), "-maxdemo", (Int.MAX_VALUE / 1024).toString())
        assertInvalid(false, "Invalid -timer value '${timerLimit + 1}' (expected 0..$timerLimit)", "-timer", (timerLimit + 1).toString())
        assertInvalid(false, "Invalid -maxdemo value '2147483647' (expected 1..2097151)", "-maxdemo", Int.MAX_VALUE.toString())
    }

    private fun assertValid(commercial: Boolean, vararg options: String) {
        val arguments = StartupArguments(35)
        arguments.replace(listOf("doom") + options)
        arguments.validate(commercial) { throw IllegalArgumentException(it) }
    }

    private fun assertInvalid(commercial: Boolean, message: String, vararg options: String) {
        val failure = assertFailsWith<IllegalArgumentException> { assertValid(commercial, *options) }
        assertEquals(message, failure.message)
    }
}
