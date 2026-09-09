package com.kunal26das.doom

expect fun startAudioOutput(render: (FloatArray) -> Unit): AudioOutput?


const val OUTPUT_RATE = 44100
