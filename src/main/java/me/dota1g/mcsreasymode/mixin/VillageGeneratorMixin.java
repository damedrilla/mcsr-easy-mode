package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeVillageBlacksmithPiece;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.VillageGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(VillageGenerator.class)
public abstract class VillageGeneratorMixin {
    @Inject(method = "addPieces", at = @At("TAIL"))
    private static void mcsreasymode$addBlacksmithIfMissing(ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos pos, List<StructurePiece> pieces, ChunkRandom random, StructurePoolFeatureConfig config, CallbackInfo ci) {
        if (!Mcsreasymode.isVillageStandardizationEnabled() || pieces.isEmpty() || mcsreasymode$hasSmith(pieces)) {
            return;
        }

        BlockBox villageBox = mcsreasymode$getVillageBox(pieces);
        if (villageBox == null) {
            return;
        }

        McsreasymodeVillageBlacksmithPiece.Variant variant = mcsreasymode$getVariant(config, pieces);
        McsreasymodeVillageBlacksmithPiece blacksmith = mcsreasymode$createBlacksmithPiece(villageBox, pieces, variant);
        pieces.add(blacksmith);
        Mcsreasymode.debug("Village standardized: added artificial " + variant.logName + " blacksmith and nearby lava pool because no smith generated.");
    }

    private static boolean mcsreasymode$hasSmith(List<StructurePiece> pieces) {
        for (StructurePiece piece : pieces) {
            if (piece instanceof PoolStructurePiece) {
                String elementName = ((PoolStructurePiece) piece).getPoolElement().toString().toLowerCase();
                if (elementName.contains("smith")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BlockBox mcsreasymode$getVillageBox(List<StructurePiece> pieces) {
        BlockBox box = null;
        for (StructurePiece piece : pieces) {
            if (box == null) {
                box = new BlockBox(piece.getBoundingBox());
            } else {
                box.encompass(piece.getBoundingBox());
            }
        }
        return box;
    }

    private static McsreasymodeVillageBlacksmithPiece mcsreasymode$createBlacksmithPiece(BlockBox villageBox, List<StructurePiece> pieces, McsreasymodeVillageBlacksmithPiece.Variant variant) {
        List<McsreasymodeVillageBlacksmithPiece> candidates = new ArrayList<>();
        int gap = 2;
        for (StructurePiece villagePiece : pieces) {
            BlockBox box = villagePiece.getBoundingBox();
            candidates.add(new McsreasymodeVillageBlacksmithPiece(box.maxX + gap, box.minZ, variant));
            candidates.add(new McsreasymodeVillageBlacksmithPiece(box.minX - McsreasymodeVillageBlacksmithPiece.FOOTPRINT_X - gap, box.minZ, variant));
            candidates.add(new McsreasymodeVillageBlacksmithPiece(box.minX, box.maxZ + gap, variant));
            candidates.add(new McsreasymodeVillageBlacksmithPiece(box.minX, box.minZ - McsreasymodeVillageBlacksmithPiece.FOOTPRINT_Z - gap, variant));
        }

        final int centerX = (villageBox.minX + villageBox.maxX) / 2;
        final int centerZ = (villageBox.minZ + villageBox.maxZ) / 2;
        candidates.sort(Comparator.comparingInt(piece -> mcsreasymode$distanceTo(piece.getBoundingBox(), centerX, centerZ)));

        for (McsreasymodeVillageBlacksmithPiece candidate : candidates) {
            if (StructurePiece.getOverlappingPiece(pieces, candidate.getBoundingBox()) == null) {
                return candidate;
            }
        }

        return new McsreasymodeVillageBlacksmithPiece(centerX + gap, centerZ + gap, variant);
    }

    private static int mcsreasymode$distanceTo(BlockBox box, int x, int z) {
        int centerX = (box.minX + box.maxX) / 2;
        int centerZ = (box.minZ + box.maxZ) / 2;
        return Math.abs(centerX - x) + Math.abs(centerZ - z);
    }

    private static McsreasymodeVillageBlacksmithPiece.Variant mcsreasymode$getVariant(StructurePoolFeatureConfig config, List<StructurePiece> pieces) {
        McsreasymodeVillageBlacksmithPiece.Variant variant = McsreasymodeVillageBlacksmithPiece.Variant.fromPath(config.startPool.getPath().toLowerCase());
        if (variant != McsreasymodeVillageBlacksmithPiece.Variant.PLAINS) {
            return variant;
        }

        for (StructurePiece piece : pieces) {
            if (piece instanceof PoolStructurePiece) {
                variant = McsreasymodeVillageBlacksmithPiece.Variant.fromPath(((PoolStructurePiece) piece).getPoolElement().toString().toLowerCase());
                if (variant != McsreasymodeVillageBlacksmithPiece.Variant.PLAINS) {
                    return variant;
                }
            }
        }
        return variant;
    }
}
