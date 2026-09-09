package doom.engine.savegame

import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.savebuffer
import doom.engine.geometry.FRACUNIT
import doom.engine.simulation.pAddThinker
import doom.engine.world.MapLine
import doom.engine.world.movers.activeplats
import doom.engine.world.movers.evStopPlat
import doom.engine.world.movers.IN_STASIS
import doom.engine.world.movers.pActivateInStasis
import doom.engine.world.movers.pAddActivePlat
import doom.engine.world.movers.PERPETUAL_RAISE
import doom.engine.world.movers.PlatformMover
import doom.engine.world.movers.tPlatRaise
import doom.engine.world.movers.WAITING
import doom.engine.world.Sector
import doom.engine.world.sectors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

class SaveGameSpecialsTest {
    @Test
    fun stoppedPlatformRestoresIntoItsOwnSectorAndResumesItsOriginalCountdown() {
        val source = world()
        val platform = PlatformMover().apply {
            sector = source.sectors[0]
            type = PERPETUAL_RAISE
            speed = FRACUNIT
            high = 16 * FRACUNIT
            wait = 7
            count = 3
            status = WAITING
            tag = 42
            function = { source.tPlatRaise(it as PlatformMover) }
        }
        platform.sector!!.specialdata = platform
        source.pAddThinker(platform)
        source.pAddActivePlat(platform)
        source.evStopPlat(MapLine().apply { tag = 42 })
        assertEquals(IN_STASIS, platform.status)
        assertNull(platform.function)
        source.savebuffer = ByteArray(1024)
        source.pArchiveSpecials()

        val restored = world().apply {
            savebuffer = source.savebuffer.copyOf(source.saveP)
            pUnArchiveSpecials()
        }

        val restoredPlatform = assertIs<PlatformMover>(restored.sectors[0].specialdata)
        assertNotSame(platform, restoredPlatform)
        assertSame(restored.sectors[0], restoredPlatform.sector)
        assertSame(restoredPlatform, restored.activeplats.filterNotNull().single())
        assertEquals(source.saveP, restored.saveP)
        assertEquals(IN_STASIS, restoredPlatform.status)
        assertEquals(WAITING, restoredPlatform.oldstatus)
        assertEquals(FRACUNIT, restoredPlatform.speed)
        assertEquals(16 * FRACUNIT, restoredPlatform.high)
        assertEquals(7, restoredPlatform.wait)
        assertEquals(3, restoredPlatform.count)
        assertNull(restoredPlatform.function)
        restored.thinkers.run()
        assertEquals(3, restoredPlatform.count)
        restored.pActivateInStasis(41)
        assertNull(restoredPlatform.function)

        restored.pActivateInStasis(42)
        assertEquals(WAITING, restoredPlatform.status)
        assertNotNull(restoredPlatform.function)
        restored.thinkers.run()

        assertEquals(2, restoredPlatform.count)
        assertEquals(3, platform.count)
        assertEquals(IN_STASIS, platform.status)
        assertNull(platform.function)
    }

    private fun world(): DoomEngineCore = DoomEngineCore().apply {
        sectors = arrayOf(Sector().apply { ceilingheight = 128 * FRACUNIT })
    }
}
