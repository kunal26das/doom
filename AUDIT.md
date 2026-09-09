# Project audit and architecture refactor

Updated: 2026-09-10

Project: `doom`

The initial application audit used commit `701369f`. This update describes the
subsequent engine refactor, type-file cleanup, package/name migration and
suppression cleanup in the current working tree, including earlier project changes
that remain in place.
Current build/test verification and earlier runtime checks are recorded separately below.

## Outcome and scope

The application retains separate `:domain`, `:engine`, and `:composeApp`
responsibilities. MVVM remains in presentation, where ViewModels expose state
and a domain use case drives the game. The engine exposes a host-facing facade
and typed input/host ports; it does not depend on Compose or ViewModels.

The earlier per-instance conversion fixed global ownership, but left a broad
execution context and state holders that retained the complete core. This
refactor moves lifecycle, scheduling, framebuffer operations, palette output,
wipes, configuration and capture into objects that own behavior and state.
Geometry is shared through pure functions and explicit world-data queries.
State-holder constructors no longer accept the core, and menus receive separate
command and drawing capabilities. The application chooses its concrete audio
mixer through an injected factory.

The subsequent source-layout cleanup places every named Kotlin class,
interface, object, enum, annotation class and typealias in its own `TypeName.kt`
file, across production and test source sets. Previously nested named helpers
and sealed variants are separate types in the same package. Anonymous objects
and companions are exempt, and functions/constants-only files remain allowed.
The `verifyKotlinSourceLayout` guard checks this organization.

The preceding engine migration moved 203 files, split two mixed-responsibility
files and renamed 78 internal types. The 15 public root files keep their names
and `doom.engine` package. Implementation names now describe their role, such as
`Actor`, `TicCommand`, `FixedPoint` and `BinaryAngle`, and files live in matching
packages for core, gameplay, world, rendering, audio, resources and other
responsibilities. Generated data follow their owning packages. CI checks also
require PascalCase names and agreement between package declarations and source
directories. The [engine package map](ARCHITECTURE.md#engine-package-map)
documents the structure. This migration changes names and organization while
preserving algorithms, arithmetic and serialized representations.

The work does not make every original algorithm fully SOLID. Gameplay, actor
behavior, collision, low-level rasterization and savegame compatibility still
share mutable records through internal core receiver functions. These remaining
dependencies are called out below. The explicit inventory in
`gradle/engine-compatibility-sources.txt` records existing core-dependent
implementations; new services and models cannot depend on the core outside that
inventory. `PORTING.md` documents the package/name conventions and the C vocabulary
needed for source tracing.
Fixed-point arithmetic, simulation ordering and fixture compatibility remain
constraints on further extraction.

The current cleanup removes all 185 engine suppression annotations. Compiler
diagnostics are addressed directly: dead assignments and locals are removed,
single-assignment values become immutable, impossible conditions and redundant
conversions are eliminated, and all 37 direct parameter/local shadows are fixed.
Unused arguments are removed from the operations that do not need them;
callbacks discard an irrelevant argument at their typed boundary. Engine
functions/properties now use lowerCamelCase and compile-time constants use
UPPER_SNAKE_CASE, while binary values and original action-name strings retain
their compatibility meaning. The source guard checks these names and rejects
suppression annotations.

`ScreenWipe` now takes complete-frame captures and advances through
`advance(kind, ticks)` using framebuffer-owned dimensions. This removes unused
rectangle inputs and no-op finish stages while retaining the existing pixel,
randomness and completion-timing assertions. The cleanup fixes concrete source
issues; it does not remove the remaining shared-core algorithm dependencies.

No game assets are added or replaced by this architecture change. The current
bundled `doom1.wad` already contains Ultimate DOOM data from earlier project
work. The existing PLAY DEMO label is a legacy label for that resource, not
evidence of its edition. `BuiltInGameSelection` is the separate selection type
for the same bundled resource.

## Engine findings addressed in this refactor

| Finding | Implemented change | Relevant boundary |
| --- | --- | --- |
| The facade directly mutated clock, input, sound, configuration and host hooks. | `EngineSession` owns lifecycle policy through execution/input/audio/settings capabilities; `LegacyEngineRuntime` binds original algorithms. | `runtime/EngineSession.kt`; `core/LegacyEngineRuntime.kt` |
| Nullable hooks and driver references were spread across state holders. | `HostAttachment` owns replaceable host references; `EngineClock` keeps one lifetime epoch; `QuitSignal` removes the host callback for exit. | `runtime/`; `DoomHost.kt` |
| Application input exposed legacy event records; sound/music capabilities were combined everywhere. | `DoomInput` expresses typed host input; separate sound-effect and music ports are composed by the host driver. | `DoomInput.kt`; `DoomKeyInput.kt`, `DoomMouseInput.kt`, `DoomJoystickInput.kt`; separate host-port files |
| Implementation records and generated tables were exported alongside the facade. | Mark implementation declarations internal and enable strict explicit API compilation. | Engine build and public facade, host/input, metrics and error declarations |
| Timing, command buffering and duplicate single-tic logic accessed the whole core. | `TickScheduler` owns a private ring and timing policy through narrow clock/input/simulation contracts; gameplay receives command copies. | `simulation/TickScheduler.kt`; `simulation/TickSchedulingBindings.kt` |
| Thinker link mutation and world-tic ordering had no owning behavior object. | `ThinkerScheduler` owns list operations; `WorldTicker` owns level time and world-update order. | `simulation/ThinkerScheduler.kt`; `simulation/WorldTicker.kt` |
| Palette conversion, patch/block drawing and transition state were core extensions over public state bags. | Framebuffer, palette/video and wipe services own their algorithms and state. | `rendering/` |
| Render orchestration called scheduling functions directly. | `SceneRenderer` orders narrow render passes and invokes an explicit input checkpoint callback. | `rendering/SceneRenderer.kt`; legacy renderer adapter |
| Gameplay angle queries changed renderer camera coordinates. | `FixedGeometry` computes angles/distances/sides without camera mutation; `BspQueries` takes explicit geometry. | `geometry/`; gameplay and renderer call sites |
| Configuration mixed parsing, storage, primitive sentinels, argument checks and PCX encoding. | Typed setting bindings, `EngineConfiguration`, `StartupArguments`, `PcxEncoder` and `PcxScreenshots` separate these responsibilities. | `configuration/`; `capture/` |
| Plain state objects retained the entire engine even for constant arrays and their own fields. | Removed core constructor arguments and redundant receiver scopes while preserving per-instance lazy allocation. | Remaining legacy `*State` classes |
| Menu definitions retained the core solely to call commands and drawing routines. | `MenuState` receives typed `MenuCommandHandler` and `MenuPageRenderer`; its composition factory binds the existing menu algorithms. | `menu/`; `createMenuState` in `menu/MenuOperations.kt` |
| The application adapter chose a concrete mixer internally. | `DoomEngineAdapter` receives an `AudioMixer` factory; `GameDependencies` selects `DmxSoundDriver`. | `composeApp/.../data/audio/AudioMixer.kt`; `di/GameDependencies.kt` |
| Replacing an active or paused host could send old audio handles to the new driver. | Detach the old host before attaching and restoring music; preserve the clock epoch and terminate on cleanup failure. | `runtime/EngineSession.kt`; lifecycle regression tests |

## Earlier application and ownership repairs retained

The following findings were addressed in the preceding application/instance
refactors. They remain relevant context, not new verification claims. P2 denotes
a correctness, reliability or maintainability issue warranting repair.

| Priority | Finding and effect | Implemented repair | Current location |
| --- | --- | --- | --- |
| P2 | `App` owned WAD reads, global hooks, mixer creation, audio startup, stepping, bitmap conversion and exception handling; none could be tested separately. | Separate domain ports/session, a lifecycle ViewModel, state-driven screen, repositories, composition root and legacy engine adapter; separate engine/domain modules. | `domain/`; `composeApp/src/commonMain/kotlin/com/kunal26das/doom/{data,di,presentation}/` |
| P2 | Audio threads, AVAudioEngine callbacks and browser listeners/timers outlived the screen, with no stop contract. | Closeable platform outputs, cleanup on startup failure, idempotent release and session `finally` cleanup. | Platform `AndroidAudioOutput.kt`, `IosAudioOutput.kt`, `JvmAudioOutput.kt`, `WebAudioOutput.kt`; `data/DoomEngineAdapter.kt` |
| P2 | Recreating the screen booted partially reset process-wide globals again. | Give each `DoomEngine` independent runtime state. Resume the same instance after lifecycle suspension, and create a fresh instance when changing games; freeze background time and clear input/sound handles on detachment. | `data/DoomEngineAdapter.kt`; `engine/.../DoomEngine.kt`; `engine/.../core/DoomEngineCore.kt` |
| P2 | Platform frame buffers were process-global, allowing one screen to overwrite another screen's retained image. The game image also filled the viewport before its aspect constraint. | Give each screen its own converter behind the common `FrameBitmapConverter` interface and platform factory; retain nearest-neighbor scaling in a bounded 4:3 rectangle. | `AndroidFrameBitmapConverter.kt`, `IosFrameBitmapConverter.kt`, `DesktopFrameBitmapConverter.kt`, `WebFrameBitmapConverter.kt`; `presentation/GameScreen.kt` |
| P2 | A crash after the first frame was hidden because error text appeared only when no image existed. | Explicit Failed state and error rendering above the last frame. | `presentation/GameViewModel.kt`; `presentation/GameScreen.kt` |
| P2 | The 64-slot event ring wrapped to an apparently empty queue on input bursts, losing key-up events. | Replace the fixed ring with an instance-owned input service that preserves ordered events and key releases. | `engine/.../input/EngineInputQueue.kt`; `engine/.../core/GameLoop.kt` |
| P2 | Touch cancellation could skip release; weapon state was process-global and RUN could remain held after disposal. | Gesture `finally` release and per-screen `TouchInputController` ownership with disposal cleanup. | `presentation/input/` |
| P2 | WAD headers, directory sizes, offsets and lump indexes were unchecked; malformed input caused raw bounds failures or partially replaced state. | Overflow-safe validation before publishing the complete archive; controlled `DoomError`; preserve cache/archive on rejection. | `engine/.../resources/WadArchive.kt`; `resources/WadBindings.kt` |
| P2 | Out-of-range persisted gamma, key/button settings and related options could fail during rendering or input. | Per-setting bounds with defaults retained for invalid entries; valid settings still round-trip. | `engine/.../configuration/EngineConfiguration.kt`; `configuration/ConfigurationBindings.kt` |
| P2 | Missing/empty command-line arguments, including `-warp 1` for episode-based DOOM, indexed absent strings/arguments. | Validate option values and warp arity before subsystems consume arguments. | `engine/.../configuration/StartupArguments.kt`; startup adapters |
| P2 | Mixer channel references crossed threads without safe publication; malformed or zero-rate sounds could fail or never finish. | Publish immutable channel-array snapshots through a volatile field; validate DMX header/length/rate and stereo output shape. | `data/audio/DmxSoundDriver.kt` |
| P2 | Direct save overwrites could damage an existing file on interrupted writes; iOS ignored failed writes. | Android `AtomicFile`; JVM synced temporary file and atomic replacement where supported; check iOS write success; standard browser Base64 codec. | Platform `AndroidFileRepository.kt`, `IosFileRepository.kt`, `JvmFileRepository.kt`, `BrowserFileRepository.kt` |
| P2 | Headless caught crashes and exited successfully, used wall-clock timing, included a developer-specific path, and read real user configuration. | Propagate failures; inject deterministic ticks; accept explicit WAD selection; isolate storage; optional frame output. | `composeApp/src/jvmMain/kotlin/com/kunal26das/doom/Headless.kt` |
| P2 | No automated tests or pull-request verification existed. | Domain, ViewModel, mixer, input, engine and isolated adapter regressions; architecture boundary check; PR verification and deployment test gate. | Test source sets; `build.gradle.kts`; `.github/workflows/` |

## Ownership and extension rules

- `:domain` owns application ports and session policy; outer data adapters
  implement those ports. Presentation does not import concrete engine internals.
- `DoomEngine` is the engine integration facade. Hosts supply clock, storage,
  video and audio capabilities and post `DoomInput`; C-compatible records and
  compatibility algorithms remain internal implementation details.
- Extracted engine services and models do not import or receive `DoomEngineCore`.
  Composition factories and `LegacyEngineRuntime` may bind their focused ports
  to legacy algorithms. State ownership is distinct from complete isolation of
  those algorithms.
- `GameFrame` copies the borrowed engine pixels before publication. Legacy
  rasterization still borrows indexed planes from `FrameBuffers`; consumers
  must not replace that storage or retain host-frame buffers as immutable data.
- Ordinary lifecycle suspension detaches and later resumes the same engine;
  failures and quit are terminal for that ViewModel. RESTART creates a fresh
  session with the chosen WAD, and CHOOSE GAME returns to the chooser.
- Startup installs only application-owned file/import repository adapters.
  File/database access remains deferred; ViewModels and engines are created
  freshly by `GameDependencies`. Startup lookups stay outside domain, engine and
  presentation layers.
- Menu commands and menu drawing are separate capabilities. The existing menu
  graph and per-instance mutable status remain owned by its `MenuState`.
- Architecture checks protect layer references and instance ownership. They are
  guardrails for known failure patterns, not a proof of complete SOLID design.
- `verifyKotlinSourceLayout` checks one named type per matching file, PascalCase
  names and package/directory agreement, including production declarations and
  test fixtures. It also enforces engine function/property/constant naming and
  rejects Kotlin suppression annotations. `verifyArchitecture` checks core access against the explicit
  compatibility inventory and applies gameplay/world/savegame/rendering rules by package.
  File organization does not replace dependency or behavior checks.

See [ARCHITECTURE.md](ARCHITECTURE.md) for service responsibilities, dependency
flow, lifecycle ownership and extension points.

## Existing asset selection and storage behavior

The chooser offers the bundled resource through PLAY DEMO and imported IWADs
through a platform file picker. Web also supports drag/drop through the same
reader and validation path. The current bundle is Ultimate DOOM; this refactor
does not change its contents or launcher labels. The selection declarations are
now `BuiltInGameSelection` and `ImportedGameSelection`, each in its own file.
The engine menu supplies episode and skill choices for the selected data.

`GameHostViewModel` depends on `ImportedGameRepository`. Valid imported data are
remembered locally and revalidated on restoration; Web uses binary IndexedDB
records and native targets use versioned records with atomic file replacement.
The import validator bounds images and directory work and screens structural
and game-family problems. The import path does not upload selected files.
If persistence fails, PLAY FOR THIS VISIT can use the validated in-memory data.

Remembered data launch automatically. Choosing the bundled resource does not
replace the remembered import. Imported saves/configuration use content-derived
prefixes, while the built-in selection retains its existing namespace. A failed
restore returns to the chooser instead of silently substituting another game.

## Verification requirements and current status

### Save and load across platforms

The save audit fixes stopped platforms disappearing from the saved thinker
list. Stopped movers have no active callback, so the serializer previously
skipped them. Registered platforms now retain their existing `TC_PLAT` record,
stopped state and ability to resume after loading. The save format and storage
namespaces are unchanged.

Empty save slots now suggest the current map name (`E1M1` or `MAP01`). Touch
controls can confirm that name without a keyboard. Shared menu tests exercise
confirmation, both naming schemes, editing and cancellation while preserving
existing save names.

Android now verifies committed bytes and reports unsuccessful writes instead
of relying on `AtomicFile` methods that can log a failure without throwing.
Verification happens after the transaction: a read-back failure cannot roll
back and delete an already committed save on older Android releases. Android
and iOS reads distinguish absent files from unreadable files and invalid file
destinations. Those failures propagate through the existing game error path.

| Platform | Executed coverage |
| --- | --- |
| macOS JVM | Real E1M1 save/load into a fresh engine, menu slots and overwrite, inventory/world/actors/movers, continued play, isolated native file persistence, and reopening storage in a new JVM process. |
| Android 8/API 26 and Android 17/API 37 emulators | Six instrumented tests on each device: binary persistence and recreation, overwrite/empty/missing distinctions, invalid and unreadable files, interrupted-write recovery, and failure after a committed write. |
| iOS 26.5 simulator | Eight native Foundation storage tests plus the shared engine suite, including the stopped-platform round trip. Tests use temporary directories and check binary data, slots, shorter/empty overwrites and read/write failures. |
| Browser/Wasm | Real local-storage tests in headless Chrome and the shared engine suite. Interactive browser checks save inventory, change it, load it, reload the page, and load the persisted slot again. |
| Linux and Windows | CI runs the real engine round trips and JVM storage tests on both operating systems. Linux also runs the Wasm browser suites. Local macOS results do not substitute for those CI executions. |

Native and browser repository tests use isolated storage; the actual map-level
engine tests inject isolated in-memory storage. Only the desktop subprocess
test exercises process termination directly. Browser reload exercises page
recreation. Physical Android/iOS devices, general touch save-name entry,
browser storage quotas and malformed save recovery are not fully verified.
The classic save format still resets some transient references and random
state; it does not promise identical future randomness to uninterrupted play.

Final local verification passes 192 JVM tests, 157 Android host tests, 144 Wasm
browser tests, 144 iOS simulator engine/application tests, six device-storage
tests on each Android emulator, and 51 JavaScript browser-adapter tests.
Architecture and source-layout guards, the production Wasm distribution and
the iOS arm64 debug framework link also pass. Full-engine failure regressions
confirm that unsuccessful saves do not replace previous bytes or display a
success message, and failed reads do not replace the current level.

Running the cross-platform suites also exposed a JVM-only exception assertion
in a common raster test. That invalid-array test now runs on JVM, where its
exception type is meaningful; valid fuzz-rendering cases remain common.
The shared ViewModel cleanup test now explicitly constructs `AutoCloseable`,
so it compiles on native and Wasm targets too.

The published Compose UIKit dependency embeds its publisher's Xcode 26.4 Swift
library path. iOS test/framework linking now also resolves the active Xcode
Swift runtime directory through `xcrun`; other hosts do not execute this lookup.
This allows the native tests to link with the installed Xcode without changing
dependency versions or suppressing linker diagnostics.

### Rendering performance

The renderer reuses per-engine sprite clipping buffers and caches WAD lumps in
a dense array. Scratch buffers are reset between sprites, including positions
accessed by low-detail drawing. Pixel algorithms, fixed-point arithmetic and
mutation order are unchanged.
The simulation remains at 35 ticks per second.

Deterministic JVM demo playback measures engine-step allocations after startup,
with complete-demo pixel hashes checked on every warmup and measured run.
Per-frame engine allocation falls by approximately 95%, from roughly 23–31 KB
to 1–1.7 KB. Repeated JVM forks showed substantial baseline timing variation,
so the measurements do not establish a repeatable CPU-time improvement.

The original and final production builds both rendered approximately 120 frames
per second in 20-second browser samples at the E1M1 starting view (736 × 772,
device pixel ratio 2). Neither sample had frame intervals above 25 ms or observed
long tasks. The release preview had no console warnings or errors. This confirms
the display cap was already reached in that scene; it does not demonstrate a
browser FPS gain.

Verification passes 177 JVM tests (105 engine, 21 domain and 51 application),
51 browser adapter tests, architecture/source-layout checks and the production
Wasm build. Added regressions cover WAD cache replacement and rollback,
raster wrapping/clamping, live buffer aliasing, error/mutation order and
successive low-detail sprite clipping. Existing rendered-demo fixtures retain
their frame counts and sampled pixel hashes.

### Comment removal

Comments were removed from 306 source, test and configuration files. Kotlin's
lexer and PSI parser verified identical executable tokens and syntax trees across
all 404 tracked Kotlin files, with no remaining comment tokens. Acorn verified
the JavaScript token streams and syntax trees. Shell commands, parsed workflow
and Xcode settings, literal strings and game assets are unchanged. The executable
shell shebang remains; attribution and license text are retained in NOTICE and
LICENSE. Project documentation records conventions without source comments.

Verification passed all 166 JVM and 51 browser adapter tests, architecture/source
layout checks, Wasm compilation, Swift syntax parsing, shell syntax/ShellCheck,
and Xcode project parsing. This cleanup does not change executable behavior or
extend the earlier device and long-session testing coverage.

### Suppression and toolchain cleanup

Kotlin compilations enable `extraWarnings` and `allWarningsAsErrors` in all four
modules. Gradle Kotlin scripts also treat warnings as errors. Wasm declarations
use the actual required opt-in annotations, and the converter uses a common
interface with platform factories instead of experimental expect/actual classes.

Compose is aligned to 1.13.0-alpha01 across all targets. The plugin selects the
Skiko 0.152.0-alpha02 runtime variant; the separate browser version override and
custom runtime configuration are removed. The Skiko compile/runtime compatibility
check and strict Wasm linkage remain. Repository setup now uses standard Gradle
project APIs through the shared dependency-repositories script.

The KMP Android targets have host tests configured. The application's fixed
orientation restriction is removed, and Android lint treats warnings as errors.
Final verification passed 359 tests with no failures or skips: 166 JVM tests
(94 engine, 21 domain, 51 application), 142 Android host tests (80 engine,
21 domain, 41 application) and 51 browser adapter tests. All six rendered-demo
frame counts and sampled hashes remain unchanged. Strict Android dependency lint
reports no issues. Android packaging, both iOS Kotlin targets, production Wasm
and the Skiko pairing check pass without compiler diagnostics.

Sixteen isolated fixtures exercise valid names/literal text and reject malformed
function/property/parameter/constant names, suppression annotations/comments,
disabled compiler diagnostics, Android/shell suppressions, forbidden renderer
calls and new core coupling. The source layout check covers 396 Kotlin files.
A PSI audit finds zero engine production parameter shadows and zero unused
non-override parameters. Independent comparison across 278 engine files confirms
that the naming pass preserved strings, literal values and operators; the
reviewed dead menu-index declarations and unused imports were removed.

A headless run completed 80 tics and wrote three 320×200 frames. The production
browser build rendered the title, running demo, episode/skill menus and a new
game after the revised wipe transition, with no console errors or warnings.
Physical-device and multi-hour runtime validation remain separate.

Two build-performance notices remain visible. Kotlin 2.4.10's npm plugin resolves
`wasmJsNpmAggregated` and test configurations while constructing task dependencies
in `NodeJsRootPluginApplier.kt:337–348` and `KotlinCompilationNpmResolver.kt:148/214`;
AGP reports this as early dependency resolution. Removing the library browser
environments breaks npm installation, so that attempted workaround was reverted.
Correcting the plugin's eager resolver requires an upstream change. Webpack also
emits its default asset/entrypoint-size hints for the required Skiko/game Wasm
payloads and JavaScript runtime. No log filters, disabled hints or raised size
thresholds were added. These notices are distinct from compiler and lint results.

The Linux CI runner also reports that iOS simulator tests require macOS. Both
iOS Kotlin targets were compiled locally on macOS; the Linux notice remains
visible without disabling targets or diagnostics. CI actions use Node 24 runtimes
to avoid the deprecated Node 20 action runtime and its bundled Node API warnings.

Earlier completed results follow below.

### Earlier engine package/name migration

The migrated source passed all 166 JVM tests: 94 engine, 21 domain and 51
application tests. The six rendered-demo frame counts and sampled ARGB hashes
remained unchanged. `verifyArchitecture` and the strengthened
`verifyKotlinSourceLayout` passed across 396 Kotlin files.

Temporary guard fixtures accepted matching platform filenames, companions and
anonymous objects, and rejected lowercase filenames, legacy type names,
type/filename mismatches, misplaced or missing packages, fake declarations in
comments/strings, and extra nested types. Architecture fixtures also rejected
new core-coupled services, root-package implementation files, gameplay calls into
renderer geometry, and renderer calls into tick scheduling. All fixtures were
removed before the final clean-tree guard run.
Both verification tasks also passed with Gradle's configuration cache enabled,
first storing the configuration and then reusing it. The layout task owns its
lexer and managed file inputs, avoiding unsupported script-instance capture.

A Kotlin-token comparison covers all 4,087 production top-level declarations.
After the intended identifier renames, their content is identical when imports,
comments and whitespace are excluded; string literal contents remain part of
the comparison. An independent review matched 389 files plus declarations
from the split sources and found no omissions. These checks support the intended
name/layout-only scope alongside the behavior regressions.

The final verification run, after unused-import cleanup, completed in 28 seconds
and included the JVM suites, architecture checks and these platform tasks:
`:androidApp:assembleDebug`, `:composeApp:compileKotlinIosArm64`,
`:composeApp:compileKotlinIosSimulatorArm64` and
`:composeApp:wasmJsBrowserDistribution`, including the Skiko runtime
compatibility check. These results cover Android packaging, both iOS Kotlin
targets and the production web distribution. They do not establish a linked iOS
application, physical-device validation or a new browser runtime check.
The records below retain earlier verification context.

### Earlier browser freeze follow-up

The reported live tab responded during inspection: frames advanced, and the
menu responded after focusing the game. No console error or graphics-loss
dialog was captured, so the precise cause of that occurrence is unconfirmed.

Two concrete stall paths were found and corrected. The browser now derives its
application lifecycle from current visibility and focus together, avoiding the
upstream event-order case where a visible, focused page remains only STARTED.
Screen wipes ignore nonpositive elapsed ticks, rebase backward clock samples,
and stop scanning once a large batch has completed the image. Their ordinary
pixel progression and following-tick completion notification remain unchanged.

That verification passed 166 JVM tests (94 engine, 21 domain, 51 application),
51 browser adapter tests, architecture/source-layout checks, and the production
Wasm build with the Skiko compatibility check. The six existing rendered-demo
frame counts and pixel hashes matched their pre-refactor baselines.
Local browser checks exercised the launcher, title, episode/skill menus and a
new level with no console errors. Browser Back reloaded the page in the test
browser; back-forward cache state restoration is covered by the event tests,
not claimed as an observed live-browser result.

### Earlier file-layout verification

That post-layout verification passed all 163 JVM tests, Android debug packaging,
iOS arm64/simulator Kotlin compilation, and the production Wasm distribution
with the Skiko compatibility check. Swift source parsing and the Xcode project
file check passed after separating `ComposeView.swift` from `ContentView.swift`.
These checks do not constitute a full linked iOS app or physical-device test.

`verifyKotlinSourceLayout` passed for all 391 Kotlin files. An independent Kotlin
syntax-tree scan found 290 named declarations, each in its own matching file.
Temporary fixtures confirmed that the guard accepts companions, anonymous
objects, comments and strings, and rejects extra nested types, additional type
aliases and incorrectly named files containing external declarations. The
fixtures were removed. `verifyArchitecture` now runs this check in CI.

The five JavaScript browser-test fixture classes also live in separate matching
files under `scripts/tests/fixtures/`; their former harness-local state is passed
through constructors. All 35 browser adapter tests passed after that split.

The following paragraphs retain the earlier engine-architecture verification
record, including runtime checks performed before the file-layout cleanup.

That JVM run passed 163 tests with no failures or skips: 91 engine,
21 domain and 51 application tests. All six rendered-demo regressions matched
the pre-refactor end tics, frame counts and sampled pixel hashes. The architecture
task passed for that source tree; a temporary negative fixture also confirmed
that it rejected a service and state holder depending on the whole engine. The fixture
was removed after verification.

The same verification run successfully packaged the Android debug application,
compiled the application and engine for iOS arm64 and simulator arm64, and built
the production Wasm distribution with the Skiko runtime compatibility check.
That historical run included the former browser Compose version split and
Kotlin opt-in/expect-actual warnings; it was not a warning-free build. The current
toolchain cleanup addresses those sources separately. That verification record
did not establish a deployment.

Local browser smoke checks exercised the chooser, title screen, running demos,
keyboard-driven episode/skill menus and a new game. After rebuilding the final
lifecycle change, a page reload rendered the title and game again, and quitting
returned to the application's restart/choose-game controls. Restart then created
a fresh engine and resumed rendering. These checks showed no browser console
errors. They do not establish multi-hour rendering stability.

The following coverage exists in source and defines the acceptance scope:

| Coverage | Behaviors checked |
| --- | --- |
| Domain and presentation | Frame ownership, input gating, lifecycle cancellation/resume, failed/quit states, import validation, remembered startup and storage failures. |
| Engine lifetime | Single-use boot, immutable clock epoch, input gating, host detachment, music resume and primary/suppressed cleanup failures. |
| Scheduling | Fake-clock polling, backlog rejection after input collection, bounded catch-up, command-ring wrap and copy isolation, single-tic ordering. |
| World updates | Same-tic thinker insertion, deferred/self removal, level-time restoration and pause, deterministic player/thinker/special ordering. |
| Menus | Typed command choices, drawing capability separation, lazy navigation links and independent mutable menu instances. |
| Geometry and rendering | Angle/distance/side behavior without camera mutation, explicit BSP lookup, patch/block pixels, palette conversion, wipe transitions and render checkpoint order. |
| Resources and persistence | Archive bounds and transactional replacement, typed configuration, argument snapshots/validation, PCX encoding and session screenshot numbering. |
| Integrated simulation | Complete bundled demo checkpoints, interleaved engines, mutable-table isolation and fresh startup after another engine fails. |
| Rendered compatibility | All three bundled demos in single-tic and timed modes, comparing end tics, frame counts and sampled ARGB hashes with the pre-refactor binary. |
| Application adapters | Actual engine startup/detach/resume, injected mixer selection, optional audio, frame handoff and cleanup failures. |
| Build and runtime | Architecture guards, JVM suites, Android packaging, iOS device/simulator compilation, production Wasm/Skiko compatibility, and runtime rendering checks. |

Canonical demo expectations are maintained in `BundledDemoRegressionTest` and
`InstanceIsolationTest`. They correspond to the current bundled Ultimate DOOM
fixture, not the older shareware fixture and its historical hashes. Checkpoints
cover player/world/random state periodically and at completion. They characterize
this Kotlin port, not independent equivalence to the DOS executable. Those
checks disable drawing and use single-tic stepping, so they cannot replace
rendering tests or fake-clock timing tests.

Earlier runtime work exercised all maps in a locally supplied Ultimate DOOM WAD
for startup/rendering, as well as browser import, remembered startup and chooser
flows. Earlier resize stress and forced WebGL context loss/restoration checks
exercised the independent recovery dialog and explicit reload. Browser drop
checks used actual browser file/event APIs; an OS-level Finder drag was not
automated. These are historical smoke checks, not current-refactor results,
complete campaign playthroughs or proof of multi-hour stability.

The aligned Compose stack retains the matched browser Skiko runtime and upstream
[graphics-context lifetime correction](https://github.com/JetBrains/skiko/pull/1256),
strict Wasm linkage and a packaged-runtime compatibility check. The synthetic
resize watchdog was removed. Actual context loss exposes a DOM recovery dialog
outside the failed canvas; reload is explicit and can lose unsaved progress.
Listener cleanup, stale callbacks, reader cancellation and recovery actions have
separate JavaScript regression coverage.

Deployment remains the existing GitHub Pages workflow. Build verification and
public page/asset checks are distinct; a local architecture change does not
itself establish a deployed result. Earlier Pages configuration work switched
from branch publishing to workflow deployment to avoid competing publishers.

## Remaining findings and limits

1. **P2 — Legacy subsystem coupling remains.** Actor behavior, collision,
   gameplay, savegames and low-level raster algorithms still access shared
   mutable records through core extensions. They require further behavior
   extraction, explicit collaborators and invariant ownership. Removing state
   constructor backreferences is a useful boundary, not a substitute for that
   work. Do not describe the complete engine as fully SOLID or cleanly layered.
2. **P2 — Binary content validation is incomplete.** WAD directory bounds are
   validated, but map relationships and save/demo streams still assume valid
   content. Structural import checks do not prove every record is safe. Dedicated
   readers and malformed map/save/demo fixtures are needed for recoverable errors.
3. **Rendering recovery limit.** The removed bitmap heartbeat was not a true
   presentation signal. Context-loss events now expose a recovery action, but
   stalls that emit no such event still need reliable presentation-level
   detection. Synthetic resizes should not be reintroduced as a generic remedy.
4. **Hardware verification limit.** Compilation and headless tests do not prove
   playback, audio interruption or storage failure behavior on every physical
   Android/iOS device, browser or audio device. Multi-hour and device-specific
   runtime checks remain separate work.
5. **Build maintenance.** Platform and browser toolchains have deliberate
   version constraints, including the browser renderer pairing. Dependency
   upgrades must preserve target compatibility and rerun resolution/build checks;
   successful compilation alone does not establish browser rendering correctness.
6. **Feature limits.** Multiplayer is not implemented. Embedded DeHackEd/BEX
   changes are not applied, and the current mixer does not configure a music
   synthesizer backend. These are independent of WAD campaign availability.
7. **Campaign verification limit.** Map startup/rendering and demo checkpoints
   do not establish a complete Ultimate DOOM campaign playthrough or DOOM II
   coverage. Current assets were not changed as part of this architecture work.
