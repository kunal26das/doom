
package doom.engine.world

import doom.engine.geometry.FRACBITS
import doom.engine.geometry.FRACUNIT
import doom.engine.resources.MAXINT
import doom.engine.resources.MININT

internal const val FLOATSPEED = FRACUNIT * 4
internal const val MAXHEALTH = 100
internal const val VIEWHEIGHT = 41 * FRACUNIT

internal const val MAPBLOCKUNITS = 128
internal const val MAPBLOCKSIZE = MAPBLOCKUNITS * FRACUNIT
internal const val MAPBLOCKSHIFT = FRACBITS + 7
internal const val MAPBMASK = MAPBLOCKSIZE - 1
internal const val MAPBTOFRAC = MAPBLOCKSHIFT - FRACBITS

internal const val PLAYERRADIUS = 16 * FRACUNIT

internal const val MAXRADIUS = 32 * FRACUNIT

internal const val GRAVITY = FRACUNIT
internal const val MAXMOVE = 30 * FRACUNIT

internal const val USERANGE = 64 * FRACUNIT
internal const val MELEERANGE = 64 * FRACUNIT
internal const val MISSILERANGE = 32 * 64 * FRACUNIT

internal const val BASETHRESHOLD = 100

internal const val ONFLOORZ = MININT
internal const val ONCEILINGZ = MAXINT

internal const val ITEMQUESIZE = 128
