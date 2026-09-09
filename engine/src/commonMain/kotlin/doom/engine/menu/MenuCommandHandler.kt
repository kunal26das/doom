package doom.engine.menu

internal fun interface MenuCommandHandler {
    fun execute(command: MenuCommand, choice: Int)
}
