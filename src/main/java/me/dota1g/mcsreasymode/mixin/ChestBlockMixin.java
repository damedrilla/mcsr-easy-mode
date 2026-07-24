package me.dota1g.mcsreasymode.mixin;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin {
    @Redirect(
            method = "onStateReplaced",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/inventory/Inventory;)V"
            )
    )
    private void mcsreasymode$generateLootBeforeScattering(World world, BlockPos pos, Inventory inventory) {
        if (!world.isClient && inventory instanceof LootableContainerBlockEntity) {
            ((LootableContainerBlockEntity) inventory).checkLootInteraction(null);
        }

        ItemScatterer.spawn(world, pos, inventory);
    }
}
