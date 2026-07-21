package me.dota1g.mcsreasymode;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Random;

public class McsreasymodeVillageLavaPoolPiece extends StructurePiece {
    public static final int FOOTPRINT_X = 16;
    public static final int FOOTPRINT_Z = 16;
    private static final int FOOTPRINT_Y = 8;

    private final int originX;
    private final int originZ;
    private final long lakeSeed;

    public McsreasymodeVillageLavaPoolPiece(int originX, int originZ, long lakeSeed) {
        super(McsreasymodeStructurePieces.VILLAGE_LAVA_POOL, 0);
        this.originX = originX;
        this.originZ = originZ;
        this.lakeSeed = lakeSeed;
        this.boundingBox = new BlockBox(originX, 0, originZ, originX + FOOTPRINT_X - 1, 255, originZ + FOOTPRINT_Z - 1);
    }

    public McsreasymodeVillageLavaPoolPiece(CompoundTag tag) {
        super(McsreasymodeStructurePieces.VILLAGE_LAVA_POOL, tag);
        this.originX = tag.getInt("OriginX");
        this.originZ = tag.getInt("OriginZ");
        this.lakeSeed = tag.contains("LakeSeed") ? tag.getLong("LakeSeed") : (((long) this.originX << 32) ^ this.originZ);
        this.boundingBox = new BlockBox(this.originX, 0, this.originZ, this.originX + FOOTPRINT_X - 1, 255, this.originZ + FOOTPRINT_Z - 1);
    }

    @Override
    protected void toNbt(CompoundTag tag) {
        tag.putInt("OriginX", this.originX);
        tag.putInt("OriginZ", this.originZ);
        tag.putLong("LakeSeed", this.lakeSeed);
    }

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        int lakeBottomY = this.getAverageSurfaceY(world) - 4;
        if (lakeBottomY <= 4) {
            return false;
        }

        boolean[] shape = this.createVanillaLakeShape();
        if (!this.canGenerateLake(world, lakeBottomY, shape)) {
            return false;
        }

        this.carveLake(world, chunkBox, lakeBottomY, shape);
        this.restoreSurface(world, chunkBox, lakeBottomY, shape);
        this.addStoneRim(world, chunkBox, lakeBottomY, shape);
        return true;
    }

    private boolean[] createVanillaLakeShape() {
        Random random = new Random(this.lakeSeed);
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

    private boolean canGenerateLake(ServerWorldAccess world, int lakeBottomY, boolean[] shape) {
        for (int x = 0; x < FOOTPRINT_X; x++) {
            for (int z = 0; z < FOOTPRINT_Z; z++) {
                for (int y = 0; y < FOOTPRINT_Y; y++) {
                    if (!isBoundary(shape, x, z, y)) {
                        continue;
                    }

                    Material material = world.getBlockState(this.pos(x, lakeBottomY + y, z)).getMaterial();
                    if (y >= 4 && material.isLiquid()) {
                        return false;
                    }
                    if (y < 4 && !material.isSolid() && material != Material.LAVA) {
                        return false;
                    }
                }
            }
        }
        return true;
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

    private void addStoneRim(ServerWorldAccess world, BlockBox chunkBox, int lakeBottomY, boolean[] shape) {
        Random random = new Random(this.lakeSeed ^ 0x5DEECE66DL);
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
