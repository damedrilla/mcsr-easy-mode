# MCSR Easy Mode

Minecraft speedrunning practice, but less RWDA.

This mod is for practice only and is not legal for leaderboard submissions.

Current release: `1.0`

## What It Does

MCSR Easy Mode is a Fabric `1.16.1` practice mod that adds toggleable quality-of-life changes, mob aggression softening, Ranked-style RNG standardization, and optional worldgen practice helpers.

Highlights:

- Toggle piglin, ghast, and hoglin aggression.
- Configure RNG as `Vanilla`, `Ranked`, or per-feature `Custom`.
- Standardize key speedrun resources such as rods, pearls, obsidian, string, flint, eyes, and selected chest loot.
- Improve hoglin stable layouts while preserving vanilla bastion placement rules.
- Add optional village and stronghold practice helpers.
- Move the pause menu Save and Quit button away from common misclick paths.
- Display keybind labels on the vanilla hotbar and hotbar slots inside inventory/container screens.
- Hide advancement toasts and optionally echo debug logs into chat.
- Mark the F3 debug HUD with `MCSR Easy Mode` and the active RNG mode.

## Requirements

| Component | Requirement |
| --- | --- |
| Minecraft | `1.16.1` |
| Mod loader | Fabric Loader `0.16.3` or newer compatible loader |
| Java for playing | Java 8 compatible runtime |
| Required mod | SpeedrunAPI for Minecraft `1.16.1` |
| Bundled library | Fabric Resource Loader v0 |
| Full Fabric API | Not required |

## Mod Compatibility

| Mod | Status | Notes |
| --- | --- | --- |
| SpeedrunAPI | Required | Used for the config/mods menu. |
| Fabric Resource Loader v0 | Bundled | Included inside this mod jar for mod-owned loot table resources. |
| Sodium `1.16.1` forks | Compatible | No known direct conflict. |
| SpeedRunIGT / timers | Compatible | This mod does not provide timing validation. |
| MCSR Ranked | Blocked | `fabric.mod.json` marks `mcsrranked` as incompatible. |
| MCSR FairPlay public | Blocked | `fabric.mod.json` marks `mcsr-fairplay-public` as incompatible. |
| Full Fabric API | Not needed | Installing it should not be required for this mod. |

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
