package doom.engine

public interface DoomStorage {
    public fun read(name: String): ByteArray?
    public fun write(name: String, data: ByteArray)
}
