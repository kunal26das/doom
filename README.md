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

## Play DOOM

The app opens directly into the game. The bundled `doom1.wad` contains the full
Ultimate DOOM game, with all four episodes and 36 maps. Open the engine's menu
and choose **New Game** to select an episode and difficulty. There is no
game-file selection screen: every launch plays the bundled game.

After quitting or an engine error, **RESTART** creates a fresh session with the
same game data. Saves and settings remain available after a restart.

The browser uses a renderer fix for delayed black screens after resizing.
If the browser loses its graphics context, a visible **RELOAD GAME** action
replaces the blank display. Reloading keeps saved games, but discards unsaved
progress; recovery never reloads the page automatically.

The bundled Ultimate DOOM resource comes from earlier project work; this
architecture refactor does not add or replace game assets. Earlier map smoke
checks covered startup and rendering, not a complete campaign playthrough.

## What's inside

| | |
|---|---|
| Engine | `engine/src/commonMain/kotlin/doom/engine/` — public facade and host/input ports at the root; implementation packages for core, gameplay, world, rendering, audio, resources, geometry, simulation and runtime; no Compose or platform APIs |
| Domain | `domain/` — game contracts, frame snapshots, file and WAD repository ports, and session orchestration |
| Presentation | `composeApp/.../presentation/` — lifecycle ViewModels, observable UI state, game screen, restart action and input controls |
| Adapters | `composeApp/.../data/` — engine lifecycle adapter, resource/storage repositories and an injected audio-mixer factory |
| Data tables | generated from the original C and located with actor, audio, geometry, rendering and resource code |
| Platform layer | Compose UI, keyboard/touch input, PCM audio (AudioTrack / AVAudioEngine / javax.sound / Web Audio), storage |
| Game data | The current `doom1.wad` resource contains all four Ultimate DOOM episodes and starts automatically. |

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

# Browser lifecycle and graphics recovery behavior (Node.js 20+; no packages to install)
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
**RESTART** creates another independent engine without restarting the app.
Regression coverage and remaining verification limits are recorded in
[AUDIT.md](AUDIT.md).

## Controls

Original DOOM bindings on desktop/web: **arrows** move/turn, **Ctrl** fire,
**Space** use, **Shift** run, **Alt** strafe, **,**/**.** strafe left/right,
**1–7** weapons, **Tab** automap, **Esc** menu, **F2/F3/F6/F9** save/load,
**+/-** view size, **F11** gamma. Type the classic cheats (`iddqd`, `idkfa`,
`idclev31`, …) during play.

On phones/tablets, including mobile browsers, use the **left stick** to move
forward/back and turn left/right. Push diagonally to move and turn together.
Hold **FIRE** with your right thumb for continuous fire: movement, turning and
shooting use two thumbs. Dragging the right half is also available for fine
turning, but is optional. USE / weapon-cycle / RUN / MAP / ESC remain on screen.

Touching the stick away from its center starts movement immediately. Its knob
follows your thumb, and held buttons light up. A control stays held by the finger
that started it, even outside its circle, until that finger lifts or the gesture
is cancelled. Releasing FIRE leaves the stick active, and releasing the stick
leaves FIRE active.

Touch-capable browsers show these controls automatically, including tablets
using a desktop browser identity. Tap ESC to open the menu, move the stick
up/down to select an item, and tap FIRE to confirm. Browser page gestures are
disabled so dragging controls stays with the game.

**F2** opens Save Game, **F3** opens Load Game, **F6** quicksaves and **F9**
quickloads. Empty slots start with the current map name; confirm it or edit it.
The ESC menu also provides Save Game and Load Game. Touch controls navigate
menus with the stick and confirm with FIRE. Custom save-name text entry requires
a keyboard; saving with the suggested name does not.

Saves stay on the device: desktop uses `~/.doom-kmp`, Android uses private app
storage, iOS uses the app's Documents directory, and web uses local storage for
the current site. Reloading the browser preserves saves; clearing site data or
app storage removes them. Saves are not synchronized between devices.

## Game data & licensing

- **Engine**: derivative of id Software's DOOM source, released under the
  **GNU GPL v2** — this project is likewise GPL-2.0 (see [LICENSE](LICENSE)).
  Consolidated engine and Gradle launcher notices are in [NOTICE](NOTICE).
  DOOM is a trademark of id Software LLC; this project is not affiliated with
  or endorsed by id Software.
- **Game data**: the current `doom1.wad` contains Ultimate DOOM retail data from
  earlier project work. It is separate from the engine source and is used by
  automatic default launch and compatibility fixtures. Its filename should not
  be interpreted as a shareware designation.
