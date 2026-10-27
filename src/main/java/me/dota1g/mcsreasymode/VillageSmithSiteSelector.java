package me.dota1g.mcsreasymode;

import net.minecraft.util.math.BlockBox;

import java.util.ArrayList;
import java.util.List;

/** Finds a dry, level footprint beside the village for a missing smith. */
public final class VillageSmithSiteSelector {
    private VillageSmithSiteSelector() {
    }

    public interface SurfaceProbe {
        int surfaceY(int x, int z);

        int oceanFloorY(int x, int z);
    }

    public static final class Site {
        public final int x;
        public final int z;
        public final int groundY;
        public final int score;

        private Site(int x, int z, int groundY, int score) {
            this.x = x;
            this.z = z;
            this.groundY = groundY;
            this.score = score;
        }
    }

    public static Site select(BlockBox village, List<BlockBox> occupied, int width, int depth, SurfaceProbe probe) {
        int centerX = (village.minX + village.maxX) / 2;
        int centerZ = (village.minZ + village.maxZ) / 2;
        List<int[]> positions = new ArrayList<>();
        for (BlockBox box : occupied) {
            positions.add(new int[]{box.maxX + 2, (box.minZ + box.maxZ - depth + 1) / 2});
            positions.add(new int[]{box.minX - width - 1, (box.minZ + box.maxZ - depth + 1) / 2});
            positions.add(new int[]{(box.minX + box.maxX - width + 1) / 2, box.maxZ + 2});
            positions.add(new int[]{(box.minX + box.maxX - width + 1) / 2, box.minZ - depth - 1});
        }
        // The nearby grid handles villages whose piece edges all face steep ground.
        for (int radius = 0; radius <= 80; radius += 8) {
            for (int dx = -radius; dx <= radius; dx += 8) {
                for (int dz = -radius; dz <= radius; dz += 8) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    positions.add(new int[]{centerX + dx - width / 2, centerZ + dz - depth / 2});
                }
            }
        }

        Site best = null;
        for (int index = 0; index < positions.size(); index++) {
            if (index == occupied.size() * 4 && best != null && best.score < 250) return best;
            int x = positions.get(index)[0];
            int z = positions.get(index)[1];
            if (Math.max(Math.abs(x + width / 2 - centerX), Math.abs(z + depth / 2 - centerZ)) > 96) continue;
            boolean overlaps = false;
            for (BlockBox box : occupied) {
                if (box.intersectsXZ(x, z, x + width - 1, z + depth - 1)) {
                    overlaps = true;
                    break;
                }
            }
            if (overlaps) continue;

            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            int[] ground = new int[9];
            boolean wet = false;
            for (int sx = 0; sx < 3; sx++) {
                for (int sz = 0; sz < 3; sz++) {
                    int sampleX = x + sx * (width - 1) / 2;
                    int sampleZ = z + sz * (depth - 1) / 2;
                    int floorY = probe.oceanFloorY(sampleX, sampleZ);
                    ground[sx * 3 + sz] = floorY;
                    minY = Math.min(minY, floorY);
                    maxY = Math.max(maxY, floorY);
                    wet |= probe.surfaceY(sampleX, sampleZ) > floorY;
                }
            }
            int middleY = ground[4];
            int slope = maxY - minY;
            int[] outside = {
                    probe.oceanFloorY(x - 2, z), probe.oceanFloorY(x - 2, z + depth - 1),
                    probe.oceanFloorY(x + width + 1, z), probe.oceanFloorY(x + width + 1, z + depth - 1),
                    probe.oceanFloorY(x, z - 2), probe.oceanFloorY(x + width - 1, z - 2),
                    probe.oceanFloorY(x, z + depth + 1), probe.oceanFloorY(x + width - 1, z + depth + 1)
            };
            int exposure = 0;
            for (int outsideY : outside) {
                exposure = Math.max(exposure, Math.max(maxY - outsideY, outsideY - minY));
            }
            int distance = Math.abs(x + width / 2 - centerX) + Math.abs(z + depth / 2 - centerZ);
            // Prefer ground where the complete building footprint can meet its foundation.
            int score = (wet ? 100000 : 0) + (slope > 2 || exposure > 2 ? 10000 : 0)
                    + slope * 1000 + exposure * 500 + (index >= occupied.size() * 4 ? 250 : 0) + distance;
            if (best == null || score < best.score) best = new Site(x, z, middleY, score);
        }
        return best;
    }
}
