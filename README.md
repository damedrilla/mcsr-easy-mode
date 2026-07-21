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
