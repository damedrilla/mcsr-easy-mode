package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin {
    @Shadow
    protected Identifier lootTableId;

    @Shadow
    protected abstract DefaultedList<ItemStack> getInvStackList();

    @Unique
    private Identifier mcsreasymode$lootTableIdBeforeGeneration;

    @Unique
    private Identifier mcsreasymode$assignedLootTableId;

    @Unique
    private boolean mcsreasymode$adjustingLoot;

    @Unique
    private boolean mcsreasymode$bastionStandardized;

    @Inject(method = "setLootTable(Lnet/minecraft/util/Identifier;J)V", at = @At("TAIL"))
    private void mcsreasymode$rememberAssignedLootTable(Identifier lootTableId, long lootTableSeed, CallbackInfo ci) {
        this.mcsreasymode$assignedLootTableId = lootTableId;
    }

    @Inject(method = "deserializeLootTable", at = @At("TAIL"))
    private void mcsreasymode$rememberLoadedLootTable(CompoundTag tag, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            this.mcsreasymode$assignedLootTableId = this.lootTableId;
        }
    }

    @Inject(method = "checkLootInteraction", at = @At("HEAD"))
    private void mcsreasymode$captureLootTable(PlayerEntity player, CallbackInfo ci) {
        this.mcsreasymode$lootTableIdBeforeGeneration = this.lootTableId != null ? this.lootTableId : this.mcsreasymode$assignedLootTableId;
    }

    @Inject(method = "checkLootInteraction", at = @At("TAIL"))
    private void mcsreasymode$adjustBastionChest(PlayerEntity player, CallbackInfo ci) {
        if (!Mcsreasymode.isRankedRngEnabled() || this.mcsreasymode$adjustingLoot) {
            return;
        }

        this.mcsreasymode$adjustingLoot = true;
        try {
            this.mcsreasymode$adjustBastionLoot();
        } finally {
            this.mcsreasymode$adjustingLoot = false;
        }
    }

    @Unique
    private void mcsreasymode$adjustBastionLoot() {
        if (this.mcsreasymode$bastionStandardized || !this.mcsreasymode$isEligibleBastionChest() || !RankedRngState.shouldAdjustBastionChest()) {
            return;
        }

        Inventory inventory = (Inventory) this;
        int[] slots = this.mcsreasymode$shuffledSlots(inventory.size());
        this.mcsreasymode$ensureAtLeast(Items.IRON_INGOT, RankedRngState.getBastionIronMinimum(), slots);
        this.mcsreasymode$ensureAtLeast(Items.OBSIDIAN, RankedRngState.getBastionObsidianMinimum(), slots);
        this.mcsreasymode$bastionStandardized = true;
        inventory.markDirty();
        Mcsreasymode.debug("Bastion chest standardized: ensured at least "
                + RankedRngState.getBastionIronMinimum() + " iron ingots and "
                + RankedRngState.getBastionObsidianMinimum() + " obsidian.");
    }

    @Unique
    private boolean mcsreasymode$isEligibleBastionChest() {
        Identifier generatedLootTableId = this.mcsreasymode$lootTableIdBeforeGeneration != null
                ? this.mcsreasymode$lootTableIdBeforeGeneration
                : this.mcsreasymode$assignedLootTableId;
        return LootTables.BASTION_OTHER_CHEST.equals(generatedLootTableId)
                || LootTables.BASTION_BRIDGE_CHEST.equals(generatedLootTableId);
    }

    @Unique
    private void mcsreasymode$ensureAtLeast(Item item, int minimum, int... preferredSlots) {
        int current = this.mcsreasymode$countItem(item);
        int missing = minimum - current;
        if (missing <= 0) {
            return;
        }

        DefaultedList<ItemStack> stacks = this.getInvStackList();
        for (ItemStack stack : stacks) {
            if (stack.getItem() == item) {
                stack.increment(missing);
                return;
            }
        }

        for (int preferredSlot : preferredSlots) {
            if (preferredSlot >= 0 && preferredSlot < stacks.size() && this.mcsreasymode$placeInPreferredSlot(stacks, preferredSlot, item, missing)) {
                return;
            }
        }

        for (int slot = 0; slot < stacks.size(); slot++) {
            if (stacks.get(slot).isEmpty()) {
                stacks.set(slot, new ItemStack(item, missing));
                return;
            }
        }
    }

    @Unique
    private boolean mcsreasymode$placeInPreferredSlot(DefaultedList<ItemStack> stacks, int slot, Item item, int count) {
        ItemStack current = stacks.get(slot);
        if (current.isEmpty()) {
            stacks.set(slot, new ItemStack(item, count));
            return true;
        }

        if (current.getItem() == item) {
            current.increment(count);
            return true;
        }

        int emptySlot = this.mcsreasymode$findEmptySlot(stacks, slot);
        if (emptySlot >= 0 && !this.mcsreasymode$isBastionStandardizedItem(current.getItem())) {
            stacks.set(emptySlot, current.copy());
            stacks.set(slot, new ItemStack(item, count));
            Mcsreasymode.debug("Bastion chest standardization moved blocking item: moved "
                    + current.getCount() + "x " + current.getItem() + " from slot "
                    + slot + " to slot " + emptySlot + " for " + item + ".");
            return true;
        }

        return false;
    }

    @Unique
    private int mcsreasymode$findEmptySlot(DefaultedList<ItemStack> stacks, int excludedSlot) {
        for (int slot = 0; slot < stacks.size(); slot++) {
            if (slot != excludedSlot && stacks.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    @Unique
    private boolean mcsreasymode$isBastionStandardizedItem(Item item) {
        return item == Items.IRON_INGOT || item == Items.OBSIDIAN;
    }

    @Unique
    private int[] mcsreasymode$shuffledSlots(int size) {
        int[] slots = new int[size];
        for (int slot = 0; slot < size; slot++) {
            slots[slot] = slot;
        }

        Random random = this.mcsreasymode$getRandom();
        for (int slot = size - 1; slot > 0; slot--) {
            int swapSlot = random.nextInt(slot + 1);
            int original = slots[slot];
            slots[slot] = slots[swapSlot];
            slots[swapSlot] = original;
        }

        return slots;
    }

    @Unique
    private Random mcsreasymode$getRandom() {
        World world = ((LootableContainerBlockEntity) (Object) this).getWorld();
        if (world != null) {
            return world.getRandom();
        }
        return new Random();
    }

    @Unique
    private int mcsreasymode$countItem(Item item) {
        int count = 0;
        for (ItemStack stack : this.getInvStackList()) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
