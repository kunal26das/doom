// Port of linuxdoom-1.10 p_telept.c -- teleportation.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

//
// TELEPORTATION
//
fun EV_Teleport(line: line_t, side: Int, thing: mobj_t): Int {
    // don't teleport missiles
    if ((thing.flags and MF_MISSILE) != 0)
        return 0

    // Don't teleport if hit back of line,
    //  so you can get out of teleporter.
    if (side == 1)
        return 0

    val tag = line.tag
    for (i in 0 until numsectors) {
        if (sectors[i].tag == tag) {
            var thinker: thinker_t = thinkercap.next!!
            while (thinker !== thinkercap) {
                // not a mobj
                // (C: thinker->function.acp1 != (actionf_p1)P_MobjThinker;
                //  a removed thinker's function is the -1 sentinel, so it
                //  fails that comparison too -- hence the `removed` check.)
                if (thinker is mobj_t && !thinker.removed) {
                    val m: mobj_t = thinker

                    // not a teleportman
                    if (m.type == MT_TELEPORTMAN) {
                        val sector = m.subsector!!.sector!!
                        // wrong sector
                        if (sector.index == i) {
                            val oldx = thing.x
                            val oldy = thing.y
                            val oldz = thing.z

                            if (!P_TeleportMove(thing, m.x, m.y))
                                return 0

                            thing.z = thing.floorz  // fixme: not needed?
                            if (thing.player != null)
                                thing.player!!.viewz = thing.z + thing.player!!.viewheight

                            // spawn teleport fog at source and destination
                            var fog = P_SpawnMobj(oldx, oldy, oldz, MT_TFOG)
                            S_StartSound(fog, sfx_telept)
                            val an = (m.angle shr ANGLETOFINESHIFT).toInt()
                            fog = P_SpawnMobj(m.x + 20 * finecosine[an], m.y + 20 * finesine[an],
                                thing.z, MT_TFOG)

                            // emit sound, where?
                            S_StartSound(fog, sfx_telept)

                            // don't move for a bit
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
