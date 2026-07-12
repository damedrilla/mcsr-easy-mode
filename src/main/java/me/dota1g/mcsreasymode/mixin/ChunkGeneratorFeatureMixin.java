package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.StrongholdProtection;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorFeatureMixin {
    @Inject(method = "generateFeatures", at = @At("HEAD"))
    private void mcsreasymode$beginStrongholdProtection(ChunkRegion region, StructureAccessor accessor, CallbackInfo ci) {
        StrongholdProtection.beginFeatureGeneration();
    }

    @Inject(method = "generateFeatures", at = @At("RETURN"))
    private void mcsreasymode$restoreStronghold(ChunkRegion region, StructureAccessor accessor, CallbackInfo ci) {
        StrongholdProtection.restoreAndEnd(region);
    }
}
