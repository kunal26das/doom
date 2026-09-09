// Port of linuxdoom-1.10 m_misc.c -- Main loop menu stuff.
// HUD drawing and composition glue for typed configuration and PCX capture.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

import doom.engine.configuration.EngineConfiguration
import doom.engine.configuration.IntegerSetting
import doom.engine.configuration.StringSetting

// C-locale toupper (ASCII a-z only, exactly like the DOS/linux original).
private fun DoomEngineCore.toupper(c: Int): Int = if (c >= 'a'.code && c <= 'z'.code) c - 32 else c

//
// M_DrawText
// Returns the final X coordinate
// HU_Init must have been called to init the font
//
internal fun DoomEngineCore.M_DrawText(x: Int, y: Int, direct: Boolean, string: String): Int {
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

/** Compatibility entry points delegate persistence and syntax to a typed service. */
internal fun DoomEngineCore.M_SaveDefaults() = configuration.save()

internal fun DoomEngineCore.M_LoadDefaults() {
    val option = M_CheckParm("-config")
    val fileName = if (option != 0 && option < myargc - 1) {
        myargv[option + 1].also { println("\tdefault file: $it") }
    } else basedefault
    configuration.load(fileName)
}

internal fun DoomEngineCore.WritePCXfile(
    filename: String,
    data: ByteArray,
    width: Int,
    height: Int,
    palette: ByteArray,
) {
    host.write(filename, screenshots.encode(data, width, height, palette))
}

internal fun DoomEngineCore.M_ScreenShot() {
    val linear = screens[2]
    I_ReadScreen(linear)
    val name = screenshots.nextFileName(::I_Error)
    WritePCXfile(name, linear, SCREENWIDTH, SCREENHEIGHT, W_CacheLumpName("PLAYPAL"))
    players[consoleplayer].message = "screen shot"
}

/** Only this composition glue knows which game state each persisted setting binds. */
internal fun DoomEngineCore.createConfiguration(storage: DoomStorage): EngineConfiguration {
    // Retained configuration values for devices whose legacy polling is not used by the host.
    var usemouse = 0
    var usejoystick = 0
    return EngineConfiguration(listOf(
        IntegerSetting("mouse_sensitivity", { mouseSensitivity }, { mouseSensitivity = it }, 5, range = 0..9),
        IntegerSetting("sfx_volume", { snd_SfxVolume }, { snd_SfxVolume = it }, 8, range = 0..15),
        IntegerSetting("music_volume", { snd_MusicVolume }, { snd_MusicVolume = it }, 8, range = 0..15),
        IntegerSetting("show_messages", { showMessages }, { showMessages = it }, 1, range = 0..1),

        // (NORMALUNIX key defaults)
        IntegerSetting("key_right", { key_right }, { key_right = it }, KEY_RIGHTARROW, range = 0 until NUMKEYS),
        IntegerSetting("key_left", { key_left }, { key_left = it }, KEY_LEFTARROW, range = 0 until NUMKEYS),
        IntegerSetting("key_up", { key_up }, { key_up = it }, KEY_UPARROW, range = 0 until NUMKEYS),
        IntegerSetting("key_down", { key_down }, { key_down = it }, KEY_DOWNARROW, range = 0 until NUMKEYS),
        IntegerSetting("key_strafeleft", { key_strafeleft }, { key_strafeleft = it }, ','.code, range = 0 until NUMKEYS),
        IntegerSetting("key_straferight", { key_straferight }, { key_straferight = it }, '.'.code, range = 0 until NUMKEYS),

        IntegerSetting("key_fire", { key_fire }, { key_fire = it }, KEY_RCTRL, range = 0 until NUMKEYS),
        IntegerSetting("key_use", { key_use }, { key_use = it }, ' '.code, range = 0 until NUMKEYS),
        IntegerSetting("key_strafe", { key_strafe }, { key_strafe = it }, KEY_RALT, range = 0 until NUMKEYS),
        IntegerSetting("key_speed", { key_speed }, { key_speed = it }, KEY_RSHIFT, range = 0 until NUMKEYS),

        // (the SNDSERV sndserver/mb_used and LINUX mousedev/mousetype UNIX hacks
        //  are platform-layer only and not ported)

        IntegerSetting("use_mouse", { usemouse }, { usemouse = it }, 1, range = 0..1),
        IntegerSetting("mouseb_fire", { mousebfire }, { mousebfire = it }, 0, range = -1..2),
        IntegerSetting("mouseb_strafe", { mousebstrafe }, { mousebstrafe = it }, 1, range = -1..2),
        IntegerSetting("mouseb_forward", { mousebforward }, { mousebforward = it }, 2, range = -1..2),

        IntegerSetting("use_joystick", { usejoystick }, { usejoystick = it }, 0, range = 0..1),
        IntegerSetting("joyb_fire", { joybfire }, { joybfire = it }, 0, range = -1..3),
        IntegerSetting("joyb_strafe", { joybstrafe }, { joybstrafe = it }, 1, range = -1..3),
        IntegerSetting("joyb_use", { joybuse }, { joybuse = it }, 3, range = -1..3),
        IntegerSetting("joyb_speed", { joybspeed }, { joybspeed = it }, 2, range = -1..3),

        IntegerSetting("screenblocks", { screenblocks }, { screenblocks = it }, 9, range = 3..11),
        IntegerSetting("detaillevel", { detailLevel }, { detailLevel = it }, 0, range = 0..1),

        IntegerSetting("snd_channels", { numChannels }, { numChannels = it }, 3, range = 0..64),

        IntegerSetting("usegamma", { usegamma }, { usegamma = it }, 0, range = 0..4),

        StringSetting("chatmacro0", { chat_macros[0] }, { chat_macros[0] = it }, HUSTR_CHATMACRO0),
        StringSetting("chatmacro1", { chat_macros[1] }, { chat_macros[1] = it }, HUSTR_CHATMACRO1),
        StringSetting("chatmacro2", { chat_macros[2] }, { chat_macros[2] = it }, HUSTR_CHATMACRO2),
        StringSetting("chatmacro3", { chat_macros[3] }, { chat_macros[3] = it }, HUSTR_CHATMACRO3),
        StringSetting("chatmacro4", { chat_macros[4] }, { chat_macros[4] = it }, HUSTR_CHATMACRO4),
        StringSetting("chatmacro5", { chat_macros[5] }, { chat_macros[5] = it }, HUSTR_CHATMACRO5),
        StringSetting("chatmacro6", { chat_macros[6] }, { chat_macros[6] = it }, HUSTR_CHATMACRO6),
        StringSetting("chatmacro7", { chat_macros[7] }, { chat_macros[7] = it }, HUSTR_CHATMACRO7),
        StringSetting("chatmacro8", { chat_macros[8] }, { chat_macros[8] = it }, HUSTR_CHATMACRO8),
        StringSetting("chatmacro9", { chat_macros[9] }, { chat_macros[9] = it }, HUSTR_CHATMACRO9),
    ), storage)
}
