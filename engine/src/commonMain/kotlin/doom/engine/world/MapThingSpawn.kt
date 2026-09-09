
package doom.engine.world

internal class MapThingSpawn(
    var x: Int = 0,
    var y: Int = 0,
    var angle: Int = 0,
    var type: Int = 0,
    var options: Int = 0,
) {
    fun copyFrom(o: MapThingSpawn) {
        x = o.x; y = o.y; angle = o.angle; type = o.type; options = o.options
    }
}
