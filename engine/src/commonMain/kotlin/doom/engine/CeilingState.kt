// Port of linuxdoom-1.10 p_ceilng.c -- Ceiling aninmation (lowering, crushing, raising).
// (Also owns the P_CEILNG thinker struct + ceiling_e consts and
//  CEILSPEED/CEILWAIT/MAXCEILINGS from p_spec.h.)
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "NAME_SHADOWING", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class CeilingState {
    var activeceilings = arrayOfNulls<ceiling_t>(MAXCEILINGS)
}
