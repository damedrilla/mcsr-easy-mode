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
- Hotbar labels use the player's current Minecraft keybinds.
- The offhand swap key is shown when the offhand slot is visible.
- Side mouse buttons are shortened to labels such as `M4` and `M5`.
- Hotbar label customization includes label corner, text size, background, and live preview.

## RNG Modes

`Vanilla` keeps normal Minecraft behavior.

`Ranked` enables reliability adjustments inspired by MCSR Ranked.

## Ranked Drops And Barters

- Iron golems always drop exactly 4 iron ingots.
- The second thrown eye of ender never breaks; the first and later throws use vanilla break odds.
- Blaze rods use 50% chance with no more than 2 dry player kills.
- Piglin barters pity ender pearls after 24 dry barters.
- Piglin barters pity obsidian after 24 dry barters.
- Piglin barters pity string after 24 dry barters.
- Gravel keeps vanilla 10% flint odds, with a guaranteed flint drop on the 10th failed gravel drop.

Piglin barter pity is tracked across all piglins in the world.

## Chest Standardization

When `RNG` is set to `Ranked`, the first eligible bastion ramparts or housing chest ensures at least:

- 3 iron ingots
- 5 obsidian

The bastion chest adjustment:

- starts from generated vanilla chest contents
- counts existing iron and obsidian first
- only adds the missing amount
- moves a blocking vanilla item into a free slot when needed
- randomizes standardized item slots to stay closer to vanilla layouts

The mod also ships custom loot table overrides for other speedrun-relevant chests:

- Ruined portal chests can include useful resources such as fire charges, iron nuggets, obsidian, and golden carrots.
- Buried treasure chests can include useful resources such as heart of the sea, iron ingots, and TNT.

## Hoglin Stable Standardization

When `RNG` is set to `Ranked`, hoglin stable bastions guarantee at least 2 adjacent full-height ramparts.

Full-height ramparts are the triple-chest hoglin stable rampart template.

The mod preserves vanilla bastion placement, spacing, and quadrant rules. It does not force bastion locations or change fortress/bastion region placement.

To keep placement vanilla-adjacent, the mod rerolls the vanilla hoglin stable jigsaw assembly at the same generated bastion position until it finds adjacent full-height ramparts, or keeps the best fallback after a limited number of attempts.

Only hoglin stable internals are standardized this way. Other bastion types use vanilla structure assembly.

## Blind Portal Anti-Cave

The first blind travel portal from the Nether to the Overworld is moved to the surface when:

- `RNG` is set to `Ranked`
- the player enters the portal from the Nether
- the Nether portal is built at Y level 48 or above
- the anti-cave surfacing has not already been used in that world

This is intended for the first Nether exit used for stronghold triangulation. It is once per world, so later Nether-to-Overworld portals keep vanilla behavior.
