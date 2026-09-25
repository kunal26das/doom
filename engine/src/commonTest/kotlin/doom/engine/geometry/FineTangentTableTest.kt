package doom.engine.geometry

import kotlin.test.Test
import kotlin.test.assertEquals

class FineTangentTableTest {
    @Test
    fun indexesPastTheTangentTableContinueIntoTheSineTable() {
        for (i in 0 until FINEANGLES / 2) assertEquals(finetangent[i], FineTangentTable[i])
        for (i in FINEANGLES / 2 until FINEANGLES) assertEquals(finesine[i - FINEANGLES / 2], FineTangentTable[i])
        assertEquals(25, FineTangentTable[FINEANGLES / 2])
    }
}
