// GENERATED from linuxdoom-1.10 sounds.h / sounds.c -- do not edit by hand.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("MagicNumber", "LargeClass", "LongMethod", "MaxLineLength", "ktlint")

package doom.engine


// musicenum_t
const val mus_None = 0
const val mus_e1m1 = 1
const val mus_e1m2 = 2
const val mus_e1m3 = 3
const val mus_e1m4 = 4
const val mus_e1m5 = 5
const val mus_e1m6 = 6
const val mus_e1m7 = 7
const val mus_e1m8 = 8
const val mus_e1m9 = 9
const val mus_e2m1 = 10
const val mus_e2m2 = 11
const val mus_e2m3 = 12
const val mus_e2m4 = 13
const val mus_e2m5 = 14
const val mus_e2m6 = 15
const val mus_e2m7 = 16
const val mus_e2m8 = 17
const val mus_e2m9 = 18
const val mus_e3m1 = 19
const val mus_e3m2 = 20
const val mus_e3m3 = 21
const val mus_e3m4 = 22
const val mus_e3m5 = 23
const val mus_e3m6 = 24
const val mus_e3m7 = 25
const val mus_e3m8 = 26
const val mus_e3m9 = 27
const val mus_inter = 28
const val mus_intro = 29
const val mus_bunny = 30
const val mus_victor = 31
const val mus_introa = 32
const val mus_runnin = 33
const val mus_stalks = 34
const val mus_countd = 35
const val mus_betwee = 36
const val mus_doom = 37
const val mus_the_da = 38
const val mus_shawn = 39
const val mus_ddtblu = 40
const val mus_in_cit = 41
const val mus_dead = 42
const val mus_stlks2 = 43
const val mus_theda2 = 44
const val mus_doom2 = 45
const val mus_ddtbl2 = 46
const val mus_runni2 = 47
const val mus_dead2 = 48
const val mus_stlks3 = 49
const val mus_romero = 50
const val mus_shawn2 = 51
const val mus_messag = 52
const val mus_count2 = 53
const val mus_ddtbl3 = 54
const val mus_ampie = 55
const val mus_theda3 = 56
const val mus_adrian = 57
const val mus_messg2 = 58
const val mus_romer2 = 59
const val mus_tense = 60
const val mus_shawn3 = 61
const val mus_openin = 62
const val mus_evil = 63
const val mus_ultima = 64
const val mus_read_m = 65
const val mus_dm2ttl = 66
const val mus_dm2int = 67
const val NUMMUSIC = 68

// sfxenum_t
const val sfx_None = 0
const val sfx_pistol = 1
const val sfx_shotgn = 2
const val sfx_sgcock = 3
const val sfx_dshtgn = 4
const val sfx_dbopn = 5
const val sfx_dbcls = 6
const val sfx_dbload = 7
const val sfx_plasma = 8
const val sfx_bfg = 9
const val sfx_sawup = 10
const val sfx_sawidl = 11
const val sfx_sawful = 12
const val sfx_sawhit = 13
const val sfx_rlaunc = 14
const val sfx_rxplod = 15
const val sfx_firsht = 16
const val sfx_firxpl = 17
const val sfx_pstart = 18
const val sfx_pstop = 19
const val sfx_doropn = 20
const val sfx_dorcls = 21
const val sfx_stnmov = 22
const val sfx_swtchn = 23
const val sfx_swtchx = 24
const val sfx_plpain = 25
const val sfx_dmpain = 26
const val sfx_popain = 27
const val sfx_vipain = 28
const val sfx_mnpain = 29
const val sfx_pepain = 30
const val sfx_slop = 31
const val sfx_itemup = 32
const val sfx_wpnup = 33
const val sfx_oof = 34
const val sfx_telept = 35
const val sfx_posit1 = 36
const val sfx_posit2 = 37
const val sfx_posit3 = 38
const val sfx_bgsit1 = 39
const val sfx_bgsit2 = 40
const val sfx_sgtsit = 41
const val sfx_cacsit = 42
const val sfx_brssit = 43
const val sfx_cybsit = 44
const val sfx_spisit = 45
const val sfx_bspsit = 46
const val sfx_kntsit = 47
const val sfx_vilsit = 48
const val sfx_mansit = 49
const val sfx_pesit = 50
const val sfx_sklatk = 51
const val sfx_sgtatk = 52
const val sfx_skepch = 53
const val sfx_vilatk = 54
const val sfx_claw = 55
const val sfx_skeswg = 56
const val sfx_pldeth = 57
const val sfx_pdiehi = 58
const val sfx_podth1 = 59
const val sfx_podth2 = 60
const val sfx_podth3 = 61
const val sfx_bgdth1 = 62
const val sfx_bgdth2 = 63
const val sfx_sgtdth = 64
const val sfx_cacdth = 65
const val sfx_skldth = 66
const val sfx_brsdth = 67
const val sfx_cybdth = 68
const val sfx_spidth = 69
const val sfx_bspdth = 70
const val sfx_vildth = 71
const val sfx_kntdth = 72
const val sfx_pedth = 73
const val sfx_skedth = 74
const val sfx_posact = 75
const val sfx_bgact = 76
const val sfx_dmact = 77
const val sfx_bspact = 78
const val sfx_bspwlk = 79
const val sfx_vilact = 80
const val sfx_noway = 81
const val sfx_barexp = 82
const val sfx_punch = 83
const val sfx_hoof = 84
const val sfx_metal = 85
const val sfx_chgun = 86
const val sfx_tink = 87
const val sfx_bdopn = 88
const val sfx_bdcls = 89
const val sfx_itmbk = 90
const val sfx_flame = 91
const val sfx_flamst = 92
const val sfx_getpow = 93
const val sfx_bospit = 94
const val sfx_boscub = 95
const val sfx_bossit = 96
const val sfx_bospn = 97
const val sfx_bosdth = 98
const val sfx_manatk = 99
const val sfx_mandth = 100
const val sfx_sssit = 101
const val sfx_ssdth = 102
const val sfx_keenpn = 103
const val sfx_keendt = 104
const val sfx_skeact = 105
const val sfx_skesit = 106
const val sfx_skeatk = 107
const val sfx_radio = 108
const val NUMSFX = 109

val S_sfx: Array<sfxinfo_t> = arrayOf(
    sfxinfo_t("none", false, 0, -1, -1, -1),  // dummy [0]
    sfxinfo_t("pistol", false, 64, -1, -1, -1),
    sfxinfo_t("shotgn", false, 64, -1, -1, -1),
    sfxinfo_t("sgcock", false, 64, -1, -1, -1),
    sfxinfo_t("dshtgn", false, 64, -1, -1, -1),
    sfxinfo_t("dbopn", false, 64, -1, -1, -1),
    sfxinfo_t("dbcls", false, 64, -1, -1, -1),
    sfxinfo_t("dbload", false, 64, -1, -1, -1),
    sfxinfo_t("plasma", false, 64, -1, -1, -1),
    sfxinfo_t("bfg", false, 64, -1, -1, -1),
    sfxinfo_t("sawup", false, 64, -1, -1, -1),
    sfxinfo_t("sawidl", false, 118, -1, -1, -1),
    sfxinfo_t("sawful", false, 64, -1, -1, -1),
    sfxinfo_t("sawhit", false, 64, -1, -1, -1),
    sfxinfo_t("rlaunc", false, 64, -1, -1, -1),
    sfxinfo_t("rxplod", false, 70, -1, -1, -1),
    sfxinfo_t("firsht", false, 70, -1, -1, -1),
    sfxinfo_t("firxpl", false, 70, -1, -1, -1),
    sfxinfo_t("pstart", false, 100, -1, -1, -1),
    sfxinfo_t("pstop", false, 100, -1, -1, -1),
    sfxinfo_t("doropn", false, 100, -1, -1, -1),
    sfxinfo_t("dorcls", false, 100, -1, -1, -1),
    sfxinfo_t("stnmov", false, 119, -1, -1, -1),
    sfxinfo_t("swtchn", false, 78, -1, -1, -1),
    sfxinfo_t("swtchx", false, 78, -1, -1, -1),
    sfxinfo_t("plpain", false, 96, -1, -1, -1),
    sfxinfo_t("dmpain", false, 96, -1, -1, -1),
    sfxinfo_t("popain", false, 96, -1, -1, -1),
    sfxinfo_t("vipain", false, 96, -1, -1, -1),
    sfxinfo_t("mnpain", false, 96, -1, -1, -1),
    sfxinfo_t("pepain", false, 96, -1, -1, -1),
    sfxinfo_t("slop", false, 78, -1, -1, -1),
    sfxinfo_t("itemup", true, 78, -1, -1, -1),
    sfxinfo_t("wpnup", true, 78, -1, -1, -1),
    sfxinfo_t("oof", false, 96, -1, -1, -1),
    sfxinfo_t("telept", false, 32, -1, -1, -1),
    sfxinfo_t("posit1", true, 98, -1, -1, -1),
    sfxinfo_t("posit2", true, 98, -1, -1, -1),
    sfxinfo_t("posit3", true, 98, -1, -1, -1),
    sfxinfo_t("bgsit1", true, 98, -1, -1, -1),
    sfxinfo_t("bgsit2", true, 98, -1, -1, -1),
    sfxinfo_t("sgtsit", true, 98, -1, -1, -1),
    sfxinfo_t("cacsit", true, 98, -1, -1, -1),
    sfxinfo_t("brssit", true, 94, -1, -1, -1),
    sfxinfo_t("cybsit", true, 92, -1, -1, -1),
    sfxinfo_t("spisit", true, 90, -1, -1, -1),
    sfxinfo_t("bspsit", true, 90, -1, -1, -1),
    sfxinfo_t("kntsit", true, 90, -1, -1, -1),
    sfxinfo_t("vilsit", true, 90, -1, -1, -1),
    sfxinfo_t("mansit", true, 90, -1, -1, -1),
    sfxinfo_t("pesit", true, 90, -1, -1, -1),
    sfxinfo_t("sklatk", false, 70, -1, -1, -1),
    sfxinfo_t("sgtatk", false, 70, -1, -1, -1),
    sfxinfo_t("skepch", false, 70, -1, -1, -1),
    sfxinfo_t("vilatk", false, 70, -1, -1, -1),
    sfxinfo_t("claw", false, 70, -1, -1, -1),
    sfxinfo_t("skeswg", false, 70, -1, -1, -1),
    sfxinfo_t("pldeth", false, 32, -1, -1, -1),
    sfxinfo_t("pdiehi", false, 32, -1, -1, -1),
    sfxinfo_t("podth1", false, 70, -1, -1, -1),
    sfxinfo_t("podth2", false, 70, -1, -1, -1),
    sfxinfo_t("podth3", false, 70, -1, -1, -1),
    sfxinfo_t("bgdth1", false, 70, -1, -1, -1),
    sfxinfo_t("bgdth2", false, 70, -1, -1, -1),
    sfxinfo_t("sgtdth", false, 70, -1, -1, -1),
    sfxinfo_t("cacdth", false, 70, -1, -1, -1),
    sfxinfo_t("skldth", false, 70, -1, -1, -1),
    sfxinfo_t("brsdth", false, 32, -1, -1, -1),
    sfxinfo_t("cybdth", false, 32, -1, -1, -1),
    sfxinfo_t("spidth", false, 32, -1, -1, -1),
    sfxinfo_t("bspdth", false, 32, -1, -1, -1),
    sfxinfo_t("vildth", false, 32, -1, -1, -1),
    sfxinfo_t("kntdth", false, 32, -1, -1, -1),
    sfxinfo_t("pedth", false, 32, -1, -1, -1),
    sfxinfo_t("skedth", false, 32, -1, -1, -1),
    sfxinfo_t("posact", true, 120, -1, -1, -1),
    sfxinfo_t("bgact", true, 120, -1, -1, -1),
    sfxinfo_t("dmact", true, 120, -1, -1, -1),
    sfxinfo_t("bspact", true, 100, -1, -1, -1),
    sfxinfo_t("bspwlk", true, 100, -1, -1, -1),
    sfxinfo_t("vilact", true, 100, -1, -1, -1),
    sfxinfo_t("noway", false, 78, -1, -1, -1),
    sfxinfo_t("barexp", false, 60, -1, -1, -1),
    sfxinfo_t("punch", false, 64, -1, -1, -1),
    sfxinfo_t("hoof", false, 70, -1, -1, -1),
    sfxinfo_t("metal", false, 70, -1, -1, -1),
    sfxinfo_t("chgun", false, 64, sfx_pistol, 150, 0),
    sfxinfo_t("tink", false, 60, -1, -1, -1),
    sfxinfo_t("bdopn", false, 100, -1, -1, -1),
    sfxinfo_t("bdcls", false, 100, -1, -1, -1),
    sfxinfo_t("itmbk", false, 100, -1, -1, -1),
    sfxinfo_t("flame", false, 32, -1, -1, -1),
    sfxinfo_t("flamst", false, 32, -1, -1, -1),
    sfxinfo_t("getpow", false, 60, -1, -1, -1),
    sfxinfo_t("bospit", false, 70, -1, -1, -1),
    sfxinfo_t("boscub", false, 70, -1, -1, -1),
    sfxinfo_t("bossit", false, 70, -1, -1, -1),
    sfxinfo_t("bospn", false, 70, -1, -1, -1),
    sfxinfo_t("bosdth", false, 70, -1, -1, -1),
    sfxinfo_t("manatk", false, 70, -1, -1, -1),
    sfxinfo_t("mandth", false, 70, -1, -1, -1),
    sfxinfo_t("sssit", false, 70, -1, -1, -1),
    sfxinfo_t("ssdth", false, 70, -1, -1, -1),
    sfxinfo_t("keenpn", false, 70, -1, -1, -1),
    sfxinfo_t("keendt", false, 70, -1, -1, -1),
    sfxinfo_t("skeact", false, 70, -1, -1, -1),
    sfxinfo_t("skesit", false, 70, -1, -1, -1),
    sfxinfo_t("skeatk", false, 70, -1, -1, -1),
    sfxinfo_t("radio", false, 60, -1, -1, -1),
)

val S_music: Array<musicinfo_t> = arrayOf(
    musicinfo_t(""),  // dummy [0]
    musicinfo_t("e1m1"),
    musicinfo_t("e1m2"),
    musicinfo_t("e1m3"),
    musicinfo_t("e1m4"),
    musicinfo_t("e1m5"),
    musicinfo_t("e1m6"),
    musicinfo_t("e1m7"),
    musicinfo_t("e1m8"),
    musicinfo_t("e1m9"),
    musicinfo_t("e2m1"),
    musicinfo_t("e2m2"),
    musicinfo_t("e2m3"),
    musicinfo_t("e2m4"),
    musicinfo_t("e2m5"),
    musicinfo_t("e2m6"),
    musicinfo_t("e2m7"),
    musicinfo_t("e2m8"),
    musicinfo_t("e2m9"),
    musicinfo_t("e3m1"),
    musicinfo_t("e3m2"),
    musicinfo_t("e3m3"),
    musicinfo_t("e3m4"),
    musicinfo_t("e3m5"),
    musicinfo_t("e3m6"),
    musicinfo_t("e3m7"),
    musicinfo_t("e3m8"),
    musicinfo_t("e3m9"),
    musicinfo_t("inter"),
    musicinfo_t("intro"),
    musicinfo_t("bunny"),
    musicinfo_t("victor"),
    musicinfo_t("introa"),
    musicinfo_t("runnin"),
    musicinfo_t("stalks"),
    musicinfo_t("countd"),
    musicinfo_t("betwee"),
    musicinfo_t("doom"),
    musicinfo_t("the_da"),
    musicinfo_t("shawn"),
    musicinfo_t("ddtblu"),
    musicinfo_t("in_cit"),
    musicinfo_t("dead"),
    musicinfo_t("stlks2"),
    musicinfo_t("theda2"),
    musicinfo_t("doom2"),
    musicinfo_t("ddtbl2"),
    musicinfo_t("runni2"),
    musicinfo_t("dead2"),
    musicinfo_t("stlks3"),
    musicinfo_t("romero"),
    musicinfo_t("shawn2"),
    musicinfo_t("messag"),
    musicinfo_t("count2"),
    musicinfo_t("ddtbl3"),
    musicinfo_t("ampie"),
    musicinfo_t("theda3"),
    musicinfo_t("adrian"),
    musicinfo_t("messg2"),
    musicinfo_t("romer2"),
    musicinfo_t("tense"),
    musicinfo_t("shawn3"),
    musicinfo_t("openin"),
    musicinfo_t("evil"),
    musicinfo_t("ultima"),
    musicinfo_t("read_m"),
    musicinfo_t("dm2ttl"),
    musicinfo_t("dm2int"),
)
