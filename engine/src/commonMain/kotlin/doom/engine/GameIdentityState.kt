// Port of linuxdoom-1.10 doomstat.c -- global mode/mission/language state.
// (Most game-state globals live in the files that define them in C:
//  g_game.kt, d_main.kt, etc. -- doomstat.h only declared them extern.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("PropertyName", "unused", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class GameIdentityState {
    var gamemode = indetermined

    var gamemission = doom

    var language = english
}
