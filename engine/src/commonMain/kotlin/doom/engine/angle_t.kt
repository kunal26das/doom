// Port of linuxdoom-1.10 tables.h/tables.c (lookup data itself is generated
// into gen/TablesGen.kt; this file holds the constants and SlopeDiv).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "unused", "MagicNumber", "ktlint")

package doom.engine

/** Binary Angle Measurement: 32-bit unsigned, wraps like the C angle_t. */
internal typealias angle_t = UInt
