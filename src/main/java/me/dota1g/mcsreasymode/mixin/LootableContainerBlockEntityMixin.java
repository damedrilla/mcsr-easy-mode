package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.RankedRngState;
import me.dota1g.mcsreasymode.SsgModeState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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
import net.minecraft.util.math.BlockPos;
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
    @Unique
    private static final Identifier MCSREASYMODE_RUINED_PORTAL_CHEST = new Identifier("minecraft", "chests/ruined_portal");
    @Unique
    private static final Identifier MCSREASYMODE_BURIED_TREASURE_CHEST = new Identifier("minecraft", "chests/buried_treasure");
    @Unique
    private static final Identifier MCSREASYMODE_DESERT_PYRAMID_CHEST = new Identifier("minecraft", "chests/desert_pyramid");
    @Unique
    private static final Identifier MCSREASYMODE_SHIPWRECK_SUPPLY_CHEST = new Identifier("minecraft", "chests/shipwreck_supply");
    @Unique
    private static final Identifier MCSREASYMODE_SHIPWRECK_TREASURE_CHEST = new Identifier("minecraft", "chests/shipwreck_treasure");
    @Unique
    private static final Identifier MCSREASYMODE_VILLAGE_WEAPONSMITH_CHEST = new Identifier("minecraft", "chests/village/village_weaponsmith");
    @Unique
    private static final Identifier MCSREASYMODE_BASTION_OTHER_CHEST = new Identifier("minecraft", "chests/bastion_other");
    @Unique
    private static final Identifier MCSREASYMODE_BASTION_BRIDGE_CHEST = new Identifier("minecraft", "chests/bastion_bridge");
    @Unique
    private static final Identifier MCSREASYMODE_BASTION_HOGLIN_STABLE_CHEST = new Identifier("minecraft", "chests/bastion_hoglin_stable");
    @Unique
    private static final Identifier MCSREASYMODE_BASTION_TREASURE_CHEST = new Identifier("minecraft", "chests/bastion_treasure");

    @Shadow
    protected Identifier lootTableId;

    @Shadow
    protected abstract DefaultedList<ItemStack> getInvStackList();

    @Unique
    private Identifier mcsreasymode$lootTableIdBeforeGeneration;

    @Unique
    private Identifier mcsreasymode$assignedLootTableId;

    @Unique
    private boolean mcsreasymode$usingRankedBastionLootTable;

    @Unique
    private boolean mcsreasymode$boostingSsgBastionChest;

    @Unique
    private int mcsreasymode$ssgBastionBoostIndex;

    @Unique
    private boolean mcsreasymode$ssgBastionBoostApplied;

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
    private void mcsreasymode$captureAndMaybeSwapLootTable(PlayerEntity player, CallbackInfo ci) {
        this.mcsreasymode$lootTableIdBeforeGeneration = this.lootTableId != null ? this.lootTableId : this.mcsreasymode$assignedLootTableId;
        this.mcsreasymode$usingRankedBastionLootTable = false;
        this.mcsreasymode$boostingSsgBastionChest = false;
        this.mcsreasymode$ssgBastionBoostIndex = 0;
        if (this.mcsreasymode$lootTableIdBeforeGeneration == null) {
            return;
        }

        if (this.mcsreasymode$isAnyBastionLootTable(this.mcsreasymode$lootTableIdBeforeGeneration)) {
            int ssgBoostIndex = SsgModeState.consumeBastionChestBoost();
            if (ssgBoostIndex > 0) {
                this.mcsreasymode$boostingSsgBastionChest = true;
                this.mcsreasymode$ssgBastionBoostIndex = ssgBoostIndex;
                return;
            }
        }

        Identifier rankedLootTableId = this.mcsreasymode$getRankedLootTable(this.mcsreasymode$lootTableIdBeforeGeneration);
        if (rankedLootTableId != null && this.lootTableId != null) {
            this.lootTableId = rankedLootTableId;
            Mcsreasymode.debug("Ranked chest loot table selected: " + this.mcsreasymode$lootTableIdBeforeGeneration + " -> " + rankedLootTableId + ".");
        }
    }

    @Inject(method = "checkLootInteraction", at = @At("TAIL"))
    private void mcsreasymode$logRankedBastionChest(PlayerEntity player, CallbackInfo ci) {
        if (this.mcsreasymode$boostingSsgBastionChest) {
            this.mcsreasymode$applySsgBastionBoost();
            return;
        }

        if (this.mcsreasymode$shouldApplyGeneratedBastionFallbackBoost()) {
            int ssgBoostIndex = SsgModeState.consumeBastionChestBoost();
            if (ssgBoostIndex > 0) {
                this.mcsreasymode$ssgBastionBoostIndex = ssgBoostIndex;
                Mcsreasymode.debug("SSG bastion chest fallback matched generated Nether bastion chest near blackstone.");
                this.mcsreasymode$applySsgBastionBoost();
                return;
            }
        }

        if (this.mcsreasymode$usingRankedBastionLootTable) {
            Mcsreasymode.debug("Bastion chest standardized: generated Ranked bastion loot table with guaranteed "
                    + RankedRngState.getBastionIronMinimum() + " iron ingots and "
                    + RankedRngState.getBastionObsidianMinimum() + " obsidian. Later bastion chests use vanilla.");
            this.mcsreasymode$usingRankedBastionLootTable = false;
        }
    }

    @Unique
    private Identifier mcsreasymode$getRankedLootTable(Identifier vanillaLootTableId) {
        Identifier rankedBastionLootTable = this.mcsreasymode$getRankedBastionLootTable(vanillaLootTableId);
        if (rankedBastionLootTable != null) {
            return rankedBastionLootTable;
        }
        if (!Mcsreasymode.isRankedChestLootTablesEnabled()) {
            return null;
        }
        if (MCSREASYMODE_RUINED_PORTAL_CHEST.equals(vanillaLootTableId)) {
            return new Identifier(Mcsreasymode.MOD_ID, "chests/ruined_portal");
        }
        if (MCSREASYMODE_BURIED_TREASURE_CHEST.equals(vanillaLootTableId)) {
            return new Identifier(Mcsreasymode.MOD_ID, "chests/buried_treasure");
        }
        if (MCSREASYMODE_DESERT_PYRAMID_CHEST.equals(vanillaLootTableId)) {
            return new Identifier(Mcsreasymode.MOD_ID, "chests/desert_pyramid");
        }
        if (MCSREASYMODE_SHIPWRECK_SUPPLY_CHEST.equals(vanillaLootTableId)) {
            return new Identifier(Mcsreasymode.MOD_ID, "chests/shipwreck_supply");
        }
        if (MCSREASYMODE_SHIPWRECK_TREASURE_CHEST.equals(vanillaLootTableId)) {
            return new Identifier(Mcsreasymode.MOD_ID, "chests/shipwreck_treasure");
        }
        if (MCSREASYMODE_VILLAGE_WEAPONSMITH_CHEST.equals(vanillaLootTableId)) {
            return new Identifier(Mcsreasymode.MOD_ID, "chests/village/village_weaponsmith");
        }
        return null;
    }

    @Unique
    private Identifier mcsreasymode$getRankedBastionLootTable(Identifier vanillaLootTableId) {
        if (!Mcsreasymode.isRankedBastionChestLootEnabled()) {
            return null;
        }
        if (!this.mcsreasymode$isBastionLootTable(vanillaLootTableId)) {
            return null;
        }
        if (!RankedRngState.shouldAdjustBastionChest()) {
            return null;
        }
        this.mcsreasymode$usingRankedBastionLootTable = true;
        if (MCSREASYMODE_BASTION_OTHER_CHEST.equals(vanillaLootTableId)) {
            return new Identifier(Mcsreasymode.MOD_ID, "chests/bastion_other");
        }
        return new Identifier(Mcsreasymode.MOD_ID, "chests/bastion_bridge");
    }

    @Unique
    private void mcsreasymode$applySsgBastionBoost() {
        int ironMinimum = SsgModeState.getIronMinimum(this.mcsreasymode$ssgBastionBoostIndex);
        int obsidianMinimum = SsgModeState.getObsidianMinimum(this.mcsreasymode$ssgBastionBoostIndex);
        int goldBlockMinimum = SsgModeState.getGoldBlockMinimum(this.mcsreasymode$ssgBastionBoostIndex);
        int[] slots = this.mcsreasymode$shuffledSlots(this.getInvStackList().size());
        this.mcsreasymode$ensureAtLeast(Items.IRON_INGOT, ironMinimum, slots);
        this.mcsreasymode$ensureAtLeast(Items.OBSIDIAN, obsidianMinimum, slots);
        this.mcsreasymode$ensureAtLeast(Items.GOLD_BLOCK, goldBlockMinimum, slots);
        Mcsreasymode.debug("SSG bastion chest boost " + this.mcsreasymode$ssgBastionBoostIndex + "/"
                + SsgModeState.BASTION_CHEST_LIMIT + ": guaranteed at least "
                + ironMinimum + " iron ingots, "
                + obsidianMinimum + " obsidian, and "
                + goldBlockMinimum + " gold blocks.");
        this.mcsreasymode$ssgBastionBoostApplied = true;
        this.mcsreasymode$boostingSsgBastionChest = false;
        this.mcsreasymode$ssgBastionBoostIndex = 0;
    }

    @Unique
    private boolean mcsreasymode$shouldApplyGeneratedBastionFallbackBoost() {
        if (this.mcsreasymode$ssgBastionBoostApplied || !Mcsreasymode.isSsgModeEnabled()) {
            return false;
        }

        LootableContainerBlockEntity blockEntity = (LootableContainerBlockEntity) (Object) this;
        World world = blockEntity.getWorld();
        if (world == null || world.isClient || world.getRegistryKey() != World.NETHER) {
            return false;
        }

        return this.mcsreasymode$hasNearbyBastionBlocks(world, blockEntity.getPos())
                && this.mcsreasymode$hasBastionChestLootSignature();
    }

    @Unique
    private boolean mcsreasymode$hasNearbyBastionBlocks(World world, BlockPos center) {
        int blackstoneBlocks = 0;
        int radius = 8;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = Math.max(0, center.getY() - 4); y <= Math.min(255, center.getY() + 4); y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    mutable.set(x, y, z);
                    Block block = world.getBlockState(mutable).getBlock();
                    if (block == Blocks.BLACKSTONE
                            || block == Blocks.POLISHED_BLACKSTONE_BRICKS
                            || block == Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS
                            || block == Blocks.CHISELED_POLISHED_BLACKSTONE
                            || block == Blocks.GILDED_BLACKSTONE) {
                        blackstoneBlocks++;
                        if (blackstoneBlocks >= 8) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Unique
    private boolean mcsreasymode$hasBastionChestLootSignature() {
        return this.mcsreasymode$countItem(Items.GILDED_BLACKSTONE) > 0
                || this.mcsreasymode$countItem(Items.CRYING_OBSIDIAN) > 0
                || this.mcsreasymode$countItem(Items.GOLD_BLOCK) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_CHESTPLATE) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_BOOTS) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_LEGGINGS) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_HELMET) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_SWORD) > 0
                || this.mcsreasymode$countItem(Items.CROSSBOW) > 0
                || this.mcsreasymode$countItem(Items.SPECTRAL_ARROW) > 0;
    }

    @Unique
    private boolean mcsreasymode$isBastionLootTable(Identifier lootTableId) {
        return MCSREASYMODE_BASTION_OTHER_CHEST.equals(lootTableId) || MCSREASYMODE_BASTION_BRIDGE_CHEST.equals(lootTableId);
    }

    @Unique
    private boolean mcsreasymode$isAnyBastionLootTable(Identifier lootTableId) {
        return this.mcsreasymode$isBastionLootTable(lootTableId)
                || MCSREASYMODE_BASTION_HOGLIN_STABLE_CHEST.equals(lootTableId)
                || MCSREASYMODE_BASTION_TREASURE_CHEST.equals(lootTableId);
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

        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (!stack.isEmpty() && !this.mcsreasymode$isBastionStandardizedItem(stack.getItem())) {
                Mcsreasymode.debug("Bastion chest standardization replaced "
                        + stack.getCount() + "x " + stack.getItem() + " in full chest for " + item + ".");
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
        return item == Items.IRON_INGOT || item == Items.OBSIDIAN || item == Items.GOLD_BLOCK;
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
