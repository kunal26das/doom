// Port of linuxdoom-1.10 p_saveg.c -- archiving: SaveGame I/O.
// Vanilla memcpy's whole structs into the save buffer; this port follows
// chocolate-doom's p_saveg.c and reads/writes every struct field-by-field
// (endian-safe, pointer-free) while keeping the vanilla savegame layout of
// records. `save_p` is an Int cursor into `savebuffer` (owned by g_game.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

var save_p = 0  // C: byte* save_p -> index into savebuffer


// Pads save_p to a 4-byte boundary
//  so that the load/save works on SGI&Gecko.
// (No-op in the port: the padding only mattered for in-memory pointer
//  alignment of the memcpy'd structs; read and write both skip it.)
fun PADSAVEP() {
}


// Placeholder installed when a savegame recorded a non-NULL thinker function;
// P_UnArchiveThinkers / P_UnArchiveSpecials replace it with the real dispatch.
// (C keeps the raw saved pointer around only to test it against NULL.)
private val saveg_restored_action: (thinker_t) -> Unit = { }


// Endian-safe integer read/write functions

private fun saveg_read8(): Int {
    val result = savebuffer.u8(save_p)
    save_p++
    return result
}

private fun saveg_write8(value: Int) {
    savebuffer[save_p] = value.toByte()
    save_p++
}

private fun saveg_read16(): Int {
    var result: Int

    result = saveg_read8()
    result = result or (saveg_read8() shl 8)

    return result.toShort().toInt()  // C helper returns short (sign-extends)
}

private fun saveg_write16(value: Int) {
    saveg_write8(value and 0xff)
    saveg_write8((value shr 8) and 0xff)
}

private fun saveg_read32(): Int {
    var result: Int

    result = saveg_read8()
    result = result or (saveg_read8() shl 8)
    result = result or (saveg_read8() shl 16)
    result = result or (saveg_read8() shl 24)

    return result
}

private fun saveg_write32(value: Int) {
    saveg_write8(value and 0xff)
    saveg_write8((value shr 8) and 0xff)
    saveg_write8((value shr 16) and 0xff)
    saveg_write8((value shr 24) and 0xff)
}

// Pointers
// Pointer values themselves cannot be restored; only NULL/non-NULL survives
// (all that the load path ever tests). Reads return the raw saved word.

private fun saveg_readp(): Int {
    return saveg_read32()
}

private fun saveg_writep(p: Any?) {
    saveg_write32(if (p != null) 1 else 0)
}

// Enum values are 32-bit integers.

private fun saveg_read_enum(): Int = saveg_read32()

private fun saveg_write_enum(value: Int) = saveg_write32(value)

//
// Structure read/write functions
//

//
// mapthing_t
//

private fun saveg_read_mapthing_t(str: mapthing_t) {
    // short x;
    str.x = saveg_read16()

    // short y;
    str.y = saveg_read16()

    // short angle;
    str.angle = saveg_read16()

    // short type;
    str.type = saveg_read16()

    // short options;
    str.options = saveg_read16()
}

private fun saveg_write_mapthing_t(str: mapthing_t?) {
    // (a NULL spawnpoint stands in for C's zeroed embedded struct)
    // short x;
    saveg_write16(str?.x ?: 0)

    // short y;
    saveg_write16(str?.y ?: 0)

    // short angle;
    saveg_write16(str?.angle ?: 0)

    // short type;
    saveg_write16(str?.type ?: 0)

    // short options;
    saveg_write16(str?.options ?: 0)
}

//
// actionf_t
//

private fun saveg_read_actionf_t(str: thinker_t) {
    // actionf_p1 acp1;
    str.function = if (saveg_readp() != 0) saveg_restored_action else null
}

private fun saveg_write_actionf_t(str: thinker_t) {
    // actionf_p1 acp1;
    saveg_writep(str.function)
}

//
// think_t
//
// This is just an actionf_t.
//

private fun saveg_read_think_t(str: thinker_t) = saveg_read_actionf_t(str)

private fun saveg_write_think_t(str: thinker_t) = saveg_write_actionf_t(str)

//
// thinker_t
//

private fun saveg_read_thinker_t(str: thinker_t) {
    // struct thinker_s* prev;
    saveg_readp()

    // struct thinker_s* next;
    saveg_readp()

    // think_t function;
    saveg_read_think_t(str)
}

private fun saveg_write_thinker_t(str: thinker_t) {
    // struct thinker_s* prev;
    saveg_writep(str.prev)

    // struct thinker_s* next;
    saveg_writep(str.next)

    // think_t function;
    saveg_write_think_t(str)
}

//
// mobj_t
//

private fun saveg_read_mobj_t(str: mobj_t) {
    val pl: Int

    // thinker_t thinker;
    saveg_read_thinker_t(str)

    // fixed_t x;
    str.x = saveg_read32()

    // fixed_t y;
    str.y = saveg_read32()

    // fixed_t z;
    str.z = saveg_read32()

    // struct mobj_s* snext;
    saveg_readp()

    // struct mobj_s* sprev;
    saveg_readp()

    // angle_t angle;
    str.angle = saveg_read32().toUInt()

    // spritenum_t sprite;
    str.sprite = saveg_read_enum()

    // int frame;
    str.frame = saveg_read32()

    // struct mobj_s* bnext;
    saveg_readp()

    // struct mobj_s* bprev;
    saveg_readp()

    // struct subsector_s* subsector;
    saveg_readp()

    // fixed_t floorz;
    str.floorz = saveg_read32()

    // fixed_t ceilingz;
    str.ceilingz = saveg_read32()

    // fixed_t radius;
    str.radius = saveg_read32()

    // fixed_t height;
    str.height = saveg_read32()

    // fixed_t momx;
    str.momx = saveg_read32()

    // fixed_t momy;
    str.momy = saveg_read32()

    // fixed_t momz;
    str.momz = saveg_read32()

    // int validcount;
    str.validcount = saveg_read32()

    // mobjtype_t type;
    str.type = saveg_read_enum()

    // mobjinfo_t* info;
    saveg_readp()

    // int tics;
    str.tics = saveg_read32()

    // state_t* state;
    str.state = states[saveg_read32()]

    // int flags;
    str.flags = saveg_read32()

    // int health;
    str.health = saveg_read32()

    // int movedir;
    str.movedir = saveg_read32()

    // int movecount;
    str.movecount = saveg_read32()

    // struct mobj_s* target;
    saveg_readp()

    // int reactiontime;
    str.reactiontime = saveg_read32()

    // int threshold;
    str.threshold = saveg_read32()

    // struct player_s* player;
    pl = saveg_read32()

    if (pl > 0) {
        str.player = players[pl - 1]
        str.player!!.mo = str
    } else {
        str.player = null
    }

    // int lastlook;
    str.lastlook = saveg_read32()

    // mapthing_t spawnpoint;
    // (embedded struct in C; allocate one so the zeroed default round-trips)
    str.spawnpoint = mapthing_t()
    saveg_read_mapthing_t(str.spawnpoint!!)

    // struct mobj_s* tracer;
    saveg_readp()
}

private fun saveg_write_mobj_t(str: mobj_t) {
    // thinker_t thinker;
    saveg_write_thinker_t(str)

    // fixed_t x;
    saveg_write32(str.x)

    // fixed_t y;
    saveg_write32(str.y)

    // fixed_t z;
    saveg_write32(str.z)

    // struct mobj_s* snext;
    saveg_writep(str.snext)

    // struct mobj_s* sprev;
    saveg_writep(str.sprev)

    // angle_t angle;
    saveg_write32(str.angle.toInt())

    // spritenum_t sprite;
    saveg_write_enum(str.sprite)

    // int frame;
    saveg_write32(str.frame)

    // struct mobj_s* bnext;
    saveg_writep(str.bnext)

    // struct mobj_s* bprev;
    saveg_writep(str.bprev)

    // struct subsector_s* subsector;
    saveg_writep(str.subsector)

    // fixed_t floorz;
    saveg_write32(str.floorz)

    // fixed_t ceilingz;
    saveg_write32(str.ceilingz)

    // fixed_t radius;
    saveg_write32(str.radius)

    // fixed_t height;
    saveg_write32(str.height)

    // fixed_t momx;
    saveg_write32(str.momx)

    // fixed_t momy;
    saveg_write32(str.momy)

    // fixed_t momz;
    saveg_write32(str.momz)

    // int validcount;
    saveg_write32(str.validcount)

    // mobjtype_t type;
    saveg_write_enum(str.type)

    // mobjinfo_t* info;
    saveg_writep(str.info)

    // int tics;
    saveg_write32(str.tics)

    // state_t* state;
    saveg_write32(str.state!!.index)  // (state - states)

    // int flags;
    saveg_write32(str.flags)

    // int health;
    saveg_write32(str.health)

    // int movedir;
    saveg_write32(str.movedir)

    // int movecount;
    saveg_write32(str.movecount)

    // struct mobj_s* target;
    saveg_writep(str.target)

    // int reactiontime;
    saveg_write32(str.reactiontime)

    // int threshold;
    saveg_write32(str.threshold)

    // struct player_s* player;
    if (str.player != null) {
        saveg_write32(players.indexOf(str.player!!) + 1)  // (player - players + 1)
    } else {
        saveg_write32(0)
    }

    // int lastlook;
    saveg_write32(str.lastlook)

    // mapthing_t spawnpoint;
    saveg_write_mapthing_t(str.spawnpoint)

    // struct mobj_s* tracer;
    saveg_writep(str.tracer)
}


//
// ticcmd_t
//

private fun saveg_read_ticcmd_t(str: ticcmd_t) {
    // signed char forwardmove;
    str.forwardmove = saveg_read8().toByte().toInt()

    // signed char sidemove;
    str.sidemove = saveg_read8().toByte().toInt()

    // short angleturn;
    str.angleturn = saveg_read16()

    // short consistancy;
    str.consistancy = saveg_read16()

    // byte chatchar;
    str.chatchar = saveg_read8()

    // byte buttons;
    str.buttons = saveg_read8()
}

private fun saveg_write_ticcmd_t(str: ticcmd_t) {
    // signed char forwardmove;
    saveg_write8(str.forwardmove)

    // signed char sidemove;
    saveg_write8(str.sidemove)

    // short angleturn;
    saveg_write16(str.angleturn)

    // short consistancy;
    saveg_write16(str.consistancy)

    // byte chatchar;
    saveg_write8(str.chatchar)

    // byte buttons;
    saveg_write8(str.buttons)
}

//
// pspdef_t
//

private fun saveg_read_pspdef_t(str: pspdef_t) {
    val state: Int

    // state_t* state;
    state = saveg_read32()

    if (state > 0) {
        str.state = states[state]
    } else {
        str.state = null
    }

    // int tics;
    str.tics = saveg_read32()

    // fixed_t sx;
    str.sx = saveg_read32()

    // fixed_t sy;
    str.sy = saveg_read32()
}

private fun saveg_write_pspdef_t(str: pspdef_t) {
    // state_t* state;
    if (str.state != null) {
        saveg_write32(str.state!!.index)  // (state - states)
    } else {
        saveg_write32(0)
    }

    // int tics;
    saveg_write32(str.tics)

    // fixed_t sx;
    saveg_write32(str.sx)

    // fixed_t sy;
    saveg_write32(str.sy)
}

//
// player_t
//

private fun saveg_read_player_t(str: player_t) {
    var i: Int

    // mobj_t* mo;
    saveg_readp()
    str.mo = null

    // playerstate_t playerstate;
    str.playerstate = saveg_read_enum()

    // ticcmd_t cmd;
    saveg_read_ticcmd_t(str.cmd)

    // fixed_t viewz;
    str.viewz = saveg_read32()

    // fixed_t viewheight;
    str.viewheight = saveg_read32()

    // fixed_t deltaviewheight;
    str.deltaviewheight = saveg_read32()

    // fixed_t bob;
    str.bob = saveg_read32()

    // int health;
    str.health = saveg_read32()

    // int armorpoints;
    str.armorpoints = saveg_read32()

    // int armortype;
    str.armortype = saveg_read32()

    // int powers[NUMPOWERS];
    i = 0
    while (i < NUMPOWERS) {
        str.powers[i] = saveg_read32()
        i++
    }

    // boolean cards[NUMCARDS];
    i = 0
    while (i < NUMCARDS) {
        str.cards[i] = saveg_read32() != 0
        i++
    }

    // boolean backpack;
    str.backpack = saveg_read32() != 0

    // int frags[MAXPLAYERS];
    i = 0
    while (i < MAXPLAYERS) {
        str.frags[i] = saveg_read32()
        i++
    }

    // weapontype_t readyweapon;
    str.readyweapon = saveg_read_enum()

    // weapontype_t pendingweapon;
    str.pendingweapon = saveg_read_enum()

    // boolean weaponowned[NUMWEAPONS];
    i = 0
    while (i < NUMWEAPONS) {
        str.weaponowned[i] = saveg_read32() != 0
        i++
    }

    // int ammo[NUMAMMO];
    i = 0
    while (i < NUMAMMO) {
        str.ammo[i] = saveg_read32()
        i++
    }

    // int maxammo[NUMAMMO];
    i = 0
    while (i < NUMAMMO) {
        str.maxammo[i] = saveg_read32()
        i++
    }

    // int attackdown;
    str.attackdown = saveg_read32() != 0

    // int usedown;
    str.usedown = saveg_read32() != 0

    // int cheats;
    str.cheats = saveg_read32()

    // int refire;
    str.refire = saveg_read32()

    // int killcount;
    str.killcount = saveg_read32()

    // int itemcount;
    str.itemcount = saveg_read32()

    // int secretcount;
    str.secretcount = saveg_read32()

    // char* message;
    saveg_readp()
    str.message = null

    // int damagecount;
    str.damagecount = saveg_read32()

    // int bonuscount;
    str.bonuscount = saveg_read32()

    // mobj_t* attacker;
    saveg_readp()
    str.attacker = null

    // int extralight;
    str.extralight = saveg_read32()

    // int fixedcolormap;
    str.fixedcolormap = saveg_read32()

    // int colormap;
    str.colormap = saveg_read32()

    // pspdef_t psprites[NUMPSPRITES];
    i = 0
    while (i < NUMPSPRITES) {
        saveg_read_pspdef_t(str.psprites[i])
        i++
    }

    // boolean didsecret;
    str.didsecret = saveg_read32() != 0
}

private fun saveg_write_player_t(str: player_t) {
    var i: Int

    // mobj_t* mo;
    saveg_writep(str.mo)

    // playerstate_t playerstate;
    saveg_write_enum(str.playerstate)

    // ticcmd_t cmd;
    saveg_write_ticcmd_t(str.cmd)

    // fixed_t viewz;
    saveg_write32(str.viewz)

    // fixed_t viewheight;
    saveg_write32(str.viewheight)

    // fixed_t deltaviewheight;
    saveg_write32(str.deltaviewheight)

    // fixed_t bob;
    saveg_write32(str.bob)

    // int health;
    saveg_write32(str.health)

    // int armorpoints;
    saveg_write32(str.armorpoints)

    // int armortype;
    saveg_write32(str.armortype)

    // int powers[NUMPOWERS];
    i = 0
    while (i < NUMPOWERS) {
        saveg_write32(str.powers[i])
        i++
    }

    // boolean cards[NUMCARDS];
    i = 0
    while (i < NUMCARDS) {
        saveg_write32(if (str.cards[i]) 1 else 0)
        i++
    }

    // boolean backpack;
    saveg_write32(if (str.backpack) 1 else 0)

    // int frags[MAXPLAYERS];
    i = 0
    while (i < MAXPLAYERS) {
        saveg_write32(str.frags[i])
        i++
    }

    // weapontype_t readyweapon;
    saveg_write_enum(str.readyweapon)

    // weapontype_t pendingweapon;
    saveg_write_enum(str.pendingweapon)

    // boolean weaponowned[NUMWEAPONS];
    i = 0
    while (i < NUMWEAPONS) {
        saveg_write32(if (str.weaponowned[i]) 1 else 0)
        i++
    }

    // int ammo[NUMAMMO];
    i = 0
    while (i < NUMAMMO) {
        saveg_write32(str.ammo[i])
        i++
    }

    // int maxammo[NUMAMMO];
    i = 0
    while (i < NUMAMMO) {
        saveg_write32(str.maxammo[i])
        i++
    }

    // int attackdown;
    saveg_write32(if (str.attackdown) 1 else 0)

    // int usedown;
    saveg_write32(if (str.usedown) 1 else 0)

    // int cheats;
    saveg_write32(str.cheats)

    // int refire;
    saveg_write32(str.refire)

    // int killcount;
    saveg_write32(str.killcount)

    // int itemcount;
    saveg_write32(str.itemcount)

    // int secretcount;
    saveg_write32(str.secretcount)

    // char* message;
    saveg_writep(str.message)

    // int damagecount;
    saveg_write32(str.damagecount)

    // int bonuscount;
    saveg_write32(str.bonuscount)

    // mobj_t* attacker;
    saveg_writep(str.attacker)

    // int extralight;
    saveg_write32(str.extralight)

    // int fixedcolormap;
    saveg_write32(str.fixedcolormap)

    // int colormap;
    saveg_write32(str.colormap)

    // pspdef_t psprites[NUMPSPRITES];
    i = 0
    while (i < NUMPSPRITES) {
        saveg_write_pspdef_t(str.psprites[i])
        i++
    }

    // boolean didsecret;
    saveg_write32(if (str.didsecret) 1 else 0)
}


//
// ceiling_t
//

private fun saveg_read_ceiling_t(str: ceiling_t) {
    val sector: Int

    // thinker_t thinker;
    saveg_read_thinker_t(str)

    // ceiling_e type;
    str.type = saveg_read_enum()

    // sector_t* sector;
    sector = saveg_read32()
    str.sector = sectors[sector]

    // fixed_t bottomheight;
    str.bottomheight = saveg_read32()

    // fixed_t topheight;
    str.topheight = saveg_read32()

    // fixed_t speed;
    str.speed = saveg_read32()

    // boolean crush;
    str.crush = saveg_read32() != 0

    // int direction;
    str.direction = saveg_read32()

    // int tag;
    str.tag = saveg_read32()

    // int olddirection;
    str.olddirection = saveg_read32()
}

private fun saveg_write_ceiling_t(str: ceiling_t) {
    // thinker_t thinker;
    saveg_write_thinker_t(str)

    // ceiling_e type;
    saveg_write_enum(str.type)

    // sector_t* sector;
    saveg_write32(str.sector!!.index)  // (sector - sectors)

    // fixed_t bottomheight;
    saveg_write32(str.bottomheight)

    // fixed_t topheight;
    saveg_write32(str.topheight)

    // fixed_t speed;
    saveg_write32(str.speed)

    // boolean crush;
    saveg_write32(if (str.crush) 1 else 0)

    // int direction;
    saveg_write32(str.direction)

    // int tag;
    saveg_write32(str.tag)

    // int olddirection;
    saveg_write32(str.olddirection)
}

//
// vldoor_t
//

private fun saveg_read_vldoor_t(str: vldoor_t) {
    val sector: Int

    // thinker_t thinker;
    saveg_read_thinker_t(str)

    // vldoor_e type;
    str.type = saveg_read_enum()

    // sector_t* sector;
    sector = saveg_read32()
    str.sector = sectors[sector]

    // fixed_t topheight;
    str.topheight = saveg_read32()

    // fixed_t speed;
    str.speed = saveg_read32()

    // int direction;
    str.direction = saveg_read32()

    // int topwait;
    str.topwait = saveg_read32()

    // int topcountdown;
    str.topcountdown = saveg_read32()
}

private fun saveg_write_vldoor_t(str: vldoor_t) {
    // thinker_t thinker;
    saveg_write_thinker_t(str)

    // vldoor_e type;
    saveg_write_enum(str.type)

    // sector_t* sector;
    saveg_write32(str.sector!!.index)  // (sector - sectors)

    // fixed_t topheight;
    saveg_write32(str.topheight)

    // fixed_t speed;
    saveg_write32(str.speed)

    // int direction;
    saveg_write32(str.direction)

    // int topwait;
    saveg_write32(str.topwait)

    // int topcountdown;
    saveg_write32(str.topcountdown)
}

//
// floormove_t
//

private fun saveg_read_floormove_t(str: floormove_t) {
    val sector: Int

    // thinker_t thinker;
    saveg_read_thinker_t(str)

    // floor_e type;
    str.type = saveg_read_enum()

    // boolean crush;
    str.crush = saveg_read32() != 0

    // sector_t* sector;
    sector = saveg_read32()
    str.sector = sectors[sector]

    // int direction;
    str.direction = saveg_read32()

    // int newspecial;
    str.newspecial = saveg_read32()

    // short texture;
    str.texture = saveg_read16()

    // fixed_t floordestheight;
    str.floordestheight = saveg_read32()

    // fixed_t speed;
    str.speed = saveg_read32()
}

private fun saveg_write_floormove_t(str: floormove_t) {
    // thinker_t thinker;
    saveg_write_thinker_t(str)

    // floor_e type;
    saveg_write_enum(str.type)

    // boolean crush;
    saveg_write32(if (str.crush) 1 else 0)

    // sector_t* sector;
    saveg_write32(str.sector!!.index)  // (sector - sectors)

    // int direction;
    saveg_write32(str.direction)

    // int newspecial;
    saveg_write32(str.newspecial)

    // short texture;
    saveg_write16(str.texture)

    // fixed_t floordestheight;
    saveg_write32(str.floordestheight)

    // fixed_t speed;
    saveg_write32(str.speed)
}

//
// plat_t
//

private fun saveg_read_plat_t(str: plat_t) {
    val sector: Int

    // thinker_t thinker;
    saveg_read_thinker_t(str)

    // sector_t* sector;
    sector = saveg_read32()
    str.sector = sectors[sector]

    // fixed_t speed;
    str.speed = saveg_read32()

    // fixed_t low;
    str.low = saveg_read32()

    // fixed_t high;
    str.high = saveg_read32()

    // int wait;
    str.wait = saveg_read32()

    // int count;
    str.count = saveg_read32()

    // plat_e status;
    str.status = saveg_read_enum()

    // plat_e oldstatus;
    str.oldstatus = saveg_read_enum()

    // boolean crush;
    str.crush = saveg_read32() != 0

    // int tag;
    str.tag = saveg_read32()

    // plattype_e type;
    str.type = saveg_read_enum()
}

private fun saveg_write_plat_t(str: plat_t) {
    // thinker_t thinker;
    saveg_write_thinker_t(str)

    // sector_t* sector;
    saveg_write32(str.sector!!.index)  // (sector - sectors)

    // fixed_t speed;
    saveg_write32(str.speed)

    // fixed_t low;
    saveg_write32(str.low)

    // fixed_t high;
    saveg_write32(str.high)

    // int wait;
    saveg_write32(str.wait)

    // int count;
    saveg_write32(str.count)

    // plat_e status;
    saveg_write_enum(str.status)

    // plat_e oldstatus;
    saveg_write_enum(str.oldstatus)

    // boolean crush;
    saveg_write32(if (str.crush) 1 else 0)

    // int tag;
    saveg_write32(str.tag)

    // plattype_e type;
    saveg_write_enum(str.type)
}

//
// lightflash_t
//

private fun saveg_read_lightflash_t(str: lightflash_t) {
    val sector: Int

    // thinker_t thinker;
    saveg_read_thinker_t(str)

    // sector_t* sector;
    sector = saveg_read32()
    str.sector = sectors[sector]

    // int count;
    str.count = saveg_read32()

    // int maxlight;
    str.maxlight = saveg_read32()

    // int minlight;
    str.minlight = saveg_read32()

    // int maxtime;
    str.maxtime = saveg_read32()

    // int mintime;
    str.mintime = saveg_read32()
}

private fun saveg_write_lightflash_t(str: lightflash_t) {
    // thinker_t thinker;
    saveg_write_thinker_t(str)

    // sector_t* sector;
    saveg_write32(str.sector!!.index)  // (sector - sectors)

    // int count;
    saveg_write32(str.count)

    // int maxlight;
    saveg_write32(str.maxlight)

    // int minlight;
    saveg_write32(str.minlight)

    // int maxtime;
    saveg_write32(str.maxtime)

    // int mintime;
    saveg_write32(str.mintime)
}

//
// strobe_t
//

private fun saveg_read_strobe_t(str: strobe_t) {
    val sector: Int

    // thinker_t thinker;
    saveg_read_thinker_t(str)

    // sector_t* sector;
    sector = saveg_read32()
    str.sector = sectors[sector]

    // int count;
    str.count = saveg_read32()

    // int minlight;
    str.minlight = saveg_read32()

    // int maxlight;
    str.maxlight = saveg_read32()

    // int darktime;
    str.darktime = saveg_read32()

    // int brighttime;
    str.brighttime = saveg_read32()
}

private fun saveg_write_strobe_t(str: strobe_t) {
    // thinker_t thinker;
    saveg_write_thinker_t(str)

    // sector_t* sector;
    saveg_write32(str.sector!!.index)  // (sector - sectors)

    // int count;
    saveg_write32(str.count)

    // int minlight;
    saveg_write32(str.minlight)

    // int maxlight;
    saveg_write32(str.maxlight)

    // int darktime;
    saveg_write32(str.darktime)

    // int brighttime;
    saveg_write32(str.brighttime)
}

//
// glow_t
//

private fun saveg_read_glow_t(str: glow_t) {
    val sector: Int

    // thinker_t thinker;
    saveg_read_thinker_t(str)

    // sector_t* sector;
    sector = saveg_read32()
    str.sector = sectors[sector]

    // int minlight;
    str.minlight = saveg_read32()

    // int maxlight;
    str.maxlight = saveg_read32()

    // int direction;
    str.direction = saveg_read32()
}

private fun saveg_write_glow_t(str: glow_t) {
    // thinker_t thinker;
    saveg_write_thinker_t(str)

    // sector_t* sector;
    saveg_write32(str.sector!!.index)  // (sector - sectors)

    // int minlight;
    saveg_write32(str.minlight)

    // int maxlight;
    saveg_write32(str.maxlight)

    // int direction;
    saveg_write32(str.direction)
}


//
// P_ArchivePlayers
//
fun P_ArchivePlayers() {
    var i: Int

    i = 0
    while (i < MAXPLAYERS) {
        if (!playeringame[i]) {
            i++
            continue
        }

        PADSAVEP()

        saveg_write_player_t(players[i])
        i++
    }
}


//
// P_UnArchivePlayers
//
fun P_UnArchivePlayers() {
    var i: Int

    i = 0
    while (i < MAXPLAYERS) {
        if (!playeringame[i]) {
            i++
            continue
        }

        PADSAVEP()

        saveg_read_player_t(players[i])

        // will be set when unarc thinker
        players[i].mo = null
        players[i].message = null
        players[i].attacker = null
        i++
    }
}


//
// P_ArchiveWorld
//
fun P_ArchiveWorld() {
    var i: Int
    var j: Int
    var si: side_t

    // do sectors
    i = 0
    while (i < numsectors) {
        val sec = sectors[i]
        saveg_write16(sec.floorheight shr FRACBITS)
        saveg_write16(sec.ceilingheight shr FRACBITS)
        saveg_write16(sec.floorpic)
        saveg_write16(sec.ceilingpic)
        saveg_write16(sec.lightlevel)
        saveg_write16(sec.special)      // needed?
        saveg_write16(sec.tag)          // needed?
        i++
    }

    // do lines
    i = 0
    while (i < numlines) {
        val li = lines[i]
        saveg_write16(li.flags)
        saveg_write16(li.special)
        saveg_write16(li.tag)
        j = 0
        while (j < 2) {
            if (li.sidenum[j] == -1) {
                j++
                continue
            }

            si = sides[li.sidenum[j]]

            saveg_write16(si.textureoffset shr FRACBITS)
            saveg_write16(si.rowoffset shr FRACBITS)
            saveg_write16(si.toptexture)
            saveg_write16(si.bottomtexture)
            saveg_write16(si.midtexture)
            j++
        }
        i++
    }
}


//
// P_UnArchiveWorld
//
fun P_UnArchiveWorld() {
    var i: Int
    var j: Int
    var si: side_t

    // do sectors
    i = 0
    while (i < numsectors) {
        val sec = sectors[i]
        sec.floorheight = saveg_read16() shl FRACBITS
        sec.ceilingheight = saveg_read16() shl FRACBITS
        sec.floorpic = saveg_read16()
        sec.ceilingpic = saveg_read16()
        sec.lightlevel = saveg_read16()
        sec.special = saveg_read16()    // needed?
        sec.tag = saveg_read16()        // needed?
        sec.specialdata = null
        sec.soundtarget = null
        i++
    }

    // do lines
    i = 0
    while (i < numlines) {
        val li = lines[i]
        li.flags = saveg_read16()
        li.special = saveg_read16()
        li.tag = saveg_read16()
        j = 0
        while (j < 2) {
            if (li.sidenum[j] == -1) {
                j++
                continue
            }
            si = sides[li.sidenum[j]]
            si.textureoffset = saveg_read16() shl FRACBITS
            si.rowoffset = saveg_read16() shl FRACBITS
            si.toptexture = saveg_read16()
            si.bottomtexture = saveg_read16()
            si.midtexture = saveg_read16()
            j++
        }
        i++
    }
}


//
// Thinkers
//
// thinkerclass_t
private const val tc_end = 0
private const val tc_mobj = 1


//
// P_ArchiveThinkers
//
fun P_ArchiveThinkers() {
    var th: thinker_t

    // save off the current thinkers
    th = thinkercap.next!!
    while (th !== thinkercap) {
        // (C: function.acp1 == P_MobjThinker; a removed thinker's function is
        //  the -1 sentinel, so it never matches -- hence the !removed check.)
        if (th is mobj_t && !th.removed) {
            saveg_write8(tc_mobj)
            PADSAVEP()
            saveg_write_mobj_t(th)

            th = th.next!!
            continue
        }

        // I_Error ("P_ArchiveThinkers: Unknown thinker function");
        th = th.next!!
    }

    // add a terminating marker
    saveg_write8(tc_end)
}


//
// P_UnArchiveThinkers
//
fun P_UnArchiveThinkers() {
    var tclass: Int
    var currentthinker: thinker_t
    var next: thinker_t
    var mobj: mobj_t

    // remove all the current thinkers
    currentthinker = thinkercap.next!!
    while (currentthinker !== thinkercap) {
        next = currentthinker.next!!

        if (currentthinker is mobj_t && !currentthinker.removed)
            P_RemoveMobj(currentthinker)
        // else Z_Free (currentthinker);  -- garbage collected

        currentthinker = next
    }
    P_InitThinkers()

    // read in saved thinkers
    while (true) {
        tclass = saveg_read8()
        when (tclass) {
            tc_end ->
                return  // end of list

            tc_mobj -> {
                PADSAVEP()
                mobj = mobj_t()  // Z_Malloc + memcpy
                saveg_read_mobj_t(mobj)

                mobj.target = null
                mobj.tracer = null
                P_SetThingPosition(mobj)
                mobj.info = mobjinfo[mobj.type]
                mobj.floorz = mobj.subsector!!.sector!!.floorheight
                mobj.ceilingz = mobj.subsector!!.sector!!.ceilingheight
                mobj.function = { th -> P_MobjThinker(th as mobj_t) }
                P_AddThinker(mobj)
            }

            else ->
                I_Error("Unknown tclass $tclass in savegame")
        }
    }
}


//
// P_ArchiveSpecials
//
// specials_e
private const val tc_ceiling = 0
private const val tc_door = 1
private const val tc_floor = 2
private const val tc_plat = 3
private const val tc_flash = 4
private const val tc_strobe = 5
private const val tc_glow = 6
private const val tc_endspecials = 7


//
// Things to handle:
//
// T_MoveCeiling, (ceiling_t: sector_t * swizzle), - active list
// T_VerticalDoor, (vldoor_t: sector_t * swizzle),
// T_MoveFloor, (floormove_t: sector_t * swizzle),
// T_LightFlash, (lightflash_t: sector_t * swizzle),
// T_StrobeFlash, (strobe_t: sector_t *),
// T_Glow, (glow_t: sector_t *),
// T_PlatRaise, (plat_t: sector_t *), - active list
//
fun P_ArchiveSpecials() {
    var th: thinker_t
    var i: Int

    // save off the current thinkers
    th = thinkercap.next!!
    while (th !== thinkercap) {
        // (C: a removed thinker's function is the -1 sentinel; it matches
        //  neither the NULL check nor any T_* check, so nothing is archived.)
        if (th.removed) {
            th = th.next!!
            continue
        }

        if (th.function == null) {
            // ceilings in stasis are still in the active list.
            // (plats in stasis fall through unarchived -- vanilla bug.)
            i = 0
            while (i < MAXCEILINGS) {
                if (activeceilings[i] === th)
                    break
                i++
            }

            if (i < MAXCEILINGS) {
                saveg_write8(tc_ceiling)
                PADSAVEP()
                saveg_write_ceiling_t(th as ceiling_t)
            }
            th = th.next!!
            continue
        }

        if (th is ceiling_t) {  // T_MoveCeiling
            saveg_write8(tc_ceiling)
            PADSAVEP()
            saveg_write_ceiling_t(th)
            th = th.next!!
            continue
        }

        if (th is vldoor_t) {  // T_VerticalDoor
            saveg_write8(tc_door)
            PADSAVEP()
            saveg_write_vldoor_t(th)
            th = th.next!!
            continue
        }

        if (th is floormove_t) {  // T_MoveFloor
            saveg_write8(tc_floor)
            PADSAVEP()
            saveg_write_floormove_t(th)
            th = th.next!!
            continue
        }

        if (th is plat_t) {  // T_PlatRaise
            saveg_write8(tc_plat)
            PADSAVEP()
            saveg_write_plat_t(th)
            th = th.next!!
            continue
        }

        if (th is lightflash_t) {  // T_LightFlash
            saveg_write8(tc_flash)
            PADSAVEP()
            saveg_write_lightflash_t(th)
            th = th.next!!
            continue
        }

        if (th is strobe_t) {  // T_StrobeFlash
            saveg_write8(tc_strobe)
            PADSAVEP()
            saveg_write_strobe_t(th)
            th = th.next!!
            continue
        }

        if (th is glow_t) {  // T_Glow
            saveg_write8(tc_glow)
            PADSAVEP()
            saveg_write_glow_t(th)
            th = th.next!!
            continue
        }

        th = th.next!!
    }

    // add a terminating marker
    saveg_write8(tc_endspecials)
}


//
// P_UnArchiveSpecials
//
fun P_UnArchiveSpecials() {
    var tclass: Int
    var ceiling: ceiling_t
    var door: vldoor_t
    var floor: floormove_t
    var plat: plat_t
    var flash: lightflash_t
    var strobe: strobe_t
    var glow: glow_t

    // read in saved thinkers
    while (true) {
        tclass = saveg_read8()

        when (tclass) {
            tc_endspecials ->
                return  // end of list

            tc_ceiling -> {
                PADSAVEP()
                ceiling = ceiling_t()  // Z_Malloc + memcpy
                saveg_read_ceiling_t(ceiling)
                ceiling.sector!!.specialdata = ceiling

                if (ceiling.function != null)
                    ceiling.function = { th -> T_MoveCeiling(th as ceiling_t) }

                P_AddThinker(ceiling)
                P_AddActiveCeiling(ceiling)
            }

            tc_door -> {
                PADSAVEP()
                door = vldoor_t()  // Z_Malloc + memcpy
                saveg_read_vldoor_t(door)
                door.sector!!.specialdata = door
                door.function = { th -> T_VerticalDoor(th as vldoor_t) }
                P_AddThinker(door)
            }

            tc_floor -> {
                PADSAVEP()
                floor = floormove_t()  // Z_Malloc + memcpy
                saveg_read_floormove_t(floor)
                floor.sector!!.specialdata = floor
                floor.function = { th -> T_MoveFloor(th as floormove_t) }
                P_AddThinker(floor)
            }

            tc_plat -> {
                PADSAVEP()
                plat = plat_t()  // Z_Malloc + memcpy
                saveg_read_plat_t(plat)
                plat.sector!!.specialdata = plat

                if (plat.function != null)
                    plat.function = { th -> T_PlatRaise(th as plat_t) }

                P_AddThinker(plat)
                P_AddActivePlat(plat)
            }

            tc_flash -> {
                PADSAVEP()
                flash = lightflash_t()  // Z_Malloc + memcpy
                saveg_read_lightflash_t(flash)
                flash.function = { th -> T_LightFlash(th as lightflash_t) }
                P_AddThinker(flash)
            }

            tc_strobe -> {
                PADSAVEP()
                strobe = strobe_t()  // Z_Malloc + memcpy
                saveg_read_strobe_t(strobe)
                strobe.function = { th -> T_StrobeFlash(th as strobe_t) }
                P_AddThinker(strobe)
            }

            tc_glow -> {
                PADSAVEP()
                glow = glow_t()  // Z_Malloc + memcpy
                saveg_read_glow_t(glow)
                glow.function = { th -> T_Glow(th as glow_t) }
                P_AddThinker(glow)
            }

            else ->
                I_Error("P_UnarchiveSpecials:Unknown tclass $tclass in savegame")
        }
    }
}
