# Installation

## Requirements

| Component | Requirement |
| --- | --- |
| Minecraft | `1.16.1` |
| Mod loader | Fabric Loader `0.16.3` or newer compatible loader |
| Java for playing | Java 8 compatible runtime |
| Required mod | SpeedrunAPI for Minecraft `1.16.1` |
| Bundled library | Fabric Resource Loader v0 |
| Full Fabric API | Not required |

Fabric Resource Loader v0 is bundled so included loot table resources can load.

## Install Steps

1. Download the latest `mcsreasymode` jar from releases.
2. Download SpeedrunAPI for Minecraft `1.16.1`.
3. Place both jars in your Minecraft instance `mods` folder.
4. Launch the game with Fabric.

Common `mods` folder locations:

- Windows: `%appdata%\.minecraft\mods`
- macOS: `~/Library/Application Support/minecraft/mods`
- Linux: `~/.minecraft/mods`

For Prism Launcher, MultiMC, or other instance-based launchers, use that instance's `mods` folder.

## Compatibility

| Mod | Status | Notes |
| --- | --- | --- |
| SpeedrunAPI | Required | Used for the config/mods menu. |
| Fabric Resource Loader v0 | Bundled | Included inside this mod jar for mod-owned loot table resources. |
| Sodium `1.16.1` forks | Compatible | No known direct conflict. |
| SpeedRunIGT / timers | Compatible | This mod does not provide timing validation. |
| MCSR Ranked | Blocked | `fabric.mod.json` marks `mcsrranked` as incompatible. |
| MCSR FairPlay public | Blocked | `fabric.mod.json` marks `mcsr-fairplay-public` as incompatible. |
| Full Fabric API | Not needed | Installing it should not be required for this mod. |
