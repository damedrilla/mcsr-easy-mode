package me.dota1g.mcsreasymode;

import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.nbt.CompoundTag;

public final class McsreasymodeStructurePieces {
    public static final StructurePieceType VILLAGE_LAVA_POOL = StructurePieceType.register(McsreasymodeStructurePieces::loadVillageLavaPool, "mcsreasymode_village_lava_pool");

    private McsreasymodeStructurePieces() {
    }

    public static void init() {
    }

    private static StructurePiece loadVillageLavaPool(StructureManager structureManager, CompoundTag tag) {
        return new McsreasymodeVillageLavaPoolPiece(tag);
    }
}
