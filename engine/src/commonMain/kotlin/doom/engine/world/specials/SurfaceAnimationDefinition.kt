
package doom.engine.world.specials

internal class SurfaceAnimationDefinition {
    var istexture = 0
    var endname = ""
    var startname = ""
    var speed = 0

    constructor(istexture: Boolean, endname: String, startname: String, speed: Int) {
        this.istexture = if (istexture) 1 else 0
        this.endname = endname
        this.startname = startname
        this.speed = speed
    }

    constructor(istexture: Int) {
        this.istexture = istexture
    }
}
