# DOOM → Kotlin Multiplatform porting conventions

This project ports linuxdoom-1.10 (id Software, GPL v2) to common Kotlin.
Reference C sources were used at translation time and are not required at
runtime. Kotlin files and types use descriptive PascalCase names, with matching
package directories under `engine/src/commonMain/kotlin/doom/engine/`.
The public API remains at the root; implementations are grouped by responsibility.
Documentation retains the original C vocabulary for tracing algorithms.
Kotlin functions and properties use lowerCamelCase, while `const val`
names use UPPER_SNAKE_CASE; for example, C `R_PointToAngle` is Kotlin `rPointToAngle`.

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
operations when their dependencies can be made explicit. Keep source and
configuration files free of comments; record algorithm explanations and
provenance in project documentation. Keep copyright and license notices in
[NOTICE](NOTICE), without repeated source-file headers.

Do not make incidental arithmetic changes during extraction. A targeted bug fix
may intentionally change behavior when its trigger and expected result are
characterized, such as the zero-distance renderer guard. Existing quirks should
be investigated rather than silently normalized. Fix diagnostics at their source;
do not add suppression annotations or disable a diagnostic to retain translated
code unchanged. The build enables Kotlin's extra warnings and treats compiler
warnings as errors. Use the actual experimental API's `OptIn` marker when needed.

## Type mapping

| C | Kotlin |
|---|---|
| `fixed_t` | `geometry.FixedPoint` = `Int` typealias (16.16). Use `fixedMul`, `fixedDiv`, `shl`/`shr` |
| `angle_t` | `geometry.BinaryAngle` = `UInt` typealias (BAM). Wraps + compares unsigned like C |
| `int`, `short`, `char`, `byte` (arithmetic) | `Int` |
| `unsigned` (non-angle) | `Int` unless comparison semantics need `UInt`; then convert locally |
| `boolean` | `Boolean` (C ints used as 0/1 flags stay `Int` only if stored/or-ed numerically) |
| `char*` string | `String` |
| C enums in binary/simulation code | Internal `Int` constants retain their original values. New host/service contracts may use typed enums or sealed values. |
| `byte*` pixel/lump data | `ByteArray` + `Int` index. A C pointer-into-buffer becomes a pair (`buf: ByteArray`, `ofs: Int`); pointer `++`/`+=` becomes index arithmetic |
| struct pointer (`mobj_t*`, `sector_t*` …) | `Actor`, `Sector`, etc. object reference (nullable if C can be NULL) |
| `ticcmd_t`, `event_t`, `thinker_t` | `input.TicCommand`, `input.EngineEvent`, `simulation.Thinker` |
| pointer subtraction `sec - sectors` | every map struct has an `index: Int` field set at load time — use it |
| function pointer | Kotlin function reference / nullable lambda |

### BinaryAngle rules (UInt)

- table index: `finesine[(ang shr ANGLETOFINESHIFT).toInt()]`, cosine via
  `FineCosineTable[(ang shr ANGLETOFINESHIFT).toInt()]`.
- `tantoangle` is an `IntArray`; wrap at use: `tantoangle[i].toUInt()`.
- arithmetic wraps automatically; unary minus: `0u - ang`; `(angle_t)(x)` on an int
  expression: `x.toUInt()` (sign-extend semantics of C casts from int are
  preserved by `Int.toUInt()`).
- comparisons are already unsigned — write them directly.
- `cmd.angleturn << 16` where angleturn is short: `(cmd.angleturn shl 16).toUInt()`.
- degrees→fine: `ang shr ANGLETOFINESHIFT` **must** use `shr` on UInt (logical).

### FixedPoint rules

- `fixedMul(a,b)` = `((a.toLong()*b) shr 16).toInt()`; `fixedDiv` uses the int64
  variant already characterized by the port. Do not replace fixed-point operations with Double.
- C `a>>b` on possibly-negative fixed = `shr` (arithmetic) — Kotlin `shr` on Int is
  arithmetic; that is correct. `>>>` in C never appears on fixed_t.

## Structural conventions

- **One named type per file:** Put each class, interface, object, enum,
  annotation class and typealias in `TypeName.kt`, including generated types,
  sealed variants and test helpers. Move named nested helpers to separate files
  with unique descriptive names. Anonymous objects and companion objects may
  stay with their owner. Classless functions/constants files remain allowed,
  using descriptive PascalCase names such as `FixedArithmetic.kt` and
  `WorldTickBindings.kt`. Platform actuals use the type's filename within each
  source set; classless platform adapters may use a recognized suffix such as
  `.wasm.kt`. The `verifyKotlinSourceLayout` task checks named-type ownership,
  PascalCase names and package/directory agreement in CI. Moving a declaration
  must not change initialization order, visibility beyond what the move
  requires, or runtime behavior.
- **Engine member names:** Functions, properties and local variables use
  lowerCamelCase; compile-time constants use UPPER_SNAKE_CASE. The layout guard
  enforces these conventions for engine sources and rejects Kotlin suppression
  annotations across the application modules. C names may remain in documentation,
  original action-name strings and diagnostics.
  Do not change serialized bytes or lookup keys as part of a symbol rename.
- **Packages:** Keep the public facade and host/input contracts in `doom.engine`.
  Put internal implementations in their owning packages, such as `gameplay`,
  `world`, `rendering`, `audio`, `resources`, `geometry`, `simulation` and
  `runtime`. Generated tables follow the same organization. The complete
  [package map](ARCHITECTURE.md#engine-package-map) records responsibilities;
  a package move alone does not remove remaining shared-core dependencies.
- **Instance state:** A mutable C global or static becomes state owned by one
  engine or one focused service. Do not add mutable top-level variables,
  singleton sessions or shared mutable actor/sound/configuration tables.
  `states`, `mobjinfo` and `sSfx` are mutable and must remain per-instance.
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
  `gradle/engine-compatibility-sources.txt` explicitly inventories the existing
  core-dependent implementations; `verifyArchitecture` rejects new service or
  model dependencies on the core outside that inventory. Gameplay/world/savegame
  geometry and renderer scheduling checks use packages rather than legacy filename prefixes.
  For example, scheduling uses clock/input/simulation capabilities, while menu
  commands and page drawing are separate capabilities.
- **Visibility:** Default implementation classes, records, constants and helpers
  to `internal` or `private`. The public engine API consists of the facade,
  typed host/input contracts, metrics, errors and required host-facing constants.
  Application code must not import compatibility event/actor/renderer records.
  Engine input variants are `DoomKeyInput`, `DoomMouseInput` and
  `DoomJoystickInput`; their common contract remains `DoomInput`.
- **Statics and helpers:** Mutable scratch state belongs to its owning instance.
  Pure helpers may be top-level or grouped by a focused responsibility. Use
  Kotlin names such as `rPointToAngle` and `pMobjThinker`; documentation can
  identify their original `R_PointToAngle` and `P_MobjThinker` counterparts.
- **Z_Malloc / Z_Free / Z_ChangeTag / zone tags:** Allocate Kotlin objects/arrays
  directly. Zone tags disappear; archive caching belongs to `WadArchive`.
- **Errors and output:** `iError` is internal compatibility glue that throws
  the public `DoomError` defined in `DoomError.kt`. Services may report errors
  without depending on the legacy global vocabulary. Existing diagnostic
  `printf`/`fprintf` calls map to `print`/`println` where retained.
- **goto / switch fallthrough:** Use labeled loops, flags or explicit shared
  tails while preserving execution order. Refactoring is allowed; silent
  changes to loop exit conditions or fallthrough are not.
- **memset/memcpy:** Use `fill`/`copyInto`, preserving aliasing, copy order and
  bounds behavior relevant to valid fixtures.
- **Thinker traversal:** `ThinkerScheduler` alone changes list links. It retains
  deferred removal through `Thinker.removed` and reads the next link after the
  callback, so newly appended thinkers can run in the same tic. Savegame code
  may traverse its sentinel. Where C compares a function pointer with
  `P_MobjThinker`, use `th is Actor`.
- **State action dispatch:** Generated states store `actionName`; the engine's
  `infoResolveActions()` binds each instance's `StateAction` from its own registry.
  `StateAction` carries either an actor callback or a player/weapon-sprite callback.
  Do not share callbacks that capture one engine with another engine.
- **Sound origins:** `Actor` and `SectorSoundOrigin` implement the internal
  `SoundOrigin` coordinate contract. Hosts receive sound/music operations,
  not mutable game objects.
- **Lump byte access:** Use the little-endian ByteArray helpers in `resources/BinaryReaders.kt`:
  `u8/i8/u16/i16/i32/str(off,len)`. `SHORT()`/`LONG()` swap macros disappear.
  Directory validation is not a substitute for validating map/save/demo records.
- **Demo / savegame streams:** Preserve explicit byte positions and serialization
  order when replacing mutable `demoP`/`saveP` logic with dedicated readers.
- **Simulation and rendering:** Simulation advances at 35 Hz; the host controls
  display pacing. `TickScheduler` owns timing and command buffering, and
  `SceneRenderer` retains the legacy input checkpoints between render passes.
  Rendering writes indexed 320×200 pixels into borrowed `FrameBuffers` storage;
  `PaletteVideoOutput` supplies a borrowed ARGB frame to the host. Do not add
  interpolation or alter frame/tic ordering as an incidental refactor.
- **Transition inputs:** `ScreenWipe.captureStart()` and `captureEnd()` capture
  the complete framebuffer; `advance(kind, ticks)` uses the dimensions owned by
  `FrameBuffers`. Do not restore unused rectangle arguments or no-op finish
  callbacks. Keep melt pixel pairing, presentation-random consumption and the
  original following-tick completion notification covered by the existing tests.

## Existing platform replacements and scope

The original platform/zone code is already replaced or omitted. Do not re-add
X11, DOS, platform I/O, networking or allocation behavior to common engine
services. `core/PlatformBindings.kt`, `rendering/HostVideoBindings.kt` and
`audio/HostAudioBindings.kt` provide internal compatibility adapters; public
host contracts remain in the root package. WAD loading, fixed-point helpers, tables,
actor/sound definitions, bounding-box helpers, randomness and argument handling
are already implemented or generated.

Networking remains single-player. `simulation/TickSchedulingBindings.kt` adapts
`netUpdate`/`tryRunTics` to `TickScheduler`, and `core/GameLoop.kt` retains event
routing and frame orchestration.
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
configuration and serialization. Simulation-only demo tests disable drawing and
force single-tic stepping; `RenderedDemoRegressionTest` separately checks both
single-tic and timed rendering against the pre-refactor binary. Run
`verifyArchitecture`, `verifyKotlinSourceLayout` and relevant JVM/multiplatform builds;
use runtime rendering checks when behavior crosses a platform graphics boundary.
