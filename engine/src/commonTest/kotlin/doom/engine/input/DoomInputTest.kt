package doom.engine.input

import doom.engine.DoomKeyInput

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DoomInputTest {
    @Test
    fun keyContractRejectsCodesOutsideTheNonzeroEightBitProtocol() {
        for (code in listOf(Int.MIN_VALUE, -1, 0, 256, Int.MAX_VALUE)) {
            val failure = assertFailsWith<IllegalArgumentException> { DoomKeyInput(code, true) }
            assertEquals("Key codes must be in 1..255", failure.message)
        }
        for (code in 1..255) {
            assertEquals(code, DoomKeyInput(code, true).code)
            assertEquals(code, DoomKeyInput(code, false).code)
        }
        assertFailsWith<IllegalArgumentException> { DoomKeyInput(1, true).copy(code = 0) }
    }
}
