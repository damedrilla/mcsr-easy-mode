package me.dota1g.mcsreasymode.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.UnderwaterCaveCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.BitSet;
import java.util.Random;

@Mixin(UnderwaterCaveCarver.class)
public interface UnderwaterCaveCarverInvoker {
    @Invoker("carveAtPoint")
    static boolean mcsreasymode$carveAtPoint(Carver<?> carver, Chunk chunk, BitSet carvingMask, Random random, BlockPos.Mutable mutable, int seaLevel, int mainChunkX, int mainChunkZ, int x, int z, int relativeX, int y, int relativeZ) {
        throw new AssertionError();
    }
}
