package doom.engine.menu

/** Drawing is a separate capability from dispatching a menu command. */
internal fun interface MenuPageRenderer {
    fun draw(page: MenuPage)
}
