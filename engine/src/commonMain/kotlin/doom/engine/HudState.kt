// Port of linuxdoom-1.10 hu_stuff.c/hu_stuff.h -- Heads-up displays.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class HudState {
    val chat_macros by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        HUSTR_CHATMACRO0,
        HUSTR_CHATMACRO1,
        HUSTR_CHATMACRO2,
        HUSTR_CHATMACRO3,
        HUSTR_CHATMACRO4,
        HUSTR_CHATMACRO5,
        HUSTR_CHATMACRO6,
        HUSTR_CHATMACRO7,
        HUSTR_CHATMACRO8,
        HUSTR_CHATMACRO9,
    ) }

    val player_names by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        HUSTR_PLRGREEN,
        HUSTR_PLRINDIGO,
        HUSTR_PLRBROWN,
        HUSTR_PLRRED,
    ) }

    var chat_char = 0

    lateinit var plr: player_t

    val hu_font by lazy(LazyThreadSafetyMode.NONE) { Array(HU_FONTSIZE) { ByteArray(0) } }

    val w_title by lazy(LazyThreadSafetyMode.NONE) { hu_textline_t() }

    var chat_on = false

    val w_chat by lazy(LazyThreadSafetyMode.NONE) { hu_itext_t() }

    var always_off = false

    val chat_dest by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    val w_inputbuffer by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { hu_itext_t() } }

    var message_on = false

    var message_dontfuckwithme = false

    var message_nottobefuckedwith = false

    val w_message by lazy(LazyThreadSafetyMode.NONE) { hu_stext_t() }

    var message_counter = 0

    var headsupactive = false

    val mapnames by lazy(LazyThreadSafetyMode.NONE) { arrayOf( // DOOM shareware/registered/retail (Ultimate) names.

        HUSTR_E1M1,
        HUSTR_E1M2,
        HUSTR_E1M3,
        HUSTR_E1M4,
        HUSTR_E1M5,
        HUSTR_E1M6,
        HUSTR_E1M7,
        HUSTR_E1M8,
        HUSTR_E1M9,

        HUSTR_E2M1,
        HUSTR_E2M2,
        HUSTR_E2M3,
        HUSTR_E2M4,
        HUSTR_E2M5,
        HUSTR_E2M6,
        HUSTR_E2M7,
        HUSTR_E2M8,
        HUSTR_E2M9,

        HUSTR_E3M1,
        HUSTR_E3M2,
        HUSTR_E3M3,
        HUSTR_E3M4,
        HUSTR_E3M5,
        HUSTR_E3M6,
        HUSTR_E3M7,
        HUSTR_E3M8,
        HUSTR_E3M9,

        HUSTR_E4M1,
        HUSTR_E4M2,
        HUSTR_E4M3,
        HUSTR_E4M4,
        HUSTR_E4M5,
        HUSTR_E4M6,
        HUSTR_E4M7,
        HUSTR_E4M8,
        HUSTR_E4M9,

        "NEWLEVEL",
        "NEWLEVEL",
        "NEWLEVEL",
        "NEWLEVEL",
        "NEWLEVEL",
        "NEWLEVEL",
        "NEWLEVEL",
        "NEWLEVEL",
        "NEWLEVEL",
    ) }

    val mapnames2 by lazy(LazyThreadSafetyMode.NONE) { arrayOf( // DOOM 2 map names.
        HUSTR_1,
        HUSTR_2,
        HUSTR_3,
        HUSTR_4,
        HUSTR_5,
        HUSTR_6,
        HUSTR_7,
        HUSTR_8,
        HUSTR_9,
        HUSTR_10,
        HUSTR_11,

        HUSTR_12,
        HUSTR_13,
        HUSTR_14,
        HUSTR_15,
        HUSTR_16,
        HUSTR_17,
        HUSTR_18,
        HUSTR_19,
        HUSTR_20,

        HUSTR_21,
        HUSTR_22,
        HUSTR_23,
        HUSTR_24,
        HUSTR_25,
        HUSTR_26,
        HUSTR_27,
        HUSTR_28,
        HUSTR_29,
        HUSTR_30,
        HUSTR_31,
        HUSTR_32,
    ) }

    val mapnamesp by lazy(LazyThreadSafetyMode.NONE) { arrayOf( // Plutonia WAD map names.
        PHUSTR_1,
        PHUSTR_2,
        PHUSTR_3,
        PHUSTR_4,
        PHUSTR_5,
        PHUSTR_6,
        PHUSTR_7,
        PHUSTR_8,
        PHUSTR_9,
        PHUSTR_10,
        PHUSTR_11,

        PHUSTR_12,
        PHUSTR_13,
        PHUSTR_14,
        PHUSTR_15,
        PHUSTR_16,
        PHUSTR_17,
        PHUSTR_18,
        PHUSTR_19,
        PHUSTR_20,

        PHUSTR_21,
        PHUSTR_22,
        PHUSTR_23,
        PHUSTR_24,
        PHUSTR_25,
        PHUSTR_26,
        PHUSTR_27,
        PHUSTR_28,
        PHUSTR_29,
        PHUSTR_30,
        PHUSTR_31,
        PHUSTR_32,
    ) }

    val mapnamest by lazy(LazyThreadSafetyMode.NONE) { arrayOf( // TNT WAD map names.
        THUSTR_1,
        THUSTR_2,
        THUSTR_3,
        THUSTR_4,
        THUSTR_5,
        THUSTR_6,
        THUSTR_7,
        THUSTR_8,
        THUSTR_9,
        THUSTR_10,
        THUSTR_11,

        THUSTR_12,
        THUSTR_13,
        THUSTR_14,
        THUSTR_15,
        THUSTR_16,
        THUSTR_17,
        THUSTR_18,
        THUSTR_19,
        THUSTR_20,

        THUSTR_21,
        THUSTR_22,
        THUSTR_23,
        THUSTR_24,
        THUSTR_25,
        THUSTR_26,
        THUSTR_27,
        THUSTR_28,
        THUSTR_29,
        THUSTR_30,
        THUSTR_31,
        THUSTR_32,
    ) }

    var shiftxform = IntArray(0)

    val english_shiftxform by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(

        0,
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
        31,
        ' '.code, '!'.code, '"'.code, '#'.code, '$'.code, '%'.code, '&'.code,
        '"'.code, // shift-'
        '('.code, ')'.code, '*'.code, '+'.code,
        '<'.code, // shift-,
        '_'.code, // shift--
        '>'.code, // shift-.
        '?'.code, // shift-/
        ')'.code, // shift-0
        '!'.code, // shift-1
        '@'.code, // shift-2
        '#'.code, // shift-3
        '$'.code, // shift-4
        '%'.code, // shift-5
        '^'.code, // shift-6
        '&'.code, // shift-7
        '*'.code, // shift-8
        '('.code, // shift-9
        ':'.code,
        ':'.code, // shift-;
        '<'.code,
        '+'.code, // shift-=
        '>'.code, '?'.code, '@'.code,
        'A'.code, 'B'.code, 'C'.code, 'D'.code, 'E'.code, 'F'.code, 'G'.code,
        'H'.code, 'I'.code, 'J'.code, 'K'.code, 'L'.code, 'M'.code, 'N'.code,
        'O'.code, 'P'.code, 'Q'.code, 'R'.code, 'S'.code, 'T'.code, 'U'.code,
        'V'.code, 'W'.code, 'X'.code, 'Y'.code, 'Z'.code,
        '['.code, // shift-[
        '!'.code, // shift-backslash - OH MY GOD DOES WATCOM SUCK
        ']'.code, // shift-]
        '"'.code, '_'.code,
        '\''.code, // shift-`
        'A'.code, 'B'.code, 'C'.code, 'D'.code, 'E'.code, 'F'.code, 'G'.code,
        'H'.code, 'I'.code, 'J'.code, 'K'.code, 'L'.code, 'M'.code, 'N'.code,
        'O'.code, 'P'.code, 'Q'.code, 'R'.code, 'S'.code, 'T'.code, 'U'.code,
        'V'.code, 'W'.code, 'X'.code, 'Y'.code, 'Z'.code,
        '{'.code, '|'.code, '}'.code, '~'.code, 127,
    ) }

    val chatchars by lazy(LazyThreadSafetyMode.NONE) { IntArray(QUEUESIZE) }

    var head = 0

    var tail = 0

    var lastmessage = ""

    var shiftdown = false

    var altdown = false

    val destination_keys by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        HUSTR_KEYGREEN,
        HUSTR_KEYINDIGO,
        HUSTR_KEYBROWN,
        HUSTR_KEYRED,
    ) }

    var num_nobrainers = 0
}
