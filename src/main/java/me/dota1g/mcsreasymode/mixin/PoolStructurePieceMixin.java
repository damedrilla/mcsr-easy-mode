package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.BastionProtection;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(PoolStructurePiece.class)
public abstract class PoolStructurePieceMixin {
    @Inject(method = "generate", at = @At("RETURN"))
    private void mcsreasymode$captureBastionPiece(
            ServerWorldAccess world,
            StructureAccessor accessor,
            ChunkGenerator generator,
            Random random,
            BlockBox generationBox,
            ChunkPos chunkPos,
            BlockPos pivot,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValue()) {
            BastionProtection.capturePiece(world, (PoolStructurePiece) (Object) this, generationBox);
        }
    }
}
