
package doom.engine.input


internal class TicCommand {
    var forwardmove = 0
    var sidemove = 0
    var angleturn = 0
    var consistancy = 0
    var chatchar = 0
    var buttons = 0

    fun copyFrom(other: TicCommand) {
        forwardmove = other.forwardmove
        sidemove = other.sidemove
        angleturn = other.angleturn
        consistancy = other.consistancy
        chatchar = other.chatchar
        buttons = other.buttons
    }
}
