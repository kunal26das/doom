// Port of linuxdoom-1.10 p_mobj.h -- map object definition and flags.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// mobjflag_t
const val MF_SPECIAL = 1          // Call P_SpecialThing when touched.
const val MF_SOLID = 2            // Blocks.
const val MF_SHOOTABLE = 4        // Can be hit.
const val MF_NOSECTOR = 8         // Don't use the sector links (invisible but touchable).
const val MF_NOBLOCKMAP = 16      // Don't use the blocklinks (inert but displayable).
const val MF_AMBUSH = 32          // Not to be activated by sound, deaf monster.
const val MF_JUSTHIT = 64         // Will try to attack right back.
const val MF_JUSTATTACKED = 128   // Will take at least one step before attacking.
const val MF_SPAWNCEILING = 256   // On level spawning (initial position), hang from ceiling.
const val MF_NOGRAVITY = 512      // Don't apply gravity (every tic).

// Movement flags.
const val MF_DROPOFF = 0x400      // This allows jumps from high places.
const val MF_PICKUP = 0x800       // For players, will pick up items.
const val MF_NOCLIP = 0x1000      // Player cheat.
const val MF_SLIDE = 0x2000       // Player: keep info about sliding along walls.
const val MF_FLOAT = 0x4000       // Allow moves to any height, no gravity.
const val MF_TELEPORT = 0x8000    // Don't cross lines or look at heights on teleport.
const val MF_MISSILE = 0x10000    // Don't hit same species, explode on block.
const val MF_DROPPED = 0x20000    // Dropped by a demon, not level spawned.
const val MF_SHADOW = 0x40000     // Use fuzzy draw (shadow demons or spectres).
const val MF_NOBLOOD = 0x80000    // Flag: don't bleed when shot (use puff).
const val MF_CORPSE = 0x100000    // Don't stop moving halfway off a step.
const val MF_INFLOAT = 0x200000   // Floating to a height for a move.
const val MF_COUNTKILL = 0x400000 // Count towards intermission kill total.
const val MF_COUNTITEM = 0x800000 // Count towards intermission item total.
const val MF_SKULLFLY = 0x1000000 // Special handling: skull in flight.
const val MF_NOTDMATCH = 0x2000000 // Don't spawn this object in death match mode.

// Player sprites in multiplayer modes are modified using an internal color mapping.
const val MF_TRANSLATION = 0xc000000
const val MF_TRANSSHIFT = 26      // Hmm ???.

/** Map Object definition. */
class mobj_t : thinker_t(), soundorigin_t {
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
