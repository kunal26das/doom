// Port of linuxdoom-1.10 p_mobj.h -- map object definition and flags.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

// mobjflag_t
internal const val MF_SPECIAL = 1          // Call P_SpecialThing when touched.
internal const val MF_SOLID = 2            // Blocks.
internal const val MF_SHOOTABLE = 4        // Can be hit.
internal const val MF_NOSECTOR = 8         // Don't use the sector links (invisible but touchable).
internal const val MF_NOBLOCKMAP = 16      // Don't use the blocklinks (inert but displayable).
internal const val MF_AMBUSH = 32          // Not to be activated by sound, deaf monster.
internal const val MF_JUSTHIT = 64         // Will try to attack right back.
internal const val MF_JUSTATTACKED = 128   // Will take at least one step before attacking.
internal const val MF_SPAWNCEILING = 256   // On level spawning (initial position), hang from ceiling.
internal const val MF_NOGRAVITY = 512      // Don't apply gravity (every tic).

// Movement flags.
internal const val MF_DROPOFF = 0x400      // This allows jumps from high places.
internal const val MF_PICKUP = 0x800       // For players, will pick up items.
internal const val MF_NOCLIP = 0x1000      // Player cheat.
internal const val MF_SLIDE = 0x2000       // Player: keep info about sliding along walls.
internal const val MF_FLOAT = 0x4000       // Allow moves to any height, no gravity.
internal const val MF_TELEPORT = 0x8000    // Don't cross lines or look at heights on teleport.
internal const val MF_MISSILE = 0x10000    // Don't hit same species, explode on block.
internal const val MF_DROPPED = 0x20000    // Dropped by a demon, not level spawned.
internal const val MF_SHADOW = 0x40000     // Use fuzzy draw (shadow demons or spectres).
internal const val MF_NOBLOOD = 0x80000    // Flag: don't bleed when shot (use puff).
internal const val MF_CORPSE = 0x100000    // Don't stop moving halfway off a step.
internal const val MF_INFLOAT = 0x200000   // Floating to a height for a move.
internal const val MF_COUNTKILL = 0x400000 // Count towards intermission kill total.
internal const val MF_COUNTITEM = 0x800000 // Count towards intermission item total.
internal const val MF_SKULLFLY = 0x1000000 // Special handling: skull in flight.
internal const val MF_NOTDMATCH = 0x2000000 // Don't spawn this object in death match mode.

// Player sprites in multiplayer modes are modified using an internal color mapping.
internal const val MF_TRANSLATION = 0xc000000
internal const val MF_TRANSSHIFT = 26      // Hmm ???.
