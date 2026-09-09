// Port of linuxdoom-1.10 p_mobj.h -- map object definition and flags.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

/** Map Object definition. */
internal class mobj_t : thinker_t(), soundorigin_t {
    // Info for drawing: position.
    override var x: fixed_t = 0
    override var y: fixed_t = 0
    var z: fixed_t = 0

    // More list: links in sector (if needed)
    var snext: mobj_t? = null
    var sprev: mobj_t? = null

    // More drawing info: to determine current sprite.
    var angle: angle_t = 0u   // orientation
    var sprite = 0            // spritenum_t: used to find patch_t and flip value
    var frame = 0             // might be ORed with FF_FULLBRIGHT

    // Interaction info, by BLOCKMAP. Links in blocks (if needed).
    var bnext: mobj_t? = null
    var bprev: mobj_t? = null

    var subsector: subsector_t? = null

    // The closest interval over all contacted Sectors.
    var floorz: fixed_t = 0
    var ceilingz: fixed_t = 0

    // For movement checking.
    var radius: fixed_t = 0
    var height: fixed_t = 0

    // Momentums, used to update position.
    var momx: fixed_t = 0
    var momy: fixed_t = 0
    var momz: fixed_t = 0

    // If == validcount, already checked.
    var validcount = 0

    var type = 0              // mobjtype_t
    var info: mobjinfo_t? = null  // &mobjinfo[mobj->type]

    var tics = 0              // state tic counter
    var state: state_t? = null
    var flags = 0
    var health = 0

    // Movement direction, movement generation (zig-zagging).
    var movedir = 0           // 0-7
    var movecount = 0         // when 0, select a new dir

    // Thing being chased/attacked (or NULL), also the originator for missiles.
    var target: mobj_t? = null

    // Reaction time: if non 0, don't attack yet.
    // Used by player to freeze a bit after teleporting.
    var reactiontime = 0

    // If >0, the target will be chased no matter what (even if shot).
    var threshold = 0

    // Additional info record for player avatars only. Only valid if type == MT_PLAYER.
    var player: player_t? = null

    // Player number last looked for.
    var lastlook = 0

    // For nightmare respawn.
    var spawnpoint: mapthing_t? = null

    // Thing being chased/attacked for tracers.
    var tracer: mobj_t? = null
}
