# DOOM — Kotlin Multiplatform

A port of the original **DOOM (1993)** engine to pure Kotlin, running on
**Android, iOS, Web (Wasm), and Desktop** from a single codebase with
[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

**▶ Play it in your browser: [kunal26das.github.io/doom](https://kunal26das.github.io/doom/)**

The game algorithms were translated from id Software's released engine source
(`linuxdoom-1.10`), preserving the original renderer (BSP, visplanes, fuzz
effect, screen-melt), single-player gameplay (monsters, weapons,
sector specials), menus, HUD, status bar, automap, intermissions, finales,
cheats, demo playback, and savegames, retaining compatibility-sensitive behavior
covered by deterministic demo checks. The application uses MVVM, domain ports
and constructor injection. Each engine owns its runtime, with separate services
for lifecycle, scheduling, rendering, geometry, configuration and capture.
Engine internals use responsibility-based packages and descriptive Kotlin type
names. Legacy gameplay and raster algorithms still share an internal core model;
[ARCHITECTURE.md](ARCHITECTURE.md) explains those remaining boundaries.

## Play your original DOOM game

If no imported game is remembered, the chooser offers **PLAY DEMO** to start
the bundled resource. Despite that legacy label, the current `doom1.wad` bundle
contains Ultimate DOOM data, not the nine-level shareware episode. To select a
local copy in the browser, drag `DOOM.WAD` from your installation onto the launcher. The drop area highlights while a file
is over the page. You can also select **LOAD DOOM.WAD** and choose the file;
the picker is available on every platform. `DOOM2.WAD` is also supported.
Import one `.wad` file at a time, up to 64 MB. Choose **New Game** in the
engine's own menu to begin.

The app remembers an imported file on this device and starts it automatically
on later visits. Playing the demo does not replace a remembered full game.

The file stays on your device and is never uploaded. Web stores it as binary
data in IndexedDB; native apps use local file storage. If storage is unavailable
or full, **PLAY FOR THIS VISIT** starts the validated file without remembering
it. Clearing the app's data or the browser's site storage requires another import.

After quitting or an engine error, **RESTART** creates a fresh session with the
same game data; **CHOOSE GAME** returns to the demo/import screen. Saves and
settings are separated by game-data content and remain available after a restart.

The browser uses a renderer fix for delayed black screens after resizing.
If the browser loses its graphics context, a visible **RELOAD GAME** action
replaces the blank display. Reloading keeps the remembered WAD and saved games,
but discards unsaved progress; recovery never reloads the page automatically.

The bundled Ultimate DOOM resource comes from earlier project work; this
architecture refactor does not add or replace game assets. Earlier map smoke
checks covered startup and rendering, not a complete campaign playthrough.

## What's inside

| | |
|---|---|
| Engine | `engine/src/commonMain/kotlin/doom/engine/` — public facade and host/input ports at the root; implementation packages for core, gameplay, world, rendering, audio, resources, geometry, simulation and runtime; no Compose or platform APIs |
| Domain | `domain/` — game contracts, frame snapshots, imported-game storage port, session orchestration and WAD validation |
| Presentation | `composeApp/.../presentation/` — lifecycle ViewModels, observable UI state, local import, game screen, restart/choose-game actions and input controls |
| Adapters | `composeApp/.../data/` — engine lifecycle adapter, resource/storage repositories and an injected audio-mixer factory |
| Data tables | generated from the original C and located with actor, audio, geometry, rendering and resource code |
| Platform layer | Compose UI, keyboard/touch input, PCM audio (AudioTrack / AVAudioEngine / javax.sound / Web Audio), storage |
| Game data | The current `doom1.wad` resource contains Ultimate DOOM data and powers the legacy PLAY DEMO choice; local IWAD import remains available. |

Architecture, ownership and extension points are documented in [ARCHITECTURE.md](ARCHITECTURE.md).
The audit and remaining limitations are in [AUDIT.md](AUDIT.md).
Engine translation conventions are documented in [PORTING.md](PORTING.md).

Kotlin production and test sources keep each named type in its own
`TypeName.kt` file, including sealed variants and test helpers. Anonymous
objects and companions may stay with their owner; functions/constants-only
files remain allowed with descriptive PascalCase names. Package declarations
match source directories. `verifyKotlinSourceLayout` checks these rules in CI;
it also enforces lowerCamelCase engine functions/properties and UPPER_SNAKE_CASE
constants, and rejects diagnostic suppression annotations. Compiler builds enable
extra warnings and treat warnings as errors; Android lint does the same.
`verifyArchitecture` separately checks dependency direction and restricts core
access to the existing compatibility implementations listed in
`gradle/engine-compatibility-sources.txt`. The package map and remaining shared
dependencies are described in [ARCHITECTURE.md](ARCHITECTURE.md#engine-package-map).

## Building & running

Requires JDK 17+. The Android SDK / Xcode are only needed for their respective targets.
All targets use Compose 1.13.0-alpha01. The web renderer uses Skiko
0.152.0-alpha02 through Compose's variant selection, with a compatibility check
on the resolved compile/runtime dependencies.

```sh
# Desktop (macOS/Windows/Linux)
./gradlew :composeApp:run

# Web (Kotlin/Wasm) — opens a dev server
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Android
./gradlew :androidApp:installDebug

# iOS — open iosApp/iosApp.xcodeproj in Xcode, set your team, run
```

## Verification

```sh
# Architecture, source layout, domain/ViewModel tests, engine regressions and demo checkpoints
./gradlew verifyArchitecture verifyKotlinSourceLayout :domain:jvmTest :engine:jvmTest :composeApp:jvmTest

# Browser file import and graphics recovery behavior (Node.js 20+; no packages to install)
node --test scripts/tests/*.test.mjs

# Deterministic demo playback, without a window, audio, or access to your saves
./gradlew :composeApp:runHeadless --args="--demo DEMO1 --ticks 10000 --no-frames"

# Target compilation
./gradlew :composeApp:compileKotlinWasmJs :composeApp:compileAndroidMain :composeApp:compileKotlinIosSimulatorArm64
```

Pull requests run regression tests on Linux and Windows, plus engine and storage
tests in headless Chrome. Deployment also runs the tests before publishing.
The app pauses simulation and releases audio when it is
not resumed; returning resumes the same engine instance without advancing time
spent in the background. Quitting or a fatal engine error ends that session;
**RESTART** creates another independent engine without restarting the app, and
**CHOOSE GAME** returns to the demo/import screen. Regression coverage and remaining
verification limits are recorded in [AUDIT.md](AUDIT.md).

## Controls

Original DOOM bindings on desktop/web: **arrows** move/turn, **Ctrl** fire,
**Space** use, **Shift** run, **Alt** strafe, **,**/**.** strafe left/right,
**1–7** weapons, **Tab** automap, **Esc** menu, **F2/F3/F6/F9** save/load,
**+/-** view size, **F11** gamma. Type the classic cheats (`iddqd`, `idkfa`,
`idclev31`, …) during play.

On phones/tablets: left virtual stick (move + strafe), drag right half to turn,
on-screen FIRE / USE / weapon-cycle / RUN / MAP / ESC buttons.

**F2** opens Save Game, **F3** opens Load Game, **F6** quicksaves and **F9**
quickloads. Empty slots start with the current map name; confirm it or edit it.
The ESC menu also provides Save Game and Load Game. Touch controls navigate
menus with the stick and confirm with FIRE. Custom save-name text entry requires
a keyboard; saving with the suggested name does not.

Saves stay on the device: desktop uses `~/.doom-kmp`, Android uses private app
storage, iOS uses the app's Documents directory, and web uses local storage for
the current site. Reloading the browser preserves saves; clearing site data or
app storage removes them. Imported WADs have separate save slots identified by
their content. Saves are not synchronized between devices.

## Game data & licensing

- **Engine**: derivative of id Software's DOOM source, released under the
  **GNU GPL v2** — this project is likewise GPL-2.0 (see [LICENSE](LICENSE)).
  Consolidated engine and Gradle launcher notices are in [NOTICE](NOTICE).
  DOOM is a trademark of id Software LLC; this project is not affiliated with
  or endorsed by id Software.
- **Game data**: the current `doom1.wad` contains Ultimate DOOM retail data from
  earlier project work. It is separate from the engine source and is used by
  the bundled-play choice and compatibility fixtures. Its filename and the
  legacy PLAY DEMO label should not be interpreted as a shareware designation.
