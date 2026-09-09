
package doom.engine.world.specials

internal class SwitchState {
    val alphSwitchList by lazy(LazyThreadSafetyMode.NONE) { arrayOf(
        SwitchDefinition("SW1BRCOM", "SW2BRCOM", 1),
        SwitchDefinition("SW1BRN1", "SW2BRN1", 1),
        SwitchDefinition("SW1BRN2", "SW2BRN2", 1),
        SwitchDefinition("SW1BRNGN", "SW2BRNGN", 1),
        SwitchDefinition("SW1BROWN", "SW2BROWN", 1),
        SwitchDefinition("SW1COMM", "SW2COMM", 1),
        SwitchDefinition("SW1COMP", "SW2COMP", 1),
        SwitchDefinition("SW1DIRT", "SW2DIRT", 1),
        SwitchDefinition("SW1EXIT", "SW2EXIT", 1),
        SwitchDefinition("SW1GRAY", "SW2GRAY", 1),
        SwitchDefinition("SW1GRAY1", "SW2GRAY1", 1),
        SwitchDefinition("SW1METAL", "SW2METAL", 1),
        SwitchDefinition("SW1PIPE", "SW2PIPE", 1),
        SwitchDefinition("SW1SLAD", "SW2SLAD", 1),
        SwitchDefinition("SW1STARG", "SW2STARG", 1),
        SwitchDefinition("SW1STON1", "SW2STON1", 1),
        SwitchDefinition("SW1STON2", "SW2STON2", 1),
        SwitchDefinition("SW1STONE", "SW2STONE", 1),
        SwitchDefinition("SW1STRTN", "SW2STRTN", 1),

        SwitchDefinition("SW1BLUE", "SW2BLUE", 2),
        SwitchDefinition("SW1CMT", "SW2CMT", 2),
        SwitchDefinition("SW1GARG", "SW2GARG", 2),
        SwitchDefinition("SW1GSTON", "SW2GSTON", 2),
        SwitchDefinition("SW1HOT", "SW2HOT", 2),
        SwitchDefinition("SW1LION", "SW2LION", 2),
        SwitchDefinition("SW1SATYR", "SW2SATYR", 2),
        SwitchDefinition("SW1SKIN", "SW2SKIN", 2),
        SwitchDefinition("SW1VINE", "SW2VINE", 2),
        SwitchDefinition("SW1WOOD", "SW2WOOD", 2),

        SwitchDefinition("SW1PANEL", "SW2PANEL", 3),
        SwitchDefinition("SW1ROCK", "SW2ROCK", 3),
        SwitchDefinition("SW1MET2", "SW2MET2", 3),
        SwitchDefinition("SW1WDMET", "SW2WDMET", 3),
        SwitchDefinition("SW1BRIK", "SW2BRIK", 3),
        SwitchDefinition("SW1MOD1", "SW2MOD1", 3),
        SwitchDefinition("SW1ZIM", "SW2ZIM", 3),
        SwitchDefinition("SW1STON6", "SW2STON6", 3),
        SwitchDefinition("SW1TEK", "SW2TEK", 3),
        SwitchDefinition("SW1MARB", "SW2MARB", 3),
        SwitchDefinition("SW1SKULL", "SW2SKULL", 3),

        SwitchDefinition("\u0000", "\u0000", 0),
    ) }

    val switchlist by lazy(LazyThreadSafetyMode.NONE) { IntArray(MAXSWITCHES * 2) }

    var numswitches = 0
}
