// Port of linuxdoom-1.10 m_misc.c -- Main loop menu stuff.
// Default Config File. PCX Screenshots.
// (M_ReadFile/M_WriteFile live in i_system.kt as host-persistence hooks.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

// C-locale toupper (ASCII a-z only, exactly like the DOS/linux original).
private fun toupper(c: Int): Int = if (c >= 'a'.code && c <= 'z'.code) c - 32 else c

//
// M_DrawText
// Returns the final X coordinate
// HU_Init must have been called to init the font
//
fun M_DrawText(x: Int, y: Int, direct: Boolean, string: String): Int {
    var x = x
    var pos = 0

    while (pos < string.length) {
        val c = toupper(string[pos].code) - HU_FONTSTART
        pos++
        // (vanilla bug: `>` instead of `>=`, kept as-is)
        if (c < 0 || c > HU_FONTSIZE) {
            x += 4
            continue
        }

        val w = patchWidth(hu_font[c])
        if (x + w > SCREENWIDTH)
            break
        if (direct)
            V_DrawPatchDirect(x, y, 0, hu_font[c])
        else
            V_DrawPatch(x, y, 0, hu_font[c])
        x += w
    }

    return x
}

// M_WriteFile / M_ReadFile: see i_system.kt (host persistence hooks).

//
// DEFAULTS
//
var usemouse = 0
var usejoystick = 0

// C default_t: {name, int* location, defaultvalue}. The location pointer
// becomes a get/set lambda pair; the (char*) string-default hack (chatmacros)
// becomes the nullable strGet/strSet/strDefault trio. For string entries the
// C table stored the char* pointer in defaultvalue -- modeled as MAXINT so
// M_SaveDefaults' original range check still discriminates int vs string.
class default_t(
    val name: String,
    val get: (() -> Int)? = null,
    val set: ((Int) -> Unit)? = null,
    val defaultvalue: Int = MAXINT,
    val strGet: (() -> String)? = null,
    val strSet: ((String) -> Unit)? = null,
    val strDefault: String? = null,
)

val defaults: Array<default_t> = arrayOf(
    default_t("mouse_sensitivity", { mouseSensitivity }, { mouseSensitivity = it }, 5),
    default_t("sfx_volume", { snd_SfxVolume }, { snd_SfxVolume = it }, 8),
    default_t("music_volume", { snd_MusicVolume }, { snd_MusicVolume = it }, 8),
    default_t("show_messages", { showMessages }, { showMessages = it }, 1),

    // (NORMALUNIX key defaults)
    default_t("key_right", { key_right }, { key_right = it }, KEY_RIGHTARROW),
    default_t("key_left", { key_left }, { key_left = it }, KEY_LEFTARROW),
    default_t("key_up", { key_up }, { key_up = it }, KEY_UPARROW),
    default_t("key_down", { key_down }, { key_down = it }, KEY_DOWNARROW),
    default_t("key_strafeleft", { key_strafeleft }, { key_strafeleft = it }, ','.code),
    default_t("key_straferight", { key_straferight }, { key_straferight = it }, '.'.code),

    default_t("key_fire", { key_fire }, { key_fire = it }, KEY_RCTRL),
    default_t("key_use", { key_use }, { key_use = it }, ' '.code),
    default_t("key_strafe", { key_strafe }, { key_strafe = it }, KEY_RALT),
    default_t("key_speed", { key_speed }, { key_speed = it }, KEY_RSHIFT),

    // (the SNDSERV sndserver/mb_used and LINUX mousedev/mousetype UNIX hacks
    //  are platform-layer only and not ported)

    default_t("use_mouse", { usemouse }, { usemouse = it }, 1),
    default_t("mouseb_fire", { mousebfire }, { mousebfire = it }, 0),
    default_t("mouseb_strafe", { mousebstrafe }, { mousebstrafe = it }, 1),
    default_t("mouseb_forward", { mousebforward }, { mousebforward = it }, 2),

    default_t("use_joystick", { usejoystick }, { usejoystick = it }, 0),
    default_t("joyb_fire", { joybfire }, { joybfire = it }, 0),
    default_t("joyb_strafe", { joybstrafe }, { joybstrafe = it }, 1),
    default_t("joyb_use", { joybuse }, { joybuse = it }, 3),
    default_t("joyb_speed", { joybspeed }, { joybspeed = it }, 2),

    default_t("screenblocks", { screenblocks }, { screenblocks = it }, 9),
    default_t("detaillevel", { detailLevel }, { detailLevel = it }, 0),

    default_t("snd_channels", { numChannels }, { numChannels = it }, 3),

    default_t("usegamma", { usegamma }, { usegamma = it }, 0),

    default_t("chatmacro0", strGet = { chat_macros[0] }, strSet = { chat_macros[0] = it }, strDefault = HUSTR_CHATMACRO0),
    default_t("chatmacro1", strGet = { chat_macros[1] }, strSet = { chat_macros[1] = it }, strDefault = HUSTR_CHATMACRO1),
    default_t("chatmacro2", strGet = { chat_macros[2] }, strSet = { chat_macros[2] = it }, strDefault = HUSTR_CHATMACRO2),
    default_t("chatmacro3", strGet = { chat_macros[3] }, strSet = { chat_macros[3] = it }, strDefault = HUSTR_CHATMACRO3),
    default_t("chatmacro4", strGet = { chat_macros[4] }, strSet = { chat_macros[4] = it }, strDefault = HUSTR_CHATMACRO4),
    default_t("chatmacro5", strGet = { chat_macros[5] }, strSet = { chat_macros[5] = it }, strDefault = HUSTR_CHATMACRO5),
    default_t("chatmacro6", strGet = { chat_macros[6] }, strSet = { chat_macros[6] = it }, strDefault = HUSTR_CHATMACRO6),
    default_t("chatmacro7", strGet = { chat_macros[7] }, strSet = { chat_macros[7] = it }, strDefault = HUSTR_CHATMACRO7),
    default_t("chatmacro8", strGet = { chat_macros[8] }, strSet = { chat_macros[8] = it }, strDefault = HUSTR_CHATMACRO8),
    default_t("chatmacro9", strGet = { chat_macros[9] }, strSet = { chat_macros[9] = it }, strDefault = HUSTR_CHATMACRO9),
)

var numdefaults = 0
var defaultfile = ""

//
// M_SaveDefaults
//
fun M_SaveDefaults() {
    val f = StringBuilder()

    for (i in 0 until numdefaults) {
        if (defaults[i].defaultvalue > -0xfff &&
            defaults[i].defaultvalue < 0xfff
        ) {
            val v = defaults[i].get!!()
            f.append("${defaults[i].name}\t\t$v\n")
        } else {
            f.append("${defaults[i].name}\t\t\"${defaults[i].strGet!!()}\"\n")
        }
    }

    // can't write the file, but don't complain (M_WriteFile is best-effort)
    M_WriteFile(defaultfile, f.toString().encodeToByteArray())
}

//
// M_LoadDefaults
//
fun M_LoadDefaults() {
    var i: Int

    // set everything to base values
    numdefaults = defaults.size
    for (j in 0 until numdefaults) {
        val d = defaults[j]
        if (d.strDefault != null)
            d.strSet!!(d.strDefault)
        else
            d.set!!(d.defaultvalue)
    }

    // check for a custom default file
    i = M_CheckParm("-config")
    if (i != 0 && i < myargc - 1) {
        defaultfile = myargv[i + 1]
        println("	default file: $defaultfile")
    } else
        defaultfile = basedefault

    // read the file in, overriding any set defaults
    // (fscanf "%79s %[^\n]\n" per line -> name token + rest-of-line value)
    val f = M_ReadFile(defaultfile)
    if (f != null) {
        for (line in f.decodeToString().split('\n')) {
            var isstring = false
            var parm = 0
            var newstring = ""

            val trimmed = line.trim { it == ' ' || it == '\t' || it == '\r' }
            if (trimmed.isEmpty())
                continue
            var sp = 0
            while (sp < trimmed.length && trimmed[sp] != ' ' && trimmed[sp] != '\t')
                sp++
            if (sp >= trimmed.length)
                continue // no value field
            val def = trimmed.substring(0, sp)
            val strparm = trimmed.substring(sp).trimStart(' ', '\t')
            if (strparm.isEmpty())
                continue

            if (strparm[0] == '"') {
                // get a string default
                isstring = true
                newstring = if (strparm.length >= 2)
                    strparm.substring(1, strparm.length - 1)
                else
                    ""
            } else if (strparm.length > 1 && strparm[0] == '0' && strparm[1] == 'x') {
                parm = strparm.substring(2).toIntOrNull(16) ?: continue
            } else {
                parm = strparm.toIntOrNull() ?: continue
            }

            for (j in 0 until numdefaults)
                if (def == defaults[j].name) {
                    if (!isstring)
                        defaults[j].set?.invoke(parm)
                    else
                        defaults[j].strSet?.invoke(newstring)
                    break
                }
        }
    }
}

//
// SCREEN SHOTS
//
// pcx_t: the 128-byte PCX header is written straight into the output
// ByteArray at the offsets of the original packed struct.

//
// WritePCXfile
//
fun WritePCXfile(
    filename: String,
    data: ByteArray,
    width: Int,
    height: Int,
    palette: ByteArray,
) {
    val pcx = ByteArray(width * height * 2 + 1000)

    fun putShort(off: Int, v: Int) {
        pcx[off] = (v and 0xff).toByte()
        pcx[off + 1] = ((v shr 8) and 0xff).toByte()
    }

    pcx[0] = 0x0a               // PCX id (manufacturer)
    pcx[1] = 5                  // 256 color (version)
    pcx[2] = 1                  // uncompressed (encoding)
    pcx[3] = 8                  // 256 color (bits_per_pixel)
    putShort(4, 0)              // xmin
    putShort(6, 0)              // ymin
    putShort(8, width - 1)      // xmax
    putShort(10, height - 1)    // ymax
    putShort(12, width)         // hres
    putShort(14, height)        // vres
    // palette[48] at 16..63 already zeroed
    // reserved at 64 already zeroed
    pcx[65] = 1                 // chunky image (color_planes)
    putShort(66, width)         // bytes_per_line
    putShort(68, 2)             // not a grey scale (palette_type)
    // filler[58] at 70..127 already zeroed

    // pack the image
    var pack = 128              // &pcx->data
    var dataOfs = 0

    for (i in 0 until width * height) {
        if ((data.u8(dataOfs) and 0xc0) != 0xc0) {
            pcx[pack] = data[dataOfs]
            pack++
            dataOfs++
        } else {
            pcx[pack] = 0xc1.toByte()
            pack++
            pcx[pack] = data[dataOfs]
            pack++
            dataOfs++
        }
    }

    // write the palette
    pcx[pack] = 0x0c            // palette ID byte
    pack++
    var paletteOfs = 0
    for (i in 0 until 768) {
        pcx[pack] = palette[paletteOfs]
        pack++
        paletteOfs++
    }

    // write output file
    val length = pack
    M_WriteFile(filename, pcx.copyOf(length))
}

// C probed the filesystem with access() for the first free DOOM00..99 name;
// common Kotlin can't list/probe files, so a session counter picks the name.
private var shotNumber = 0

//
// M_ScreenShot
//
fun M_ScreenShot() {
    // munge planar buffer to linear
    val linear = screens[2]
    I_ReadScreen(linear)

    // find a file name to save it to
    val i = shotNumber
    if (i == 100)
        I_Error("M_ScreenShot: Couldn't create a PCX")
    shotNumber++
    val lbmname = "DOOM${'0' + i / 10}${'0' + i % 10}.pcx"

    // save the pcx file
    WritePCXfile(lbmname, linear,
        SCREENWIDTH, SCREENHEIGHT,
        W_CacheLumpName("PLAYPAL"))

    players[consoleplayer].message = "screen shot"
}
