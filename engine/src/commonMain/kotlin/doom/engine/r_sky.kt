// Port of linuxdoom-1.10 r_sky.c -- Sky rendering. The DOOM sky is a texture
// map like any wall, wrapping around. A 1024 columns equal 360 degrees.
// The default sky map is 256 columns and repeats 4 times on a 320 screen?
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

// SKY, store the number for name. (r_sky.h)
internal const val SKYFLATNAME = "F_SKY1"

// The sky map is 256*128*4 maps. (r_sky.h)
internal const val ANGLETOSKYSHIFT = 22

internal var DoomEngineCore.skyflatnum
    get() = stateSkyRenderer.skyflatnum
    set(value) { stateSkyRenderer.skyflatnum = value }
internal var DoomEngineCore.skytexture
    get() = stateSkyRenderer.skytexture
    set(value) { stateSkyRenderer.skytexture = value }
internal var DoomEngineCore.skytexturemid
    get() = stateSkyRenderer.skytexturemid
    set(value) { stateSkyRenderer.skytexturemid = value }

//
// R_InitSkyMap
// Called whenever the view size changes.
//
internal fun DoomEngineCore.R_InitSkyMap() {
    // skyflatnum = R_FlatNumForName ( SKYFLATNAME );
    skytexturemid = 100 * FRACUNIT
}
