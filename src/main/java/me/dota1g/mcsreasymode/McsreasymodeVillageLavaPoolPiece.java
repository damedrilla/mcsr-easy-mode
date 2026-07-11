package me.dota1g.mcsreasymode;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Random;

public class McsreasymodeVillageLavaPoolPiece extends StructurePiece {
    public static final int FOOTPRINT_X = 10;
    public static final int FOOTPRINT_Z = 8;
    private static final String[] LAVA_POOL_SHAPE = new String[]{
            "  SSSSS  ",
            " SLLLLS ",
            "SLLLLLLS",
            "SLLLLLLS",
            " SLLLLS ",
            "  SSSS  "
    };

    private final int originX;
    private final int originZ;

    public McsreasymodeVillageLavaPoolPiece(int originX, int originZ) {
        super(McsreasymodeStructurePieces.VILLAGE_LAVA_POOL, 0);
        this.originX = originX;
        this.originZ = originZ;
        this.boundingBox = new BlockBox(originX, 0, originZ, originX + FOOTPRINT_X - 1, 255, originZ + FOOTPRINT_Z - 1);
    }

    public McsreasymodeVillageLavaPoolPiece(CompoundTag tag) {
        super(McsreasymodeStructurePieces.VILLAGE_LAVA_POOL, tag);
        this.originX = tag.getInt("OriginX");
        this.originZ = tag.getInt("OriginZ");
        this.boundingBox = new BlockBox(this.originX, 0, this.originZ, this.originX + FOOTPRINT_X - 1, 255, this.originZ + FOOTPRINT_Z - 1);
    }

    @Override
    protected void toNbt(CompoundTag tag) {
        tag.putInt("OriginX", this.originX);
        tag.putInt("OriginZ", this.originZ);
    }

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        int baseY = this.getAverageSurfaceY(world) - 1;
        for (int z = 0; z < LAVA_POOL_SHAPE.length; z++) {
            String row = LAVA_POOL_SHAPE[z];
            for (int x = 0; x < row.length(); x++) {
                char marker = row.charAt(x);
                if (marker == ' ') {
                    continue;
                }

                BlockState edge = ((x + z) % 3 == 0 ? Blocks.DIRT : Blocks.STONE).getDefaultState();
                this.set(world, chunkBox, x, baseY - 1, z, Blocks.STONE.getDefaultState());
                this.set(world, chunkBox, x, baseY, z, marker == 'L' ? Blocks.LAVA.getDefaultState() : edge);
                this.set(world, chunkBox, x, baseY + 1, z, Blocks.AIR.getDefaultState());
                this.set(world, chunkBox, x, baseY + 2, z, Blocks.AIR.getDefaultState());
            }
        }
        return true;
    }

    private int getAverageSurfaceY(ServerWorldAccess world) {
        int total = 0;
        int samples = 0;
        for (int x = 0; x <= 8; x += 4) {
            for (int z = 0; z <= 5; z += 2) {
                total += world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, this.originX + x, this.originZ + z);
                samples++;
            }
        }
        return samples == 0 ? 64 : total / samples;
    }

    private void set(ServerWorldAccess world, BlockBox chunkBox, int localX, int y, int localZ, BlockState state) {
        BlockPos pos = new BlockPos(this.originX + localX, y, this.originZ + localZ);
        if (chunkBox.contains(pos)) {
            world.setBlockState(pos, state, 2);
        }
    }
}
