package me.dota1g.mcsreasymode;

import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.nbt.CompoundTag;

public final class McsreasymodeStructurePieces {
    public static final StructurePieceType VILLAGE_BLACKSMITH = StructurePieceType.register(McsreasymodeStructurePieces::loadVillageBlacksmith, "mcsreasymode_village_blacksmith");

    private McsreasymodeStructurePieces() {
    }

    public static void init() {
    }

    private static StructurePiece loadVillageBlacksmith(StructureManager structureManager, CompoundTag tag) {
        return new McsreasymodeVillageBlacksmithPiece(tag);
    }
}
