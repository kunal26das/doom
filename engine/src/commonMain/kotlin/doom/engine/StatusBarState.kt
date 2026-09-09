// Port of linuxdoom-1.10 st_stuff.c/st_stuff.h -- status bar code.
// Does the face/direction indicator animatin.
// Does palette indicators as well (red pain/berserk, bright pickup)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine


/** State owned by one engine; no mutable process-wide storage. */
internal class StatusBarState {
    lateinit var plyr: player_t

    var st_firsttime = false

    var veryfirsttime = 1

    var lu_palette = 0

    var st_clock = 0

    var st_msgcounter = 0

    var st_chatstate = StartChatState

    var st_gamestate = FirstPersonState

    var st_statusbaron = false

    var st_chat = false

    var st_oldchat = false

    var st_cursoron = false

    var st_notdeathmatch = false

    var st_armson = false

    var st_fragson = false

    var sbar: ByteArray = ByteArray(0)

    val tallnum by lazy(LazyThreadSafetyMode.NONE) { Array(10) { ByteArray(0) } }

    var tallpercent: ByteArray = ByteArray(0)

    val shortnum by lazy(LazyThreadSafetyMode.NONE) { Array(10) { ByteArray(0) } }

    val keys by lazy(LazyThreadSafetyMode.NONE) { Array(NUMCARDS) { ByteArray(0) } }

    val faces by lazy(LazyThreadSafetyMode.NONE) { Array(ST_NUMFACES) { ByteArray(0) } }

    var faceback: ByteArray = ByteArray(0)

    var armsbg: ByteArray = ByteArray(0)

    val arms by lazy(LazyThreadSafetyMode.NONE) { Array(6) { Array(2) { ByteArray(0) } } }

    val w_ready by lazy(LazyThreadSafetyMode.NONE) { st_number_t() }

    val w_frags by lazy(LazyThreadSafetyMode.NONE) { st_number_t() }

    val w_health by lazy(LazyThreadSafetyMode.NONE) { st_percent_t() }

    val w_armsbg by lazy(LazyThreadSafetyMode.NONE) { st_binicon_t() }

    val w_arms by lazy(LazyThreadSafetyMode.NONE) { Array(6) { st_multicon_t() } }

    val w_faces by lazy(LazyThreadSafetyMode.NONE) { st_multicon_t() }

    val w_keyboxes by lazy(LazyThreadSafetyMode.NONE) { Array(3) { st_multicon_t() } }

    val w_armor by lazy(LazyThreadSafetyMode.NONE) { st_percent_t() }

    val w_ammo by lazy(LazyThreadSafetyMode.NONE) { Array(4) { st_number_t() } }

    val w_maxammo by lazy(LazyThreadSafetyMode.NONE) { Array(4) { st_number_t() } }

    var st_fragscount = 0

    var st_oldhealth = -1

    val oldweaponsowned by lazy(LazyThreadSafetyMode.NONE) { BooleanArray(NUMWEAPONS) }

    var st_facecount = 0

    var st_faceindex = 0

    val keyboxes by lazy(LazyThreadSafetyMode.NONE) { IntArray(3) }

    var st_randomnumber = 0

    val cheat_mus_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xb6, 0xae, 0xea, 1, 0, 0, 0xff
    ) }

    val cheat_choppers_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xe2, 0x32, 0xf6, 0x2a, 0x2a, 0xa6, 0x6a, 0xea, 0xff // id...
    ) }

    val cheat_god_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0x26, 0xaa, 0x26, 0xff  // iddqd
    ) }

    val cheat_ammo_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xf2, 0x66, 0xa2, 0xff  // idkfa
    ) }

    val cheat_ammonokey_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0x66, 0xa2, 0xff        // idfa
    ) }

    val cheat_noclip_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xea, 0x2a, 0xb2,       // idspispopd
        0xea, 0x2a, 0xf6, 0x2a, 0x26, 0xff
    ) }

    val cheat_commercial_noclip_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xe2, 0x36, 0xb2, 0x2a, 0xff  // idclip
    ) }

    val cheat_powerup_seq by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0x6e, 0xff),  // beholdv
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0xea, 0xff),  // beholds
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0xb2, 0xff),  // beholdi
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0x6a, 0xff),  // beholdr
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0xa2, 0xff),  // beholda
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0x36, 0xff),  // beholdl
        intArrayOf(0xb2, 0x26, 0x62, 0xa6, 0x32, 0xf6, 0x36, 0x26, 0xff)         // behold
    ) }

    val cheat_clev_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xe2, 0x36, 0xa6, 0x6e, 1, 0, 0, 0xff  // idclev
    ) }

    val cheat_mypos_seq by lazy(LazyThreadSafetyMode.NONE) { intArrayOf(
        0xb2, 0x26, 0xb6, 0xba, 0x2a, 0xf6, 0xea, 0xff  // idmypos
    ) }

    val cheat_mus by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_mus_seq, 0) }

    val cheat_god by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_god_seq, 0) }

    val cheat_ammo by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_ammo_seq, 0) }

    val cheat_ammonokey by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_ammonokey_seq, 0) }

    val cheat_noclip by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_noclip_seq, 0) }

    val cheat_commercial_noclip by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_commercial_noclip_seq, 0) }

    val cheat_powerup by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        cheatseq_t(cheat_powerup_seq[0], 0),
        cheatseq_t(cheat_powerup_seq[1], 0),
        cheatseq_t(cheat_powerup_seq[2], 0),
        cheatseq_t(cheat_powerup_seq[3], 0),
        cheatseq_t(cheat_powerup_seq[4], 0),
        cheatseq_t(cheat_powerup_seq[5], 0),
        cheatseq_t(cheat_powerup_seq[6], 0)
    ) }

    val cheat_choppers by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_choppers_seq, 0) }

    val cheat_clev by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_clev_seq, 0) }

    val cheat_mypos by lazy(LazyThreadSafetyMode.NONE) { cheatseq_t(cheat_mypos_seq, 0) }

    var lastcalc = 0

    var oldhealth = -1

    var lastattackdown = -1

    var priority = 0

    var largeammo = 1994

    var st_palette = 0

    var st_stopped = true
}
