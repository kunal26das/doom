// Port of linuxdoom-1.10 p_local.h -- play/simulation constants.
// (divline_t/intercept_t and the traverser machinery live in p_maputl.kt;
// the extern declarations disappear -- same package.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

const val FLOATSPEED = FRACUNIT * 4
const val MAXHEALTH = 100
const val VIEWHEIGHT = 41 * FRACUNIT

// mapblocks are used to check movement against lines and things
const val MAPBLOCKUNITS = 128
const val MAPBLOCKSIZE = MAPBLOCKUNITS * FRACUNIT
const val MAPBLOCKSHIFT = FRACBITS + 7
const val MAPBMASK = MAPBLOCKSIZE - 1
const val MAPBTOFRAC = MAPBLOCKSHIFT - FRACBITS

// player radius for movement checking
const val PLAYERRADIUS = 16 * FRACUNIT

// MAXRADIUS is for precalculated sector block boxes; the spider demon
// is larger, but we do not have any moving sectors nearby
const val MAXRADIUS = 32 * FRACUNIT

const val GRAVITY = FRACUNIT
const val MAXMOVE = 30 * FRACUNIT

const val USERANGE = 64 * FRACUNIT
const val MELEERANGE = 64 * FRACUNIT
const val MISSILERANGE = 32 * 64 * FRACUNIT

// follow a player exlusively for 3 seconds
const val BASETHRESHOLD = 100

// p_mobj.h spawn z specials
const val ONFLOORZ = MININT
const val ONCEILINGZ = MAXINT

// time interval for item respawning
const val ITEMQUESIZE = 128
