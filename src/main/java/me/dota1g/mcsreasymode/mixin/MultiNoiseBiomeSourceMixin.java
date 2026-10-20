package me.dota1g.mcsreasymode.mixin;

import com.mojang.datafixers.util.Pair;
import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
    @ModifyVariable(method = "<init>(JLjava/util/List;Ljava/util/Optional;)V",
            at = @At("HEAD"), argsOnly = true)
    private static List<Pair<Biome.MixedNoisePoint, Biome>> mcsreasymode$replaceBasaltDeltas(
            List<Pair<Biome.MixedNoisePoint, Biome>> points) {
        McsreasymodeConfig config = Mcsreasymode.CONFIG;
        if (config == null || !config.removeBasaltDeltas) {
            return points;
        }
        Biome replacement = config.basaltReplacementBiome == null
                ? Biomes.NETHER_WASTES : config.basaltReplacementBiome.biome();
        // Keep the original climate points so only basalt regions change. Supplying
        // the list before super() also keeps BiomeSource's feature caches consistent.
        List<Pair<Biome.MixedNoisePoint, Biome>> replaced = new ArrayList<>(points.size());
        for (Pair<Biome.MixedNoisePoint, Biome> point : points) {
            replaced.add(point.getSecond() == Biomes.BASALT_DELTAS
                    ? Pair.of(point.getFirst(), replacement) : point);
        }
        return replaced;
    }
}
