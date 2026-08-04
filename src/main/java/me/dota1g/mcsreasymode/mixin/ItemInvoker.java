package me.dota1g.mcsreasymode.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Item.class)
public interface ItemInvoker {
    @Invoker("rayTrace")
    static BlockHitResult mcsreasymode$rayTrace(World world, PlayerEntity player, RayTraceContext.FluidHandling fluidHandling) {
        throw new AssertionError();
    }
}
