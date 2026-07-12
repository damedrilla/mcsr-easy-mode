package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.StrongholdProtection;
import me.dota1g.mcsreasymode.StrongholdPieceMarker;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
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

@Mixin({
        StrongholdGenerator.ChestCorridor.class,
        StrongholdGenerator.Corridor.class,
        StrongholdGenerator.FiveWayCrossing.class,
        StrongholdGenerator.LeftTurn.class,
        StrongholdGenerator.Library.class,
        StrongholdGenerator.PortalRoom.class,
        StrongholdGenerator.PrisonHall.class,
        StrongholdGenerator.RightTurn.class,
        StrongholdGenerator.SmallCorridor.class,
        StrongholdGenerator.SpiralStaircase.class,
        StrongholdGenerator.SquareRoom.class,
        StrongholdGenerator.Stairs.class
})
public abstract class StrongholdPieceMixin implements StrongholdPieceMarker {
    @Inject(method = "generate", at = @At("RETURN"))
    private void mcsreasymode$captureStrongholdPiece(
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
            StrongholdProtection.capturePiece(world, ((StructurePiece) (Object) this).getBoundingBox(), generationBox);
        }
    }
}
