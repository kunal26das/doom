// Port of linuxdoom-1.10 r_sky.c -- Sky rendering. The DOOM sky is a texture
// map like any wall, wrapping around. A 1024 columns equal 360 degrees.
// The default sky map is 256 columns and repeats 4 times on a 320 screen?
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class SkyRendererState {
    var skyflatnum = 0

    var skytexture = 0

    var skytexturemid = 0
}
