package com.kunal26das.doom.domain

// Logical DOOM input protocol, independent of Compose and platform key codes.
// The adapter regression test verifies compatibility with the legacy engine.
const val KEY_RIGHTARROW = 0xae
const val KEY_LEFTARROW = 0xac
const val KEY_UPARROW = 0xad
const val KEY_DOWNARROW = 0xaf
const val KEY_ESCAPE = 27
const val KEY_ENTER = 13
const val KEY_TAB = 9
const val KEY_F1 = 0x80 + 0x3b
const val KEY_F2 = 0x80 + 0x3c
const val KEY_F3 = 0x80 + 0x3d
const val KEY_F4 = 0x80 + 0x3e
const val KEY_F5 = 0x80 + 0x3f
const val KEY_F6 = 0x80 + 0x40
const val KEY_F7 = 0x80 + 0x41
const val KEY_F8 = 0x80 + 0x42
const val KEY_F9 = 0x80 + 0x43
const val KEY_F10 = 0x80 + 0x44
const val KEY_F11 = 0x80 + 0x57
const val KEY_F12 = 0x80 + 0x58

const val KEY_BACKSPACE = 127
const val KEY_PAUSE = 0xff

const val KEY_EQUALS = 0x3d
const val KEY_MINUS = 0x2d

const val KEY_RSHIFT = 0x80 + 0x36
const val KEY_RCTRL = 0x80 + 0x1d
const val KEY_RALT = 0x80 + 0x38
const val KEY_LALT = KEY_RALT
