
package doom.engine.gameplay.player

import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.NUMAMMO
import doom.engine.gameplay.NUMCARDS
import doom.engine.gameplay.NUMPOWERS
import doom.engine.gameplay.NUMWEAPONS
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.weapons.WeaponSprite
import doom.engine.gameplay.WP_FIST
import doom.engine.gameplay.WP_NOCHANGE
import doom.engine.geometry.FixedPoint
import doom.engine.input.TicCommand

internal class Player {
    var mo: Actor? = null
    var playerstate = PST_LIVE
    val cmd = TicCommand()

    var viewz: FixedPoint = 0
    var viewheight: FixedPoint = 0
    var deltaviewheight: FixedPoint = 0
    var bob: FixedPoint = 0

    var health = 0
    var armorpoints = 0
    var armortype = 0

    val powers = IntArray(NUMPOWERS)
    val cards = BooleanArray(NUMCARDS)
    var backpack = false

    val frags = IntArray(MAXPLAYERS)
    var readyweapon = WP_FIST

    var pendingweapon = WP_NOCHANGE

    val weaponowned = BooleanArray(NUMWEAPONS)
    val ammo = IntArray(NUMAMMO)
    val maxammo = IntArray(NUMAMMO)

    var attackdown = false
    var usedown = false

    var cheats = 0

    var refire = 0

    var killcount = 0
    var itemcount = 0
    var secretcount = 0

    var message: String? = null

    var damagecount = 0
    var bonuscount = 0

    var attacker: Actor? = null

    var extralight = 0

    var fixedcolormap = 0

    var colormap = 0

    val psprites = Array(NUMPSPRITES) { WeaponSprite() }

    var didsecret = false
}
