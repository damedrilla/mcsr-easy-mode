package me.dota1g.mcsreasymode.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.ItemTags;

import java.util.List;

public class ItemHighlightHelper {

    // Returns true if the given stack's item is one we track AND the total
    // visible count across all slots in this screen meets the ideal threshold.
    public static boolean shouldHighlight(List<Slot> slots, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();

        if (item == Items.OBSIDIAN) {
            return sumMatchingCount(slots, s -> s.getItem() == Items.OBSIDIAN) >= 20;
        }
        if (item == Items.STRING) {
            return sumMatchingCount(slots, s -> s.getItem() == Items.STRING) >= 64;
        }
        if (item == Items.ENDER_PEARL) {
            return sumMatchingCount(slots, s -> s.getItem() == Items.ENDER_PEARL) >= 20;
        }
        if (ItemTags.PLANKS.contains(item)) {
            return sumMatchingCount(slots, s -> ItemTags.PLANKS.contains(s.getItem())) >= 15;
        }
        if (item == Items.POTION && isFireResistance(stack)) {
            return countMatchingStacks(slots, s -> s.getItem() == Items.POTION && isFireResistance(s)) >= 2;
        }

        return false;
    }

    private static boolean isFireResistance(ItemStack stack) {
        return PotionUtil.getPotion(stack) == Potions.FIRE_RESISTANCE
                || PotionUtil.getPotion(stack) == Potions.LONG_FIRE_RESISTANCE;
    }

    private static int sumMatchingCount(List<Slot> slots, java.util.function.Predicate<ItemStack> matcher) {
        int total = 0;
        for (Slot slot : slots) {
            ItemStack s = slot.getStack();
            if (!s.isEmpty() && matcher.test(s)) {
                total += s.getCount();
            }
        }
        return total;
    }

    private static int countMatchingStacks(List<Slot> slots, java.util.function.Predicate<ItemStack> matcher) {
        int total = 0;
        for (Slot slot : slots) {
            ItemStack s = slot.getStack();
            if (!s.isEmpty() && matcher.test(s)) {
                total++;
            }
        }
        return total;
    }
}