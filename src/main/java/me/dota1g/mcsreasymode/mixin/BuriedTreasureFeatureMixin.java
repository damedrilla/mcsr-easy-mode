package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.world.gen.feature.BuriedTreasureFeature;
import net.minecraft.world.gen.feature.BuriedTreasureFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BuriedTreasureFeature.class)
public class BuriedTreasureFeatureMixin {
    private static final float RANKED_BURIED_TREASURE_PROBABILITY = 0.10F;

    @Redirect(
            method = "shouldStartAt(Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/biome/source/BiomeSource;JLnet/minecraft/world/gen/ChunkRandom;IILnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/gen/feature/BuriedTreasureFeatureConfig;)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/BuriedTreasureFeatureConfig;probability:F"
            )
    )
    private float mcsreasymode$rankedBuriedTreasureProbability(BuriedTreasureFeatureConfig config) {
        if (Mcsreasymode.isRankedBuriedTreasureChanceEnabled()) {
            return RANKED_BURIED_TREASURE_PROBABILITY;
        }
        return config.probability;
    }
}
