package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {

    private static Set<Item> mcsreasymode$clutterItems;

    private static Set<Item> mcsreasymode$getClutterItems() {
        if (mcsreasymode$clutterItems == null) {
            Set<Item> items = new HashSet<>();
            items.add(Items.OAK_SAPLING);
            items.add(Items.SPRUCE_SAPLING);
            items.add(Items.BIRCH_SAPLING);
            items.add(Items.JUNGLE_SAPLING);
            items.add(Items.ACACIA_SAPLING);
            items.add(Items.DARK_OAK_SAPLING);
            items.add(Items.WHEAT_SEEDS);
            items.add(Items.BEETROOT_SEEDS);
            items.add(Items.MELON_SEEDS);
            items.add(Items.PUMPKIN_SEEDS);
            items.add(Items.FERN);
            items.add(Items.LARGE_FERN);
            items.add(Items.GRASS);
            items.add(Items.TALL_GRASS);
            items.add(Items.SEAGRASS);
            items.add(Items.DANDELION);
            items.add(Items.POPPY);
            items.add(Items.BLUE_ORCHID);
            items.add(Items.ALLIUM);
            items.add(Items.AZURE_BLUET);
            items.add(Items.RED_TULIP);
            items.add(Items.ORANGE_TULIP);
            items.add(Items.WHITE_TULIP);
            items.add(Items.PINK_TULIP);
            items.add(Items.OXEYE_DAISY);
            items.add(Items.CORNFLOWER);
            items.add(Items.LILY_OF_THE_VALLEY);
            items.add(Items.SUNFLOWER);
            mcsreasymode$clutterItems = items;
        }
        return mcsreasymode$clutterItems;
    }

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/loot/context/LootContext$Builder;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void mcsreasymode$filterExplosionClutterDrops(BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!Mcsreasymode.isFilterExplosionClutterDropsEnabled()) {
            return;
        }
        if (!me.dota1g.mcsreasymode.ExplosionDropState.isActive()) {
            return;
        }

        List<ItemStack> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) {
            return;
        }

        List<ItemStack> filtered = new ArrayList<>(original.size());
        for (ItemStack stack : original) {
            if (!mcsreasymode$getClutterItems().contains(stack.getItem())) {
                filtered.add(stack);
            }
        }

        if (filtered.size() != original.size()) {
            Mcsreasymode.debug("Explosion clutter filter: removed " + (original.size() - filtered.size()) + " item(s) during active explosion.");
        }

        cir.setReturnValue(filtered);
    }
}