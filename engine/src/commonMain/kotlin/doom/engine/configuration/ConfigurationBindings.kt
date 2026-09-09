
package doom.engine.configuration

import doom.engine.DoomStorage
import doom.engine.KEY_DOWNARROW
import doom.engine.KEY_LEFTARROW
import doom.engine.KEY_RALT
import doom.engine.KEY_RCTRL
import doom.engine.KEY_RIGHTARROW
import doom.engine.KEY_RSHIFT
import doom.engine.KEY_UPARROW
import doom.engine.audio.numChannels
import doom.engine.audio.sndMusicVolume
import doom.engine.audio.sndSfxVolume
import doom.engine.core.DoomEngineCore
import doom.engine.core.basedefault
import doom.engine.gameplay.NUMKEYS
import doom.engine.gameplay.joybfire
import doom.engine.gameplay.joybspeed
import doom.engine.gameplay.joybstrafe
import doom.engine.gameplay.joybuse
import doom.engine.gameplay.keyDown
import doom.engine.gameplay.keyFire
import doom.engine.gameplay.keyLeft
import doom.engine.gameplay.keyRight
import doom.engine.gameplay.keySpeed
import doom.engine.gameplay.keyStrafe
import doom.engine.gameplay.keyStrafeleft
import doom.engine.gameplay.keyStraferight
import doom.engine.gameplay.keyUp
import doom.engine.gameplay.keyUse
import doom.engine.gameplay.mousebfire
import doom.engine.gameplay.mousebforward
import doom.engine.gameplay.mousebstrafe
import doom.engine.hud.chatMacros
import doom.engine.menu.detailLevel
import doom.engine.menu.mouseSensitivity
import doom.engine.menu.screenblocks
import doom.engine.menu.showMessages
import doom.engine.rendering.usegamma
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

internal fun DoomEngineCore.mSaveDefaults() = configuration.save()

internal fun DoomEngineCore.mLoadDefaults() {
    val option = mCheckParm("-config")
    val fileName = if (option != 0 && option < myargc - 1) {
        myargv[option + 1].also { println("\tdefault file: $it") }
    } else basedefault
    configuration.load(fileName)
}

internal fun DoomEngineCore.createConfiguration(storage: DoomStorage): EngineConfiguration {
    var usemouse = 0
    var usejoystick = 0
    return EngineConfiguration(listOf(
        IntegerSetting("mouse_sensitivity", { mouseSensitivity }, { mouseSensitivity = it }, 5, range = 0..9),
        IntegerSetting("sfx_volume", { sndSfxVolume }, { sndSfxVolume = it }, 8, range = 0..15),
        IntegerSetting("music_volume", { sndMusicVolume }, { sndMusicVolume = it }, 8, range = 0..15),
        IntegerSetting("show_messages", { showMessages }, { showMessages = it }, 1, range = 0..1),

        IntegerSetting("key_right", { keyRight }, { keyRight = it }, KEY_RIGHTARROW, range = 0 until NUMKEYS),
        IntegerSetting("key_left", { keyLeft }, { keyLeft = it }, KEY_LEFTARROW, range = 0 until NUMKEYS),
        IntegerSetting("key_up", { keyUp }, { keyUp = it }, KEY_UPARROW, range = 0 until NUMKEYS),
        IntegerSetting("key_down", { keyDown }, { keyDown = it }, KEY_DOWNARROW, range = 0 until NUMKEYS),
        IntegerSetting("key_strafeleft", { keyStrafeleft }, { keyStrafeleft = it }, ','.code, range = 0 until NUMKEYS),
        IntegerSetting("key_straferight", { keyStraferight }, { keyStraferight = it }, '.'.code, range = 0 until NUMKEYS),

        IntegerSetting("key_fire", { keyFire }, { keyFire = it }, KEY_RCTRL, range = 0 until NUMKEYS),
        IntegerSetting("key_use", { keyUse }, { keyUse = it }, ' '.code, range = 0 until NUMKEYS),
        IntegerSetting("key_strafe", { keyStrafe }, { keyStrafe = it }, KEY_RALT, range = 0 until NUMKEYS),
        IntegerSetting("key_speed", { keySpeed }, { keySpeed = it }, KEY_RSHIFT, range = 0 until NUMKEYS),


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

        StringSetting("chatmacro0", { chatMacros[0] }, { chatMacros[0] = it }, HUSTR_CHATMACRO0),
        StringSetting("chatmacro1", { chatMacros[1] }, { chatMacros[1] = it }, HUSTR_CHATMACRO1),
        StringSetting("chatmacro2", { chatMacros[2] }, { chatMacros[2] = it }, HUSTR_CHATMACRO2),
        StringSetting("chatmacro3", { chatMacros[3] }, { chatMacros[3] = it }, HUSTR_CHATMACRO3),
        StringSetting("chatmacro4", { chatMacros[4] }, { chatMacros[4] = it }, HUSTR_CHATMACRO4),
        StringSetting("chatmacro5", { chatMacros[5] }, { chatMacros[5] = it }, HUSTR_CHATMACRO5),
        StringSetting("chatmacro6", { chatMacros[6] }, { chatMacros[6] = it }, HUSTR_CHATMACRO6),
        StringSetting("chatmacro7", { chatMacros[7] }, { chatMacros[7] = it }, HUSTR_CHATMACRO7),
        StringSetting("chatmacro8", { chatMacros[8] }, { chatMacros[8] = it }, HUSTR_CHATMACRO8),
        StringSetting("chatmacro9", { chatMacros[9] }, { chatMacros[9] = it }, HUSTR_CHATMACRO9),
    ), storage)
}
