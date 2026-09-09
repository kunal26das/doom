package doom.engine.input


import kotlin.test.Test
import kotlin.test.assertEquals

class EngineInputQueueTest {
    @Test
    fun burstsRemainOrderedAndQueuesAreIndependent() {
        val first = EngineInputQueue()
        val second = EngineInputQueue()
        repeat(1024) { first.post(EngineEvent(EV_KEYDOWN, it)) }
        second.post(EngineEvent(EV_KEYUP, 7))
        first.collect()
        val values = ArrayList<Int>()
        first.dispatch { values.add(it.data1) }
        assertEquals((0 until 1024).toList(), values)
        second.collect()
        val releases = ArrayList<Int>()
        second.dispatch { releases.add(it.data1) }
        assertEquals(listOf(7), releases)
        first.close(); second.close()
    }

    @Test
    fun clearAndCloseDiscardPendingInput() {
        val queue = EngineInputQueue()
        queue.stage(EngineEvent())
        queue.post(EngineEvent())
        queue.clear()
        queue.collect()
        assertEquals(0, queue.pendingCount)
        queue.close()
        queue.post(EngineEvent())
        queue.collect()
        assertEquals(0, queue.pendingCount)
    }
}
