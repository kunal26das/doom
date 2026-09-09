package doom.engine.savegame

import doom.engine.DoomClock
import doom.engine.DoomEngine
import doom.engine.DoomHost
import doom.engine.DoomKeyInput
import doom.engine.KEY_RIGHTARROW
import doom.engine.KEY_UPARROW
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.gameepisode
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.gameskill
import doom.engine.gameplay.gDoLoadGame
import doom.engine.gameplay.nodrawers
import doom.engine.gameplay.players
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.menu.loadMenu
import doom.engine.menu.mDoSave
import doom.engine.menu.mLoadSelect
import doom.engine.menu.mReadSaveStrings
import doom.engine.menu.savegamestrings
import doom.engine.resources.GGSAVED
import doom.engine.simulation.leveltime
import doom.engine.simulation.thinkercap
import doom.engine.world.lines
import doom.engine.world.movers.activeplats
import doom.engine.world.movers.DOWN_WAIT_UP_STAY
import doom.engine.world.movers.evDoPlat
import doom.engine.world.movers.evStopPlat
import doom.engine.world.movers.evVerticalDoor
import doom.engine.world.movers.IN_STASIS
import doom.engine.world.movers.pActivateInStasis
import doom.engine.world.movers.PlatformMover
import doom.engine.world.movers.VerticalDoor
import doom.engine.world.sectors
import doom.engine.world.sides
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SaveGameRoundTripTest {
    private val wad: ByteArray get() = File(requireNotNull(System.getProperty("doom.test.wad"))).readBytes()

    @Test
    fun menuSavesRestoreActualPlayerWorldAndActorsIntoFreshEnginesAcrossSlotsAndOverwrite() {
        val storage = SaveGameMemoryStorage()
        val expected = linkedMapOf<Int, Map<String, List<Int>>>()
        val source = boot(storage)
        try {
            val start = source.core.players[0].mo!!
            val position = start.x to start.y
            source.postInput(DoomKeyInput(KEY_UPARROW, true))
            step(source, storage, 10)
            source.postInput(DoomKeyInput(KEY_UPARROW, false))
            step(source, storage, 3)
            assertNotEquals(position, start.x to start.y)
            with(source.core) {
                players[0].health = 73
                players[0].mo!!.health = 73
                players[0].armorpoints = 51
                players[0].armortype = 1
                players[0].ammo[0] = 31
                players[0].weaponowned[2] = true
                players[0].cards[0] = true
                players[0].killcount = 7
                players[0].itemcount = 4
                players[0].secretcount = 1
                sectors[0].lightlevel = 77
                sides[0].textureoffset = 12 * FRACUNIT
                sides[0].rowoffset = -3 * FRACUNIT
            }
            save(source, storage, 0, "ORIGINAL SLOT")
            source.core.players[0].armorpoints = 29
            expected[3] = save(source, storage, 3, "OTHER SLOT")
            source.core.players[0].ammo[0] = 19
            expected[0] = save(source, storage, 0, "UPDATED SLOT")
        } finally {
            storage.onSave = null
            source.close()
        }

        for ((slot, state) in expected) {
            val restored = boot(storage, map = 2)
            try {
                with(restored.core) {
                    assertEquals(2, gamemap)
                    mReadSaveStrings()
                    assertEquals(if (slot == 0) "UPDATED SLOT" else "OTHER SLOT", savegamestrings[slot])
                    assertEquals(1, loadMenu[slot].status)
                    mLoadSelect(slot)
                    gDoLoadGame()
                    assertEquals(state, snapshot(this))
                    val player = players[0]
                    val actor = assertNotNull(player.mo)
                    assertSame(player, actor.player)
                    assertTrue(sectors.any { it === actor.subsector!!.sector })
                    val angle = actor.angle
                    val savedTime = leveltime
                    restored.postInput(DoomKeyInput(KEY_RIGHTARROW, true))
                    step(restored, storage, 8)
                    restored.postInput(DoomKeyInput(KEY_RIGHTARROW, false))
                    assertNotEquals(angle, actor.angle)
                    assertEquals(savedTime + 8, leveltime)
                    assertFalse(restored.quitRequested)
                }
            } finally { restored.close() }
        }
    }

    @Test
    fun savesRestoreMovingDoorsAndStoppedPlatformsThatCanResumeAfterRestart() {
        val storage = SaveGameMemoryStorage()
        val source = boot(storage)
        val platformSector: Int
        val doorSector: Int
        val state: Map<String, List<Int>>
        val platformState: List<Int>
        val doorState: List<Int>
        try {
            with(source.core) {
                val platformLine = lines.first { it.special == 88 }
                assertEquals(1, evDoPlat(platformLine, DOWN_WAIT_UP_STAY, 0))
                val platform = assertNotNull(activeplats.firstOrNull { it != null })
                platformSector = platform.sector!!.index
                val doorLine = lines.first { it.special == 1 }
                evVerticalDoor(doorLine, players[0].mo!!)
                val door = assertIs<VerticalDoor>(sides[doorLine.sidenum[1]].sector!!.specialdata)
                doorSector = door.sector!!.index
                step(source, storage, 3)
                evStopPlat(platformLine)
                assertEquals(IN_STASIS, platform.status)
                assertNull(platform.function)
                state = save(source, storage, 1, "MOVERS")
                platformState = platformSnapshot(platform)
                doorState = state.getValue("door:$doorSector")
            }
        } finally {
            storage.onSave = null
            source.close()
        }

        val restored = boot(storage, map = 2)
        try {
            with(restored.core) {
                mLoadSelect(1)
                gDoLoadGame()
                assertEquals(state, snapshot(this))
                val platform = assertIs<PlatformMover>(sectors[platformSector].specialdata)
                val door = assertIs<VerticalDoor>(sectors[doorSector].specialdata)
                assertEquals(platformState, platformSnapshot(platform))
                assertEquals(doorState, doorSnapshot(door))
                assertSame(platform, activeplats.first { it === platform })
                assertNull(platform.function)
                assertNotNull(door.function)
                val floor = platform.sector!!.floorheight
                val ceiling = door.sector!!.ceilingheight
                step(restored, storage, 3)
                assertEquals(floor, platform.sector!!.floorheight)
                assertTrue(door.sector!!.ceilingheight > ceiling)
                pActivateInStasis(platform.tag)
                assertNotNull(platform.function)
                step(restored, storage, 3)
                assertTrue(platform.sector!!.floorheight < floor)
            }
        } finally { restored.close() }
    }

    @Test
    fun failedMenuSavePropagatesStorageErrorWithoutSuccessMessageOrReplacingPreviousSave() {
        val storage = SaveGameMemoryStorage()
        val engine = boot(storage)
        try {
            save(engine, storage, 0, "KEEP THIS SAVE")
            val previous = assertNotNull(storage.read("doomsav0.dsg"))
            val failure = IllegalStateException("The save disk is full")
            storage.saveFailure = failure
            engine.core.players[0].message = null
            engine.core.savegamestrings[0] = "FAILED OVERWRITE"
            engine.core.mDoSave(0)

            val reported = assertFailsWith<IllegalStateException> {
                step(engine, storage, 4)
            }

            assertSame(failure, reported)
            assertNotEquals(GGSAVED, engine.core.players[0].message)
            assertContentEquals(previous, storage.read("doomsav0.dsg"))
            assertTrue(engine.core.host.isDetached)
        } finally {
            storage.onSave = null
            storage.saveFailure = null
            engine.close()
        }
    }

    @Test
    fun failedMenuLoadPropagatesStorageErrorWithoutReplacingTheLevelOrSavedBytes() {
        val storage = SaveGameMemoryStorage()
        val source = boot(storage)
        try {
            save(source, storage, 0, "READ FAILURE")
        } finally {
            storage.onSave = null
            source.close()
        }
        val previous = assertNotNull(storage.read("doomsav0.dsg"))
        val restored = boot(storage, map = 2)
        try {
            val failure = IllegalStateException("The save file is unreadable")
            storage.loadFailure = failure
            restored.core.mLoadSelect(0)

            val reported = assertFailsWith<IllegalStateException> {
                step(restored, storage, 1)
            }

            assertSame(failure, reported)
            assertEquals(2, restored.core.gamemap)
            assertTrue(restored.core.host.isDetached)
            storage.loadFailure = null
            assertContentEquals(previous, storage.read("doomsav0.dsg"))
        } finally {
            storage.loadFailure = null
            restored.close()
        }
    }

    private fun boot(storage: SaveGameMemoryStorage, map: Int = 1): DoomEngine {
        val engine = DoomEngine(DoomHost(clock = DoomClock { storage.ticks }, storage = storage))
        try {
            engine.boot(listOf(wad), listOf("-warp", "1", map.toString(), "-skill", "3"))
            engine.core.nodrawers = true
            return engine
        } catch (failure: Throwable) {
            engine.close()
            throw failure
        }
    }

    private fun step(engine: DoomEngine, storage: SaveGameMemoryStorage, count: Int) {
        repeat(count) {
            storage.ticks++
            engine.stepSingleTic()
        }
    }

    private fun save(engine: DoomEngine, storage: SaveGameMemoryStorage, slot: Int, description: String): Map<String, List<Int>> {
        var captured: Map<String, List<Int>>? = null
        storage.onSave = { captured = snapshot(engine.core) }
        engine.core.savegamestrings[slot] = description
        engine.core.mDoSave(slot)
        repeat(4) { if (captured == null) step(engine, storage, 1) }
        storage.onSave = null
        assertNotNull(storage.read("doomsav$slot.dsg"))
        return assertNotNull(captured, "The menu save request must reach the storage port")
    }

    private fun snapshot(core: DoomEngineCore): Map<String, List<Int>> = with(core) {
        val result = linkedMapOf<String, List<Int>>()
        result["level"] = listOf(gameepisode, gamemap, gameskill, leveltime)
        val player = players[0]
        result["player"] = listOf(
            player.playerstate, player.health, player.armorpoints, player.armortype,
            player.readyweapon, player.pendingweapon, player.killcount, player.itemcount, player.secretcount,
            player.viewz, player.viewheight, player.deltaviewheight, player.bob,
        ) + player.ammo.toList() + player.maxammo.toList() + player.powers.toList() +
            player.cards.map { if (it) 1 else 0 } + player.weaponowned.map { if (it) 1 else 0 }
        result["weaponSprites"] = player.psprites.flatMap { listOf(it.state?.index ?: 0, it.tics, it.sx, it.sy) }
        result["sectors"] = sectors.flatMap {
            listOf(it.floorheight shr FRACBITS, it.ceilingheight shr FRACBITS, it.floorpic, it.ceilingpic, it.lightlevel, it.special, it.tag)
        }
        result["lines"] = lines.flatMap { listOf(it.flags, it.special, it.tag) }
        result["sides"] = sides.flatMap {
            listOf(it.textureoffset shr FRACBITS, it.rowoffset shr FRACBITS, it.toptexture, it.bottomtexture, it.midtexture)
        }
        var thinker = thinkercap.next
        var actorIndex = 0
        while (thinker != null && thinker !== thinkercap) {
            val current = thinker
            if (!current.removed) when (current) {
                is Actor -> {
                    result["actor:${actorIndex++}"] = listOf(
                        current.type, current.x, current.y, current.z, current.angle.toInt(),
                        current.momx, current.momy, current.momz, current.health, current.flags,
                        current.state?.index ?: 0, current.tics, current.movedir, current.movecount,
                        current.reactiontime, current.threshold,
                    )
                }
                is PlatformMover -> result["platform:${current.sector!!.index}"] = platformSnapshot(current)
                is VerticalDoor -> result["door:${current.sector!!.index}"] = doorSnapshot(current)
            }
            thinker = current.next
        }
        result
    }

    private fun platformSnapshot(platform: PlatformMover): List<Int> = with(platform) {
        listOf(sector!!.index, speed, low, high, wait, count, status, oldstatus, if (crush) 1 else 0, tag, type)
    }

    private fun doorSnapshot(door: VerticalDoor): List<Int> = with(door) {
        listOf(sector!!.index, type, topheight, speed, direction, topwait, topcountdown)
    }
}
