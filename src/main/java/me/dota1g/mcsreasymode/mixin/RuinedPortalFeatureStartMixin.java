package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeRuinedPortalLootPiece;
import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.structure.RuinedPortalStructurePiece;
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
    private void mcsreasymode$planPortalLoot(ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, RuinedPortalFeatureConfig config, CallbackInfo ci) {
        if (config.portalType == RuinedPortalFeature.Type.NETHER) return;
        StructureStart<?> start = (StructureStart<?>) (Object) this;
        if (start.getChildren().isEmpty() || !(start.getChildren().get(0) instanceof RuinedPortalStructurePiece)) return;
        boolean animalsEnabled = Mcsreasymode.isRankedRuinedPortalAnimalsEnabled();
        McsreasymodeRuinedPortalLootPiece loot = null;
        if (Mcsreasymode.isRankedChestLootTablesEnabled()) {
            long seed = ((StructureStartRandomAccessor) start).mcsreasymode$getRandom().nextLong();
            loot = McsreasymodeRuinedPortalLootPiece.create(
                    (RuinedPortalStructurePiece) start.getChildren().get(0), seed, animalsEnabled);
        }
        if (loot != null) start.getChildren().add(loot);
        if (animalsEnabled && (loot == null || loot.isAnimalFoodRoute())) {
            BlockPos center = new BlockPos(start.getBoundingBox().getCenter());
            RankedRngState.registerPendingRuinedPortalAnimalSpawn(center);
        }
    }
}
