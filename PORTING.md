# DOOM → Kotlin Multiplatform porting conventions

This project is a **faithful port of linuxdoom-1.10** (id Software, GPL v2) to pure
common-Kotlin. Reference C sources live outside the repo (translation-time only).
Every engine file lives in `composeApp/src/commonMain/kotlin/doom/engine/`, package
`doom.engine`, **one Kotlin file per C file, same lowercase name** (`p_enemy.c` →
`p_enemy.kt`). The flat package mirrors C's single global namespace: everything is
top-level, no imports needed between engine files.

**Prime directive: translate, don't redesign.** Keep function names, variable
names, control flow, comments, magic numbers, and *bugs* exactly as vanilla.
When C does something weird, the Kotlin does the same weird thing. No new
features, no cleanups, no `// improved` anything. Files start with:

```kotlin
@file:Suppress("FunctionName", "ClassName", "PropertyName", "LocalVariableName",
    "unused", "UNUSED_PARAMETER", "MagicNumber", "ktlint")
package doom.engine
```

## Type mapping

| C | Kotlin |
|---|---|
| `fixed_t` | `fixed_t` = `Int` typealias (16.16). Use `FixedMul`, `FixedDiv`, `shl`/`shr` |
| `angle_t` | `angle_t` = `UInt` typealias (BAM). Wraps + compares unsigned like C |
| `int`, `short`, `char`, `byte` (arithmetic) | `Int` |
| `unsigned` (non-angle) | `Int` unless comparison semantics need `UInt`; then convert locally |
| `boolean` | `Boolean` (C ints used as 0/1 flags stay `Int` only if stored/or-ed numerically) |
| `char*` string | `String` |
| C enums | `Int` constants with identical names (`const val sk_baby = 0` …) — already done for generated tables; do the same for enums you own |
| `byte*` pixel/lump data | `ByteArray` + `Int` index. A C pointer-into-buffer becomes a pair (`buf: ByteArray`, `ofs: Int`); pointer `++`/`+=` becomes index arithmetic |
| struct pointer (`mobj_t*`, `sector_t*` …) | object reference (nullable if C can be NULL) |
| pointer subtraction `sec - sectors` | every map struct has an `index: Int` field set at load time — use it |
| function pointer | Kotlin function reference / nullable lambda |

### angle_t rules (UInt)
- table index: `finesine[(ang shr ANGLETOFINESHIFT).toInt()]`, cosine via
  `finecosine[(ang shr ANGLETOFINESHIFT).toInt()]`.
- `tantoangle` is an `IntArray`; wrap at use: `tantoangle[i].toUInt()`.
- arithmetic wraps automatically; unary minus: `0u - ang`; `(angle_t)(x)` on an int
  expression: `x.toUInt()` (sign-extend semantics of C casts from int are
  preserved by `Int.toUInt()`).
- comparisons are already unsigned — write them directly.
- `cmd.angleturn << 16` where angleturn is short: `(cmd.angleturn shl 16).toUInt()`.
- degrees→fine: `ang shr ANGLETOFINESHIFT` **must** use `shr` on UInt (logical).

### fixed_t rules
- `FixedMul(a,b)` = `((a.toLong()*b) shr 16).toInt()`; `FixedDiv` uses the int64
  variant (identical to DOS behavior — chocolate-doom semantics). Never use Double.
- C `a>>b` on possibly-negative fixed = `shr` (arithmetic) — Kotlin `shr` on Int is
  arithmetic; that is correct. `>>>` in C never appears on fixed_t.

## Structural conventions

- **Globals**: a C global defined in `foo.c` becomes a top-level `var` in `foo.kt`.
  `extern` declarations disappear (same package). **Never** initialize a top-level
  property from another file's *mutable* state — `const val`s and the generated
  data tables (`states`, `mobjinfo`, `S_sfx`, `finesine`…) are safe to use.
  Anything else is assigned inside the `*_Init` / `*_Setup` functions, exactly
  where vanilla does it.
- **Statics**: file-`private` top-level `var`/`fun`. If a static function name
  collides with another file's, prefix it with the file tag (`R_`, `P_`… already
  prevent most).
- **Z_Malloc / Z_Free / Z_ChangeTag / zone tags**: deleted. Allocate the target
  Kotlin object/array directly; `Z_Free`/`Z_ChangeTag` calls vanish. Lump caching
  is inside `W_CacheLumpNum`.
- **I_Error** throws `DoomError` (defined in `i_system.kt`); `printf`/`fprintf`
  become `print`/`println` (drop `stderr` distinction).
- **goto**: restructure with labeled loops/flags, preserving exact behavior.
  `switch` fallthrough: replicate by duplicating the shared tail or using a
  sequence of `if`s — behavior must be identical.
- **memset/memcpy** on arrays → `fill`/`copyInto`.
- Thinker function pointers: `thinker_t.function: ((thinker_t) -> Unit)?` plus
  `thinker_t.removed: Boolean` replacing the `(actionf_v)(-1)` hack
  (`P_RemoveThinker` sets `removed = true`). Where C tests
  `function.acp1 == (actionf_p1)P_MobjThinker` use `th is mobj_t`.
- State action dispatch: `states[i].actionName: String?` (generated). At startup
  `InfoResolveActions(actionMap)` fills `states[i].action: ActionF?`.
  `ActionF` holds either `mobjFun: (mobj_t) -> Unit` or
  `pspFun: (player_t, pspdef_t) -> Unit`. `p_enemy.kt`/`p_pspr.kt` register all
  `A_*` functions in `actionMap` (see `info.kt`).
- Sound origins: `mobj_t` and `degenmobj_t` implement `soundorigin_t` (`x`, `y`).
- Lump byte access: little-endian helpers on ByteArray from `doomtype.kt`:
  `u8/i8/u16/i16/i32/str(off,len)`. `SHORT()`/`LONG()` swap macros disappear —
  the helpers already read little-endian.
- Demo / savegame streams: `ByteArray` + explicit position variable (ports of
  `demo_p`, `save_p`).
- Vanilla renders at 35 fps (one render per tic) into `screens[0]`
  (320×200 palettized bytes). Do not add interpolation.

## What NOT to port

- `i_main.c`, `i_video.c`, `i_sound.c`, `i_net.c`, `i_system.c` (platform layer —
  replaced by `i_*.kt` interfaces already in the tree), `z_zone.c`, `m_swap.c`,
  `v_video.c`, `w_wad.c`, `m_fixed.c`, `tables.c`, `info.c`, `sounds.c`,
  `dstrings.c`, `m_bbox.c`, `m_random.c`, `m_argv.c` (all already ported/generated).
- `d_net.c`/`i_net.c` networking: single local player only. `d_net.kt` keeps
  `D_ProcessEvents`/`TryRunTics`/`NetUpdate` API with netgame always false
  (structure follows chocolate-doom's d_loop simplification, vanilla timing:
  35 Hz, ticdup 1).
- X11/DOS-specific parms and the `-devparm` file paths that make no sense here.

## Verification invariant

The engine must play back the IWAD's built-in DEMO1/DEMO2/DEMO3 without desync
(same RNG sequence, same movements). Any "small" numeric deviation breaks this —
which is why FixedDiv/angle/RNG exactness matters.
