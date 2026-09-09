// Port of linuxdoom-1.10 sounds.h struct definitions (tables generated into
// gen/SoundsGen.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("ClassName", "PropertyName", "unused", "ktlint")

package doom.engine

/**
 * SoundFX struct. `link` is an index into S_sfx (-1 = none) replacing the C
 * pointer; only sfx_chgun links (to sfx_pistol).
 */
internal class sfxinfo_t(
    val name: String,        // up to 6-character name (lump is DS<name>)
    val singularity: Boolean, // Sfx singularity (only one at a time)
    var priority: Int,
    val link: Int,           // referenced sound if a link, else -1
    val pitch: Int,          // pitch if a link
    val volume: Int,         // volume if a link
) {
    var usefulness: Int = 0  // -1 = unused/free for channel selection
    var lumpnum: Int = -1    // lump number of sfx
}
