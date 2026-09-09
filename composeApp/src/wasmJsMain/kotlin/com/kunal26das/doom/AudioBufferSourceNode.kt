package com.kunal26das.doom

internal external class AudioBufferSourceNode {
    var buffer: AudioBuffer?
    fun connect(destination: AudioDestinationNode)
    fun start(`when`: Double)
}
