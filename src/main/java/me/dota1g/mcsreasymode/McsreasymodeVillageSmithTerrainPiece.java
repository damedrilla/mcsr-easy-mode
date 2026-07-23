package me.dota1g.mcsreasymode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Random;

public class McsreasymodeVillageSmithTerrainPiece extends StructurePiece {
    private final BlockBox smithBox;

    public McsreasymodeVillageSmithTerrainPiece(BlockBox smithBox) {
        super(McsreasymodeStructurePieces.VILLAGE_SMITH_TERRAIN, 0);
        this.smithBox = new BlockBox(smithBox);
        this.boundingBox = new BlockBox(
                smithBox.minX - 1,
                smithBox.minY,
                smithBox.minZ - 1,
                smithBox.maxX + 1,
                smithBox.maxY + 4,
                smithBox.maxZ + 1
        );
    }

    public McsreasymodeVillageSmithTerrainPiece(StructureManager structureManager, CompoundTag tag) {
        super(McsreasymodeStructurePieces.VILLAGE_SMITH_TERRAIN, tag);
        this.smithBox = new BlockBox(
                tag.getInt("SmithMinX"),
                tag.getInt("SmithMinY"),
                tag.getInt("SmithMinZ"),
                tag.getInt("SmithMaxX"),
                tag.getInt("SmithMaxY"),
                tag.getInt("SmithMaxZ")
        );
        this.boundingBox = new BlockBox(
                tag.getInt("MinX"),
                tag.getInt("MinY"),
                tag.getInt("MinZ"),
                tag.getInt("MaxX"),
                tag.getInt("MaxY"),
                tag.getInt("MaxZ")
        );
    }

    @Override
    protected void toNbt(CompoundTag tag) {
        tag.putInt("SmithMinX", this.smithBox.minX);
        tag.putInt("SmithMinY", this.smithBox.minY);
        tag.putInt("SmithMinZ", this.smithBox.minZ);
        tag.putInt("SmithMaxX", this.smithBox.maxX);
        tag.putInt("SmithMaxY", this.smithBox.maxY);
        tag.putInt("SmithMaxZ", this.smithBox.maxZ);
        tag.putInt("MinX", this.boundingBox.minX);
        tag.putInt("MinY", this.boundingBox.minY);
        tag.putInt("MinZ", this.boundingBox.minZ);
        tag.putInt("MaxX", this.boundingBox.maxX);
        tag.putInt("MaxY", this.boundingBox.maxY);
        tag.putInt("MaxZ", this.boundingBox.maxZ);
    }

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = this.boundingBox.minX; x <= this.boundingBox.maxX; x++) {
            for (int z = this.boundingBox.minZ; z <= this.boundingBox.maxZ; z++) {
                for (int y = this.boundingBox.minY; y <= this.boundingBox.maxY; y++) {
                    mutable.set(x, y, z);
                    if (!chunkBox.contains(mutable)) {
                        continue;
                    }

                    BlockState state = world.getBlockState(mutable);
                    if (isTerrainToClear(state)) {
                        world.setBlockState(mutable, Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
        }
        return true;
    }

    private static boolean isTerrainToClear(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS_BLOCK
                || block == Blocks.DIRT
                || block == Blocks.COARSE_DIRT
                || block == Blocks.PODZOL
                || block == Blocks.STONE
                || block == Blocks.GRANITE
                || block == Blocks.DIORITE
                || block == Blocks.ANDESITE
                || block == Blocks.GRAVEL
                || block == Blocks.SAND
                || block == Blocks.RED_SAND
                || block == Blocks.SANDSTONE
                || block == Blocks.RED_SANDSTONE
                || block == Blocks.CLAY
                || block == Blocks.SNOW
                || block == Blocks.SNOW_BLOCK
                || block instanceof LeavesBlock;
    }
}
