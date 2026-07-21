package me.dota1g.mcsreasymode.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.carver.Carver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Carver.class)
public interface CarverInvoker {
    @Invoker("canAlwaysCarveBlock")
    boolean mcsreasymode$canAlwaysCarveBlock(BlockState state);
}
