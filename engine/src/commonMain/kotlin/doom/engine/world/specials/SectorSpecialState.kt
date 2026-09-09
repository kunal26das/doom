
package doom.engine.world.specials

import doom.engine.world.MapLine

internal class SectorSpecialState {
    val buttonlist by lazy(LazyThreadSafetyMode.NONE) { Array(MAXBUTTONS) { SwitchButton() } }

    val animdefs by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        SurfaceAnimationDefinition(false, "NUKAGE3", "NUKAGE1", 8),
        SurfaceAnimationDefinition(false, "FWATER4", "FWATER1", 8),
        SurfaceAnimationDefinition(false, "SWATER4", "SWATER1", 8),
        SurfaceAnimationDefinition(false, "LAVA4", "LAVA1", 8),
        SurfaceAnimationDefinition(false, "BLOOD3", "BLOOD1", 8),

        SurfaceAnimationDefinition(false, "RROCK08", "RROCK05", 8),
        SurfaceAnimationDefinition(false, "SLIME04", "SLIME01", 8),
        SurfaceAnimationDefinition(false, "SLIME08", "SLIME05", 8),
        SurfaceAnimationDefinition(false, "SLIME12", "SLIME09", 8),

        SurfaceAnimationDefinition(true, "BLODGR4", "BLODGR1", 8),
        SurfaceAnimationDefinition(true, "SLADRIP3", "SLADRIP1", 8),

        SurfaceAnimationDefinition(true, "BLODRIP4", "BLODRIP1", 8),
        SurfaceAnimationDefinition(true, "FIREWALL", "FIREWALA", 8),
        SurfaceAnimationDefinition(true, "GSTFONT3", "GSTFONT1", 8),
        SurfaceAnimationDefinition(true, "FIRELAVA", "FIRELAV3", 8),
        SurfaceAnimationDefinition(true, "FIREMAG3", "FIREMAG1", 8),
        SurfaceAnimationDefinition(true, "FIREBLU2", "FIREBLU1", 8),
        SurfaceAnimationDefinition(true, "ROCKRED3", "ROCKRED1", 8),

        SurfaceAnimationDefinition(true, "BFALL4", "BFALL1", 8),
        SurfaceAnimationDefinition(true, "SFALL4", "SFALL1", 8),
        SurfaceAnimationDefinition(true, "WFALL4", "WFALL1", 8),
        SurfaceAnimationDefinition(true, "DBRAIN4", "DBRAIN1", 8),

        SurfaceAnimationDefinition(-1),
    ) }

    val anims by lazy(LazyThreadSafetyMode.NONE) { Array(MAXANIMS) { SurfaceAnimation() } }

    var lastanim = 0

    var levelTimer = false

    var levelTimeCount = 0

    var numlinespecials = 0

    val linespeciallist by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<MapLine>(MAXLINEANIMS) }
}
