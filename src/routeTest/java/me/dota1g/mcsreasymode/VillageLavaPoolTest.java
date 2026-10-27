package me.dota1g.mcsreasymode;

import net.minecraft.Bootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class VillageLavaPoolTest {
    public static void main(String[] args) {
        Bootstrap.initialize();
        BlockBox village = new BlockBox(-20, 60, -20, 20, 85, 20);
        List<BlockBox> occupied = Arrays.asList(village, new BlockBox(32, 60, 0, 47, 85, 15));
        VillageLavaPoolPlanner.SurfaceProbe probe = new VillageLavaPoolPlanner.SurfaceProbe() {
            @Override
            public int surfaceY(int x, int z) { return 70; }

            @Override
            public int oceanFloorY(int x, int z) { return 70; }

            @Override
            public boolean vanillaSite(int x, int z, int bottomY, long seed) {
                return x < -32;
            }
        };
        List<VillageLavaPoolPlanner.Site> first = VillageLavaPoolPlanner.select(village, occupied, 12345L, probe);
        List<VillageLavaPoolPlanner.Site> again = VillageLavaPoolPlanner.select(village, occupied, 12345L, probe);
        require(first.size() == 3 && again.size() == 3, "Expected three pools");
        for (int i = 0; i < 3; i++) {
            VillageLavaPoolPlanner.Site site = first.get(i);
            VillageLavaPoolPlanner.Site repeated = again.get(i);
            require(site.x == repeated.x && site.z == repeated.z && site.seed == repeated.seed, "Placement changed with the same seed");
            require(Math.abs(site.x + 8) <= 96 && Math.abs(site.z + 8) <= 96, "Pool too far from center");
            require(site.x % 16 == 0 && site.z % 16 == 0 && site.bottomY == 65, "Pool footprint or elevation changed");
            for (BlockBox box : occupied) require(!box.intersectsXZ(site.x, site.z, site.x + 15, site.z + 15), "Pool intersects village piece");
            for (int j = 0; j < i; j++) {
                require(site.x != first.get(j).x || site.z != first.get(j).z, "Duplicate pool");
                require(Math.abs(site.x - first.get(j).x) >= 32 || Math.abs(site.z - first.get(j).z) >= 32, "Pool shores can join");
            }
            McsreasymodeVillageLavaPoolPiece piece = new McsreasymodeVillageLavaPoolPiece(site.x, site.z, site.bottomY, site.seed, site.needsSupport);
            require(!piece.intersectsChunk(new ChunkPos(site.x >> 4, site.z >> 4), 12), "Pool must not reshape terrain during noise generation");
            require(piece.getBoundingBox().intersects(new BlockBox(site.x, 0, site.z, site.x + 15, 255, site.z + 15)), "Pool must remain eligible for structure generation");
            CompoundTag saved = new CompoundTag();
            piece.toNbt(saved);
            CompoundTag roundTrip = new CompoundTag();
            McsreasymodeVillageLavaPoolPiece loaded = new McsreasymodeVillageLavaPoolPiece(saved);
            require(!loaded.intersectsChunk(new ChunkPos(site.x >> 4, site.z >> 4), 12), "Loaded pool must not reshape terrain");
            loaded.toNbt(roundTrip);
            require(saved.equals(roundTrip), "Pool NBT changed on load");
        }
        List<VillageLavaPoolPlanner.Site> fallback = VillageLavaPoolPlanner.select(village, Collections.singletonList(village), 2L,
                new VillageLavaPoolPlanner.SurfaceProbe() {
                    @Override public int surfaceY(int x, int z) { return 70; }
                    @Override public int oceanFloorY(int x, int z) { return 70; }
                    @Override public boolean vanillaSite(int x, int z, int y, long seed) { return false; }
                });
        require(fallback.size() == 3, "Fallback did not guarantee three pools");
        for (VillageLavaPoolPlanner.Site site : fallback) {
            require(site.needsSupport, "Fallback support flag missing");
            CompoundTag saved = new CompoundTag();
            new McsreasymodeVillageLavaPoolPiece(site.x, site.z, site.bottomY, site.seed, site.needsSupport).toNbt(saved);
            require(saved.getBoolean("NeedsSupport"), "Fallback support was not saved");
        }
        List<VillageLavaPoolPlanner.Site> dryFirst = VillageLavaPoolPlanner.select(village, Collections.singletonList(village), 3L,
                new VillageLavaPoolPlanner.SurfaceProbe() {
                    @Override public int surfaceY(int x, int z) { return x >= 0 ? 70 : 76; }
                    @Override public int oceanFloorY(int x, int z) { return 70; }
                    @Override public boolean vanillaSite(int x, int z, int y, long seed) { return false; }
                });
        for (VillageLavaPoolPlanner.Site site : dryFirst) require(site.x >= 0, "Wet fallback selected before dry ground");
        final boolean[] basinProbed = {false};
        VillageLavaPoolPlanner.select(village, Collections.singletonList(village), 5L,
                new VillageLavaPoolPlanner.SurfaceProbe() {
                    @Override public int surfaceY(int x, int z) { return oceanFloorY(x, z); }
                    @Override public int oceanFloorY(int x, int z) { return x >= 32 && x < 48 && z >= 32 && z < 48 ? 60 : 72; }
                    @Override public boolean vanillaSite(int x, int z, int y, long seed) {
                        if (x == 32 && z == 32) basinProbed[0] = true;
                        return false;
                    }
                });
        require(!basinProbed[0], "A basin was treated as a vanilla surface site");
        List<VillageLavaPoolPlanner.Site> sloped = VillageLavaPoolPlanner.select(village, Collections.singletonList(village), 4L,
                new VillageLavaPoolPlanner.SurfaceProbe() {
                    @Override public int surfaceY(int x, int z) { return oceanFloorY(x, z); }
                    @Override public int oceanFloorY(int x, int z) { return Math.floorMod(x, 16) < 4 ? 67 : 70; }
                    @Override public boolean vanillaSite(int x, int z, int y, long seed) { return true; }
                });
        for (VillageLavaPoolPlanner.Site site : sloped) require(site.bottomY == 62, "Lake was raised above the low side of a slope");
        McsreasymodeVillageLavaPoolPiece carvedGround = new McsreasymodeVillageLavaPoolPiece(32, 48, 64, 7L, false);
        carvedGround.resolveElevationFromGround(new int[]{61, 61, 61, 61, 61, 80}, 6, new int[]{61, 61, 61, 61, 61, 80}, 6);
        require(carvedGround.getBoundingBox().minY == 56, "Pool did not follow post-carving ground");
        CompoundTag resolved = new CompoundTag();
        carvedGround.toNbt(resolved);
        require(resolved.getInt("LakeBottomY") == 56 && resolved.getBoolean("ElevationResolved"), "Resolved elevation was not saved");
        require(new McsreasymodeVillageLavaPoolPiece(resolved).getBoundingBox().minY == 56, "Resolved elevation was not restored");
        McsreasymodeVillageLavaPoolPiece pond = new McsreasymodeVillageLavaPoolPiece(64, 80, 60, 8L, true);
        pond.resolveElevationFromGround(new int[]{65, 65, 65, 66, 66, 66}, 6,
                new int[]{45, 45, 63, 65, 65, 66, 66, 66}, 8);
        require(pond.getBoundingBox().minY == 60, "A water hole dragged the lake below its dry shore");
        McsreasymodeVillageLavaPoolPiece isolatedHole = new McsreasymodeVillageLavaPoolPiece(80, 80, 60, 9L, true);
        isolatedHole.resolveElevationFromGround(new int[]{45, 65, 65, 65, 65, 65, 65, 65}, 8,
                new int[]{45, 65, 65, 65, 65, 65, 65, 65}, 8);
        require(isolatedHole.getBoundingBox().minY == 60, "One deep hole sank the entire lake");
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("OriginX", 16);
        legacy.putInt("OriginZ", -32);
        legacy.putLong("LakeSeed", 42L);
        CompoundTag loadedLegacy = new CompoundTag();
        new McsreasymodeVillageLavaPoolPiece(legacy).toNbt(loadedLegacy);
        require(loadedLegacy.getInt("OriginX") == 16 && loadedLegacy.getInt("OriginZ") == -32
                && loadedLegacy.getLong("LakeSeed") == 42L && !loadedLegacy.contains("LakeBottomY"), "Legacy pool NBT changed");
        System.out.println("Village lava pool checks passed (determinism, distance, overlap, NBT, fallback).");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
