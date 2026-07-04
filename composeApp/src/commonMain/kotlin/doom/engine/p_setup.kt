// Port of linuxdoom-1.10 p_setup.c -- do all the WAD I/O, get map description,
// set up initial state and misc. LUTs.
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

//
// MAP related Lookup tables.
// Store VERTEXES, LINEDEFS, SIDEDEFS, etc.
//
var numvertexes = 0
var vertexes: Array<vertex_t> = emptyArray()

var numsegs = 0
var segs: Array<seg_t> = emptyArray()

var numsectors = 0
var sectors: Array<sector_t> = emptyArray()

var numsubsectors = 0
var subsectors: Array<subsector_t> = emptyArray()

var numnodes = 0
var nodes: Array<node_t> = emptyArray()

var numlines = 0
var lines: Array<line_t> = emptyArray()

var numsides = 0
var sides: Array<side_t> = emptyArray()


// BLOCKMAP
// Created from axis aligned bounding box
// of the map, a rectangular array of
// blocks of size ...
// Used to speed up collision detection
// by spatial subdivision in 2D.
//
// Blockmap size.
var bmapwidth = 0
var bmapheight = 0          // size in mapblocks
// offsets in blockmap are from here: C `blockmap = blockmaplump+4` becomes a
// base index into blockmaplump.
var blockmap = 0
// the raw shorts of the BLOCKMAP lump, byte-swap applied (signed, like vanilla)
var blockmaplump: IntArray = IntArray(0)
// origin of block map
var bmaporgx: fixed_t = 0
var bmaporgy: fixed_t = 0
// for thing chains
var blocklinks: Array<mobj_t?> = emptyArray()


// REJECT
// For fast sight rejection.
// Speeds up enemy AI by skipping detailed
//  LineOf Sight calculation.
// Without special effect, this could be
//  used as a PVS lookup as well.
//
var rejectmatrix: ByteArray = ByteArray(0)


// Maintain single and multi player starting spots.
const val MAX_DEATHMATCH_STARTS = 10

val deathmatchstarts = Array(MAX_DEATHMATCH_STARTS) { mapthing_t() }
var deathmatch_p = 0  // C: mapthing_t* deathmatch_p -> index into deathmatchstarts
val playerstarts = Array(MAXPLAYERS) { mapthing_t() }


//
// P_LoadVertexes
//
fun P_LoadVertexes(lump: Int) {
    val data: ByteArray
    var i: Int

    // Determine number of lumps:
    //  total lump length / vertex record length.
    numvertexes = W_LumpLength(lump) / 4  // sizeof(mapvertex_t)

    // Allocate zone memory for buffer.
    vertexes = Array(numvertexes) { vertex_t() }

    // Load data into cache.
    data = W_CacheLumpNum(lump)

    // Copy and convert vertex coordinates,
    // internal representation as fixed.
    i = 0
    while (i < numvertexes) {
        val li = vertexes[i]
        val ml = i * 4
        li.x = data.i16(ml + 0) shl FRACBITS
        li.y = data.i16(ml + 2) shl FRACBITS
        i++
    }
}


//
// P_LoadSegs
//
fun P_LoadSegs(lump: Int) {
    val data: ByteArray
    var i: Int
    var ldef: line_t
    var linedef: Int
    var side: Int

    numsegs = W_LumpLength(lump) / 12  // sizeof(mapseg_t)
    segs = Array(numsegs) { seg_t() }  // Z_Malloc + memset 0
    data = W_CacheLumpNum(lump)

    i = 0
    while (i < numsegs) {
        val li = segs[i]
        val ml = i * 12
        li.v1 = vertexes[data.i16(ml + 0)]
        li.v2 = vertexes[data.i16(ml + 2)]

        li.angle = (data.i16(ml + 4) shl 16).toUInt()
        li.offset = data.i16(ml + 10) shl 16
        linedef = data.i16(ml + 6)
        ldef = lines[linedef]
        li.linedef = ldef
        side = data.i16(ml + 8)
        li.sidedef = sides[ldef.sidenum[side]]
        li.frontsector = sides[ldef.sidenum[side]].sector
        if ((ldef.flags and ML_TWOSIDED) != 0)
            li.backsector = sides[ldef.sidenum[side xor 1]].sector
        else
            li.backsector = null
        li.index = i
        i++
    }
}


//
// P_LoadSubsectors
//
fun P_LoadSubsectors(lump: Int) {
    val data: ByteArray
    var i: Int

    numsubsectors = W_LumpLength(lump) / 4  // sizeof(mapsubsector_t)
    subsectors = Array(numsubsectors) { subsector_t() }  // Z_Malloc + memset 0
    data = W_CacheLumpNum(lump)

    i = 0
    while (i < numsubsectors) {
        val ss = subsectors[i]
        val ms = i * 4
        ss.numlines = data.i16(ms + 0)
        ss.firstline = data.i16(ms + 2)
        ss.index = i
        i++
    }
}


//
// P_LoadSectors
//
fun P_LoadSectors(lump: Int) {
    val data: ByteArray
    var i: Int

    numsectors = W_LumpLength(lump) / 26  // sizeof(mapsector_t)
    sectors = Array(numsectors) { sector_t() }  // Z_Malloc + memset 0
    data = W_CacheLumpNum(lump)

    i = 0
    while (i < numsectors) {
        val ss = sectors[i]
        val ms = i * 26
        ss.floorheight = data.i16(ms + 0) shl FRACBITS
        ss.ceilingheight = data.i16(ms + 2) shl FRACBITS
        ss.floorpic = R_FlatNumForName(data.str(ms + 4, 8))
        ss.ceilingpic = R_FlatNumForName(data.str(ms + 12, 8))
        ss.lightlevel = data.i16(ms + 20)
        ss.special = data.i16(ms + 22)
        ss.tag = data.i16(ms + 24)
        ss.thinglist = null
        ss.index = i
        i++
    }
}


//
// P_LoadNodes
//
fun P_LoadNodes(lump: Int) {
    val data: ByteArray
    var i: Int
    var j: Int
    var k: Int

    numnodes = W_LumpLength(lump) / 28  // sizeof(mapnode_t)
    nodes = Array(numnodes) { node_t() }
    data = W_CacheLumpNum(lump)

    i = 0
    while (i < numnodes) {
        val no = nodes[i]
        val mn = i * 28
        no.x = data.i16(mn + 0) shl FRACBITS
        no.y = data.i16(mn + 2) shl FRACBITS
        no.dx = data.i16(mn + 4) shl FRACBITS
        no.dy = data.i16(mn + 6) shl FRACBITS
        j = 0
        while (j < 2) {
            // C stores into `unsigned short children[2]`, so the value is
            // effectively an unsigned 16-bit read (NF_SUBSECTOR flag intact).
            no.children[j] = data.u16(mn + 24 + j * 2)
            k = 0
            while (k < 4) {
                no.bbox[j][k] = data.i16(mn + 8 + (j * 4 + k) * 2) shl FRACBITS
                k++
            }
            j++
        }
        i++
    }
}


//
// P_LoadThings
//
fun P_LoadThings(lump: Int) {
    val data: ByteArray
    var i: Int
    val numthings: Int
    var spawn: Boolean

    data = W_CacheLumpNum(lump)
    numthings = W_LumpLength(lump) / 10  // sizeof(mapthing_t)

    i = 0
    while (i < numthings) {
        val mtOfs = i * 10
        spawn = true

        // Do not spawn cool, new monsters if !commercial
        if (gamemode != commercial) {
            when (data.i16(mtOfs + 6)) {
                68,   // Arachnotron
                64,   // Archvile
                88,   // Boss Brain
                89,   // Boss Shooter
                69,   // Hell Knight
                67,   // Mancubus
                71,   // Pain Elemental
                65,   // Former Human Commando
                66,   // Revenant
                84 -> // Wolf SS
                    spawn = false
            }
        }
        if (spawn == false)
            break  // (vanilla: break, not continue)

        // Do spawn all other stuff.
        val mt = mapthing_t(
            x = data.i16(mtOfs + 0),
            y = data.i16(mtOfs + 2),
            angle = data.i16(mtOfs + 4),
            type = data.i16(mtOfs + 6),
            options = data.i16(mtOfs + 8),
        )

        P_SpawnMapThing(mt)
        i++
    }
}


//
// P_LoadLineDefs
// Also counts secret lines for intermissions.
//
fun P_LoadLineDefs(lump: Int) {
    val data: ByteArray
    var i: Int
    var v1: vertex_t
    var v2: vertex_t

    numlines = W_LumpLength(lump) / 14  // sizeof(maplinedef_t)
    lines = Array(numlines) { line_t() }  // Z_Malloc + memset 0
    data = W_CacheLumpNum(lump)

    i = 0
    while (i < numlines) {
        val ld = lines[i]
        val mld = i * 14
        ld.flags = data.i16(mld + 4)
        ld.special = data.i16(mld + 6)
        ld.tag = data.i16(mld + 8)
        v1 = vertexes[data.i16(mld + 0)]
        ld.v1 = v1
        v2 = vertexes[data.i16(mld + 2)]
        ld.v2 = v2
        ld.dx = v2.x - v1.x
        ld.dy = v2.y - v1.y

        if (ld.dx == 0)
            ld.slopetype = ST_VERTICAL
        else if (ld.dy == 0)
            ld.slopetype = ST_HORIZONTAL
        else {
            if (FixedDiv(ld.dy, ld.dx) > 0)
                ld.slopetype = ST_POSITIVE
            else
                ld.slopetype = ST_NEGATIVE
        }

        if (v1.x < v2.x) {
            ld.bbox[BOXLEFT] = v1.x
            ld.bbox[BOXRIGHT] = v2.x
        } else {
            ld.bbox[BOXLEFT] = v2.x
            ld.bbox[BOXRIGHT] = v1.x
        }

        if (v1.y < v2.y) {
            ld.bbox[BOXBOTTOM] = v1.y
            ld.bbox[BOXTOP] = v2.y
        } else {
            ld.bbox[BOXBOTTOM] = v2.y
            ld.bbox[BOXTOP] = v1.y
        }

        ld.sidenum[0] = data.i16(mld + 10)
        ld.sidenum[1] = data.i16(mld + 12)

        if (ld.sidenum[0] != -1)
            ld.frontsector = sides[ld.sidenum[0]].sector
        else
            ld.frontsector = null

        if (ld.sidenum[1] != -1)
            ld.backsector = sides[ld.sidenum[1]].sector
        else
            ld.backsector = null

        ld.index = i
        i++
    }
}


//
// P_LoadSideDefs
//
fun P_LoadSideDefs(lump: Int) {
    val data: ByteArray
    var i: Int

    numsides = W_LumpLength(lump) / 30  // sizeof(mapsidedef_t)
    sides = Array(numsides) { side_t() }  // Z_Malloc + memset 0
    data = W_CacheLumpNum(lump)

    i = 0
    while (i < numsides) {
        val sd = sides[i]
        val msd = i * 30
        sd.textureoffset = data.i16(msd + 0) shl FRACBITS
        sd.rowoffset = data.i16(msd + 2) shl FRACBITS
        sd.toptexture = R_TextureNumForName(data.str(msd + 4, 8))
        sd.bottomtexture = R_TextureNumForName(data.str(msd + 12, 8))
        sd.midtexture = R_TextureNumForName(data.str(msd + 20, 8))
        sd.sector = sectors[data.i16(msd + 28)]
        sd.index = i
        i++
    }
}


//
// P_LoadBlockMap
//
fun P_LoadBlockMap(lump: Int) {
    var i: Int
    var count: Int

    val data = W_CacheLumpNum(lump)
    count = W_LumpLength(lump) / 2
    blockmaplump = IntArray(count)
    blockmap = 4  // C: blockmap = blockmaplump+4

    i = 0
    while (i < count) {
        blockmaplump[i] = data.i16(i * 2)  // SHORT() byte swap (signed, vanilla)
        i++
    }

    bmaporgx = blockmaplump[0] shl FRACBITS
    bmaporgy = blockmaplump[1] shl FRACBITS
    bmapwidth = blockmaplump[2]
    bmapheight = blockmaplump[3]

    // clear out mobj chains
    count = bmapwidth * bmapheight
    blocklinks = arrayOfNulls(count)  // Z_Malloc + memset 0
}


//
// P_GroupLines
// Builds sector line lists and subsector sector numbers.
// Finds block bounding boxes for sectors.
//
fun P_GroupLines() {
    var linebuffer: Int
    var i: Int
    var j: Int
    var total: Int
    val bbox = IntArray(4)
    var block: Int

    // look up sector number for each subsector
    i = 0
    while (i < numsubsectors) {
        val ss = subsectors[i]
        val seg = segs[ss.firstline]
        ss.sector = seg.sidedef!!.sector
        i++
    }

    // count number of lines in each sector
    total = 0
    i = 0
    while (i < numlines) {
        val li = lines[i]
        total++
        li.frontsector!!.linecount++

        if (li.backsector != null && li.backsector !== li.frontsector) {
            li.backsector!!.linecount++
            total++
        }
        i++
    }

    // build line tables for each sector
    // (C allocates one shared linebuffer of `total` line_t*; here each sector
    // gets its own array of the size it counted.)
    i = 0
    while (i < numsectors) {
        val sector = sectors[i]
        M_ClearBox(bbox)
        sector.lines = arrayOfNulls(sector.linecount)
        linebuffer = 0
        j = 0
        while (j < numlines) {
            val li = lines[j]
            if (li.frontsector === sector || li.backsector === sector) {
                sector.lines[linebuffer] = li
                linebuffer++
                M_AddToBox(bbox, li.v1.x, li.v1.y)
                M_AddToBox(bbox, li.v2.x, li.v2.y)
            }
            j++
        }
        if (linebuffer != sector.linecount)
            I_Error("P_GroupLines: miscounted")

        // set the degenmobj_t to the middle of the bounding box
        sector.soundorg.x = (bbox[BOXRIGHT] + bbox[BOXLEFT]) / 2
        sector.soundorg.y = (bbox[BOXTOP] + bbox[BOXBOTTOM]) / 2

        // adjust bounding box to map blocks
        block = (bbox[BOXTOP] - bmaporgy + MAXRADIUS) shr MAPBLOCKSHIFT
        block = if (block >= bmapheight) bmapheight - 1 else block
        sector.blockbox[BOXTOP] = block

        block = (bbox[BOXBOTTOM] - bmaporgy - MAXRADIUS) shr MAPBLOCKSHIFT
        block = if (block < 0) 0 else block
        sector.blockbox[BOXBOTTOM] = block

        block = (bbox[BOXRIGHT] - bmaporgx + MAXRADIUS) shr MAPBLOCKSHIFT
        block = if (block >= bmapwidth) bmapwidth - 1 else block
        sector.blockbox[BOXRIGHT] = block

        block = (bbox[BOXLEFT] - bmaporgx - MAXRADIUS) shr MAPBLOCKSHIFT
        block = if (block < 0) 0 else block
        sector.blockbox[BOXLEFT] = block

        i++
    }
}


//
// P_SetupLevel
//
fun P_SetupLevel(episode: Int, map: Int, playermask: Int, skill: Int) {
    var i: Int
    val lumpname: String
    val lumpnum: Int

    totalkills = 0
    totalitems = 0
    totalsecret = 0
    wminfo.maxfrags = 0
    wminfo.partime = 180
    i = 0
    while (i < MAXPLAYERS) {
        players[i].killcount = 0
        players[i].secretcount = 0
        players[i].itemcount = 0
        i++
    }

    // Initial height of PointOfView
    // will be set by player think.
    players[consoleplayer].viewz = 1

    // Make sure all sounds are stopped before Z_FreeTags.
    S_Start()

    // Z_FreeTags (PU_LEVEL, PU_PURGELEVEL-1);  -- zone allocation dropped

    // UNUSED W_Profile ();
    P_InitThinkers()

    // if working with a devlopment map, reload it
    // W_Reload ();  -- development-map reload not ported

    // find map name
    if (gamemode == commercial) {
        if (map < 10)
            lumpname = "map0$map"
        else
            lumpname = "map$map"
    } else {
        lumpname = "E" + ('0' + episode) + "M" + ('0' + map)
    }

    lumpnum = W_GetNumForName(lumpname)

    leveltime = 0

    // note: most of this ordering is important
    P_LoadBlockMap(lumpnum + ML_BLOCKMAP)
    P_LoadVertexes(lumpnum + ML_VERTEXES)
    P_LoadSectors(lumpnum + ML_SECTORS)
    P_LoadSideDefs(lumpnum + ML_SIDEDEFS)

    P_LoadLineDefs(lumpnum + ML_LINEDEFS)
    P_LoadSubsectors(lumpnum + ML_SSECTORS)
    P_LoadNodes(lumpnum + ML_NODES)
    P_LoadSegs(lumpnum + ML_SEGS)

    rejectmatrix = W_CacheLumpNum(lumpnum + ML_REJECT)
    P_GroupLines()

    bodyqueslot = 0
    deathmatch_p = 0  // deathmatch_p = deathmatchstarts
    P_LoadThings(lumpnum + ML_THINGS)

    // if deathmatch, randomly spawn the active players
    if (deathmatch != 0) {
        i = 0
        while (i < MAXPLAYERS) {
            if (playeringame[i]) {
                players[i].mo = null
                G_DeathMatchSpawnPlayer(i)
            }
            i++
        }
    }

    // clear special respawning que
    iquehead = 0
    iquetail = 0

    // set up world state
    P_SpawnSpecials()

    // build subsector connect matrix
    //	UNUSED P_ConnectSubsectors ();

    // preload graphics
    if (precache)
        R_PrecacheLevel()

    //printf ("free memory: 0x%x\n", Z_FreeMemory());
}


//
// P_Init
//
fun P_Init() {
    P_InitSwitchList()
    P_InitPicAnims()
    R_InitSprites(sprnames)
}
