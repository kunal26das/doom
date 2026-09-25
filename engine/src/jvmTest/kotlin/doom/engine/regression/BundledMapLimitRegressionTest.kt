package doom.engine.regression

import doom.engine.DoomClock
import doom.engine.DoomEngine
import doom.engine.DoomHost
import doom.engine.gameplay.actors.MT_SERGEANT
import doom.engine.gameplay.actors.pSpawnMobj
import doom.engine.gameplay.players
import doom.engine.geometry.BinaryAngle
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FixedPoint
import doom.engine.rendering.MAXVISPLANES
import doom.engine.rendering.lastvisplane
import doom.engine.rendering.rExecuteSetViewSize
import doom.engine.rendering.rRenderPlayerView
import doom.engine.rendering.rSetViewSize
import doom.engine.world.ONFLOORZ
import doom.engine.world.VIEWHEIGHT
import doom.engine.world.collision.MAXSPECIALCROSS
import doom.engine.world.collision.numspechit
import doom.engine.world.collision.pCheckPosition
import doom.engine.world.collision.pSetThingPosition
import doom.engine.world.collision.pUnsetThingPosition
import doom.engine.world.collision.spechit
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BundledMapLimitRegressionTest {
    private val wad: ByteArray get() = File(requireNotNull(System.getProperty("doom.test.wad"))).readBytes()

    @Test
    fun wallColumnsSeenJustPastParallelReadTheOriginalContiguousTables() {
        onMap(1, 4) { renderFrom(-4218775, 23980464, 1866915725u, screenBlocks = 11) }
        onMap(1, 7) { renderFrom(-41328551, -5654634, 2839528689u, screenBlocks = 9) }
    }

    @Test
    fun framesNeedingMoreThanTheOriginalVisplaneCapacityStillRender() {
        onMap(4, 8) {
            renderFrom(64 shl FRACBITS, -3504 shl FRACBITS, 7u shl 28, screenBlocks = 11)
            assertTrue(core.lastvisplane > MAXVISPLANES)
        }
    }

    @Test
    fun demonOnTheTeleporterStarRecordsEveryTouchedSpecialLine() {
        onMap(1, 5) {
            with(core) {
                val demon = pSpawnMobj(-800 shl FRACBITS, 1504 shl FRACBITS, ONFLOORZ, MT_SERGEANT)
                assertTrue(pCheckPosition(demon, demon.x, demon.y))
                assertTrue(numspechit > MAXSPECIALCROSS)
                assertEquals((787..796).toList(), (0 until numspechit).map { requireNotNull(spechit[it]).index })
            }
        }
    }

    private fun DoomEngine.renderFrom(x: FixedPoint, y: FixedPoint, angle: BinaryAngle, screenBlocks: Int) = with(core) {
        val player = players[0]
        val mo = requireNotNull(player.mo)
        pUnsetThingPosition(mo)
        mo.x = x
        mo.y = y
        pSetThingPosition(mo)
        mo.z = requireNotNull(mo.subsector?.sector).floorheight
        mo.angle = angle
        player.viewz = mo.z + VIEWHEIGHT
        rSetViewSize(screenBlocks, 0)
        rExecuteSetViewSize()
        rRenderPlayerView(player)
    }

    private fun onMap(episode: Int, map: Int, block: DoomEngine.() -> Unit) {
        var ticks = 0
        val engine = DoomEngine(DoomHost(clock = DoomClock { ticks }))
        try {
            engine.boot(listOf(wad), listOf("-warp", "$episode", "$map", "-skill", "4"))
            ticks++
            engine.stepSingleTic()
            engine.block()
        } finally {
            engine.close()
        }
    }
}
