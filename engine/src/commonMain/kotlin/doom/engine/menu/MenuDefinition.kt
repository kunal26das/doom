
package doom.engine.menu

internal class MenuDefinition(
    var numitems: Int,
    var prevMenu: MenuDefinition?,
    var menuitems: Array<MenuItem>,
    var routine: (() -> Unit)?,
    var x: Int,
    var y: Int,
    var lastOn: Int,
)
