package me.dota1g.mcsreasymode;

import net.minecraft.inventory.Inventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Adds only the resources missing from an already rolled vanilla portal chest. */
public final class RuinedPortalResourceRules {
    private RuinedPortalResourceRules() {
    }

    public static void apply(Inventory chest, int missingFrameObsidian, boolean bucketRoute, boolean animalFoodRoute) {
        int obsidian = count(chest, Items.OBSIDIAN);
        int gap = Math.max(0, missingFrameObsidian - obsidian);
        if (bucketRoute && gap > 0) {
            // A water bucket supplies the water and then becomes the empty
            // bucket used to move one surviving portal lava source into place.
            if (count(chest, Items.WATER_BUCKET) == 0) add(chest, Items.WATER_BUCKET, 1);
            gap--;
        }
        add(chest, Items.OBSIDIAN, gap);

        if (animalFoodRoute) {
            for (int slot = 0; slot < chest.size(); slot++) {
                Item item = chest.getStack(slot).getItem();
                if (item == Items.ENCHANTED_GOLDEN_APPLE || item == Items.GOLDEN_APPLE
                        || item == Items.GOLDEN_CARROT) chest.setStack(slot, ItemStack.EMPTY);
            }
            ensureFireAspectSword(chest);
        } else if (count(chest, Items.ENCHANTED_GOLDEN_APPLE) == 0
                && count(chest, Items.GOLDEN_APPLE) == 0
                && count(chest, Items.GOLDEN_CARROT) == 0) {
            add(chest, Items.GOLDEN_CARROT, 6);
        }

        int nuggets = count(chest, Items.IRON_NUGGET);
        if (nuggets < 18) add(chest, Items.IRON_NUGGET, 18 - nuggets);
        // If flint and nuggets are the only ignition route, nine nuggets must
        // remain available for an ingot *besides* the guaranteed eighteen.
        boolean ignition = count(chest, Items.FLINT_AND_STEEL) > 0
                || count(chest, Items.FIRE_CHARGE) > 0
                || (count(chest, Items.FLINT) > 0
                    && (count(chest, Items.IRON_INGOT) > 0 || count(chest, Items.IRON_NUGGET) >= 27));
        if (!ignition) add(chest, Items.FIRE_CHARGE, 1);
    }

    private static void ensureFireAspectSword(Inventory chest) {
        int unenchantedSwordSlot = -1;
        for (int slot = 0; slot < chest.size(); slot++) {
            ItemStack stack = chest.getStack(slot);
            if (stack.getItem() != Items.GOLDEN_SWORD) continue;
            if (EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack) > 0) return;
            if (unenchantedSwordSlot < 0) unenchantedSwordSlot = slot;
        }
        if (unenchantedSwordSlot >= 0) {
            chest.getStack(unenchantedSwordSlot).addEnchantment(Enchantments.FIRE_ASPECT, 1);
            return;
        }
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.addEnchantment(Enchantments.FIRE_ASPECT, 1);
        addStack(chest, sword);
    }

    private static void addStack(Inventory chest, ItemStack stack) {
        for (int slot = 0; slot < chest.size(); slot++) {
            if (chest.getStack(slot).isEmpty()) {
                chest.setStack(slot, stack);
                return;
            }
        }
        for (int slot = 0; slot < chest.size(); slot++) {
            Item existing = chest.getStack(slot).getItem();
            if (existing != Items.OBSIDIAN && existing != Items.WATER_BUCKET
                    && existing != Items.IRON_NUGGET && existing != Items.FIRE_CHARGE
                    && existing != Items.FLINT_AND_STEEL && existing != Items.GOLDEN_SWORD) {
                chest.setStack(slot, stack);
                return;
            }
        }
        throw new IllegalStateException("Ruined portal chest has no room for Fire Aspect sword");
    }

    private static int count(Inventory chest, Item item) {
        int count = 0;
        for (int slot = 0; slot < chest.size(); slot++) {
            ItemStack stack = chest.getStack(slot);
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    private static void add(Inventory chest, Item item, int count) {
        if (count <= 0) return;
        for (int slot = 0; slot < chest.size(); slot++) {
            ItemStack stack = chest.getStack(slot);
            if (stack.getItem() == item && stack.getCount() + count <= stack.getMaxCount()) {
                stack.increment(count);
                return;
            }
        }
        for (int slot = 0; slot < chest.size(); slot++) {
            if (chest.getStack(slot).isEmpty()) {
                chest.setStack(slot, new ItemStack(item, count));
                return;
            }
        }
        for (int slot = 0; slot < chest.size(); slot++) {
            Item existing = chest.getStack(slot).getItem();
            if (existing != Items.OBSIDIAN && existing != Items.WATER_BUCKET
                    && existing != Items.IRON_NUGGET && existing != Items.FIRE_CHARGE
                    && existing != Items.FLINT_AND_STEEL && existing != Items.GOLDEN_CARROT
                    && existing != Items.GOLDEN_APPLE && existing != Items.ENCHANTED_GOLDEN_APPLE
                    && existing != Items.GOLDEN_SWORD) {
                chest.setStack(slot, new ItemStack(item, count));
                return;
            }
        }
        throw new IllegalStateException("Ruined portal chest has no room for guaranteed " + item);
    }
}
