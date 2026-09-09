// Compatibility adapters for the original thinker and world-tic vocabulary.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "PropertyName", "unused", "UNUSED_PARAMETER", "ktlint")

package doom.engine

import doom.engine.simulation.ThinkerScheduler
import doom.engine.simulation.WorldSimulation
import doom.engine.simulation.WorldTicker

internal var DoomEngineCore.leveltime
    get() = worldTicker.levelTime
    set(value) { worldTicker.restoreLevelTime(value) }

/** Savegames and deterministic verification may traverse the list without changing its links. */
internal val DoomEngineCore.thinkercap get() = thinkers.sentinel

internal fun DoomEngineCore.P_InitThinkers() = thinkers.reset()
internal fun DoomEngineCore.P_AddThinker(thinker: thinker_t) = thinkers.add(thinker)
internal fun DoomEngineCore.P_RemoveThinker(thinker: thinker_t) = thinkers.remove(thinker)
internal fun DoomEngineCore.P_RunThinkers() = thinkers.run()
internal fun DoomEngineCore.P_Ticker() = worldTicker.tick()

/** Original allocation hook; callers already allocate their own Kotlin thinker objects. */
internal fun DoomEngineCore.P_AllocateThinker(thinker: thinker_t) {}

internal fun createWorldTicker(core: DoomEngineCore, thinkers: ThinkerScheduler): WorldTicker = WorldTicker(
    thinkers = thinkers,
    world = object : WorldSimulation {
        override val paused: Boolean get() = core.paused
        override val pausedByMenu: Boolean get() = with(core) {
            !netgame && menuactive && !demoplayback && players[consoleplayer].viewz != 1
        }

        override fun updatePlayers() = with(core) {
            for (index in 0 until MAXPLAYERS) {
                if (playeringame[index]) P_PlayerThink(players[index])
            }
        }

        override fun updateSpecials() = core.P_UpdateSpecials()
        override fun respawnSpecials() = core.P_RespawnSpecials()
    },
)
