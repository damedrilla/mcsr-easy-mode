package me.dota1g.mcsreasymode;

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
    public boolean intersectsChunk(ChunkPos chunkPos, int offset) {
        // Legacy helper pieces must not alter the noise terrain around a smith.
        return false;
    }

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        // Kept for loading old village NBT; the terrain-wide clearing caused trenches.
        return true;
    }
}
