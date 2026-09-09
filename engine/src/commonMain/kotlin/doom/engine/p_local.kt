// Port of linuxdoom-1.10 p_local.h -- play/simulation constants.
// (divline_t/intercept_t and the traverser machinery live in p_maputl.kt;
// the extern declarations disappear -- same package.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

internal const val FLOATSPEED = FRACUNIT * 4
internal const val MAXHEALTH = 100
internal const val VIEWHEIGHT = 41 * FRACUNIT

// mapblocks are used to check movement against lines and things
internal const val MAPBLOCKUNITS = 128
internal const val MAPBLOCKSIZE = MAPBLOCKUNITS * FRACUNIT
internal const val MAPBLOCKSHIFT = FRACBITS + 7
internal const val MAPBMASK = MAPBLOCKSIZE - 1
internal const val MAPBTOFRAC = MAPBLOCKSHIFT - FRACBITS

// player radius for movement checking
internal const val PLAYERRADIUS = 16 * FRACUNIT

// MAXRADIUS is for precalculated sector block boxes; the spider demon
// is larger, but we do not have any moving sectors nearby
internal const val MAXRADIUS = 32 * FRACUNIT

internal const val GRAVITY = FRACUNIT
internal const val MAXMOVE = 30 * FRACUNIT

internal const val USERANGE = 64 * FRACUNIT
internal const val MELEERANGE = 64 * FRACUNIT
internal const val MISSILERANGE = 32 * 64 * FRACUNIT

// follow a player exlusively for 3 seconds
internal const val BASETHRESHOLD = 100

// p_mobj.h spawn z specials
internal const val ONFLOORZ = MININT
internal const val ONCEILINGZ = MAXINT

// time interval for item respawning
internal const val ITEMQUESIZE = 128
