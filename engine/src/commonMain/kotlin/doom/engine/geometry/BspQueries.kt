package doom.engine.geometry

import doom.engine.world.BspNode
import doom.engine.world.NF_SUBSECTOR
import doom.engine.world.Subsector

internal object BspQueries {
    fun subsectorAt(
        x: FixedPoint, y: FixedPoint, nodes: Array<BspNode>, subsectors: Array<Subsector>, nodeCount: Int = nodes.size,
    ): Subsector {
        var node: BspNode
        var side: Int
        var nodenum: Int

        if (nodeCount == 0)
            return subsectors[0]

        nodenum = nodeCount - 1

        while ((nodenum and NF_SUBSECTOR) == 0) {
            node = nodes[nodenum]
            side = FixedGeometry.sideOfNode(x, y, node)
            nodenum = node.children[side]
        }

        return subsectors[nodenum and NF_SUBSECTOR.inv()]
    }
}
