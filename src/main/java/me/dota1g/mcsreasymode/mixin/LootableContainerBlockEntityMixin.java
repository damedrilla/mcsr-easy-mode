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
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin {
    @Shadow
    protected Identifier lootTableId;

    @Unique
    private Identifier mcsreasymode$lootTableIdBeforeGeneration;

    @Inject(method = "checkLootInteraction", at = @At("HEAD"))
    private void mcsreasymode$captureLootTable(PlayerEntity player, CallbackInfo ci) {
        this.mcsreasymode$lootTableIdBeforeGeneration = this.lootTableId;
    }

    @Inject(method = "checkLootInteraction", at = @At("TAIL"))
    private void mcsreasymode$adjustBastionChest(PlayerEntity player, CallbackInfo ci) {
        if (!Mcsreasymode.isRankedRngEnabled() || !this.mcsreasymode$isEligibleBastionChest() || !RankedRngState.shouldAdjustBastionChest()) {
            return;
        }

        Inventory inventory = (Inventory) this;
        this.mcsreasymode$ensureAtLeast(inventory, Items.IRON_INGOT, RankedRngState.getBastionIronMinimum());
        this.mcsreasymode$ensureAtLeast(inventory, Items.OBSIDIAN, RankedRngState.getBastionObsidianMinimum());
        inventory.markDirty();
        Mcsreasymode.debug("Bastion chest standardized: ensured at least "
                + RankedRngState.getBastionIronMinimum() + " iron ingots and "
                + RankedRngState.getBastionObsidianMinimum() + " obsidian.");
    }

    @Unique
    private boolean mcsreasymode$isEligibleBastionChest() {
        return LootTables.BASTION_OTHER_CHEST.equals(this.mcsreasymode$lootTableIdBeforeGeneration)
                || LootTables.BASTION_BRIDGE_CHEST.equals(this.mcsreasymode$lootTableIdBeforeGeneration);
    }

    @Unique
    private void mcsreasymode$ensureAtLeast(Inventory inventory, Item item, int minimum) {
        int current = inventory.count(item);
        int missing = minimum - current;
        if (missing <= 0) {
            return;
        }

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.getItem() == item) {
                stack.increment(missing);
                return;
            }
        }

        for (int slot = 0; slot < inventory.size(); slot++) {
            if (inventory.getStack(slot).isEmpty()) {
                inventory.setStack(slot, new ItemStack(item, missing));
                return;
            }
        }
    }
}
