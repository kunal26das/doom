
package doom.engine.gameplay

import doom.engine.TICRATE

internal const val SHAREWARE = 0
internal const val REGISTERED = 1
internal const val COMMERCIAL = 2
internal const val RETAIL = 3
internal const val INDETERMINED = 4

internal const val DOOM = 0
internal const val DOOM2 = 1
internal const val PACK_TNT = 2
internal const val PACK_PLUT = 3
internal const val NONE = 4

internal const val ENGLISH = 0
internal const val FRENCH = 1
internal const val GERMAN = 2
internal const val UNKNOWN = 3

internal const val MAXPLAYERS = 4

internal const val GS_LEVEL = 0
internal const val GS_INTERMISSION = 1
internal const val GS_FINALE = 2
internal const val GS_DEMOSCREEN = 3
internal const val GS_WIPE = -1

internal const val MTF_EASY = 1
internal const val MTF_NORMAL = 2
internal const val MTF_HARD = 4
internal const val MTF_AMBUSH = 8

internal const val SK_BABY = 0
internal const val SK_EASY = 1
internal const val SK_MEDIUM = 2
internal const val SK_HARD = 3
internal const val SK_NIGHTMARE = 4

internal const val IT_BLUECARD = 0
internal const val IT_YELLOWCARD = 1
internal const val IT_REDCARD = 2
internal const val IT_BLUESKULL = 3
internal const val IT_YELLOWSKULL = 4
internal const val IT_REDSKULL = 5
internal const val NUMCARDS = 6

internal const val WP_FIST = 0
internal const val WP_PISTOL = 1
internal const val WP_SHOTGUN = 2
internal const val WP_CHAINGUN = 3
internal const val WP_MISSILE = 4
internal const val WP_PLASMA = 5
internal const val WP_BFG = 6
internal const val WP_CHAINSAW = 7
internal const val WP_SUPERSHOTGUN = 8
internal const val NUMWEAPONS = 9
internal const val WP_NOCHANGE = 9

internal const val AM_CLIP = 0
internal const val AM_SHELL = 1
internal const val AM_CELL = 2
internal const val AM_MISL = 3
internal const val NUMAMMO = 4
internal const val AM_NOAMMO = 4

internal const val PW_INVULNERABILITY = 0
internal const val PW_STRENGTH = 1
internal const val PW_INVISIBILITY = 2
internal const val PW_IRONFEET = 3
internal const val PW_ALLMAP = 4
internal const val PW_INFRARED = 5
internal const val NUMPOWERS = 6

internal const val INVULNTICS = 30 * TICRATE
internal const val INVISTICS = 60 * TICRATE
internal const val INFRATICS = 120 * TICRATE
internal const val IRONTICS = 60 * TICRATE
