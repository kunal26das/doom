
package doom.engine.world

import doom.engine.audio.sStart
import doom.engine.core.DoomEngineCore
import doom.engine.core.iError
import doom.engine.gameplay.gDeathMatchSpawnPlayer
import doom.engine.gameplay.MAXPLAYERS
import doom.engine.gameplay.actors.Actor
import doom.engine.gameplay.actors.pSpawnMapThing
import doom.engine.gameplay.actors.iquehead
import doom.engine.gameplay.actors.iquetail
import doom.engine.gameplay.actors.sprnames
import doom.engine.gameplay.bodyqueslot
import doom.engine.gameplay.COMMERCIAL
import doom.engine.gameplay.consoleplayer
import doom.engine.gameplay.deathmatch
import doom.engine.gameplay.gamemode
import doom.engine.gameplay.playeringame
import doom.engine.gameplay.players
import doom.engine.gameplay.precache
import doom.engine.gameplay.totalitems
import doom.engine.gameplay.totalkills
import doom.engine.gameplay.totalsecret
import doom.engine.gameplay.wminfo
import doom.engine.geometry.BOXBOTTOM
import doom.engine.geometry.BOXLEFT
import doom.engine.geometry.BOXRIGHT
import doom.engine.geometry.BOXTOP
import doom.engine.geometry.FRACBITS
import doom.engine.geometry.fixedDiv
import doom.engine.geometry.FixedPoint
import doom.engine.geometry.mAddToBox
import doom.engine.geometry.mClearBox
import doom.engine.rendering.rInitSprites
import doom.engine.rendering.ST_HORIZONTAL
import doom.engine.rendering.ST_NEGATIVE
import doom.engine.rendering.ST_POSITIVE
import doom.engine.rendering.ST_VERTICAL
import doom.engine.rendering.resources.rFlatNumForName
import doom.engine.rendering.resources.rPrecacheLevel
import doom.engine.rendering.resources.rTextureNumForName
import doom.engine.resources.wCacheLumpNum
import doom.engine.resources.wGetNumForName
import doom.engine.resources.wLumpLength
import doom.engine.resources.i16
import doom.engine.resources.str
import doom.engine.resources.u16
import doom.engine.simulation.pInitThinkers
import doom.engine.simulation.leveltime
import doom.engine.world.specials.pInitPicAnims
import doom.engine.world.specials.pInitSwitchList
import doom.engine.world.specials.pSpawnSpecials

internal var DoomEngineCore.numvertexes
    get() = stateWorldGeometry.numvertexes
    set(value) { stateWorldGeometry.numvertexes = value }
internal var DoomEngineCore.vertexes: Array<MapVertex>
    get() = stateWorldGeometry.vertexes
    set(value) { stateWorldGeometry.vertexes = value }

internal var DoomEngineCore.numsegs
    get() = stateWorldGeometry.numsegs
    set(value) { stateWorldGeometry.numsegs = value }
internal var DoomEngineCore.segs: Array<MapSegment>
    get() = stateWorldGeometry.segs
    set(value) { stateWorldGeometry.segs = value }

internal var DoomEngineCore.numsectors
    get() = stateWorldGeometry.numsectors
    set(value) { stateWorldGeometry.numsectors = value }
internal var DoomEngineCore.sectors: Array<Sector>
    get() = stateWorldGeometry.sectors
    set(value) { stateWorldGeometry.sectors = value }

internal var DoomEngineCore.numsubsectors
    get() = stateWorldGeometry.numsubsectors
    set(value) { stateWorldGeometry.numsubsectors = value }
internal var DoomEngineCore.subsectors: Array<Subsector>
    get() = stateWorldGeometry.subsectors
    set(value) { stateWorldGeometry.subsectors = value }

internal var DoomEngineCore.numnodes
    get() = stateWorldGeometry.numnodes
    set(value) { stateWorldGeometry.numnodes = value }
internal var DoomEngineCore.nodes: Array<BspNode>
    get() = stateWorldGeometry.nodes
    set(value) { stateWorldGeometry.nodes = value }

internal var DoomEngineCore.numlines
    get() = stateWorldGeometry.numlines
    set(value) { stateWorldGeometry.numlines = value }
internal var DoomEngineCore.lines: Array<MapLine>
    get() = stateWorldGeometry.lines
    set(value) { stateWorldGeometry.lines = value }

internal var DoomEngineCore.numsides
    get() = stateWorldGeometry.numsides
    set(value) { stateWorldGeometry.numsides = value }
internal var DoomEngineCore.sides: Array<MapSide>
    get() = stateWorldGeometry.sides
    set(value) { stateWorldGeometry.sides = value }

internal var DoomEngineCore.bmapwidth
    get() = stateWorldGeometry.bmapwidth
    set(value) { stateWorldGeometry.bmapwidth = value }
internal var DoomEngineCore.bmapheight
    get() = stateWorldGeometry.bmapheight
    set(value) { stateWorldGeometry.bmapheight = value }
internal var DoomEngineCore.blockmap
    get() = stateWorldGeometry.blockmap
    set(value) { stateWorldGeometry.blockmap = value }
internal var DoomEngineCore.blockmaplump: IntArray
    get() = stateWorldGeometry.blockmaplump
    set(value) { stateWorldGeometry.blockmaplump = value }
internal var DoomEngineCore.bmaporgx: FixedPoint
    get() = stateWorldGeometry.bmaporgx
    set(value) { stateWorldGeometry.bmaporgx = value }
internal var DoomEngineCore.bmaporgy: FixedPoint
    get() = stateWorldGeometry.bmaporgy
    set(value) { stateWorldGeometry.bmaporgy = value }
internal var DoomEngineCore.blocklinks: Array<Actor?>
    get() = stateWorldGeometry.blocklinks
    set(value) { stateWorldGeometry.blocklinks = value }

internal var DoomEngineCore.rejectmatrix: ByteArray
    get() = stateWorldGeometry.rejectmatrix
    set(value) { stateWorldGeometry.rejectmatrix = value }

internal const val MAX_DEATHMATCH_STARTS = 10

internal val DoomEngineCore.deathmatchstarts
    get() = stateWorldGeometry.deathmatchstarts
internal var DoomEngineCore.deathmatchP
    get() = stateWorldGeometry.deathmatchP
    set(value) { stateWorldGeometry.deathmatchP = value }
internal val DoomEngineCore.playerstarts
    get() = stateWorldGeometry.playerstarts

internal fun DoomEngineCore.pLoadVertexes(lump: Int) {
    val data: ByteArray
    var i: Int

    numvertexes = wLumpLength(lump) / 4

    vertexes = Array(numvertexes) { MapVertex() }

    data = wCacheLumpNum(lump)

    i = 0
    while (i < numvertexes) {
        val li = vertexes[i]
        val ml = i * 4
        li.x = data.i16(ml + 0) shl FRACBITS
        li.y = data.i16(ml + 2) shl FRACBITS
        i++
    }
}

internal fun DoomEngineCore.pLoadSegs(lump: Int) {
    val data: ByteArray
    var i: Int
    var ldef: MapLine
    var linedef: Int
    var side: Int

    numsegs = wLumpLength(lump) / 12
    segs = Array(numsegs) { MapSegment() }
    data = wCacheLumpNum(lump)

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

internal fun DoomEngineCore.pLoadSubsectors(lump: Int) {
    val data: ByteArray
    var i: Int

    numsubsectors = wLumpLength(lump) / 4
    subsectors = Array(numsubsectors) { Subsector() }
    data = wCacheLumpNum(lump)

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

internal fun DoomEngineCore.pLoadSectors(lump: Int) {
    val data: ByteArray
    var i: Int

    numsectors = wLumpLength(lump) / 26
    sectors = Array(numsectors) { Sector() }
    data = wCacheLumpNum(lump)

    i = 0
    while (i < numsectors) {
        val ss = sectors[i]
        val ms = i * 26
        ss.floorheight = data.i16(ms + 0) shl FRACBITS
        ss.ceilingheight = data.i16(ms + 2) shl FRACBITS
        ss.floorpic = rFlatNumForName(data.str(ms + 4, 8))
        ss.ceilingpic = rFlatNumForName(data.str(ms + 12, 8))
        ss.lightlevel = data.i16(ms + 20)
        ss.special = data.i16(ms + 22)
        ss.tag = data.i16(ms + 24)
        ss.thinglist = null
        ss.index = i
        i++
    }
}

internal fun DoomEngineCore.pLoadNodes(lump: Int) {
    val data: ByteArray
    var i: Int
    var j: Int
    var k: Int

    numnodes = wLumpLength(lump) / 28
    nodes = Array(numnodes) { BspNode() }
    data = wCacheLumpNum(lump)

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

internal fun DoomEngineCore.pLoadThings(lump: Int) {
    val data: ByteArray
    var i: Int
    val numthings: Int
    var spawn: Boolean

    data = wCacheLumpNum(lump)
    numthings = wLumpLength(lump) / 10

    i = 0
    while (i < numthings) {
        val mtOfs = i * 10
        spawn = true

        if (gamemode != COMMERCIAL) {
            when (data.i16(mtOfs + 6)) {
                68,
                64,
                88,
                89,
                69,
                67,
                71,
                65,
                66,
                84 ->
                    spawn = false
            }
        }
        if (spawn == false)
            break

        val mt = MapThingSpawn(
            x = data.i16(mtOfs + 0),
            y = data.i16(mtOfs + 2),
            angle = data.i16(mtOfs + 4),
            type = data.i16(mtOfs + 6),
            options = data.i16(mtOfs + 8),
        )

        pSpawnMapThing(mt)
        i++
    }
}

internal fun DoomEngineCore.pLoadLineDefs(lump: Int) {
    val data: ByteArray
    var i: Int
    var v1: MapVertex
    var v2: MapVertex

    numlines = wLumpLength(lump) / 14
    lines = Array(numlines) { MapLine() }
    data = wCacheLumpNum(lump)

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
            if (fixedDiv(ld.dy, ld.dx) > 0)
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

internal fun DoomEngineCore.pLoadSideDefs(lump: Int) {
    val data: ByteArray
    var i: Int

    numsides = wLumpLength(lump) / 30
    sides = Array(numsides) { MapSide() }
    data = wCacheLumpNum(lump)

    i = 0
    while (i < numsides) {
        val sd = sides[i]
        val msd = i * 30
        sd.textureoffset = data.i16(msd + 0) shl FRACBITS
        sd.rowoffset = data.i16(msd + 2) shl FRACBITS
        sd.toptexture = rTextureNumForName(data.str(msd + 4, 8))
        sd.bottomtexture = rTextureNumForName(data.str(msd + 12, 8))
        sd.midtexture = rTextureNumForName(data.str(msd + 20, 8))
        sd.sector = sectors[data.i16(msd + 28)]
        sd.index = i
        i++
    }
}

internal fun DoomEngineCore.pLoadBlockMap(lump: Int) {
    var i: Int
    var count: Int

    val data = wCacheLumpNum(lump)
    count = wLumpLength(lump) / 2
    blockmaplump = IntArray(count)
    blockmap = 4

    i = 0
    while (i < count) {
        blockmaplump[i] = data.i16(i * 2)
        i++
    }

    bmaporgx = blockmaplump[0] shl FRACBITS
    bmaporgy = blockmaplump[1] shl FRACBITS
    bmapwidth = blockmaplump[2]
    bmapheight = blockmaplump[3]

    count = bmapwidth * bmapheight
    blocklinks = arrayOfNulls(count)
}

internal fun DoomEngineCore.pGroupLines() {
    var linebuffer: Int
    var i: Int
    var j: Int
    var total: Int
    val bbox = IntArray(4)
    var block: Int

    i = 0
    while (i < numsubsectors) {
        val ss = subsectors[i]
        val seg = segs[ss.firstline]
        ss.sector = seg.sidedef!!.sector
        i++
    }

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

    i = 0
    while (i < numsectors) {
        val sector = sectors[i]
        mClearBox(bbox)
        sector.lines = arrayOfNulls(sector.linecount)
        linebuffer = 0
        j = 0
        while (j < numlines) {
            val li = lines[j]
            if (li.frontsector === sector || li.backsector === sector) {
                sector.lines[linebuffer] = li
                linebuffer++
                mAddToBox(bbox, li.v1.x, li.v1.y)
                mAddToBox(bbox, li.v2.x, li.v2.y)
            }
            j++
        }
        if (linebuffer != sector.linecount)
            iError("P_GroupLines: miscounted")

        sector.soundorg.x = (bbox[BOXRIGHT] + bbox[BOXLEFT]) / 2
        sector.soundorg.y = (bbox[BOXTOP] + bbox[BOXBOTTOM]) / 2

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

internal fun DoomEngineCore.pSetupLevel(episode: Int, map: Int) {
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

    players[consoleplayer].viewz = 1

    sStart()


    pInitThinkers()


    if (gamemode == COMMERCIAL) {
        if (map < 10)
            lumpname = "map0$map"
        else
            lumpname = "map$map"
    } else {
        lumpname = "E" + ('0' + episode) + "M" + ('0' + map)
    }

    lumpnum = wGetNumForName(lumpname)

    leveltime = 0

    pLoadBlockMap(lumpnum + ML_BLOCKMAP)
    pLoadVertexes(lumpnum + ML_VERTEXES)
    pLoadSectors(lumpnum + ML_SECTORS)
    pLoadSideDefs(lumpnum + ML_SIDEDEFS)

    pLoadLineDefs(lumpnum + ML_LINEDEFS)
    pLoadSubsectors(lumpnum + ML_SSECTORS)
    pLoadNodes(lumpnum + ML_NODES)
    pLoadSegs(lumpnum + ML_SEGS)

    rejectmatrix = wCacheLumpNum(lumpnum + ML_REJECT)
    pGroupLines()

    bodyqueslot = 0
    deathmatchP = 0
    pLoadThings(lumpnum + ML_THINGS)

    if (deathmatch != 0) {
        i = 0
        while (i < MAXPLAYERS) {
            if (playeringame[i]) {
                players[i].mo = null
                gDeathMatchSpawnPlayer(i)
            }
            i++
        }
    }

    iquehead = 0
    iquetail = 0

    pSpawnSpecials()


    if (precache)
        rPrecacheLevel()

}

internal fun DoomEngineCore.pInit() {
    pInitSwitchList()
    pInitPicAnims()
    rInitSprites(sprnames)
}
