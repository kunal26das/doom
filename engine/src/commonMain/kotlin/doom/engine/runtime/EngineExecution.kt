package doom.engine.runtime

import doom.engine.DoomMetrics

internal interface EngineExecution {
    val metrics: DoomMetrics
    fun boot(wads: List<ByteArray>, args: List<String>)
    fun step(singleTic: Boolean)
}
