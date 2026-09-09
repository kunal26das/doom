
package doom.engine.menu

internal class MenuItem(
    var status: Int,
    var name: String,
    var routine: ((Int) -> Unit)?,
    var alphaKey: Int = 0,
)
