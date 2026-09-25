package doom.engine.geometry

internal object FineTangentTable {
    operator fun get(i: Int): Int = if (i < FINEANGLES / 2) finetangent[i] else finesine[i - FINEANGLES / 2]
}
