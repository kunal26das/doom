# DOOM → Kotlin Multiplatform porting conventions

This project ports linuxdoom-1.10 (id Software, GPL v2) to common Kotlin.
Reference C sources were used at translation time and are not required at
runtime. Existing compatibility files retain their lowercase C-derived names
where useful for comparison, but a flat package and one file per C file are not
architectural requirements. Behavior-owning services now live in focused
packages under `engine/src/commonMain/kotlin/doom/engine/`.

These conventions preserve simulation compatibility; they do not override the
user's request to restructure the engine. Prefer objects that own behavior and
invariants, explicit collaborators, internal implementation details and
constructor injection. See [ARCHITECTURE.md](ARCHITECTURE.md) for actual
boundaries and [AUDIT.md](AUDIT.md) for remaining coupling. MVVM belongs to the
application presentation layer, not the simulation core.

## Compatibility during restructuring

Preserve observable simulation behavior: fixed-point rounding and overflow,
unsigned angles, random-stream consumption, action order, command timing and
save/demo representation. Names, file layout and shared-context access are not
compatibility requirements. Extract algorithms into services and pure geometry
operations when their dependencies can be made explicit. Preserve relevant C
comments and copyright notices in derived implementations.

Do not make incidental arithmetic changes during extraction. A targeted bug fix
may intentionally change behavior when its trigger and expected result are
characterized, such as the zero-distance renderer guard. Existing quirks should
be investigated rather than silently normalized. Keep legacy naming suppressions
only where the original vocabulary remains; new services need not inherit the
entire compatibility file's suppression list.

## Type mapping

| C | Kotlin |
|---|---|
| `fixed_t` | `fixed_t` = `Int` typealias (16.16). Use `FixedMul`, `FixedDiv`, `shl`/`shr` |
| `angle_t` | `angle_t` = `UInt` typealias (BAM). Wraps + compares unsigned like C |
| `int`, `short`, `char`, `byte` (arithmetic) | `Int` |
| `unsigned` (non-angle) | `Int` unless comparison semantics need `UInt`; then convert locally |
| `boolean` | `Boolean` (C ints used as 0/1 flags stay `Int` only if stored/or-ed numerically) |
| `char*` string | `String` |
| C enums in binary/simulation code | Internal `Int` constants retain their original values. New host/service contracts may use typed enums or sealed values. |
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
  variant already characterized by the port. Do not replace fixed-point operations with Double.
- C `a>>b` on possibly-negative fixed = `shr` (arithmetic) — Kotlin `shr` on Int is
  arithmetic; that is correct. `>>>` in C never appears on fixed_t.

## Structural conventions

- **One named type per file:** Put each class, interface, object, enum,
  annotation class and typealias in `TypeName.kt`, including generated types,
  sealed variants and test helpers. Move named nested helpers to separate files
  with unique descriptive names. Anonymous objects and companion objects may
  stay with their owner. Classless functions/constants files remain allowed;
  this is how compatibility adapters can retain their C-derived filenames.
  Platform actuals use the type's filename within each source set. The
  `verifyKotlinSourceLayout` task checks this rule. Moving a declaration must not
  change initialization order, visibility beyond what the move requires, or
  runtime behavior.
- **Instance state:** A mutable C global or static becomes state owned by one
  engine or one focused service. Do not add mutable top-level variables,
  singleton sessions or shared mutable actor/sound/configuration tables.
  `states`, `mobjinfo` and `S_sfx` are mutable and must remain per-instance.
  Trigonometric/gamma lookup data may be shared as implementation-only data that
  consumers treat as immutable.
- **Initialization:** Preserve lazy allocation and initialization order where
  callbacks or tables depend on one another. Do not restore engine-wide core
  constructor arguments to plain state holders. Their own fields should be
  referenced directly. Genuine cross-object dependencies belong in explicit
  ports or composition factories; constructors must not call collaborators
  before the object graph is ready.
- **Service boundaries:** Extract real algorithms and their private state
  together. New services must not import, receive or retain `DoomEngineCore`,
  nor accept a replacement object exposing the same broad context. Legacy
  adapters may bind focused operations to existing core receiver functions.
  For example, scheduling uses clock/input/simulation capabilities, while menu
  commands and page drawing are separate capabilities.
- **Visibility:** Default implementation classes, records, constants and helpers
  to `internal` or `private`. The public engine API consists of the facade,
  typed host/input contracts, metrics, errors and required host-facing constants.
  Application code must not import compatibility event/actor/renderer records.
  Engine input variants are `DoomKeyInput`, `DoomMouseInput` and
  `DoomJoystickInput`; their common contract remains `DoomInput`.
- **Statics and helpers:** Mutable scratch state belongs to its owning instance.
  Pure helpers may be top-level or grouped by a focused responsibility. Keep
  private names or conventional `R_`/`P_` prefixes where they aid source tracing.
- **Z_Malloc / Z_Free / Z_ChangeTag / zone tags:** Allocate Kotlin objects/arrays
  directly. Zone tags disappear; archive caching belongs to `WadArchive`.
- **Errors and output:** `I_Error` is internal compatibility glue that throws
  the public `DoomError` defined in `DoomError.kt`. Services may report errors
  without depending on the legacy global vocabulary. Existing diagnostic
  `printf`/`fprintf` calls map to `print`/`println` where retained.
- **goto / switch fallthrough:** Use labeled loops, flags or explicit shared
  tails while preserving execution order. Refactoring is allowed; silent
  changes to loop exit conditions or fallthrough are not.
- **memset/memcpy:** Use `fill`/`copyInto`, preserving aliasing, copy order and
  bounds behavior relevant to valid fixtures.
- **Thinker traversal:** `ThinkerScheduler` alone changes list links. It retains
  deferred removal through `thinker_t.removed` and reads the next link after the
  callback, so newly appended thinkers can run in the same tic. Savegame code
  may traverse its sentinel. Where C compares a function pointer with
  `P_MobjThinker`, use `th is mobj_t`.
- **State action dispatch:** Generated states store `actionName`; the engine's
  `InfoResolveActions()` binds each instance's `ActionF` from its own registry.
  `ActionF` carries either an actor callback or a player/weapon-sprite callback.
  Do not share callbacks that capture one engine with another engine.
- **Sound origins:** `mobj_t` and `degenmobj_t` implement the internal
  `soundorigin_t` coordinate contract. Hosts receive sound/music operations,
  not mutable game objects.
- **Lump byte access:** Use the little-endian ByteArray helpers in `doomtype.kt`:
  `u8/i8/u16/i16/i32/str(off,len)`. `SHORT()`/`LONG()` swap macros disappear.
  Directory validation is not a substitute for validating map/save/demo records.
- **Demo / savegame streams:** Preserve explicit byte positions and serialization
  order when replacing mutable `demo_p`/`save_p` logic with dedicated readers.
- **Simulation and rendering:** Simulation advances at 35 Hz; the host controls
  display pacing. `TickScheduler` owns timing and command buffering, and
  `SceneRenderer` retains the legacy input checkpoints between render passes.
  Rendering writes indexed 320×200 pixels into borrowed `FrameBuffers` storage;
  `PaletteVideoOutput` supplies a borrowed ARGB frame to the host. Do not add
  interpolation or alter frame/tic ordering as an incidental refactor.

## Existing platform replacements and scope

The original platform/zone code is already replaced or omitted. Do not re-add
X11, DOS, platform I/O, networking or allocation behavior to common engine
services. `i_*.kt` files provide internal compatibility adapters; public host
contracts are declared separately. WAD loading, fixed-point helpers, tables,
actor/sound definitions, bounding-box helpers, randomness and argument handling
are already implemented or generated.

Networking remains single-player. `d_net.kt` adapts `NetUpdate`/`TryRunTics` to
`TickScheduler`, and `d_main.kt` retains event routing and frame orchestration.
The ordinary game uses tic duplication 1. Adding multiplayer or changing the
command backlog policy requires its own design and regression coverage.

## Verification invariant

Preserve the current IWAD's complete built-in DEMO1/DEMO2/DEMO3 checkpoints,
including random state, player/world state and completion tics. Canonical
expectations live in the engine regression tests and must not be regenerated
simply to make a refactor pass. They characterize this Kotlin implementation;
they do not independently prove DOS parity or every campaign path.

Also test the extracted boundaries with small deterministic fixtures: lifecycle
cleanup failures, fake clocks, command-ring ownership, thinker mutation order,
geometry without camera mutation, rendered pixels, palette/wipe behavior,
configuration and serialization. Demo tests disable drawing and force single-tic
stepping, so passing demo hashes alone cannot validate timed scheduling or
rendering. Run `verifyArchitecture`, `verifyKotlinSourceLayout` and relevant JVM/multiplatform builds;
use runtime rendering checks when behavior crosses a platform graphics boundary.
