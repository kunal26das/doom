# DOOM — Kotlin Multiplatform

A faithful port of the original **DOOM (1993)** engine to pure Kotlin, running on
**Android, iOS, Web (Wasm), and Desktop** from a single codebase with
[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

**▶ Play it in your browser: [kunal26das.github.io/doom](https://kunal26das.github.io/doom/)**

This is a line-by-line translation of id Software's released engine source
(`linuxdoom-1.10`), preserving the original renderer (BSP, visplanes, fuzz
effect, screen-melt), the complete game simulation (all monsters, weapons,
sector specials), menus, HUD, status bar, automap, intermissions, finales,
cheats, demo playback, and savegames — including the original engine's bugs,
because demo playback desyncs without them.

## What's inside

| | |
|---|---|
| Engine | `composeApp/src/commonMain/kotlin/doom/engine/` — pure common Kotlin, no platform APIs |
| Data tables | machine-generated from the original C (`states`, `mobjinfo`, trig LUTs, strings) |
| Platform layer | Compose UI, keyboard/touch input, PCM audio (AudioTrack / AVAudioEngine / javax.sound / Web Audio), storage |
| Game data | `DOOM1.WAD` — the freely-distributable shareware episode *(Knee-Deep in the Dead)* |

Porting conventions are documented in [PORTING.md](PORTING.md).

## Building & running

Requires JDK 17+. The Android SDK / Xcode are only needed for their respective targets.

```sh
# Desktop (macOS/Windows/Linux)
./gradlew :composeApp:run

# Web (Kotlin/Wasm) — opens a dev server
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Android
./gradlew :androidApp:installDebug

# iOS — open iosApp/iosApp.xcodeproj in Xcode, set your team, run
```

## Controls

Original DOOM bindings on desktop/web: **arrows** move/turn, **Ctrl** fire,
**Space** use, **Shift** run, **Alt** strafe, **,**/**.** strafe left/right,
**1–7** weapons, **Tab** automap, **Esc** menu, **F2/F3/F6/F9** save/load,
**+/-** view size, **F11** gamma. Type the classic cheats (`iddqd`, `idkfa`,
`idclev31`, …) during play.

On phones/tablets: left virtual stick (move + strafe), drag right half to turn,
on-screen FIRE / USE / weapon-cycle / RUN / MAP / ESC buttons.

## Game data & licensing

- **Engine**: derivative of id Software's DOOM source, released under the
  **GNU GPL v2** — this project is likewise GPL-2.0 (see [LICENSE](LICENSE)).
  DOOM is a trademark of id Software LLC; this project is not affiliated with
  or endorsed by id Software.
- **Shareware WAD**: `doom1.wad` (v1.9) is the shareware episode, which id
  licensed for free distribution. The retail episodes and DOOM II data remain
  commercial products — buy them (they're on Steam/GOG) and the engine will
  happily run `doom.wad` / `doom2.wad`.
