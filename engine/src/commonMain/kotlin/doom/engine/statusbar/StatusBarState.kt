
package doom.engine.statusbar

import doom.engine.cheats.CheatSequence
import doom.engine.gameplay.NUMCARDS
import doom.engine.gameplay.NUMWEAPONS
import doom.engine.gameplay.player.Player

internal class StatusBarState {
    lateinit var plyr: Player

    var stFirsttime = false

    var veryfirsttime = 1

    var luPalette = 0

    var stClock = 0

    var stMsgcounter = 0

    var stChatstate = START_CHAT_STATE

    var stGamestate = FIRST_PERSON_STATE

    var stStatusbaron = false

    var stChat = false

    var stOldchat = false

    var stCursoron = false

    var stNotdeathmatch = false

    var stArmson = false

    var stFragson = false

    var sbar: ByteArray = ByteArray(0)

    val tallnum by lazy(LazyThreadSafetyMode.NONE) { Array(10) { ByteArray(0) } }

    var tallpercent: ByteArray = ByteArray(0)

    val shortnum by lazy(LazyThreadSafetyMode.NONE) { Array(10) { ByteArray(0) } }

    val keys by lazy(LazyThreadSafetyMode.NONE) { Array(NUMCARDS) { ByteArray(0) } }

    val faces by lazy(LazyThreadSafetyMode.NONE) { Array(ST_NUMFACES) { ByteArray(0) } }

    var faceback: ByteArray = ByteArray(0)

    var armsbg: ByteArray = ByteArray(0)

    val arms by lazy(LazyThreadSafetyMode.NONE) { Array(6) { Array(2) { ByteArray(0) } } }

    val wReady by lazy(LazyThreadSafetyMode.NONE) { NumberWidget() }

    val wFrags by lazy(LazyThreadSafetyMode.NONE) { NumberWidget() }

    val wHealth by lazy(LazyThreadSafetyMode.NONE) { PercentWidget() }

    val wArmsbg by lazy(LazyThreadSafetyMode.NONE) { BooleanIconWidget() }

    val wArms by lazy(LazyThreadSafetyMode.NONE) { Array(6) { MultiIconWidget() } }

    val wFaces by lazy(LazyThreadSafetyMode.NONE) { MultiIconWidget() }

    val wKeyboxes by lazy(LazyThreadSafetyMode.NONE) { Array(3) { MultiIconWidget() } }

    val wArmor by lazy(LazyThreadSafetyMode.NONE) { PercentWidget() }

    val wAmmo by lazy(LazyThreadSafetyMode.NONE) { Array(4) { NumberWidget() } }

    val wMaxammo by lazy(LazyThreadSafetyMode.NONE) { Array(4) { NumberWidget() } }

    var stFragscount = 0

    var stOldhealth = -1

    val oldweaponsowned by lazy(LazyThreadSafetyMode.NONE) { BooleanArray(NUMWEAPONS) }

    var stFacecount = 0

    var stFaceindex = 0

    val keyboxes by lazy(LazyThreadSafetyMode.NONE) { IntArray(3) }

    var stRandomnumber = 0

    val cheatMusSeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xb6, 0xae, 0xea, 1, 0, 0, 0xff
    ) }

    val cheatChoppersSeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xe2, 0x32, 0xf6, 0x2a, 0x2a, 0xa6, 0x6a, 0xea, 0xff
    ) }

    val cheatGodSeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0x26, 0xaa, 0x26, 0xff
    ) }

    val cheatAmmoSeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xf2, 0x66, 0xa2, 0xff
    ) }

    val cheatAmmonokeySeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0x66, 0xa2, 0xff
    ) }

    val cheatNoclipSeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xea, 0x2a, 0xb2,
        0xea, 0x2a, 0xf6, 0x2a, 0x26, 0xff
    ) }

    val cheatCommercialNoclipSeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xe2, 0x36, 0xb2, 0x2a, 0xff
    ) }

    val cheatPowerupSeq by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0x6e, 0xff),
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0xea, 0xff),
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0xb2, 0xff),
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0x6a, 0xff),
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0xa2, 0xff),
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0x36, 0xff),
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0xff)
    ) }

    val cheatClevSeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xe2, 0x36, 0xa6, 0x6e, 1, 0, 0, 0xff
    ) }

    val cheatMyposSeq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xb6, 0xba, 0x2a, 0xf6, 0xea, 0xff
    ) }

    val cheatMus by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatMusSeq, 0) }

    val cheatGod by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatGodSeq, 0) }

    val cheatAmmo by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatAmmoSeq, 0) }

    val cheatAmmonokey by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatAmmonokeySeq, 0) }

    val cheatNoclip by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatNoclipSeq, 0) }

    val cheatCommercialNoclip by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatCommercialNoclipSeq, 0) }

    val cheatPowerup by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        CheatSequence(cheatPowerupSeq[0], 0),
        CheatSequence(cheatPowerupSeq[1], 0),
        CheatSequence(cheatPowerupSeq[2], 0),
        CheatSequence(cheatPowerupSeq[3], 0),
        CheatSequence(cheatPowerupSeq[4], 0),
        CheatSequence(cheatPowerupSeq[5], 0),
        CheatSequence(cheatPowerupSeq[6], 0)
    ) }

    val cheatChoppers by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatChoppersSeq, 0) }

    val cheatClev by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatClevSeq, 0) }

    val cheatMypos by lazy(LazyThreadSafetyMode.NONE) { CheatSequence(cheatMyposSeq, 0) }

    var lastcalc = 0

    var oldhealth = -1

    var lastattackdown = -1

    var priority = 0

    var largeammo = 1994

    var stPalette = 0

    var stStopped = true
}
