package me.dota1g.mcsreasymode;

import me.dota1g.mcsreasymode.mixin.StructureFeatureRouteAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;

/** Plans without loading chunks or consulting mutable structure discovery order. */
public final class NetherRoutePlanner {
    public final NetherRouteShape entryToBastion;
    public final NetherRouteShape bastionToFortress;

    public NetherRoutePlanner(ChunkGenerator generator, long seed, BlockPos entry) {
        BlockPos bastion = nearest(generator, seed, StructureFeature.BASTION_REMNANT, entry);
        BlockPos fortress = bastion == null ? null : nearest(generator, seed, StructureFeature.FORTRESS, bastion);
        this.entryToBastion = bastion == null ? null : route(generator, entry, bastion);
        this.bastionToFortress = fortress == null ? null : route(generator, bastion, fortress);
        Mcsreasymode.debug("Nether route plan: intended entry " + entry + ", bastion " + bastion + ", fortress " + fortress);
    }

    private static BlockPos nearest(ChunkGenerator generator, long seed, StructureFeature<?> feature, BlockPos from) {
        StructureConfig settings = generator.getConfig().method_28600(feature);
        if (settings == null || !generator.getBiomeSource().hasStructureFeature(feature)) return null;
        int spacing = settings.getSpacing();
        int originX = Math.floorDiv(from.getX() >> 4, spacing);
        int originZ = Math.floorDiv(from.getZ() >> 4, spacing);
        int regions = (64 + spacing - 1) / spacing + 1;
        long bestDistance = Long.MAX_VALUE;
        BlockPos best = null;
        ChunkRandom random = new ChunkRandom();
        for (int rx = originX - regions; rx <= originX + regions; rx++) {
            for (int rz = originZ - regions; rz <= originZ + regions; rz++) {
                ChunkPos candidate = feature.method_27218(settings, seed, random, rx * spacing, rz * spacing);
                int x = candidate.x * 16 + 8, z = candidate.z * 16 + 8;
                long dx = (long) x - from.getX(), dz = (long) z - from.getZ();
                long distance = dx * dx + dz * dz;
                if (distance > 1024L * 1024L || distance >= bestDistance) continue;
                Biome biome = generator.getBiomeSource().getBiomeForNoiseGen(candidate.x * 4 + 2, 0, candidate.z * 4 + 2);
                if (!biome.hasStructureFeature(feature)) continue;
                for (ConfiguredStructureFeature<?, ?> configured : biome.method_28413()) {
                    if (configured.field_24835 != feature) continue;
                    // method_27218 leaves random at exactly the state used by vanilla shouldStartAt.
                    if (((StructureFeatureRouteAccessor) feature).mcsreasymode$shouldStartRoute(generator,
                            generator.getBiomeSource(), seed, random, candidate.x, candidate.z, biome,
                            candidate, configured.field_24836)) {
                        bestDistance = distance;
                        best = new BlockPos(x, 64, z);
                    }
                    break;
                }
            }
        }
        return best;
    }

    private static NetherRouteShape route(ChunkGenerator generator, BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX(), dz = to.getZ() - from.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        int steps = Math.max(1, (int) Math.ceil(length / 24));
        double[][] points = new double[steps + 1][3];
        points[0] = new double[]{from.getX(), from.getY(), from.getZ()};
        points[steps] = new double[]{to.getX(), to.getY(), to.getZ()};
        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            double baseX = from.getX() + t * dx, baseZ = from.getZ() + t * dz;
            double bestScore = Double.POSITIVE_INFINITY;
            for (int offset : new int[]{0, -16, 16, -32, 32}) {
                double x = baseX - dz / length * offset, z = baseZ + dx / length * offset;
                BlockView column = generator.getColumnSample((int) Math.floor(x), (int) Math.floor(z));
                for (int y : new int[]{64, 72, 80}) {
                    double score = Math.abs(offset) * 0.12 + Math.abs(y - 64) * 0.15;
                    double[] previous = points[i - 1];
                    score += Math.sqrt(Math.pow(x - previous[0], 2) + Math.pow(z - previous[2], 2)) * 0.15;
                    score += Math.abs(y - previous[1]) * 0.5;
                    for (int sampleY = y - 8; sampleY <= y + 8; sampleY += 4) {
                        if (!column.getBlockState(new BlockPos(0, sampleY, 0)).isAir()) score += 6;
                    }
                    if (score < bestScore) {
                        bestScore = score;
                        points[i] = new double[]{x, y, z};
                    }
                }
            }
        }
        return new NetherRouteShape(points);
    }
}
