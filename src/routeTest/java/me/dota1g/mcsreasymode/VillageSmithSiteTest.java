package me.dota1g.mcsreasymode;

import net.minecraft.Bootstrap;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;

import java.util.Collections;

public final class VillageSmithSiteTest {
    public static void main(String[] args) {
        Bootstrap.initialize();
        BlockBox house = new BlockBox(0, 70, 0, 10, 78, 10);
        VillageSmithSiteSelector.SurfaceProbe terrain = new VillageSmithSiteSelector.SurfaceProbe() {
            @Override public int surfaceY(int x, int z) { return z < 0 ? 72 : oceanFloorY(x, z); }
            @Override public int oceanFloorY(int x, int z) { return x < 0 ? 60 + Math.floorMod(z, 10) : 70; }
        };
        VillageSmithSiteSelector.Site site = VillageSmithSiteSelector.select(house, Collections.singletonList(house), 11, 9, terrain);
        require(site != null, "No smith site found");
        require(site.x >= 0 && site.z >= 0, "Selected water or steep terrain");
        require(site.groundY == 70, "Smith did not follow the local terrain");
        require(!house.intersectsXZ(site.x, site.z, site.x + 10, site.z + 8), "Smith overlaps an existing building");

        BlockBox oldSmith = new BlockBox(20, 69, 20, 30, 78, 28);
        McsreasymodeVillageSmithTerrainPiece helper = new McsreasymodeVillageSmithTerrainPiece(oldSmith);
        require(!helper.intersectsChunk(new ChunkPos(1, 1), 12), "Legacy smith helper must not reshape terrain");
        System.out.println("Village smith checks passed (ground height, dry site, overlap, terrain blending).");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
