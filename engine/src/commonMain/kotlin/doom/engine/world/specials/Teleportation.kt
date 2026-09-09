
package doom.engine.world.specials

import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_TELEPT
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.actors.MF_MISSILE
import doom.engine.gameplay.actors.MT_TELEPORTMAN
import doom.engine.gameplay.actors.MT_TFOG
import doom.engine.gameplay.actors.pSpawnMobj
import doom.engine.geometry.ANGLETOFINESHIFT
import doom.engine.geometry.FineCosineTable
import doom.engine.geometry.finesine
import doom.engine.simulation.Thinker
import doom.engine.simulation.thinkercap
import doom.engine.world.MapLine
import doom.engine.world.collision.pTeleportMove
import doom.engine.world.numsectors
import doom.engine.world.sectors

internal fun DoomEngineCore.evTeleport(line: MapLine, side: Int, thing: Actor): Int {
    if ((thing.flags and MF_MISSILE) != 0)
        return 0

    if (side == 1)
        return 0

    val tag = line.tag
    for (i in 0 until numsectors) {
        if (sectors[i].tag == tag) {
            var thinker: Thinker = thinkercap.next!!
            while (thinker !== thinkercap) {
                if (thinker is Actor && !thinker.removed) {
                    val m: Actor = thinker

                    if (m.type == MT_TELEPORTMAN) {
                        val sector = m.subsector!!.sector!!
                        if (sector.index == i) {
                            val oldx = thing.x
                            val oldy = thing.y
                            val oldz = thing.z

                            if (!pTeleportMove(thing, m.x, m.y))
                                return 0

                            thing.z = thing.floorz
                            if (thing.player != null)
                                thing.player!!.viewz = thing.z + thing.player!!.viewheight

                            var fog = pSpawnMobj(oldx, oldy, oldz, MT_TFOG)
                            sStartSound(fog, SFX_TELEPT)
                            val an = (m.angle shr ANGLETOFINESHIFT).toInt()
                            fog = pSpawnMobj(m.x + 20 * FineCosineTable[an], m.y + 20 * finesine[an],
                                thing.z, MT_TFOG)

                            sStartSound(fog, SFX_TELEPT)

                            if (thing.player != null)
                                thing.reactiontime = 18

                            thing.angle = m.angle
                            thing.momz = 0
                            thing.momy = 0
                            thing.momx = 0
                            return 1
                        }
                    }
                }
                thinker = thinker.next!!
            }
        }
    }
    return 0
}
