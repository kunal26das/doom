package doom.engine.simulation


import kotlin.test.Test
import kotlin.test.assertEquals

class WorldTickerTest {
    @Test
    fun worldUpdatesPreserveOrderAndAdvanceTheRestoredLevelClockLast() {
        val order = mutableListOf<String>()
        val thinkers = ThinkerScheduler()
        val world = WorldTickerRecordingWorld(order)
        val ticker = WorldTicker(thinkers, world)
        ticker.restoreLevelTime(81)
        thinkers.add(Thinker().apply {
            function = {
                order.add("thinkers")
                assertEquals(81, ticker.levelTime)
            }
        })

        ticker.tick()
        assertEquals(listOf("players", "thinkers", "specials", "respawn"), order)
        assertEquals(82, ticker.levelTime)
    }

    @Test
    fun bothGameAndMenuPauseFreezeAllWorldUpdatesAndLevelTime() {
        val order = mutableListOf<String>()
        val thinkers = ThinkerScheduler()
        thinkers.add(Thinker().apply { function = { order.add("thinkers") } })
        val world = WorldTickerRecordingWorld(order)
        val ticker = WorldTicker(thinkers, world)
        ticker.restoreLevelTime(7)
        world.paused = true
        ticker.tick()
        world.paused = false
        world.pausedByMenu = true
        ticker.tick()
        assertEquals(emptyList(), order)
        assertEquals(7, ticker.levelTime)

        world.pausedByMenu = false
        ticker.tick()
        assertEquals(listOf("players", "thinkers", "specials", "respawn"), order)
        assertEquals(8, ticker.levelTime)
    }

}
