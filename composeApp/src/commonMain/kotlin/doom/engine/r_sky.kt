// Port of linuxdoom-1.10 r_sky.c -- Sky rendering. The DOOM sky is a texture
// map like any wall, wrapping around. A 1024 columns equal 360 degrees.
// The default sky map is 256 columns and repeats 4 times on a 320 screen?
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// SKY, store the number for name. (r_sky.h)
const val SKYFLATNAME = "F_SKY1"

// The sky map is 256*128*4 maps. (r_sky.h)
const val ANGLETOSKYSHIFT = 22

//
// sky mapping
//
var skyflatnum = 0
var skytexture = 0
var skytexturemid = 0

//
// R_InitSkyMap
// Called whenever the view size changes.
//
fun R_InitSkyMap() {
    // skyflatnum = R_FlatNumForName ( SKYFLATNAME );
    skytexturemid = 100 * FRACUNIT
}
