# MCSR Easy Mode

Making minecraft speedrunning less RWDA. For practicing only, not legal for leaderboards

Current release: `0.3`

## Features

- Toggle piglin, ghast, and hoglin aggression individually.
  - Piglin anti-aggression spoofs gold armor checks, so the player does not need to wear gold armor. Vanilla anger triggers such as attacking piglins, opening guarded containers, or mining guarded gold-related blocks can still provoke piglins.
  - Hoglin anti-aggression clears attack targeting and suppresses angry ambient behavior.
  - Ghast anti-aggression blocks targeting and fireball behavior.
- Move the pause menu `Save and Quit` button to the bottom-left corner to reduce accidental quits.
- Display hotbar hotkey labels directly on the vanilla hotbar slots.
  - Uses the player's current Minecraft keybinds.
  - Includes the offhand swap key when the offhand slot is visible.
  - Shortens side mouse buttons to compact labels such as `M4` and `M5`.
  - Includes a customization screen with a live preview, label corner, text size, and background options.
- `RNG` mode selector:
  - `Vanilla` keeps normal Minecraft behavior.
  - `Ranked` enables reliability adjustments inspired by MCSR Ranked.
- Ranked RNG adjustments currently include:
  - Iron golems always drop exactly 4 iron ingots.
  - Thrown eyes of ender always drop back as items.
  - Blaze rods use 50% chance with no more than 2 dry player kills.
  - Piglin barters pity a trade of ender pearls after 24 dry barters.
  - Piglin barters pity 1 obsidian after 24 dry barters.
  - Gravel keeps its vanilla 10% flint chance, but forces flint after 9 failed drops.
  - The first eligible bastion ramparts or housing chest guarantees at least 3 iron ingots and 5 obsidian.
  - Ruined portal chests guarantee speedrun-useful resources.
  - Buried treasure chests guarantee speedrun-useful resources.
  - The first eligible blind travel portal from the Nether is surfaced to avoid cave spawns.
- An `Adjustments` screen in the config menu lists implemented and planned RNG changes.

## Ranked RNG Details

When `RNG` is set to `Ranked`, the mod applies the following standardizations.

### Drops and Barters

- Iron golems drop exactly 4 iron ingots.
- Eyes of ender thrown by the player always drop back instead of breaking.
- Blaze rods keep a 50% chance, with a pity rod after 2 dry player-killed blazes.
- Piglin barters share one world-level pity counter across all piglins:
  - Ender pearls are added after 24 dry barters without pearls.
  - Obsidian is added after 24 dry barters without obsidian.
- Gravel keeps vanilla 10% flint odds, with a guaranteed flint drop on the 10th failed gravel drop.

### Chest Standardization

- When `RNG` is set to `Ranked`, the first eligible bastion ramparts or housing chest ensures at least:
  - 3 iron ingots
  - 5 obsidian
- The bastion adjustment uses the generated vanilla chest contents as a base, counts existing iron and obsidian first, and only adds the missing amount.
- If a standardized item needs a slot that is already occupied, the mod tries to move the blocking vanilla item into a free slot before placing the standardized item.
- The standardized bastion slots are randomized to keep the result closer to vanilla chest layouts.

The mod also ships custom loot table overrides for other speedrun-relevant chests:

- Ruined portal chests can include guaranteed speedrun-useful resources such as:
  - fire charges
  - iron nuggets
  - obsidian
  - golden carrots
- Buried treasure chests can include guaranteed speedrun-useful resources such as:
  - heart of the sea
  - iron ingots
  - TNT

### Blind Portal Anti-Cave

The first blind travel portal from the Nether to the Overworld is moved to the surface when:

- `RNG` is set to `Ranked`
- the player enters the portal from the Nether
- the Nether portal is built at Y level 48 or above
- the anti-cave surfacing has not already been used in that world

This is intended for the first Nether exit used for stronghold triangulation. It is once per world, so later Nether-to-Overworld portals, such as a second portal used for shorter stronghold travel, keep vanilla behavior.

## Requirements

- Minecraft `1.16.1`
- Fabric Loader `0.16.3` or newer compatible loader
- SpeedrunAPI for Minecraft `1.16.1`

This mod does not require the full Fabric API. It bundles Fabric Resource Loader v0 so the included loot table resources can load.

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
- `Adjustments`, which opens a summary of RNG changes
- `Piglins aggression: Disabled / Vanilla`
- `Ghasts aggression: Disabled / Vanilla`
- `Hoglins aggression: Disabled / Vanilla`
- `Move Save & Quit: On / Off`
- `Display Hotbar Hotkeys: On / Off`
- `Customize Hotbar Hotkeys`, available when hotbar hotkeys are enabled

Hotbar hotkey customization:

- `Label Corner`: chooses which corner of each hotbar slot receives the keybind label
- `Text Size`: cycles through sizes `1` to `4`
- `Background`: cycles through available label backgrounds
- The preview uses your current Minecraft hotbar and offhand keybinds

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

If your shell is still using Java 8, point Gradle at a newer JDK first:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
gradle build
```

The compiled jar will be created at:

```text
build/libs/mcsreasymode-0.3.jar
```

Install that jar into your `mods` folder alongside SpeedrunAPI.

## Development Notes

- Minecraft version: `1.16.1`
- Yarn mappings: `1.16.1+build.21`
- Fabric Loom: `1.15.5`
- SpeedrunAPI dependency: `com.github.contariaa:SpeedrunAPI:v2.1-1.16.1`
- Fabric Resource Loader v0 is bundled through Gradle for resource loading.
- Full Fabric API is intentionally not used.
