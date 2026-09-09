
package doom.engine.hud

import doom.engine.KEY_ENTER
import doom.engine.KEY_ESCAPE
import doom.engine.KEY_LALT
import doom.engine.KEY_RALT
import doom.engine.KEY_RSHIFT
import doom.engine.TICRATE
import doom.engine.audio.sStartSound
import doom.engine.audio.SFX_RADIO
import doom.engine.audio.SFX_TINK
import doom.engine.automap.automapactive
import doom.engine.core.DoomEngineCore
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.gameepisode
import doom.engine.gameplay.gamemap
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.netgame
import doom.engine.gameplay.player.Player
import doom.engine.gameplay.playeringame
import doom.engine.gameplay.players
import doom.engine.gameplay.REGISTERED
import doom.engine.gameplay.RETAIL
import doom.engine.gameplay.SHAREWARE
import doom.engine.input.EngineEvent
import doom.engine.input.EV_KEYDOWN
import doom.engine.menu.showMessages
import doom.engine.rendering.patchHeight
import doom.engine.resources.HUSTR_MSGU
import doom.engine.resources.HUSTR_TALKTOSELF1
import doom.engine.resources.HUSTR_TALKTOSELF2
import doom.engine.resources.HUSTR_TALKTOSELF3
import doom.engine.resources.HUSTR_TALKTOSELF4
import doom.engine.resources.HUSTR_TALKTOSELF5
import doom.engine.resources.wCacheLumpName

internal const val HU_FONTSTART = '!'.code
internal const val HU_FONTEND = '_'.code

internal const val HU_FONTSIZE = HU_FONTEND - HU_FONTSTART + 1

internal const val HU_BROADCAST = 5

internal const val HU_MSGREFRESH = KEY_ENTER
internal const val HU_MSGX = 0
internal const val HU_MSGY = 0
internal const val HU_MSGWIDTH = 64
internal const val HU_MSGHEIGHT = 1

internal const val HU_MSGTIMEOUT = 4 * TICRATE

private const val HU_TITLEHEIGHT = 1
private const val HU_TITLEX = 0

private const val HU_INPUTTOGGLE = 't'.code
private const val HU_INPUTX = HU_MSGX
private const val HU_INPUTWIDTH = 64
private const val HU_INPUTHEIGHT = 1

internal val DoomEngineCore.chatMacros
    get() = stateHud.chatMacros

internal val DoomEngineCore.playerNames
    get() = stateHud.playerNames

internal var DoomEngineCore.chatChar
    get() = stateHud.chatChar
    set(value) { stateHud.chatChar = value }
private var DoomEngineCore.plr: Player
    get() = stateHud.plr
    set(value) { stateHud.plr = value }
internal val DoomEngineCore.huFont
    get() = stateHud.huFont
private val DoomEngineCore.wTitle
    get() = stateHud.wTitle
internal var DoomEngineCore.chatOn
    get() = stateHud.chatOn
    set(value) { stateHud.chatOn = value }
private val DoomEngineCore.wChat
    get() = stateHud.wChat
private var DoomEngineCore.alwaysOff
    get() = stateHud.alwaysOff
    set(value) { stateHud.alwaysOff = value }
private val DoomEngineCore.chatDest
    get() = stateHud.chatDest
private val DoomEngineCore.wInputbuffer
    get() = stateHud.wInputbuffer

private var DoomEngineCore.messageOn
    get() = stateHud.messageOn
    set(value) { stateHud.messageOn = value }
internal var DoomEngineCore.messageDontfuckwithme
    get() = stateHud.messageDontfuckwithme
    set(value) { stateHud.messageDontfuckwithme = value }
private var DoomEngineCore.messageNottobefuckedwith
    get() = stateHud.messageNottobefuckedwith
    set(value) { stateHud.messageNottobefuckedwith = value }

private val DoomEngineCore.wMessage
    get() = stateHud.wMessage
private var DoomEngineCore.messageCounter
    get() = stateHud.messageCounter
    set(value) { stateHud.messageCounter = value }

private var DoomEngineCore.headsupactive
    get() = stateHud.headsupactive
    set(value) { stateHud.headsupactive = value }


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

internal val DoomEngineCore.englishShiftxform
    get() = stateHud.englishShiftxform


internal fun DoomEngineCore.huInit() {
    var j: Int

    shiftxform = englishShiftxform

    j = HU_FONTSTART
    for (i in 0 until HU_FONTSIZE) {
        val buffer = "STCFN" + j.toString().padStart(3, '0')
        j++
        huFont[i] = wCacheLumpName(buffer)
    }
}

internal fun DoomEngineCore.huStop() {
    headsupactive = false
}

internal fun DoomEngineCore.huStart() {
    val s: String

    if (headsupactive)
        huStop()

    plr = players[consoleplayer]
    messageOn = false
    messageDontfuckwithme = false
    messageNottobefuckedwith = false
    chatOn = false

    huLibInitSText(
        wMessage,
        HU_MSGX, HU_MSGY, HU_MSGHEIGHT,
        huFont,
        HU_FONTSTART, { messageOn }
    )

    huLibInitTextLine(
        wTitle,
        HU_TITLEX, 167 - patchHeight(huFont[0]),
        huFont,
        HU_FONTSTART
    )

    s = when (gamemode) {
        SHAREWARE,
        REGISTERED,
        RETAIL -> mapnames[(gameepisode - 1) * 9 + gamemap - 1]


        else -> mapnames2[gamemap - 1]
    }

    for (ch in s)
        huLibAddCharToTextLine(wTitle, ch)

    huLibInitIText(
        wChat,
        HU_INPUTX, HU_MSGY + HU_MSGHEIGHT * (patchHeight(huFont[0]) + 1),
        huFont,
        HU_FONTSTART, { chatOn }
    )

    for (i in 0 until MAXPLAYERS)
        huLibInitIText(wInputbuffer[i], 0, 0, null, 0, { alwaysOff })

    headsupactive = true
}

internal fun DoomEngineCore.huDrawer() {
    huLibDrawSText(wMessage)
    huLibDrawIText(wChat)
    if (automapactive)
        huLibDrawTextLine(wTitle, false)
}

internal fun DoomEngineCore.huErase() {
    huLibEraseSText(wMessage)
    huLibEraseIText(wChat)
    huLibEraseTextLine(wTitle)
}

internal fun DoomEngineCore.huTicker() {
    var rc: Boolean
    var c: Int

    if (messageCounter != 0) {
        messageCounter--
        if (messageCounter == 0) {
            messageOn = false
            messageNottobefuckedwith = false
        }
    }

    if (showMessages != 0 || messageDontfuckwithme) {

        if ((plr.message != null && !messageNottobefuckedwith)
            || (plr.message != null && messageDontfuckwithme)
        ) {
            huLibAddMessageToSText(wMessage, null, plr.message!!)
            plr.message = null
            messageOn = true
            messageCounter = HU_MSGTIMEOUT
            messageNottobefuckedwith = messageDontfuckwithme
            messageDontfuckwithme = false
        }
    }

    if (netgame) {
        for (i in 0 until MAXPLAYERS) {
            if (!playeringame[i])
                continue
            if (i != consoleplayer) {
                c = players[i].cmd.chatchar
                if (c != 0) {
                    if (c <= HU_BROADCAST)
                        chatDest[i] = c
                    else {
                        if (c >= 'a'.code && c <= 'z'.code)
                            c = shiftxform[c]
                        rc = huLibKeyInIText(wInputbuffer[i], c)
                        if (rc && c == KEY_ENTER) {
                            if (wInputbuffer[i].l.len != 0
                                && (chatDest[i] == consoleplayer + 1
                                    || chatDest[i] == HU_BROADCAST)
                            ) {
                                huLibAddMessageToSText(
                                    wMessage,
                                    playerNames[i],
                                    wInputbuffer[i].l.l.concatToString(0, wInputbuffer[i].l.len)
                                )

                                messageNottobefuckedwith = true
                                messageOn = true
                                messageCounter = HU_MSGTIMEOUT
                                if (gamemode == COMMERCIAL)
                                    sStartSound(null, SFX_RADIO)
                                else
                                    sStartSound(null, SFX_TINK)
                            }
                            huLibResetIText(wInputbuffer[i])
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

internal fun DoomEngineCore.huQueueChatChar(c: Int) {
    if (((head + 1) and (QUEUESIZE - 1)) == tail) {
        plr.message = HUSTR_MSGU
    } else {
        chatchars[head] = c
        head = (head + 1) and (QUEUESIZE - 1)
    }
}

internal fun DoomEngineCore.huDequeueChatChar(): Int {
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

internal const val HUSTR_KEYGREEN = 'g'.code
internal const val HUSTR_KEYINDIGO = 'i'.code
internal const val HUSTR_KEYBROWN = 'b'.code
internal const val HUSTR_KEYRED = 'r'.code

private val DoomEngineCore.destinationKeys
    get() = stateHud.destinationKeys

private var DoomEngineCore.numNobrainers
    get() = stateHud.numNobrainers
    set(value) { stateHud.numNobrainers = value }

internal fun DoomEngineCore.huResponder(ev: EngineEvent): Boolean {
    val macromessage: String
    var eatkey = false
    var c: Int
    var numplayers: Int

    numplayers = 0
    for (i in 0 until MAXPLAYERS)
        if (playeringame[i]) numplayers++

    if (ev.data1 == KEY_RSHIFT) {
        shiftdown = ev.type == EV_KEYDOWN
        return false
    } else if (ev.data1 == KEY_RALT || ev.data1 == KEY_LALT) {
        altdown = ev.type == EV_KEYDOWN
        return false
    }

    if (ev.type != EV_KEYDOWN)
        return false

    if (!chatOn) {
        if (ev.data1 == HU_MSGREFRESH) {
            messageOn = true
            messageCounter = HU_MSGTIMEOUT
            eatkey = true
        } else if (netgame && ev.data1 == HU_INPUTTOGGLE) {
            chatOn = true
            eatkey = true
            huLibResetIText(wChat)
            huQueueChatChar(HU_BROADCAST)
        } else if (netgame && numplayers > 2) {
            for (i in 0 until MAXPLAYERS) {
                if (ev.data1 == destinationKeys[i]) {
                    if (playeringame[i] && i != consoleplayer) {
                        chatOn = true
                        eatkey = true
                        huLibResetIText(wChat)
                        huQueueChatChar(i + 1)
                        break
                    } else if (i == consoleplayer) {
                        numNobrainers++
                        if (numNobrainers < 3)
                            plr.message = HUSTR_TALKTOSELF1
                        else if (numNobrainers < 6)
                            plr.message = HUSTR_TALKTOSELF2
                        else if (numNobrainers < 9)
                            plr.message = HUSTR_TALKTOSELF3
                        else if (numNobrainers < 32)
                            plr.message = HUSTR_TALKTOSELF4
                        else
                            plr.message = HUSTR_TALKTOSELF5
                    }
                }
            }
        }
    } else {
        c = ev.data1 and 0xFF
        if (altdown) {
            c = (c - '0'.code) and 0xFF
            if (c > 9)
                return false
            macromessage = chatMacros[c]

            huQueueChatChar(KEY_ENTER)

            for (mc in macromessage)
                huQueueChatChar(mc.code)
            huQueueChatChar(KEY_ENTER)

            chatOn = false
            lastmessage = chatMacros[c]
            plr.message = lastmessage
            eatkey = true
        } else {
            if (shiftdown || (c >= 'a'.code && c <= 'z'.code))
                c = shiftxform[c]
            eatkey = huLibKeyInIText(wChat, c)
            if (eatkey) {
                huQueueChatChar(c)

            }
            if (c == KEY_ENTER) {
                chatOn = false
                if (wChat.l.len != 0) {
                    lastmessage = wChat.l.l.concatToString(0, wChat.l.len)
                    plr.message = lastmessage
                }
            } else if (c == KEY_ESCAPE)
                chatOn = false
        }
    }

    return eatkey
}
