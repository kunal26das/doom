package com.kunal26das.doom.data.audio

import doom.engine.ISoundDriver

/** Connects engine sound commands to the platform's interleaved stereo audio callback. */
interface AudioMixer : ISoundDriver {
    /** Called by the audio thread; fill the complete output buffer. */
    fun render(out: FloatArray)
}
