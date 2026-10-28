package me.dota1g.mcsreasymode.mixin;

import net.minecraft.structure.StructureStart;
import net.minecraft.world.gen.ChunkRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureStart.class)
public interface StructureStartRandomAccessor {
    @Accessor("random")
    ChunkRandom mcsreasymode$getRandom();
}
