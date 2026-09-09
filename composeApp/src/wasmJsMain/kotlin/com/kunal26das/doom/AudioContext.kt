package com.kunal26das.doom

internal external class AudioContext {
    val currentTime: Double
    val destination: AudioDestinationNode
    val state: String
    fun createBuffer(numberOfChannels: Int, length: Int, sampleRate: Float): AudioBuffer
    fun createBufferSource(): AudioBufferSourceNode
    fun resume()
    fun close()
}
