
package doom.engine.simulation

import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.actors.pRespawnSpecials
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.demoplayback
import doom.engine.gameplay.netgame
import doom.engine.gameplay.paused
import doom.engine.gameplay.player.pPlayerThink
import doom.engine.gameplay.playeringame
import doom.engine.gameplay.players
import doom.engine.menu.menuactive
import doom.engine.world.specials.pUpdateSpecials

internal var DoomEngineCore.leveltime
    get() = worldTicker.levelTime
    set(value) { worldTicker.restoreLevelTime(value) }

internal val DoomEngineCore.thinkercap get() = thinkers.sentinel

internal fun DoomEngineCore.pInitThinkers() = thinkers.reset()
internal fun DoomEngineCore.pAddThinker(thinker: Thinker) = thinkers.add(thinker)
internal fun DoomEngineCore.pRemoveThinker(thinker: Thinker) = thinkers.remove(thinker)
internal fun DoomEngineCore.pRunThinkers() = thinkers.run()
internal fun DoomEngineCore.pTicker() = worldTicker.tick()

internal fun createWorldTicker(core: DoomEngineCore, thinkers: ThinkerScheduler): WorldTicker = WorldTicker(
    thinkers = thinkers,
    world = object : WorldSimulation {
        override val paused: Boolean get() = core.paused
        override val pausedByMenu: Boolean get() = with(core) {
            !netgame && menuactive && !demoplayback && players[consoleplayer].viewz != 1
        }

        override fun updatePlayers() = with(core) {
            for (index in 0 until MAXPLAYERS) {
                if (playeringame[index]) pPlayerThink(players[index])
            }
        }

        override fun updateSpecials() = core.pUpdateSpecials()
        override fun respawnSpecials() = core.pRespawnSpecials()
    },
)
