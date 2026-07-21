package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.UnderwaterCaveCarver;
import net.minecraft.world.gen.carver.UnderwaterRavineCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.BitSet;
import java.util.Random;

@Mixin(UnderwaterRavineCarver.class)
public abstract class UnderwaterRavineCarverMixin {
    @Redirect(
            method = "carveAtPoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/carver/UnderwaterCaveCarver;carveAtPoint(Lnet/minecraft/world/gen/carver/Carver;Lnet/minecraft/world/chunk/Chunk;Ljava/util/BitSet;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos$Mutable;IIIIIIII)Z"
            )
    )
    private boolean mcsreasymode$forceMagmaRavineFloor(Carver<?> carver, Chunk chunk, BitSet carvingMask, Random random, BlockPos.Mutable mutable, int seaLevel, int mainChunkX, int mainChunkZ, int x, int z, int relativeX, int y, int relativeZ) {
        if (!Mcsreasymode.isUnderwaterMagmaRavinesEnabled()) {
            return UnderwaterCaveCarverInvoker.mcsreasymode$carveAtPoint(carver, chunk, carvingMask, random, mutable, seaLevel, mainChunkX, mainChunkZ, x, z, relativeX, y, relativeZ);
        }
        return this.mcsreasymode$carveMagmaRavinePoint(carver, chunk, carvingMask, random, mutable, seaLevel, mainChunkX, mainChunkZ, x, z, relativeX, y, relativeZ);
    }

    private boolean mcsreasymode$carveMagmaRavinePoint(Carver<?> carver, Chunk chunk, BitSet carvingMask, Random random, BlockPos.Mutable mutable, int seaLevel, int mainChunkX, int mainChunkZ, int x, int z, int relativeX, int y, int relativeZ) {
        if (y >= seaLevel) {
            return false;
        }

        int maskIndex = relativeX | (relativeZ << 4) | (y << 8);
        if (carvingMask.get(maskIndex)) {
            return false;
        }
        carvingMask.set(maskIndex);

        mutable.set(x, y, z);
        if (!((CarverInvoker) carver).mcsreasymode$canAlwaysCarveBlock(chunk.getBlockState(mutable))) {
            return false;
        }

        if (y == 10) {
            if (random.nextFloat() < 0.15f) {
                chunk.setBlockState(mutable, Blocks.MAGMA_BLOCK.getDefaultState(), false);
                chunk.getBlockTickScheduler().schedule(new BlockPos(mutable), Blocks.MAGMA_BLOCK, 0);
            } else {
                chunk.setBlockState(mutable, Blocks.OBSIDIAN.getDefaultState(), false);
            }
            return true;
        }
        if (y < 10) {
            chunk.setBlockState(mutable, Blocks.LAVA.getDefaultState(), false);
            return false;
        }

        BlockPos waterPos = new BlockPos(mutable);
        chunk.setBlockState(mutable, Fluids.WATER.getDefaultState().getBlockState(), false);
        chunk.getFluidTickScheduler().schedule(waterPos, Fluids.WATER, 0);
        return true;
    }
}
