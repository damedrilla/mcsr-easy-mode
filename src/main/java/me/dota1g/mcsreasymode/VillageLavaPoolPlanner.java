package me.dota1g.mcsreasymode;

import net.minecraft.util.math.BlockBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Chooses chunk-contained lake footprints before village pieces start generating. */
public final class VillageLavaPoolPlanner {
    public static final int POOL_COUNT = 3;
    public static final int MAX_DISTANCE = 6 * 16;

    private VillageLavaPoolPlanner() {
    }

    public interface SurfaceProbe {
        int surfaceY(int x, int z);

        int oceanFloorY(int x, int z);

        boolean vanillaSite(int originX, int originZ, int bottomY, long lakeSeed);
    }

    public static final class Site {
        public final int x;
        public final int z;
        public final int bottomY;
        public final long seed;
        public final boolean needsSupport;

        private Site(int x, int z, int bottomY, long seed, boolean needsSupport) {
            this.x = x;
            this.z = z;
            this.bottomY = bottomY;
            this.seed = seed;
            this.needsSupport = needsSupport;
        }
    }

    public static List<Site> select(BlockBox village, List<BlockBox> occupied, long seed, SurfaceProbe probe) {
        int centerX = (village.minX + village.maxX) / 2;
        int centerZ = (village.minZ + village.maxZ) / 2;
        int centerChunkX = Math.floorDiv(centerX, 16);
        int centerChunkZ = Math.floorDiv(centerZ, 16);
        List<int[]> candidates = new ArrayList<>();

        for (int chunkX = centerChunkX - 6; chunkX <= centerChunkX + 6; chunkX++) {
            for (int chunkZ = centerChunkZ - 6; chunkZ <= centerChunkZ + 6; chunkZ++) {
                int x = chunkX * 16;
                int z = chunkZ * 16;
                if (Math.max(Math.abs(x + 8 - centerX), Math.abs(z + 8 - centerZ)) > MAX_DISTANCE) {
                    continue;
                }
                boolean overlaps = false;
                for (BlockBox box : occupied) {
                    if (box.intersectsXZ(x, z, x + 15, z + 15)) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps) {
                    candidates.add(new int[]{x, z});
                }
            }
        }

        Collections.shuffle(candidates, new Random(seed));
        candidates.sort(Comparator.comparingInt(pos -> Math.abs(pos[0] + 8 - centerX) + Math.abs(pos[1] + 8 - centerZ)));

        List<Site> vanilla = new ArrayList<>();
        List<Site> crowdedVanilla = new ArrayList<>();
        List<Site> levelDryFallback = new ArrayList<>();
        List<Site> basinFallback = new ArrayList<>();
        List<Site> roughDryFallback = new ArrayList<>();
        List<Site> wetFallback = new ArrayList<>();
        for (int[] candidate : candidates) {
            int x = candidate[0];
            int z = candidate[1];
            int minHeight = Integer.MAX_VALUE;
            int maxHeight = Integer.MIN_VALUE;
            int[] groundHeights = new int[36];
            int sample = 0;
            boolean dry = true;
            for (int localX = 0; localX < 16; localX += 3) {
                for (int localZ = 0; localZ < 16; localZ += 3) {
                    int surface = probe.surfaceY(x + localX, z + localZ);
                    int ground = probe.oceanFloorY(x + localX, z + localZ);
                    groundHeights[sample++] = ground;
                    minHeight = Math.min(minHeight, ground);
                    maxHeight = Math.max(maxHeight, ground);
                    if (surface > ground) dry = false;
                }
            }
            Arrays.sort(groundHeights);
            int middleGround = groundHeights[groundHeights.length / 2];
            int[] surroundingHeights = new int[20];
            int surroundingCount = 0;
            for (int offset = 0; offset <= 20; offset += 5) {
                surroundingHeights[surroundingCount++] = probe.oceanFloorY(x + offset - 3, z - 8);
                surroundingHeights[surroundingCount++] = probe.oceanFloorY(x + offset - 3, z + 23);
                surroundingHeights[surroundingCount++] = probe.oceanFloorY(x - 8, z + offset - 3);
                surroundingHeights[surroundingCount++] = probe.oceanFloorY(x + 23, z + offset - 3);
            }
            Arrays.sort(surroundingHeights);
            boolean basin = surroundingHeights[surroundingHeights.length / 2] > middleGround + 3;
            int bottomY = Math.max(5, Math.min(248, minHeight - 5));
            long lakeSeed = new Random(seed ^ ((long) x << 32) ^ (z & 0xffffffffL)).nextLong();
            boolean level = maxHeight - minHeight <= 1;
            boolean valid = dry && level && !basin && probe.vanillaSite(x, z, bottomY, lakeSeed);
            Site site = new Site(x, z, bottomY, lakeSeed, !valid);
            if (valid) {
                if (farEnough(vanilla, site)) vanilla.add(site);
                else crowdedVanilla.add(site);
            }
            else if (dry && level && !basin) levelDryFallback.add(site);
            else if (dry && basin) basinFallback.add(site);
            else if (dry) roughDryFallback.add(site);
            else wetFallback.add(site);
            if (vanilla.size() == POOL_COUNT) {
                return vanilla;
            }
        }

        fill(vanilla, levelDryFallback, true);
        fill(vanilla, roughDryFallback, true);
        fill(vanilla, basinFallback, true);
        fill(vanilla, wetFallback, true);
        // Very compact villages can leave fewer than three separated footprints.
        fill(vanilla, crowdedVanilla, false);
        fill(vanilla, levelDryFallback, false);
        fill(vanilla, roughDryFallback, false);
        fill(vanilla, basinFallback, false);
        fill(vanilla, wetFallback, false);
        return vanilla;
    }

    private static void fill(List<Site> selected, List<Site> candidates, boolean separated) {
        for (Site site : candidates) {
            if (selected.size() == POOL_COUNT) return;
            if (!separated || farEnough(selected, site)) selected.add(site);
        }
    }

    private static boolean farEnough(List<Site> selected, Site site) {
        for (Site existing : selected) {
            if (Math.abs(existing.x - site.x) < 32 && Math.abs(existing.z - site.z) < 32) return false;
        }
        return true;
    }
}
