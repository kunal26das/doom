
package doom.engine.hud

import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.player.Player
import doom.engine.resources.HUSTR_1
import doom.engine.resources.HUSTR_10
import doom.engine.resources.HUSTR_11
import doom.engine.resources.HUSTR_12
import doom.engine.resources.HUSTR_13
import doom.engine.resources.HUSTR_14
import doom.engine.resources.HUSTR_15
import doom.engine.resources.HUSTR_16
import doom.engine.resources.HUSTR_17
import doom.engine.resources.HUSTR_18
import doom.engine.resources.HUSTR_19
import doom.engine.resources.HUSTR_2
import doom.engine.resources.HUSTR_20
import doom.engine.resources.HUSTR_21
import doom.engine.resources.HUSTR_22
import doom.engine.resources.HUSTR_23
import doom.engine.resources.HUSTR_24
import doom.engine.resources.HUSTR_25
import doom.engine.resources.HUSTR_26
import doom.engine.resources.HUSTR_27
import doom.engine.resources.HUSTR_28
import doom.engine.resources.HUSTR_29
import doom.engine.resources.HUSTR_3
import doom.engine.resources.HUSTR_30
import doom.engine.resources.HUSTR_31
import doom.engine.resources.HUSTR_32
import doom.engine.resources.HUSTR_4
import doom.engine.resources.HUSTR_5
import doom.engine.resources.HUSTR_6
import doom.engine.resources.HUSTR_7
import doom.engine.resources.HUSTR_8
import doom.engine.resources.HUSTR_9
import doom.engine.resources.HUSTR_CHATMACRO0
import doom.engine.resources.HUSTR_CHATMACRO1
import doom.engine.resources.HUSTR_CHATMACRO2
import doom.engine.resources.HUSTR_CHATMACRO3
import doom.engine.resources.HUSTR_CHATMACRO4
import doom.engine.resources.HUSTR_CHATMACRO5
import doom.engine.resources.HUSTR_CHATMACRO6
import doom.engine.resources.HUSTR_CHATMACRO7
import doom.engine.resources.HUSTR_CHATMACRO8
import doom.engine.resources.HUSTR_CHATMACRO9
import doom.engine.resources.HUSTR_E1M1
import doom.engine.resources.HUSTR_E1M2
import doom.engine.resources.HUSTR_E1M3
import doom.engine.resources.HUSTR_E1M4
import doom.engine.resources.HUSTR_E1M5
import doom.engine.resources.HUSTR_E1M6
import doom.engine.resources.HUSTR_E1M7
import doom.engine.resources.HUSTR_E1M8
import doom.engine.resources.HUSTR_E1M9
import doom.engine.resources.HUSTR_E2M1
import doom.engine.resources.HUSTR_E2M2
import doom.engine.resources.HUSTR_E2M3
import doom.engine.resources.HUSTR_E2M4
import doom.engine.resources.HUSTR_E2M5
import doom.engine.resources.HUSTR_E2M6
import doom.engine.resources.HUSTR_E2M7
import doom.engine.resources.HUSTR_E2M8
import doom.engine.resources.HUSTR_E2M9
import doom.engine.resources.HUSTR_E3M1
import doom.engine.resources.HUSTR_E3M2
import doom.engine.resources.HUSTR_E3M3
import doom.engine.resources.HUSTR_E3M4
import doom.engine.resources.HUSTR_E3M5
import doom.engine.resources.HUSTR_E3M6
import doom.engine.resources.HUSTR_E3M7
import doom.engine.resources.HUSTR_E3M8
import doom.engine.resources.HUSTR_E3M9
import doom.engine.resources.HUSTR_E4M1
import doom.engine.resources.HUSTR_E4M2
import doom.engine.resources.HUSTR_E4M3
import doom.engine.resources.HUSTR_E4M4
import doom.engine.resources.HUSTR_E4M5
import doom.engine.resources.HUSTR_E4M6
import doom.engine.resources.HUSTR_E4M7
import doom.engine.resources.HUSTR_E4M8
import doom.engine.resources.HUSTR_E4M9
import doom.engine.resources.HUSTR_PLRBROWN
import doom.engine.resources.HUSTR_PLRGREEN
import doom.engine.resources.HUSTR_PLRINDIGO
import doom.engine.resources.HUSTR_PLRRED
import doom.engine.resources.PHUSTR_1
import doom.engine.resources.PHUSTR_10
import doom.engine.resources.PHUSTR_11
import doom.engine.resources.PHUSTR_12
import doom.engine.resources.PHUSTR_13
import doom.engine.resources.PHUSTR_14
import doom.engine.resources.PHUSTR_15
import doom.engine.resources.PHUSTR_16
import doom.engine.resources.PHUSTR_17
import doom.engine.resources.PHUSTR_18
import doom.engine.resources.PHUSTR_19
import doom.engine.resources.PHUSTR_2
import doom.engine.resources.PHUSTR_20
import doom.engine.resources.PHUSTR_21
import doom.engine.resources.PHUSTR_22
import doom.engine.resources.PHUSTR_23
import doom.engine.resources.PHUSTR_24
import doom.engine.resources.PHUSTR_25
import doom.engine.resources.PHUSTR_26
import doom.engine.resources.PHUSTR_27
import doom.engine.resources.PHUSTR_28
import doom.engine.resources.PHUSTR_29
import doom.engine.resources.PHUSTR_3
import doom.engine.resources.PHUSTR_30
import doom.engine.resources.PHUSTR_31
import doom.engine.resources.PHUSTR_32
import doom.engine.resources.PHUSTR_4
import doom.engine.resources.PHUSTR_5
import doom.engine.resources.PHUSTR_6
import doom.engine.resources.PHUSTR_7
import doom.engine.resources.PHUSTR_8
import doom.engine.resources.PHUSTR_9
import doom.engine.resources.THUSTR_1
import doom.engine.resources.THUSTR_10
import doom.engine.resources.THUSTR_11
import doom.engine.resources.THUSTR_12
import doom.engine.resources.THUSTR_13
import doom.engine.resources.THUSTR_14
import doom.engine.resources.THUSTR_15
import doom.engine.resources.THUSTR_16
import doom.engine.resources.THUSTR_17
import doom.engine.resources.THUSTR_18
import doom.engine.resources.THUSTR_19
import doom.engine.resources.THUSTR_2
import doom.engine.resources.THUSTR_20
import doom.engine.resources.THUSTR_21
import doom.engine.resources.THUSTR_22
import doom.engine.resources.THUSTR_23
import doom.engine.resources.THUSTR_24
import doom.engine.resources.THUSTR_25
import doom.engine.resources.THUSTR_26
import doom.engine.resources.THUSTR_27
import doom.engine.resources.THUSTR_28
import doom.engine.resources.THUSTR_29
import doom.engine.resources.THUSTR_3
import doom.engine.resources.THUSTR_30
import doom.engine.resources.THUSTR_31
import doom.engine.resources.THUSTR_32
import doom.engine.resources.THUSTR_4
import doom.engine.resources.THUSTR_5
import doom.engine.resources.THUSTR_6
import doom.engine.resources.THUSTR_7
import doom.engine.resources.THUSTR_8
import doom.engine.resources.THUSTR_9

internal class HudState {
    val chatMacros by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
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

    val playerNames by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        HUSTR_PLRGREEN,
        HUSTR_PLRINDIGO,
        HUSTR_PLRBROWN,
        HUSTR_PLRRED,
    ) }

    var chatChar = 0

    lateinit var plr: Player

    val huFont by lazy(LazyThreadSafetyMode.NONE) { Array(HU_FONTSIZE) { ByteArray(0) } }

    val wTitle by lazy(LazyThreadSafetyMode.NONE) { HudTextLine() }

    var chatOn = false

    val wChat by lazy(LazyThreadSafetyMode.NONE) { HudInputText() }

    var alwaysOff = false

    val chatDest by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXPLAYERS) }

    val wInputbuffer by lazy(LazyThreadSafetyMode.NONE) { Array(MAXPLAYERS) { HudInputText() } }

    var messageOn = false

    var messageDontfuckwithme = false

    var messageNottobefuckedwith = false

    val wMessage by lazy(LazyThreadSafetyMode.NONE) { HudScrollingText() }

    var messageCounter = 0

    var headsupactive = false

    val mapnames by lazy(LazyThreadSafetyMode.NONE) { arrayOf(

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

    val mapnames2 by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
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

    val mapnamesp by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
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

    val mapnamest by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
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

    val englishShiftxform by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(

        0,
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
        31,
        ' '.code, '!'.code, '"'.code, '#'.code, '$'.code, '%'.code, '&'.code,
        '"'.code,
        '('.code, ')'.code, '*'.code, '+'.code,
        '<'.code,
        '_'.code,
        '>'.code,
        '?'.code,
        ')'.code,
        '!'.code,
        '@'.code,
        '#'.code,
        '$'.code,
        '%'.code,
        '^'.code,
        '&'.code,
        '*'.code,
        '('.code,
        ':'.code,
        ':'.code,
        '<'.code,
        '+'.code,
        '>'.code, '?'.code, '@'.code,
        'A'.code, 'B'.code, 'C'.code, 'D'.code, 'E'.code, 'F'.code, 'G'.code,
        'H'.code, 'I'.code, 'J'.code, 'K'.code, 'L'.code, 'M'.code, 'N'.code,
        'O'.code, 'P'.code, 'Q'.code, 'R'.code, 'S'.code, 'T'.code, 'U'.code,
        'V'.code, 'W'.code, 'X'.code, 'Y'.code, 'Z'.code,
        '['.code,
        '!'.code,
        ']'.code,
        '"'.code, '_'.code,
        '\''.code,
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

    val destinationKeys by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        HUSTR_KEYGREEN,
        HUSTR_KEYINDIGO,
        HUSTR_KEYBROWN,
        HUSTR_KEYRED,
    ) }

    var numNobrainers = 0
}
