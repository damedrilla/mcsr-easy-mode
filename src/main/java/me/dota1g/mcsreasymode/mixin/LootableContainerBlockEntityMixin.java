package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
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
        if (!Mcsreasymode.isRankedRngEnabled()) {
            return;
        }

        Inventory inventory = (Inventory) this;
        Identifier generatedLootTableId = this.mcsreasymode$lootTableIdBeforeGeneration != null
                ? this.mcsreasymode$lootTableIdBeforeGeneration
                : this.mcsreasymode$assignedLootTableId;

        if (LootTables.RUINED_PORTAL_CHEST.equals(generatedLootTableId) || this.mcsreasymode$isLikelyRuinedPortalChest(inventory)) {
            this.mcsreasymode$ensureAtLeast(inventory, Items.FIRE_CHARGE, 1);
            this.mcsreasymode$ensureAtLeast(inventory, Items.IRON_NUGGET, 55);
            this.mcsreasymode$ensureAtLeast(inventory, Items.OBSIDIAN, 6);
            this.mcsreasymode$ensureAtLeast(inventory, Items.GOLDEN_CARROT, 6);
            inventory.markDirty();
            Mcsreasymode.debug("Ruined portal chest standardized.");
            return;
        }

        if (LootTables.BURIED_TREASURE_CHEST.equals(generatedLootTableId) || this.mcsreasymode$isLikelyBuriedTreasureChest(inventory)) {
            this.mcsreasymode$ensureAtLeast(inventory, Items.HEART_OF_THE_SEA, 1);
            this.mcsreasymode$ensureAtLeast(inventory, Items.IRON_INGOT, 7);
            this.mcsreasymode$ensureAtLeast(inventory, Items.TNT, 1);
            inventory.markDirty();
            Mcsreasymode.debug("Buried treasure chest standardized.");
            return;
        }

        if (!this.mcsreasymode$isEligibleBastionChest() || !RankedRngState.shouldAdjustBastionChest()) {
            return;
        }

        this.mcsreasymode$ensureAtLeast(inventory, Items.IRON_INGOT, RankedRngState.getBastionIronMinimum());
        this.mcsreasymode$ensureAtLeast(inventory, Items.OBSIDIAN, RankedRngState.getBastionObsidianMinimum());
        inventory.markDirty();
        Mcsreasymode.debug("Bastion chest standardized: ensured at least "
                + RankedRngState.getBastionIronMinimum() + " iron ingots and "
                + RankedRngState.getBastionObsidianMinimum() + " obsidian.");
    }

    @Unique
    private boolean mcsreasymode$isLikelyRuinedPortalChest(Inventory inventory) {
        return this.mcsreasymode$hasNearbyRuinedPortalBlocks()
                && (this.mcsreasymode$countItem(Items.OBSIDIAN) > 0
                || this.mcsreasymode$countItem(Items.CRYING_OBSIDIAN) > 0
                || this.mcsreasymode$countItem(Items.FIRE_CHARGE) > 0
                || this.mcsreasymode$countItem(Items.FLINT_AND_STEEL) > 0
                || this.mcsreasymode$countItem(Items.FLINT) > 0
                || this.mcsreasymode$countItem(Items.IRON_NUGGET) > 0
                || this.mcsreasymode$countItem(Items.GOLD_NUGGET) > 0
                || this.mcsreasymode$countItem(Items.GOLD_INGOT) > 0
                || this.mcsreasymode$countItem(Items.GOLD_BLOCK) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_APPLE) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_CARROT) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_AXE) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_SWORD) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_SHOVEL) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_PICKAXE) > 0
                || this.mcsreasymode$countItem(Items.GOLDEN_HOE) > 0
                || this.mcsreasymode$countItem(Items.BELL) > 0
                || this.mcsreasymode$countItem(Items.CLOCK) > 0);
    }

    @Unique
    private boolean mcsreasymode$isLikelyBuriedTreasureChest(Inventory inventory) {
        return this.mcsreasymode$countItem(Items.HEART_OF_THE_SEA) > 0;
    }

    @Unique
    private boolean mcsreasymode$hasNearbyRuinedPortalBlocks() {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        World world = blockEntity.getWorld();
        BlockPos pos = blockEntity.getPos();
        if (world == null || pos == null) {
            return false;
        }

        int portalBlocks = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = -8; x <= 8; x++) {
            for (int y = -6; y <= 6; y++) {
                for (int z = -8; z <= 8; z++) {
                    mutable.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    Block block = world.getBlockState(mutable).getBlock();
                    if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERRACK || block == Blocks.MAGMA_BLOCK) {
                        portalBlocks++;
                        if (portalBlocks >= 6) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
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
    private void mcsreasymode$ensureAtLeast(Inventory inventory, Item item, int minimum) {
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

        for (int slot = 0; slot < stacks.size(); slot++) {
            if (stacks.get(slot).isEmpty()) {
                inventory.setStack(slot, new ItemStack(item, missing));
                return;
            }
        }
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
