
package doom.engine.rendering.resources

internal class Texture {
    var name: String = ""
    var width = 0
    var height = 0

    var patchcount = 0
    var patches: Array<TexturePatch> = emptyArray()
}
