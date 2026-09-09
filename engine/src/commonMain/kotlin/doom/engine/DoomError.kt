package doom.engine

/** Engine startup or game-data failure reported to its owner. */
public class DoomError(message: String) : Exception(message)
