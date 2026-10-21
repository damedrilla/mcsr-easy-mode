# Configuration

Open the config through:

```text
Options -> SpeedrunAPI Mods -> MCSR Easy Mode
```

Configs are only available from the SpeedrunAPI Mods menu while outside a world.

The main config screen is scrollable and uses section dividers with compact rows.

## Main Screen

- `RNG` -> opens the per-feature RNG submenu.
- `i` -> opens the Ranked standardization summary.
- `Piglins` -> `Disabled / Vanilla`.
- `Ghasts` -> `Disabled / Vanilla`.
- `Hoglins` -> `Disabled / Vanilla`.
- `Move Save & Quit` -> `On / Off`.
- `Hotbar Hotkeys` -> `On / Off`, plus `Customize`.
- `Debug Chat Logs` -> `On / Off`.
- `Hide Advancement Toasts` -> `On / Off`.
- `Village Standardization` -> `On / Off`.
- `Stronghold Anti-Corruption` -> `On / Off`.

Hover over labels to see short tooltips.

## Persistent Coordinates

Enable **Persistent Coordinates** in the UI section, then open **Customize** to
choose the visible rows:

- `XYZ`: precise player coordinates.
- `B`: block coordinates (floored, including negative positions).
- `LC`: local chunk coordinates, from 0 to 15 on each axis.
- `Y: 90.0 P: 0.0 FD: West (-X)`: yaw and pitch in degrees, plus horizontal facing
  (North `-Z`, South `+Z`, East `+X`, or West `-X`). Enable **Yaw / Pitch / Facing** to show this row;
  it is off by default. Yaw is wrapped to the range -180 to 180 degrees.

When Artificial ZSG is installed and its virtual coordinates apply to the current
dimension, XYZ, B, and LC use that coordinate system and carry a `(V)` marker.
This follows Artificial ZSG's Overworld and Nether rules; Y is unchanged. With
the mod absent or virtual coordinates disabled, actual coordinates are shown.
These settings only affect the HUD and do not move the player or change the world.

## Basalt Delta Replacement

Open **Worldgen -> Nether Terrain -> Customize** and turn **Remove Basalt Deltas**
on. **Replace With** cycles through Nether Wastes (default), Soul Sand Valley,
Crimson Forest, and Warped Forest. Removal is off by default and works independently
of Open Nether Terrain and its terrain presets. The settings are saved as
`removeBasaltDeltas` and `basaltReplacementBiome`.

The replacement keeps the original basalt regions' climate positions and uses
the selected biome's surfaces, decorations, mobs, and structure eligibility.
Set it before loading a world. Already generated chunks stay unchanged; changing
the setting in an existing world can create borders with newly generated chunks.

## Minimum Dragon Circling Height

In the Fun section, the **Minimum Dragon Circling Y** slider below Dragon Pathing
sets `dragonMinCirclingY` from 0 to 255. **0 (Off)** is the default. Settings save
when the config screen closes.

Positive values raise circling targets below that world Y, including the forced
first target from Dragon Pathing. Higher targets keep their original height and
all targets retain their X/Z coordinates. The existing 0-15 random height offset
is applied before the floor, with no additional random draws.

This controls target height, not the dragon's instantaneous position. Fireball
strafing, landing approach, and the final descent to the fountain are unchanged;
the setting does not guarantee a particular perch height. If configuration is
updated while circling, the floor is checked on the next server tick. Lowering
or disabling it does not undo a target already raised; new targets use the new
setting.

## RNG Submenu

The RNG submenu is scrollable and supports:

- `Toggle All` to switch all RNG features between Ranked and Vanilla.
- Individual Vanilla/Ranked toggles for every RNG feature.
- Automatic `Custom` mode when only some features are Ranked.

Individual toggles:

- Iron golem drops
- Eye breaks
- Blaze rods
- Piglin pearls/obsidian
- Piglin string
- Flint
- Blind portal anti-cave
- Bastion chest loot
- Other chest loot tables
- Hoglin stable ramparts

## Hotbar Hotkey Customization

- `HUD` controls style the in-world hotbar overlay.
- `Screen` controls style hotbar labels shown inside inventory, chest, and other handled screens.
- `Corner`: chooses which corner of each hotbar slot receives the keybind label.
- `Size`: cycles through sizes `1` to `4`.
- `BG`: cycles through available label backgrounds.
- The previews use your current Minecraft hotbar and offhand keybinds.
- Labels are drawn above item stacks and stack counts. In inventory/container screens, item tooltips stay on top of labels.

## Debug HUD Marker

When the F3 debug HUD is open, the right-side debug text shows `MCSR Easy Mode` plus the active RNG mode. This makes recordings visibly distinguishable from vanilla gameplay.

## Debug Chat Logs

When enabled, debug events are also printed into in-game chat. Launcher logs are still written either way.
