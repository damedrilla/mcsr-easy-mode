# MCSR Easy Mode

MCSR Easy Mode is a client-side Fabric mod for Minecraft 1.16.1 that adds practice-friendly speedrun assists through the SpeedrunAPI mod menu.

## Features

- Toggle piglin, ghast, and hoglin aggression individually.
- `RNG` mode selector:
  - `Vanilla` keeps normal Minecraft behavior.
  - `Ranked` enables reliability adjustments inspired by MCSR Ranked.
- Ranked RNG adjustments currently include:
  - Iron golems always drop exactly 4 iron ingots.
  - Thrown eyes of ender always drop back as items.
  - Blaze rods use 50% chance with no more than 2 dry player kills.
  - Piglin barters pity a trade of ender pearls after 72 dry barters.
  - Piglin barters pity 1 obsidian after 24 dry barters.
- An `Adjustments` screen in the config menu lists implemented and planned RNG changes.

## Requirements

- Minecraft `1.16.1`
- Fabric Loader `0.16.3` or newer compatible loader
- SpeedrunAPI for Minecraft `1.16.1`

This mod does not require the full Fabric API.

## Download

Download the latest mod jar from the repository releases page, then place it in your Minecraft `mods` folder.

You also need to install SpeedrunAPI in the same `mods` folder.

Common locations:

- Windows: `%appdata%\.minecraft\mods`
- macOS: `~/Library/Application Support/minecraft/mods`
- Linux: `~/.minecraft/mods`

For MultiMC, Prism Launcher, or other instance-based launchers, put both jars in that instance's `mods` folder.

## Configuration

Launch Minecraft with Fabric, then open:

`Options` -> `SpeedrunAPI Mods` -> `MCSR Easy Mode`

Available options:

- `RNG: Vanilla / Ranked`
- `Piglins aggression: Disabled / Vanilla`
- `Ghasts aggression: Disabled / Vanilla`
- `Hoglins aggression: Disabled / Vanilla`
- `Adjustments`, which opens a summary of RNG changes

## Building From Source

Install a JDK that can run Gradle. JDK 17 or newer is recommended for the Gradle runtime. The mod itself is compiled for Java 8 bytecode.

Clone the repository:

```powershell
git clone <repo-url>
cd mcsr-easy-mode
```

Build the mod:

```powershell
gradle build
```

If you use the Gradle wrapper, run:

```powershell
.\gradlew.bat build
```

The compiled jar will be created at:

```text
build/libs/mcsreasymode-0.1.jar
```

Install that jar into your `mods` folder alongside SpeedrunAPI.

## Development Notes

- Minecraft version: `1.16.1`
- Yarn mappings: `1.16.1+build.21`
- Fabric Loom: `1.15.5`
- SpeedrunAPI dependency: `com.github.contariaa:SpeedrunAPI:v2.1-1.16.1`
- Full Fabric API is intentionally not used.
