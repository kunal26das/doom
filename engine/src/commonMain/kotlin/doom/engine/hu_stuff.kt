// Port of linuxdoom-1.10 hu_stuff.c/hu_stuff.h -- Heads-up displays.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")

package doom.engine

//
// Globally visible constants. (hu_stuff.h)
//
internal const val HU_FONTSTART = '!'.code  // the first font characters
internal const val HU_FONTEND = '_'.code    // the last font characters

// Calculate # of glyphs in font.
internal const val HU_FONTSIZE = HU_FONTEND - HU_FONTSTART + 1

internal const val HU_BROADCAST = 5

internal const val HU_MSGREFRESH = KEY_ENTER
internal const val HU_MSGX = 0
internal const val HU_MSGY = 0
internal const val HU_MSGWIDTH = 64  // in characters
internal const val HU_MSGHEIGHT = 1  // in lines

internal const val HU_MSGTIMEOUT = 4 * TICRATE

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

internal val DoomEngineCore.chat_macros
    get() = stateHud.chat_macros

internal val DoomEngineCore.player_names
    get() = stateHud.player_names

internal var DoomEngineCore.chat_char
    get() = stateHud.chat_char
    set(value) { stateHud.chat_char = value }
private var DoomEngineCore.plr: player_t
    get() = stateHud.plr
    set(value) { stateHud.plr = value }
internal val DoomEngineCore.hu_font
    get() = stateHud.hu_font
private val DoomEngineCore.w_title
    get() = stateHud.w_title
internal var DoomEngineCore.chat_on
    get() = stateHud.chat_on
    set(value) { stateHud.chat_on = value }
private val DoomEngineCore.w_chat
    get() = stateHud.w_chat
private var DoomEngineCore.always_off
    get() = stateHud.always_off
    set(value) { stateHud.always_off = value }
private val DoomEngineCore.chat_dest
    get() = stateHud.chat_dest
private val DoomEngineCore.w_inputbuffer
    get() = stateHud.w_inputbuffer

private var DoomEngineCore.message_on
    get() = stateHud.message_on
    set(value) { stateHud.message_on = value }
internal var DoomEngineCore.message_dontfuckwithme
    get() = stateHud.message_dontfuckwithme
    set(value) { stateHud.message_dontfuckwithme = value }
private var DoomEngineCore.message_nottobefuckedwith
    get() = stateHud.message_nottobefuckedwith
    set(value) { stateHud.message_nottobefuckedwith = value }

private val DoomEngineCore.w_message
    get() = stateHud.w_message
private var DoomEngineCore.message_counter
    get() = stateHud.message_counter
    set(value) { stateHud.message_counter = value }

private var DoomEngineCore.headsupactive
    get() = stateHud.headsupactive
    set(value) { stateHud.headsupactive = value }

//
// Builtin map names.
// The actual names can be found in DStrings.h.
//

internal val DoomEngineCore.mapnames
    get() = stateHud.mapnames

internal val DoomEngineCore.mapnames2
    get() = stateHud.mapnames2

internal val DoomEngineCore.mapnamesp
    get() = stateHud.mapnamesp

internal val DoomEngineCore.mapnamest
    get() = stateHud.mapnamest

internal var DoomEngineCore.shiftxform
    get() = stateHud.shiftxform
    set(value) { stateHud.shiftxform = value }

internal val DoomEngineCore.english_shiftxform
    get() = stateHud.english_shiftxform

// frenchKeyMap / ForeignTranslation dropped -- english only.

internal fun DoomEngineCore.HU_Init() {
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

internal fun DoomEngineCore.HU_Stop() {
    headsupactive = false
}

internal fun DoomEngineCore.HU_Start() {
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

internal fun DoomEngineCore.HU_Drawer() {
    HUlib_drawSText(w_message)
    HUlib_drawIText(w_chat)
    if (automapactive)
        HUlib_drawTextLine(w_title, false)
}

internal fun DoomEngineCore.HU_Erase() {
    HUlib_eraseSText(w_message)
    HUlib_eraseIText(w_chat)
    HUlib_eraseTextLine(w_title)
}

internal fun DoomEngineCore.HU_Ticker() {
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

internal const val QUEUESIZE = 128

private val DoomEngineCore.chatchars
    get() = stateHud.chatchars
private var DoomEngineCore.head
    get() = stateHud.head
    set(value) { stateHud.head = value }
private var DoomEngineCore.tail
    get() = stateHud.tail
    set(value) { stateHud.tail = value }

internal fun DoomEngineCore.HU_queueChatChar(c: Int) {
    if (((head + 1) and (QUEUESIZE - 1)) == tail) {
        plr.message = HUSTR_MSGU
    } else {
        chatchars[head] = c
        head = (head + 1) and (QUEUESIZE - 1)
    }
}

internal fun DoomEngineCore.HU_dequeueChatChar(): Int {
    val c: Int

    if (head != tail) {
        c = chatchars[tail]
        tail = (tail + 1) and (QUEUESIZE - 1)
    } else {
        c = 0
    }

    return c
}

private var DoomEngineCore.lastmessage
    get() = stateHud.lastmessage
    set(value) { stateHud.lastmessage = value }
private var DoomEngineCore.shiftdown
    get() = stateHud.shiftdown
    set(value) { stateHud.shiftdown = value }
private var DoomEngineCore.altdown
    get() = stateHud.altdown
    set(value) { stateHud.altdown = value }

// d_englsh.h key chars (not emitted by the strings generator)
internal const val HUSTR_KEYGREEN = 'g'.code
internal const val HUSTR_KEYINDIGO = 'i'.code
internal const val HUSTR_KEYBROWN = 'b'.code
internal const val HUSTR_KEYRED = 'r'.code

private val DoomEngineCore.destination_keys
    get() = stateHud.destination_keys

private var DoomEngineCore.num_nobrainers
    get() = stateHud.num_nobrainers
    set(value) { stateHud.num_nobrainers = value }

internal fun DoomEngineCore.HU_Responder(ev: event_t): Boolean {
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
