# MCSR Easy Mode

Minecraft speedrunning practice, but less RWDA.

This mod is for practice only and is not legal for leaderboard submissions.

Current release: `1.0`

## What It Does

MCSR Easy Mode is a Fabric `1.16.1` practice mod that adds toggleable quality-of-life changes, mob aggression softening, Ranked-style RNG standardization, and optional worldgen practice helpers.

Highlights:

- Toggle piglin, ghast, and hoglin aggression.
- Show remaining status-effect time beneath transparent top-right HUD icons (QoL: Status Effect Timers).
- Configure RNG as `Vanilla`, `Ranked`, or per-feature `Custom`.
- Standardize key speedrun resources such as rods, pearls, obsidian, string, flint, eyes, and selected chest loot.
- Improve hoglin stable layouts while preserving vanilla bastion placement rules.
- Add optional village and stronghold practice helpers.
- Move the pause menu Save and Quit button away from common misclick paths.
- Display keybind labels on the vanilla hotbar and hotbar slots inside inventory/container screens.
- Hide advancement toasts and optionally echo debug logs into chat.
- Mark the F3 debug HUD with `MCSR Easy Mode` and the active RNG mode.

## Local Nether routes (experimental)

In **Nether Terrain**, enable **Entry -> Bastion** and/or **Bastion -> Fortress**. Both default to off and work with vanilla noise values or global noise tuning. Choose settings before creating a world; existing chunks are not regenerated.

The anchor uses the player's first intended Nether destination X/Z, captured after coordinate scaling and border clamping but before portal search generates chunks. It is saved with the world and stays fixed on later entries and reloads. Routing height remains Y 64. Until an entry is captured, terrain is left unchanged. The mod selects the nearest valid bastion start within 1,024 blocks, then the nearest valid fortress start within 1,024 blocks of that bastion. Missing targets leave the corresponding route unchanged. These are bounded searches, not Ranked's distance guarantees or its exact terrain check.

Routes favor nearby existing cavities, then locally reduce terrain density in broad, blended openings. Structures generate afterward. The lava sea and bedrock bands are preserved; bridging and elevation changes may still be necessary. This does not guarantee a safe walking route, a particular structure entrance, or an exact match to the final portal blocks (portal placement can shift the destination). Previously generated chunks are not modified.

## Requirements

| Component | Requirement |
| --- | --- |
| Minecraft | `1.16.1` |
| Mod loader | Fabric Loader `0.16.3` or newer compatible loader |
| Java for playing | Java 8 compatible runtime |
| Required mod | SpeedrunAPI for Minecraft `1.16.1` |
| Bundled library | Fabric Resource Loader v0 |
| Full Fabric API | Not required |

## Mod compatibility with other mods/practice kits

| Mod | Status | Notes |
| --- | --- | --- |
| Mini Practice Kit (MPK) | Compatible | MPK's Nether Exit (Blind) minimum Y level is 41, so cave blinds are possible |
| Speedrun Practice | Compatible | |
| AreEssGee | Limited | Can interfere with loot tables and pity system (blaze, flint rarity). Stronghold anti-corruption is compatible with the Max Stronghold Room feature. |
| LazyStronghold | Compatible | Does not interfere with the stronghold anti-corruption feature |
| MCSR Ranked | Incompatible | Not legal (https://mcsrranked.com/guidelines) |
| MCSR FairPlay | Incompatible | Not legal (https://mc.sr/mods/) |
| PogLoot | Incompatible | Does not force crash the game |


Attempting to run an instance with this mod and an incompatible mod will result in a force crash by the Fabric Loader (incompatible mods found warning).

## Install

Download the mod jar from releases and place it in your instance `mods` folder alongside SpeedrunAPI.

For more details, see the [wiki](https://codeberg.org/dota1g/mcsr-easy-mode/wiki).

## Build Quickstart

```powershell
gradle build
```

If Gradle is still using Java 8, point it at a newer JDK first:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
gradle build
```

The compiled jar is created at:

```text
build/libs/mcsreasymode-<version>.jar
```

## Release build

Run `gradle build -PreleaseBuild=true` to build the release UI profile into `build-release/libs/mcsreasymode-release-<version>.jar`. The regular `gradle build` keeps all settings and writes to `build/libs/`. Each profile has its own build output; no clean is required when switching.

The release settings hide the Easy Mode marker toggle, bastion type selector, and End entry hotbar sort. Nether terrain controls (including global noise sliders and presets), local Nether routes, basalt replacement, and ocean ravine settings remain available. Hiding these controls preserves their saved values and runtime behavior; the Easy Mode marker remains present by default.

The pre-release-settings jar is preserved separately under `artifacts/preserved/`, outside both build directories.

## Pie chart shortcuts

Open **QoL > Pie Chart Paths** to edit the ordered path list. Names are optional; paths use dotted notation such as `root.gameRenderer.level.entities`. Apply edits, add/remove entries, or move them earlier/later in the cycle.

Bind **Cycle Pie Chart Path** using the editor's Key button or Minecraft Controls under **MCSR Easy Mode**. It starts unbound to avoid conflicting with existing keys. Press it in-world to open the debug pie chart and jump to the next configured path; normal number-key navigation still works. A path can be selected before profiler data for that section exists.

Paths are saved to `config/mcsreasymode-pie-paths.json`. The shortcut key is saved independently in `config/mcsreasymode-pie-key.txt`, so StandardSettings cannot reset it to unbound. Changes made through Minecraft Controls are also remembered. StandardSettings is not required. Its world-join path reset can coexist with these in-world shortcuts.
