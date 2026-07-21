package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.StrongholdPieceMarker;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePiece.class)
public abstract class StructurePieceMixin {
    @Inject(method = "method_14937", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$allowStrongholdsNearLiquids(BlockView world, BlockBox box, CallbackInfoReturnable<Boolean> cir) {
        if (Mcsreasymode.isStrongholdAntiCorruptionEnabled() && (Object) this instanceof StrongholdPieceMarker) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(
            method = "fillWithOutline(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockBox;IIIIIIZLjava/util/Random;Lnet/minecraft/structure/StructurePiece$BlockRandomizer;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0
    )
    private boolean mcsreasymode$fillStrongholdWallsThroughAir(boolean existingOnly) {
        if (Mcsreasymode.isStrongholdAntiCorruptionEnabled() && (Object) this instanceof StrongholdPieceMarker) {
            return false;
        }
        return existingOnly;
    }
}
