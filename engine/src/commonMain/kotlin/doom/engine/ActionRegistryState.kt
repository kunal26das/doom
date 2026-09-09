// Port of linuxdoom-1.10 info.h struct definitions (the data tables themselves
// are generated into gen/InfoGen.kt / gen/InfoConstsGen.kt).
// Original code (C) 1993-1996 id Software, Inc., released under the GNU GPL v2.
@file:Suppress("FunctionName", "ClassName", "PropertyName", "unused", "MagicNumber", "ktlint")

package doom.engine

/** State owned by one engine; no mutable process-wide storage. */
internal class ActionRegistryState {
    val actionMap by lazy(LazyThreadSafetyMode.NONE) { HashMap<String, ActionF>() }
}
