// Port of linuxdoom-1.10 p_plats.c -- Plats (i.e. elevator platforms) code,
// raising/lowering.
// (Also owns the P_PLATS thinker struct + plat_e/plattype_e consts and
//  PLATWAIT/PLATSPEED/MAXPLATS from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class PlatformMotionState {
    var activeplats = arrayOfNulls<plat_t>(MAXPLATS)
}
