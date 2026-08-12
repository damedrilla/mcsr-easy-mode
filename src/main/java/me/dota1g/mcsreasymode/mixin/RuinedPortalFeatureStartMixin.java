package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.RuinedPortalFeature;
import net.minecraft.world.gen.feature.RuinedPortalFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RuinedPortalFeature.Start.class)
public abstract class RuinedPortalFeatureStartMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private void mcsreasymode$armAnimalGuarantee(ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, RuinedPortalFeatureConfig config, CallbackInfo ci) {
        if (!Mcsreasymode.isRankedRuinedPortalAnimalsEnabled()) {
            return;
        }
        if (config.portalType == RuinedPortalFeature.Type.NETHER) {
            return;
        }

        StructureStart<?> self = (StructureStart<?>) (Object) this;
        BlockPos center = new BlockPos(self.getBoundingBox().getCenter());
        RankedRngState.registerPendingRuinedPortalAnimalSpawn(center);
    }
}