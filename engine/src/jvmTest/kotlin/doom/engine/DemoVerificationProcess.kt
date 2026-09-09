package doom.engine

import java.io.File
import java.security.MessageDigest

/** Runs simulation checkpoints in an isolated JVM with an injected clock and no host storage. */
object DemoVerificationProcess {
    @JvmStatic
    fun main(args: Array<String>) {
        var clock = 0
        with(DoomEngineCore(DoomClock { clock })) {
            val wad = File(args[0]).readBytes()
            val demo = args[1]
            D_DoomMain(listOf(wad), listOf("-playdemo", demo))
            singletics = true
            nodrawers = true
            val digest = MessageDigest.getInstance("SHA-256")
            while (!quit.requested && gametic < 50_000) {
                clock++
                D_DoomStep()
                if (gametic % TICRATE == 0 || quit.requested) digest.checkpoint(this)
            }
            check(quit.requested) { "$demo did not reach its end marker after $gametic tics" }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            println("DEMO_RESULT $demo $gametic $hash")
        }
    }

    private fun MessageDigest.integer(value: Int) {
        update((value ushr 24).toByte())
        update((value ushr 16).toByte())
        update((value ushr 8).toByte())
        update(value.toByte())
    }

    /** Fixed-order, pointer-free simulation checkpoints, independent of render frequency. */
    internal fun MessageDigest.checkpoint(core: DoomEngineCore) = with(core) {
        integer(gametic)
        integer(leveltime)
        integer(gamestate)
        integer(gameepisode)
        integer(gamemap)
        integer(prndindex)
        for (index in players.indices) {
            integer(if (playeringame[index]) 1 else 0)
            if (!playeringame[index]) continue
            val player = players[index]
            integer(player.playerstate)
            integer(player.health)
            integer(player.armorpoints)
            integer(player.readyweapon)
            integer(player.pendingweapon)
            integer(player.killcount)
            integer(player.itemcount)
            integer(player.secretcount)
            player.ammo.forEach { integer(it) }
            player.powers.forEach { integer(it) }
            player.psprites.forEach { integer(it.state?.index ?: -1); integer(it.tics) }
        }
        val thinkers = ArrayList<thinker_t>()
        var thinker = thinkercap.next
        while (thinker != null && thinker !== thinkercap) {
            thinkers.add(thinker)
            thinker = thinker.next
        }
        integer(thinkers.size)
        for (entry in thinkers) {
            integer(if (entry.removed) 1 else 0)
            if (entry is mobj_t) {
                integer(entry.type)
                integer(entry.x); integer(entry.y); integer(entry.z)
                integer(entry.angle.toInt())
                integer(entry.momx); integer(entry.momy); integer(entry.momz)
                integer(entry.health); integer(entry.flags)
                integer(entry.state?.index ?: -1); integer(entry.tics)
                integer(entry.movedir); integer(entry.movecount)
                integer(entry.reactiontime); integer(entry.threshold)
                integer(thinkers.indexOfFirst { it === entry.target })
                integer(thinkers.indexOfFirst { it === entry.tracer })
            } else integer(-1)
        }
        for (sector in sectors) {
            integer(sector.floorheight); integer(sector.ceilingheight)
            integer(sector.floorpic); integer(sector.ceilingpic)
            integer(sector.lightlevel); integer(sector.special); integer(sector.tag)
        }
    }
}
