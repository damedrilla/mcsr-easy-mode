package me.dota1g.mcsreasymode;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Random;
import java.util.Arrays;

public class McsreasymodeVillageLavaPoolPiece extends StructurePiece {
    public static final int FOOTPRINT_X = 16;
    public static final int FOOTPRINT_Z = 16;
    private static final int FOOTPRINT_Y = 8;

    private final int originX;
    private final int originZ;
    private final long lakeSeed;
    private int lakeBottomY;
    private final boolean needsSupport;
    private final boolean legacyElevation;
    private boolean elevationResolved;

    public McsreasymodeVillageLavaPoolPiece(int originX, int originZ, int lakeBottomY, long lakeSeed, boolean needsSupport) {
        super(McsreasymodeStructurePieces.VILLAGE_LAVA_POOL, 0);
        this.originX = originX;
        this.originZ = originZ;
        this.lakeSeed = lakeSeed;
        this.lakeBottomY = lakeBottomY;
        this.needsSupport = needsSupport;
        this.legacyElevation = false;
        this.elevationResolved = false;
        this.boundingBox = new BlockBox(originX, lakeBottomY, originZ, originX + FOOTPRINT_X - 1, lakeBottomY + FOOTPRINT_Y - 1, originZ + FOOTPRINT_Z - 1);
    }

    public McsreasymodeVillageLavaPoolPiece(CompoundTag tag) {
        super(McsreasymodeStructurePieces.VILLAGE_LAVA_POOL, tag);
        this.originX = tag.getInt("OriginX");
        this.originZ = tag.getInt("OriginZ");
        this.lakeSeed = tag.contains("LakeSeed") ? tag.getLong("LakeSeed") : (((long) this.originX << 32) ^ this.originZ);
        this.legacyElevation = !tag.contains("LakeBottomY");
        this.lakeBottomY = tag.getInt("LakeBottomY");
        this.needsSupport = tag.getBoolean("NeedsSupport");
        this.elevationResolved = tag.getBoolean("ElevationResolved");
        this.boundingBox = this.legacyElevation
                ? new BlockBox(this.originX, 0, this.originZ, this.originX + FOOTPRINT_X - 1, 255, this.originZ + FOOTPRINT_Z - 1)
                : new BlockBox(this.originX, this.lakeBottomY, this.originZ, this.originX + FOOTPRINT_X - 1, this.lakeBottomY + FOOTPRINT_Y - 1, this.originZ + FOOTPRINT_Z - 1);
    }

    @Override
    protected void toNbt(CompoundTag tag) {
        tag.putInt("OriginX", this.originX);
        tag.putInt("OriginZ", this.originZ);
        tag.putLong("LakeSeed", this.lakeSeed);
        if (!this.legacyElevation) {
            tag.putInt("LakeBottomY", this.lakeBottomY);
            tag.putBoolean("NeedsSupport", this.needsSupport);
            tag.putBoolean("ElevationResolved", this.elevationResolved);
        }
    }

    @Override
    public boolean intersectsChunk(ChunkPos chunkPos, int offset) {
        // SurfaceChunkGenerator uses this hook to blend structure pieces into terrain.
        // The lake carves its own cavity during structure generation, so blending its
        // 16x16 bounding box creates a square depression around an otherwise small lake.
        return false;
    }

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        if (!this.legacyElevation && !this.elevationResolved) {
            this.resolveElevation(world);
        }
        int lakeBottomY = this.legacyElevation ? this.getAverageSurfaceY(world) - 4 : this.lakeBottomY;
        if (lakeBottomY <= 4) {
            return false;
        }

        Random lakeRandom = new Random(this.lakeSeed);
        boolean[] shape = createVanillaLakeShape(lakeRandom);
        if (!this.legacyElevation) {
            this.prepareWetGround(world, chunkBox, lakeBottomY);
        }
        if (!this.legacyElevation && (this.needsSupport || !isValidBoundary(world, this.originX, this.originZ, lakeBottomY, shape))) {
            this.prepareBoundary(world, chunkBox, lakeBottomY, shape);
        }
        this.carveLake(world, chunkBox, lakeBottomY, shape);
        this.restoreSurface(world, chunkBox, lakeBottomY, shape);
        this.addStoneRim(world, chunkBox, lakeBottomY, shape, lakeRandom);
        Mcsreasymode.debugRateLimited(
                "village.lava_pool.generated." + this.originX + "." + this.originZ,
                "Village lava pool generated at X " + this.originX + ", Y " + lakeBottomY + ", Z " + this.originZ + ".",
                5000L
        );
        return true;
    }

    private void prepareWetGround(ServerWorldAccess world, BlockBox chunkBox, int bottomY) {
        int shoreY = bottomY + 5;
        for (int x = 0; x < FOOTPRINT_X; x++) {
            for (int z = 0; z < FOOTPRINT_Z; z++) {
                int worldX = this.originX + x;
                int worldZ = this.originZ + z;
                int groundY = world.getTopY(Heightmap.Type.OCEAN_FLOOR, worldX, worldZ);
                int waterTopY = world.getTopY(Heightmap.Type.WORLD_SURFACE, worldX, worldZ);
                if (waterTopY <= groundY || !world.getBlockState(new BlockPos(worldX, waterTopY - 1, worldZ)).getMaterial().isLiquid()) continue;
                int edge = Math.min(Math.min(x, 15 - x), Math.min(z, 15 - z));
                int targetY = Math.max(waterTopY + 1, Math.min(shoreY, waterTopY + 1 + edge));
                BlockState top = world.getBiome(this.pos(x, targetY - 1, z)).getSurfaceConfig().getTopMaterial();
                for (int y = groundY; y < targetY; y++) {
                    this.set(world, chunkBox, x, y, z, y == targetY - 1 ? top : Blocks.STONE.getDefaultState());
                }
            }
        }
    }

    private void resolveElevation(ServerWorldAccess world) {
        int[] dryGround = new int[FOOTPRINT_X * FOOTPRINT_Z];
        int[] surfaceHeights = new int[FOOTPRINT_X * FOOTPRINT_Z];
        int dryCount = 0;
        int sampleCount = 0;
        for (int x = 0; x < FOOTPRINT_X; x++) {
            for (int z = 0; z < FOOTPRINT_Z; z++) {
                int worldX = this.originX + x;
                int worldZ = this.originZ + z;
                int ground = world.getTopY(Heightmap.Type.OCEAN_FLOOR, worldX, worldZ);
                int surface = world.getTopY(Heightmap.Type.WORLD_SURFACE, worldX, worldZ);
                surfaceHeights[sampleCount++] = surface;
                if (surface <= ground || !world.getBlockState(new BlockPos(worldX, surface - 1, worldZ)).getMaterial().isLiquid()) {
                    dryGround[dryCount++] = ground;
                }
            }
        }
        if (dryCount == 0) {
            for (int x = -8; x < FOOTPRINT_X + 8; x++) {
                for (int z = -8; z < FOOTPRINT_Z + 8; z++) {
                    if (x >= 0 && x < FOOTPRINT_X && z >= 0 && z < FOOTPRINT_Z) continue;
                    int worldX = this.originX + x;
                    int worldZ = this.originZ + z;
                    int ground = world.getTopY(Heightmap.Type.OCEAN_FLOOR, worldX, worldZ);
                    int surface = world.getTopY(Heightmap.Type.WORLD_SURFACE, worldX, worldZ);
                    if (surface <= ground || !world.getBlockState(new BlockPos(worldX, surface - 1, worldZ)).getMaterial().isLiquid()) {
                        if (dryCount == dryGround.length) dryGround = Arrays.copyOf(dryGround, dryGround.length * 2);
                        dryGround[dryCount++] = ground;
                    }
                }
            }
        }
        this.resolveElevationFromGround(dryGround, dryCount, surfaceHeights, sampleCount);
    }

    void resolveElevationFromGround(int[] dryGround, int dryCount, int[] surfaceHeights, int sampleCount) {
        int anchor;
        if (dryCount > 0) {
            Arrays.sort(dryGround, 0, dryCount);
            anchor = dryGround[dryCount / 4];
        } else {
            Arrays.sort(surfaceHeights, 0, sampleCount);
            anchor = surfaceHeights[sampleCount / 4];
        }
        this.lakeBottomY = Math.max(5, Math.min(248, anchor - 5));
        this.boundingBox = new BlockBox(this.originX, this.lakeBottomY, this.originZ,
                this.originX + FOOTPRINT_X - 1, this.lakeBottomY + FOOTPRINT_Y - 1, this.originZ + FOOTPRINT_Z - 1);
        this.elevationResolved = true;
    }

    private static boolean[] createVanillaLakeShape(Random random) {
        boolean[] shape = new boolean[FOOTPRINT_X * FOOTPRINT_Z * FOOTPRINT_Y];
        int blobCount = random.nextInt(4) + 4;

        for (int blob = 0; blob < blobCount; blob++) {
            double radiusX = random.nextDouble() * 6.0D + 3.0D;
            double radiusY = random.nextDouble() * 4.0D + 2.0D;
            double radiusZ = random.nextDouble() * 6.0D + 3.0D;
            double centerX = random.nextDouble() * (16.0D - radiusX - 2.0D) + 1.0D + radiusX / 2.0D;
            double centerY = random.nextDouble() * (8.0D - radiusY - 4.0D) + 2.0D + radiusY / 2.0D;
            double centerZ = random.nextDouble() * (16.0D - radiusZ - 2.0D) + 1.0D + radiusZ / 2.0D;

            for (int x = 1; x < 15; x++) {
                for (int z = 1; z < 15; z++) {
                    for (int y = 1; y < 7; y++) {
                        double normalizedX = ((double) x - centerX) / (radiusX / 2.0D);
                        double normalizedY = ((double) y - centerY) / (radiusY / 2.0D);
                        double normalizedZ = ((double) z - centerZ) / (radiusZ / 2.0D);
                        if (normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ < 1.0D) {
                            shape[index(x, z, y)] = true;
                        }
                    }
                }
            }
        }

        return shape;
    }

    public static boolean isVanillaSite(ChunkGenerator generator, int originX, int originZ, int bottomY, long lakeSeed) {
        boolean[] shape = createVanillaLakeShape(new Random(lakeSeed));
        for (int x = 0; x < FOOTPRINT_X; x++) {
            for (int z = 0; z < FOOTPRINT_Z; z++) {
                BlockView column = generator.getColumnSample(originX + x, originZ + z);
                for (int y = 0; y < FOOTPRINT_Y; y++) {
                    if (!isBoundary(shape, x, z, y)) continue;
                    BlockState state = column.getBlockState(new BlockPos(originX + x, bottomY + y, originZ + z));
                    if (invalidBoundary(state, y)) return false;
                }
            }
        }
        return true;
    }

    private static boolean isValidBoundary(ServerWorldAccess world, int originX, int originZ, int bottomY, boolean[] shape) {
        for (int x = 0; x < FOOTPRINT_X; x++) {
            for (int z = 0; z < FOOTPRINT_Z; z++) {
                for (int y = 0; y < FOOTPRINT_Y; y++) {
                    if (isBoundary(shape, x, z, y) && invalidBoundary(world.getBlockState(new BlockPos(originX + x, bottomY + y, originZ + z)), y)) return false;
                }
            }
        }
        return true;
    }

    private static boolean invalidBoundary(BlockState state, int y) {
        return y >= 4 ? state.getMaterial().isLiquid() : !state.getMaterial().isSolid() && state != Blocks.LAVA.getDefaultState();
    }

    private void prepareBoundary(ServerWorldAccess world, BlockBox chunkBox, int bottomY, boolean[] shape) {
        for (int x = 0; x < FOOTPRINT_X; x++) {
            for (int z = 0; z < FOOTPRINT_Z; z++) {
                for (int y = 0; y < FOOTPRINT_Y; y++) {
                    if (!isBoundary(shape, x, z, y)) continue;
                    BlockPos pos = this.pos(x, bottomY + y, z);
                    if (invalidBoundary(world.getBlockState(pos), y)) {
                        this.set(world, chunkBox, x, bottomY + y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private void carveLake(ServerWorldAccess world, BlockBox chunkBox, int lakeBottomY, boolean[] shape) {
        for (int x = 0; x < FOOTPRINT_X; x++) {
            for (int z = 0; z < FOOTPRINT_Z; z++) {
                for (int y = 0; y < FOOTPRINT_Y; y++) {
                    if (!shape[index(x, z, y)]) {
                        continue;
                    }

                    this.set(world, chunkBox, x, lakeBottomY + y, z, y >= 4 ? Blocks.CAVE_AIR.getDefaultState() : Blocks.LAVA.getDefaultState());
                }
            }
        }
    }

    private void restoreSurface(ServerWorldAccess world, BlockBox chunkBox, int lakeBottomY, boolean[] shape) {
        for (int x = 0; x < FOOTPRINT_X; x++) {
            for (int z = 0; z < FOOTPRINT_Z; z++) {
                for (int y = 4; y < FOOTPRINT_Y; y++) {
                    if (!shape[index(x, z, y)]) {
                        continue;
                    }

                    BlockPos floor = this.pos(x, lakeBottomY + y - 1, z);
                    if (Feature.isSoil(world.getBlockState(floor).getBlock())
                            && world.getLightLevel(net.minecraft.world.LightType.SKY, this.pos(x, lakeBottomY + y, z)) > 0) {
                        BlockState topMaterial = world.getBiome(floor).getSurfaceConfig().getTopMaterial();
                        this.set(world, chunkBox, x, lakeBottomY + y - 1, z, topMaterial.isOf(Blocks.MYCELIUM) ? Blocks.MYCELIUM.getDefaultState() : Blocks.GRASS_BLOCK.getDefaultState());
                    }
                }
            }
        }
    }

    private void addStoneRim(ServerWorldAccess world, BlockBox chunkBox, int lakeBottomY, boolean[] shape, Random random) {
        for (int x = 0; x < FOOTPRINT_X; x++) {
            for (int z = 0; z < FOOTPRINT_Z; z++) {
                for (int y = 0; y < FOOTPRINT_Y; y++) {
                    if (!isBoundary(shape, x, z, y)) {
                        continue;
                    }
                    if (y >= 4 && random.nextInt(2) == 0) {
                        continue;
                    }
                    BlockPos pos = this.pos(x, lakeBottomY + y, z);
                    if (world.getBlockState(pos).getMaterial().isSolid()) {
                        this.set(world, chunkBox, x, lakeBottomY + y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static boolean isBoundary(boolean[] shape, int x, int z, int y) {
        if (shape[index(x, z, y)]) {
            return false;
        }
        return (x < 15 && shape[index(x + 1, z, y)])
                || (x > 0 && shape[index(x - 1, z, y)])
                || (z < 15 && shape[index(x, z + 1, y)])
                || (z > 0 && shape[index(x, z - 1, y)])
                || (y < 7 && shape[index(x, z, y + 1)])
                || (y > 0 && shape[index(x, z, y - 1)]);
    }

    private static int index(int x, int z, int y) {
        return (x * FOOTPRINT_Z + z) * FOOTPRINT_Y + y;
    }

    private int getAverageSurfaceY(ServerWorldAccess world) {
        int total = 0;
        int samples = 0;
        for (int x = 0; x <= 15; x += 5) {
            for (int z = 0; z <= 15; z += 5) {
                total += world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, this.originX + x, this.originZ + z);
                samples++;
            }
        }
        return samples == 0 ? 64 : total / samples;
    }

    private BlockPos pos(int localX, int y, int localZ) {
        return new BlockPos(this.originX + localX, y, this.originZ + localZ);
    }

    private void set(ServerWorldAccess world, BlockBox chunkBox, int localX, int y, int localZ, BlockState state) {
        BlockPos pos = this.pos(localX, y, localZ);
        if (chunkBox.contains(pos)) {
            world.setBlockState(pos, state, 2);
        }
    }
}
