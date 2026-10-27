package me.dota1g.mcsreasymode;

import me.dota1g.mcsreasymode.worldgen.VillageVariant;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

/** Applies the village-wide resource minimums to already rolled chest inventories. */
public final class VillageResourceRules {
    private VillageResourceRules() {
    }

    public static void apply(VillageVariant variant, boolean hasHay, int guaranteedGolemIron,
                             List<? extends Inventory> chests, int preferredChest) {
        if (chests.isEmpty()) return;
        int iron = guaranteedGolemIron;
        int chestIron = 0;
        int diamonds = 0;
        int obsidian = 0;
        boolean ironPickaxe = false;
        boolean food = hasHay;
        for (Inventory chest : chests) {
            for (int slot = 0; slot < chest.size(); slot++) {
                ItemStack stack = chest.getStack(slot);
                if (stack.isEmpty()) continue;
                if (stack.getItem() == Items.IRON_INGOT) {
                    iron += stack.getCount();
                    chestIron += stack.getCount();
                }
                if (stack.getItem() == Items.DIAMOND) diamonds += stack.getCount();
                if (stack.getItem() == Items.OBSIDIAN) obsidian += stack.getCount();
                if (stack.getItem() == Items.IRON_PICKAXE) ironPickaxe = true;
                if (stack.getItem().isFood()) food = true;
            }
        }

        if (variant == VillageVariant.TAIGA && obsidian < 10) {
            add(chests, preferredChest, Items.OBSIDIAN, 10 - obsidian);
            obsidian = 10;
        }
        if (!food) add(chests, preferredChest, Items.BREAD, 1);
        if (obsidian < 10 && iron < 7 && !(iron >= 4 && diamonds >= 3)) {
            int target = diamonds >= 3 ? 4 : 7;
            add(chests, preferredChest, Items.IRON_INGOT, target - iron);
        }
        // Taiga's 10-obsidian route otherwise needs no chest iron. Give it a
        // practical mining route regardless of the separate golem-drop switch.
        if (variant == VillageVariant.TAIGA && !ironPickaxe && chestIron < 4) {
            add(chests, preferredChest, Items.IRON_INGOT, 4 - chestIron);
        }
    }

    private static void add(List<? extends Inventory> chests, int preferredChest, Item item, int amount) {
        if (amount <= 0) return;
        for (int offset = 0; offset < chests.size(); offset++) {
            Inventory chest = chests.get((preferredChest + offset) % chests.size());
            for (int slot = 0; slot < chest.size(); slot++) {
                ItemStack stack = chest.getStack(slot);
                if (stack.getItem() == item && stack.getCount() + amount <= stack.getMaxCount()) {
                    stack.increment(amount);
                    return;
                }
            }
            for (int slot = 0; slot < chest.size(); slot++) {
                if (chest.getStack(slot).isEmpty()) {
                    chest.setStack(slot, new ItemStack(item, amount));
                    return;
                }
            }
        }
        // A full chest still needs a place for the promised resources. Replace a
        // non-resource item, preserving the resources used by the village count.
        for (int offset = 0; offset < chests.size(); offset++) {
            Inventory chest = chests.get((preferredChest + offset) % chests.size());
            for (int slot = 0; slot < chest.size(); slot++) {
                Item existing = chest.getStack(slot).getItem();
                if (existing != Items.IRON_INGOT && existing != Items.DIAMOND && existing != Items.OBSIDIAN
                        && existing != Items.IRON_PICKAXE && !existing.isFood()) {
                    chest.setStack(slot, new ItemStack(item, amount));
                    return;
                }
            }
        }
        throw new IllegalStateException("Village chest inventories have no room for guaranteed " + item);
    }
}
