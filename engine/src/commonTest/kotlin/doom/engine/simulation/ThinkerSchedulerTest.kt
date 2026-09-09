package doom.engine.simulation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ThinkerSchedulerTest {
    @Test
    fun thinkersAppendedDuringATicRunAfterExistingThinkersInTheSameTic() {
        val scheduler = ThinkerScheduler()
        val calls = mutableListOf<String>()
        val third = Thinker().apply { function = { calls.add("third") } }
        val first = Thinker().apply {
            function = {
                calls.add("first")
                scheduler.add(third)
            }
        }
        val second = Thinker().apply { function = { calls.add("second") } }
        scheduler.add(first)
        scheduler.add(second)

        scheduler.run()
        assertEquals(listOf("first", "second", "third"), calls)
        assertSame(third, scheduler.sentinel.prev)
        assertSame(second, third.prev)
    }

    @Test
    fun removingALaterThinkerDefersUnlinkingButSkipsItsCallbackThisTic() {
        val scheduler = ThinkerScheduler()
        val calls = mutableListOf<String>()
        val second = Thinker().apply { function = { calls.add("second") } }
        val first = Thinker().apply {
            function = {
                calls.add("first")
                scheduler.remove(second)
                assertSame(second, next, "Removal must not mutate links inside this callback")
            }
        }
        val third = Thinker().apply { function = { calls.add("third") } }
        scheduler.add(first)
        scheduler.add(second)
        scheduler.add(third)

        scheduler.run()
        assertEquals(listOf("first", "third"), calls)
        assertSame(third, first.next)
        assertSame(first, third.prev)
    }

    @Test
    fun selfRemovalIsUnlinkedOnTheNextTicAndDoesNotSkipTheNextThinker() {
        val scheduler = ThinkerScheduler()
        val calls = mutableListOf<String>()
        val first = Thinker().apply {
            function = {
                calls.add("first")
                scheduler.remove(it)
            }
        }
        val second = Thinker().apply { function = { calls.add("second") } }
        scheduler.add(first)
        scheduler.add(second)
        scheduler.run()
        assertSame(first, scheduler.sentinel.next)

        scheduler.run()
        assertEquals(listOf("first", "second", "second"), calls)
        assertSame(second, scheduler.sentinel.next)
        assertSame(scheduler.sentinel, second.prev)
    }

    @Test
    fun clearingAWorldLeavesAnEmptySentinelAndFreshThinkersCanRun() {
        val scheduler = ThinkerScheduler()
        var oldCalls = 0
        scheduler.add(Thinker().apply { function = { oldCalls++ } })
        scheduler.reset()
        scheduler.run()
        assertEquals(0, oldCalls)
        assertSame(scheduler.sentinel, scheduler.sentinel.next)
        assertSame(scheduler.sentinel, scheduler.sentinel.prev)

        var newCalls = 0
        scheduler.add(Thinker().apply { function = { newCalls++ } })
        scheduler.run()
        assertEquals(1, newCalls)
    }
}
