// Port of linuxdoom-1.10 hu_stuff.c/hu_stuff.h -- Heads-up displays.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

//
// Globally visible constants. (hu_stuff.h)
//
const val HU_FONTSTART = '!'.code  // the first font characters
const val HU_FONTEND = '_'.code    // the last font characters

// Calculate # of glyphs in font.
const val HU_FONTSIZE = HU_FONTEND - HU_FONTSTART + 1

const val HU_BROADCAST = 5

const val HU_MSGREFRESH = KEY_ENTER
const val HU_MSGX = 0
const val HU_MSGY = 0
const val HU_MSGWIDTH = 64  // in characters
const val HU_MSGHEIGHT = 1  // in lines

const val HU_MSGTIMEOUT = 4 * TICRATE

//
// Locally used constants, shortcuts.
//
// HU_TITLE / HU_TITLE2 / HU_TITLEP / HU_TITLET macros are inlined in HU_Start.
private const val HU_TITLEHEIGHT = 1
private const val HU_TITLEX = 0
// #define HU_TITLEY (167 - SHORT(hu_font[0]->height)) -- computed in HU_Start

private const val HU_INPUTTOGGLE = 't'.code
private const val HU_INPUTX = HU_MSGX
// #define HU_INPUTY (HU_MSGY + HU_MSGHEIGHT*(SHORT(hu_font[0]->height) +1)) -- computed in HU_Start
private const val HU_INPUTWIDTH = 64
private const val HU_INPUTHEIGHT = 1

val chat_macros = arrayOf(
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
)

val player_names = arrayOf(
    HUSTR_PLRGREEN,
    HUSTR_PLRINDIGO,
    HUSTR_PLRBROWN,
    HUSTR_PLRRED,
)

var chat_char = 0 // remove later.
private lateinit var plr: player_t
val hu_font = Array(HU_FONTSIZE) { ByteArray(0) }  // patch_t* hu_font[HU_FONTSIZE]
private val w_title = hu_textline_t()
var chat_on = false
private val w_chat = hu_itext_t()
private var always_off = false
private val chat_dest = IntArray(MAXPLAYERS)  // char chat_dest[MAXPLAYERS]
private val w_inputbuffer = Array(MAXPLAYERS) { hu_itext_t() }

private var message_on = false
var message_dontfuckwithme = false
private var message_nottobefuckedwith = false

private val w_message = hu_stext_t()
private var message_counter = 0

private var headsupactive = false

//
// Builtin map names.
// The actual names can be found in DStrings.h.
//

val mapnames = arrayOf( // DOOM shareware/registered/retail (Ultimate) names.

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
)

val mapnames2 = arrayOf( // DOOM 2 map names.
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
)

val mapnamesp = arrayOf( // Plutonia WAD map names.
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
)

val mapnamest = arrayOf( // TNT WAD map names.
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
)

// const char* shiftxform; (french_shiftxform dropped -- english only)
var shiftxform = IntArray(0)

val english_shiftxform = intArrayOf(

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
)

// frenchKeyMap / ForeignTranslation dropped -- english only.

fun HU_Init() {
    var j: Int

    // if (french) shiftxform = french_shiftxform; else ... (english only)
    shiftxform = english_shiftxform

    // load the heads-up font
    j = HU_FONTSTART
    for (i in 0 until HU_FONTSIZE) {
        val buffer = "STCFN" + j.toString().padStart(3, '0')  // sprintf(buffer, "STCFN%.3d", j++)
        j++
        hu_font[i] = W_CacheLumpName(buffer)
    }
}

fun HU_Stop() {
    headsupactive = false
}

fun HU_Start() {
    val s: String

    if (headsupactive)
        HU_Stop()

    plr = players[consoleplayer]
    message_on = false
    message_dontfuckwithme = false
    message_nottobefuckedwith = false
    chat_on = false

    // create the message widget
    HUlib_initSText(
        w_message,
        HU_MSGX, HU_MSGY, HU_MSGHEIGHT,
        hu_font,
        HU_FONTSTART, { message_on }
    )

    // create the map title widget
    HUlib_initTextLine(
        w_title,
        HU_TITLEX, 167 - patchHeight(hu_font[0]),  // HU_TITLEY
        hu_font,
        HU_FONTSTART
    )

    s = when (gamemode) {
        shareware,
        registered,
        retail -> mapnames[(gameepisode - 1) * 9 + gamemap - 1]  // HU_TITLE

        /* FIXME
          case pack_plut:
            s = HU_TITLEP;
            break;
          case pack_tnt:
            s = HU_TITLET;
            break;
        */

        // commercial, default
        else -> mapnames2[gamemap - 1]  // HU_TITLE2
    }

    for (ch in s)
        HUlib_addCharToTextLine(w_title, ch)

    // create the chat widget
    HUlib_initIText(
        w_chat,
        HU_INPUTX, HU_MSGY + HU_MSGHEIGHT * (patchHeight(hu_font[0]) + 1),  // HU_INPUTY
        hu_font,
        HU_FONTSTART, { chat_on }
    )

    // create the inputbuffer widgets
    for (i in 0 until MAXPLAYERS)
        HUlib_initIText(w_inputbuffer[i], 0, 0, null, 0, { always_off })

    headsupactive = true
}

fun HU_Drawer() {
    HUlib_drawSText(w_message)
    HUlib_drawIText(w_chat)
    if (automapactive)
        HUlib_drawTextLine(w_title, false)
}

fun HU_Erase() {
    HUlib_eraseSText(w_message)
    HUlib_eraseIText(w_chat)
    HUlib_eraseTextLine(w_title)
}

fun HU_Ticker() {
    var rc: Boolean
    var c: Int

    // tick down message counter if message is up
    if (message_counter != 0) {
        message_counter--
        if (message_counter == 0) {
            message_on = false
            message_nottobefuckedwith = false
        }
    }

    if (showMessages != 0 || message_dontfuckwithme) {

        // display message if necessary
        if ((plr.message != null && !message_nottobefuckedwith)
            || (plr.message != null && message_dontfuckwithme)
        ) {
            HUlib_addMessageToSText(w_message, null, plr.message!!)
            plr.message = null
            message_on = true
            message_counter = HU_MSGTIMEOUT
            message_nottobefuckedwith = message_dontfuckwithme
            message_dontfuckwithme = false
        }
    } // else message_on = false;

    // check for incoming chat characters
    if (netgame) {
        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue
            if (i != consoleplayer) {
                c = players[i].cmd.chatchar
                if (c != 0) {
                    if (c <= HU_BROADCAST)
                        chat_dest[i] = c
                    else {
                        if (c >= 'a'.code && c <= 'z'.code)
                            c = shiftxform[c]
                        rc = HUlib_keyInIText(w_inputbuffer[i], c)
                        if (rc && c == KEY_ENTER) {
                            if (w_inputbuffer[i].l.len != 0
                                && (chat_dest[i] == consoleplayer + 1
                                    || chat_dest[i] == HU_BROADCAST)
                            ) {
                                HUlib_addMessageToSText(
                                    w_message,
                                    player_names[i],
                                    w_inputbuffer[i].l.l.concatToString(0, w_inputbuffer[i].l.len)
                                )

                                message_nottobefuckedwith = true
                                message_on = true
                                message_counter = HU_MSGTIMEOUT
                                if (gamemode == commercial)
                                    S_StartSound(null, sfx_radio)
                                else
                                    S_StartSound(null, sfx_tink)
                            }
                            HUlib_resetIText(w_inputbuffer[i])
                        }
                    }
                    players[i].cmd.chatchar = 0
                }
            }
        }
    }
}

private const val QUEUESIZE = 128

private val chatchars = IntArray(QUEUESIZE)
private var head = 0
private var tail = 0

fun HU_queueChatChar(c: Int) {
    if (((head + 1) and (QUEUESIZE - 1)) == tail) {
        plr.message = HUSTR_MSGU
    } else {
        chatchars[head] = c
        head = (head + 1) and (QUEUESIZE - 1)
    }
}

fun HU_dequeueChatChar(): Int {
    val c: Int

    if (head != tail) {
        c = chatchars[tail]
        tail = (tail + 1) and (QUEUESIZE - 1)
    } else {
        c = 0
    }

    return c
}

// statics of HU_Responder
private var lastmessage = ""  // static char lastmessage[HU_MAXLINELENGTH+1]
private var shiftdown = false
private var altdown = false

// d_englsh.h key chars (not emitted by the strings generator)
private const val HUSTR_KEYGREEN = 'g'.code
private const val HUSTR_KEYINDIGO = 'i'.code
private const val HUSTR_KEYBROWN = 'b'.code
private const val HUSTR_KEYRED = 'r'.code

private val destination_keys = intArrayOf(
    HUSTR_KEYGREEN,
    HUSTR_KEYINDIGO,
    HUSTR_KEYBROWN,
    HUSTR_KEYRED,
)

private var num_nobrainers = 0

fun HU_Responder(ev: event_t): Boolean {
    val macromessage: String
    var eatkey = false
    var c: Int
    var numplayers: Int

    numplayers = 0
    for (i in 0 until MAXPLAYERS)
        if (playeringame[i]) numplayers++  // numplayers += playeringame[i];

    if (ev.data1 == KEY_RSHIFT) {
        shiftdown = ev.type == ev_keydown
        return false
    } else if (ev.data1 == KEY_RALT || ev.data1 == KEY_LALT) {
        altdown = ev.type == ev_keydown
        return false
    }

    if (ev.type != ev_keydown)
        return false

    if (!chat_on) {
        if (ev.data1 == HU_MSGREFRESH) {
            message_on = true
            message_counter = HU_MSGTIMEOUT
            eatkey = true
        } else if (netgame && ev.data1 == HU_INPUTTOGGLE) {
            chat_on = true      // eatkey = chat_on = true;
            eatkey = true
            HUlib_resetIText(w_chat)
            HU_queueChatChar(HU_BROADCAST)
        } else if (netgame && numplayers > 2) {
            for (i in 0 until MAXPLAYERS) {
                if (ev.data1 == destination_keys[i]) {
                    if (playeringame[i] && i != consoleplayer) {
                        chat_on = true  // eatkey = chat_on = true;
                        eatkey = true
                        HUlib_resetIText(w_chat)
                        HU_queueChatChar(i + 1)
                        break
                    } else if (i == consoleplayer) {
                        num_nobrainers++
                        if (num_nobrainers < 3)
                            plr.message = HUSTR_TALKTOSELF1
                        else if (num_nobrainers < 6)
                            plr.message = HUSTR_TALKTOSELF2
                        else if (num_nobrainers < 9)
                            plr.message = HUSTR_TALKTOSELF3
                        else if (num_nobrainers < 32)
                            plr.message = HUSTR_TALKTOSELF4
                        else
                            plr.message = HUSTR_TALKTOSELF5
                    }
                }
            }
        }
    } else {
        c = ev.data1 and 0xFF  // unsigned char c
        // send a macro
        if (altdown) {
            c = (c - '0'.code) and 0xFF
            if (c > 9)
                return false
            // fprintf(stderr, "got here\n");
            macromessage = chat_macros[c]

            // kill last message with a '\n'
            HU_queueChatChar(KEY_ENTER) // DEBUG!!!

            // send the macro message
            for (mc in macromessage)
                HU_queueChatChar(mc.code)
            HU_queueChatChar(KEY_ENTER)

            // leave chat mode and notify that it was sent
            chat_on = false
            lastmessage = chat_macros[c]  // strcpy(lastmessage, chat_macros[c]);
            plr.message = lastmessage
            eatkey = true
        } else {
            // if (french) c = ForeignTranslation(c); (english only)
            if (shiftdown || (c >= 'a'.code && c <= 'z'.code))
                c = shiftxform[c]
            eatkey = HUlib_keyInIText(w_chat, c)
            if (eatkey) {
                // static unsigned char buf[20]; // DEBUG
                HU_queueChatChar(c)

                // sprintf(buf, "KEY: %d => %d", ev->data1, c);
                //      plr->message = buf;
            }
            if (c == KEY_ENTER) {
                chat_on = false
                if (w_chat.l.len != 0) {
                    lastmessage = w_chat.l.l.concatToString(0, w_chat.l.len)  // strcpy(lastmessage, w_chat.l.l);
                    plr.message = lastmessage
                }
            } else if (c == KEY_ESCAPE)
                chat_on = false
        }
    }

    return eatkey
}
