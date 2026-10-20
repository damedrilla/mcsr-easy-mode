package me.dota1g.mcsreasymode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class EndEntryHotbarSorter {
    private static final int MAIN_INVENTORY_SIZE = 36;
    private static final int MINIMUM_BUILDING_BLOCKS = 32;

    private EndEntryHotbarSorter() {
    }

    public static void sort(ServerPlayerEntity player) {
        PlayerInventory inventory = player.inventory;
        DefaultedList<ItemStack> stacks = inventory.main;
        boolean[] reserved = new boolean[Math.min(MAIN_INVENTORY_SIZE, stacks.size())];
        List<String> filled = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        moveBestStack(stacks, reserved, 0, EndEntryHotbarSorter::isSafeFood, "food", filled, missing);
        moveBestStack(stacks, reserved, 1, stack -> stack.getItem() instanceof PickaxeItem, "pickaxe", filled, missing);
        moveBestStack(stacks, reserved, 2, stack -> stack.getItem() instanceof BowItem, "bow", filled, missing);
        moveBestStack(stacks, reserved, 3, stack -> stack.getItem() instanceof BedItem, "bed", filled, missing);
        moveBuildingBlocks(player, stacks, reserved, 4, filled, missing);
        moveBestStack(stacks, reserved, 5, EndEntryHotbarSorter::isBlastResistantPracticeBlock, "blast-resistant block", filled, missing);
        moveBestStack(stacks, reserved, 6, stack -> stack.getItem() == Items.ENDER_PEARL, "ender pearl", filled, missing);

        inventory.markDirty();
        player.playerScreenHandler.sendContentUpdates();
        if (player.currentScreenHandler != player.playerScreenHandler) {
            player.currentScreenHandler.sendContentUpdates();
        }

        Mcsreasymode.debugRateLimited(
                "qol.end_entry_hotbar_sort",
                "End entry hotbar sorted. Filled: " + joinOrNone(filled) + "; missing: " + joinOrNone(missing) + ".",
                1000L
        );
    }

    private static void moveBestStack(DefaultedList<ItemStack> stacks, boolean[] reserved, int targetSlot,
                                      Predicate<ItemStack> predicate, String label,
                                      List<String> filled, List<String> missing) {
        int sourceSlot = findBestStack(stacks, reserved, predicate);
        if (sourceSlot < 0) {
            missing.add(label);
            return;
        }

        swap(stacks, sourceSlot, targetSlot);
        reserved[targetSlot] = true;
        filled.add(label);
    }

    private static int findBestStack(DefaultedList<ItemStack> stacks, boolean[] reserved,
                                     Predicate<ItemStack> predicate) {
        int bestSlot = -1;
        int bestCount = -1;
        int limit = Math.min(MAIN_INVENTORY_SIZE, stacks.size());
        for (int slot = 0; slot < limit; slot++) {
            ItemStack stack = stacks.get(slot);
            if (reserved[slot] || stack.isEmpty() || !predicate.test(stack)) {
                continue;
            }
            if (stack.getCount() > bestCount) {
                bestSlot = slot;
                bestCount = stack.getCount();
            }
        }
        return bestSlot;
    }

    private static void moveBuildingBlocks(ServerPlayerEntity player, DefaultedList<ItemStack> stacks,
                                           boolean[] reserved, int targetSlot,
                                           List<String> filled, List<String> missing) {
        BlockGroup best = null;
        int limit = Math.min(MAIN_INVENTORY_SIZE, stacks.size());
        for (int slot = 0; slot < limit; slot++) {
            ItemStack stack = stacks.get(slot);
            if (reserved[slot] || stack.isEmpty() || !isOrdinarySolidBlock(player, stack)) {
                continue;
            }

            BlockGroup group = findCompatibleGroup(stacks, reserved, slot, player);
            if (group.totalCount < MINIMUM_BUILDING_BLOCKS) {
                continue;
            }
            if (best == null || group.totalCount > best.totalCount
                    || (group.totalCount == best.totalCount && group.firstSlot < best.firstSlot)) {
                best = group;
            }
        }

        if (best == null) {
            missing.add("building blocks");
            return;
        }

        swap(stacks, best.largestSlot, targetSlot);
        ItemStack target = stacks.get(targetSlot);
        for (int slot = 0; slot < limit && target.getCount() < target.getMaxCount(); slot++) {
            if (slot == targetSlot || reserved[slot]) {
                continue;
            }
            ItemStack source = stacks.get(slot);
            if (!canCombine(target, source)) {
                continue;
            }

            int transfer = Math.min(target.getMaxCount() - target.getCount(), source.getCount());
            target.increment(transfer);
            source.decrement(transfer);
            if (source.isEmpty()) {
                stacks.set(slot, ItemStack.EMPTY);
            }
        }

        reserved[targetSlot] = true;
        filled.add("building blocks (" + target.getCount() + ")");
    }

    private static BlockGroup findCompatibleGroup(DefaultedList<ItemStack> stacks, boolean[] reserved,
                                                   int firstSlot, ServerPlayerEntity player) {
        ItemStack prototype = stacks.get(firstSlot);
        int total = 0;
        int largestSlot = firstSlot;
        int limit = Math.min(MAIN_INVENTORY_SIZE, stacks.size());
        for (int slot = firstSlot; slot < limit; slot++) {
            ItemStack stack = stacks.get(slot);
            if (reserved[slot] || stack.isEmpty() || !isOrdinarySolidBlock(player, stack)
                    || !canCombine(prototype, stack)) {
                continue;
            }
            total += stack.getCount();
            if (stack.getCount() > stacks.get(largestSlot).getCount()) {
                largestSlot = slot;
            }
        }
        return new BlockGroup(firstSlot, largestSlot, total);
    }

    private static boolean canCombine(ItemStack first, ItemStack second) {
        return !first.isEmpty()
                && !second.isEmpty()
                && first.getItem() == second.getItem()
                && ItemStack.areTagsEqual(first, second);
    }

    private static boolean isSafeFood(ItemStack stack) {
        if (!stack.isFood()) {
            return false;
        }
        Item item = stack.getItem();
        return item != Items.ROTTEN_FLESH
                && item != Items.SPIDER_EYE
                && item != Items.POISONOUS_POTATO
                && item != Items.PUFFERFISH
                && item != Items.CHICKEN
                && item != Items.SUSPICIOUS_STEW
                && item != Items.CHORUS_FRUIT;
    }

    private static boolean isOrdinarySolidBlock(ServerPlayerEntity player, ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)
                || stack.getItem() instanceof BedItem
                || isBlastResistantPracticeBlock(stack)) {
            return false;
        }
        BlockState state = ((BlockItem) stack.getItem()).getBlock().getDefaultState();
        return state.getMaterial().isSolid()
                && Block.isShapeFullCube(state.getCollisionShape(player.world, player.getBlockPos()));
    }

    private static boolean isBlastResistantPracticeBlock(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.OBSIDIAN || item == Items.CRYING_OBSIDIAN || item == Items.RESPAWN_ANCHOR;
    }

    private static void swap(DefaultedList<ItemStack> stacks, int sourceSlot, int targetSlot) {
        if (sourceSlot == targetSlot) {
            return;
        }
        ItemStack displaced = stacks.get(targetSlot);
        stacks.set(targetSlot, stacks.get(sourceSlot));
        stacks.set(sourceSlot, displaced);
    }

    private static String joinOrNone(List<String> values) {
        return values.isEmpty() ? "none" : String.join(", ", values);
    }

    private static final class BlockGroup {
        private final int firstSlot;
        private final int largestSlot;
        private final int totalCount;

        private BlockGroup(int firstSlot, int largestSlot, int totalCount) {
            this.firstSlot = firstSlot;
            this.largestSlot = largestSlot;
            this.totalCount = totalCount;
        }
    }
}
