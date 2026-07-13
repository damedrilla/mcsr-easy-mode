# Features

## Mob Aggression

Aggression toggles are mob-specific:

- Piglins
- Ghasts
- Hoglins

Piglin anti-aggression spoofs gold armor checks, so the player does not need to wear gold armor. Vanilla anger triggers such as attacking piglins, opening guarded containers, or mining guarded gold-related blocks can still provoke piglins.

Ghast anti-aggression blocks targeting and fireball behavior.

Hoglin anti-aggression clears attack targeting and suppresses angry ambient behavior.

## UI Features

- Move the pause menu `Save and Quit` button to the bottom-left corner.
- Display hotbar hotkey labels directly on vanilla hotbar slots.
- Display the same hotkey labels on hotbar slots inside inventory, chest, and other handled screens.
- Hotbar labels use the player's current Minecraft keybinds.
- The offhand swap key is shown when the offhand slot is visible.
- Side mouse buttons are shortened to labels such as `M4` and `M5`.
- Hotbar label customization includes label corner, text size, background, and live previews.
- HUD hotbar labels and inventory/container hotbar labels have separate styling controls.
- Optional debug chat logs echo MCSR Easy Mode debug events into in-game chat while keeping launcher logs.
- Optional advancement toast hiding prevents advancement popups from covering routing/crafting.
- The F3 debug HUD shows `MCSR Easy Mode` and the active RNG mode.

## RNG Modes

`Vanilla` keeps normal Minecraft behavior.

`Ranked` enables all implemented Ranked-style reliability adjustments.

`Custom` appears when individual RNG features are mixed between Vanilla and Ranked in the RNG submenu.

The RNG submenu includes one-button `Toggle All` behavior plus per-feature toggles for:

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

Hovering labels in the config screens shows short tooltips for each feature.

## Ranked Drops And Barters

- Iron golems always drop exactly 4 iron ingots.
- The second thrown eye of ender never breaks; the first and later throws use vanilla break odds.
- Blaze rods use vanilla drop rates with no more than 2 dry player kills.
- Piglin barters pity 4-8 ender pearls after 24 dry barters.
- Piglin barters pity 1 obsidian after 24 dry barters.
- Piglin barters pity 8-24 string after 24 dry barters.
- Gravel keeps vanilla 10% flint odds, with a guaranteed flint drop on the 10th failed gravel drop.

Piglin barter pity is tracked across all piglins in the world.

## Chest Standardization

When `Bastion Chest Loot` is Ranked, the first eligible `bastion_other` or `bastion_bridge` chest uses a Ranked loot table that includes the vanilla table plus guaranteed:

- 3 iron ingots
- 5 obsidian

The bastion chest adjustment:

- only applies once per world
- falls back to vanilla bastion loot tables after the guaranteed chest has generated
- keeps the original vanilla bastion table inside the Ranked table before adding the guaranteed items
- does not currently apply to `bastion_hoglin_stable` or `bastion_treasure` chests

When `Other Chest Loot Tables` is Ranked, the mod swaps selected vanilla chest loot tables to mod-owned Ranked tables at generation time. When set to Vanilla, these chests use the normal Minecraft loot tables because the mod does not globally override the `minecraft` namespace loot tables.

Supported non-bastion tables include:

- Ruined portal
- Buried treasure
- Desert pyramid
- Shipwreck supply
- Shipwreck treasure
- Village weaponsmith

## Hoglin Stable Standardization

When `Hoglin Stable Ramparts` is Ranked, hoglin stable bastions guarantee at least 2 adjacent full-height ramparts.

Full-height ramparts are the triple-chest hoglin stable rampart template.

The mod preserves vanilla bastion placement, spacing, and quadrant rules. It does not force bastion locations or change fortress/bastion region placement.

To keep placement vanilla-adjacent, the mod rerolls the vanilla hoglin stable jigsaw assembly at the same generated bastion position until it finds adjacent full-height ramparts, or keeps the best fallback after a limited number of attempts.

Only hoglin stable internals are standardized this way. Other bastion types use vanilla structure assembly.

## Blind Portal Anti-Cave

The first blind travel portal from the Nether to the Overworld is created on the surface when:

- `Blind Portal Anti-Cave` is Ranked
- the player enters the portal from the Nether
- the Nether portal is built at Y level 48 or above
- the anti-cave surfacing has not already been used in that world

This is intended for the first Nether exit used for stronghold triangulation. It is once per world, so later Nether-to-Overworld portals keep vanilla behavior. If the target surface is liquid, the mod builds the portal with a small footing instead of placing it in the cave search result.

## Worldgen Practice Helpers

These options are separate from RNG mode and are disabled by default.

### Village Standardization

When a vanilla village generates without a smith, the mod adds:

- one vanilla NBT-backed smith template matching the village type
- one nearby custom lava pool

Supported smith templates include desert, plains, savanna, snowy, and taiga village smiths. The mod does not force village spawning; it only adjusts villages that already generated.

### Stronghold Anti-Corruption

Stronghold anti-corruption snapshots generated stronghold rooms during chunk feature generation and restores them after later worldgen features run. This is intended to protect rooms from caves, liquids, and other later feature passes that can overwrite useful stronghold blocks.

### Nether Terrain

The codebase includes experimental Nether terrain tuning values, but the current main config screen hides the Nether terrain customization button. Treat this as experimental/development-only unless you are working on the alpha worldgen branch.

## Integrity And Visibility

- The F3 debug HUD marker makes recordings visibly distinguishable from vanilla gameplay.
- A pre-launch integrity check verifies that the mod's mixin config is registered and that the F3 marker mixin is still present.
- Fabric metadata blocks loading alongside `mcsrranked` and `mcsr-fairplay-public`.
