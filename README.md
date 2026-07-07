# MCSR Easy Mode

Minecraft speedrunning, but less RWDA.

This mod is for practice only and is not legal for leaderboard submissions.

Current release: `0.7`

## What It Does

MCSR Easy Mode is a Fabric `1.16.1` practice mod that adds toggleable quality-of-life changes, mob aggression softening, and Ranked-style RNG standardization.

Highlights:

- Toggle piglin, ghast, and hoglin aggression.
- Switch between vanilla RNG and Ranked-style RNG adjustments.
- Standardize key speedrun resources such as rods, pearls, obsidian, flint, and selected bastion loot.
- Improve hoglin stable layouts while preserving vanilla bastion placement rules.
- Move the pause menu Save and Quit button away from common misclick paths.
- Display keybind labels on the vanilla hotbar and hotbar slots inside inventory/container screens.
- Customize hotkey label position, size, and background separately for HUD and handled screens.

## Requirements

- Minecraft `1.16.1`
- Fabric Loader `0.16.3` or newer compatible loader
- SpeedrunAPI for Minecraft `1.16.1`

This mod does not require the full Fabric API. It bundles Fabric Resource Loader v0 for included loot table resources.

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
