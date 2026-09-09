package com.kunal26das.doom.data.audio

import doom.engine.ISoundDriver

interface AudioMixer : ISoundDriver {
    fun render(out: FloatArray)
}
