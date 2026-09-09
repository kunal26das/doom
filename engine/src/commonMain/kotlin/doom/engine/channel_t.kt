// Port of linuxdoom-1.10 s_sound.c -- high-level sound/music handling
// (channel allocation, distance attenuation, per-level music selection).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING",
    "SENSELESS_COMPARISON", "KotlinConstantConditions", "ktlint")

package doom.engine


internal class channel_t {
    // sound information (if null, channel avail.)
    var sfxinfo: sfxinfo_t? = null

    // origin of sound
    var origin: soundorigin_t? = null

    // handle of the sound being played
    var handle = 0
}
