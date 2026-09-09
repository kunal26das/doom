// BSP traversal from DOOM. Original (C) id Software, GPL-2.0.
package doom.engine.geometry

import doom.engine.NF_SUBSECTOR
import doom.engine.fixed_t
import doom.engine.node_t
import doom.engine.subsector_t

/** Read-only lookup over explicit world geometry, independent of render and actor state. */
internal object BspQueries {
    fun subsectorAt(
        x: fixed_t, y: fixed_t, nodes: Array<node_t>, subsectors: Array<subsector_t>, nodeCount: Int = nodes.size,
    ): subsector_t {
        var node: node_t
        var side: Int
        var nodenum: Int

        // single subsector is a special case
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
