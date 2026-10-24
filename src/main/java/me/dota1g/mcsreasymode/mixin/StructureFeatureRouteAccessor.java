package me.dota1g.mcsreasymode.mixin;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureFeature.class)
public interface StructureFeatureRouteAccessor {
    @Invoker("shouldStartAt")
    boolean mcsreasymode$shouldStartRoute(ChunkGenerator generator, BiomeSource biomes, long seed,
            ChunkRandom random, int chunkX, int chunkZ, Biome biome, ChunkPos candidate, FeatureConfig config);
}
