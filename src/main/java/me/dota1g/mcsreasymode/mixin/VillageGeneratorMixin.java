package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeVillageLavaPoolPiece;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.VillageGenerator;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
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
import java.util.Collections;
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

        VillageVariant variant = mcsreasymode$getVariant(config, pieces);
        VillageGenerator.Piece smith = mcsreasymode$createSmithPiece(structureManager, villageBox, pieces, variant);
        pieces.add(smith);
        pieces.add(mcsreasymode$createLavaPoolPiece(smith, pieces));
        Mcsreasymode.debug("Village standardized: added vanilla " + variant.logName + " smith template and nearby lava pool because no smith generated.");
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

    private static VillageGenerator.Piece mcsreasymode$createSmithPiece(StructureManager structureManager, BlockBox villageBox, List<StructurePiece> pieces, VillageVariant variant) {
        StructurePoolElement element = new SinglePoolElement(variant.template, Collections.emptyList()).setProjection(StructurePool.Projection.TERRAIN_MATCHING);
        List<VillageGenerator.Piece> candidates = new ArrayList<>();
        int gap = 2;
        for (StructurePiece villagePiece : pieces) {
            BlockBox box = villagePiece.getBoundingBox();
            for (BlockRotation rotation : BlockRotation.values()) {
                candidates.add(mcsreasymode$newSmithCandidate(structureManager, element, box.maxX + gap, box.minY, box.minZ, rotation));
                candidates.add(mcsreasymode$newSmithCandidate(structureManager, element, box.minX - 12 - gap, box.minY, box.minZ, rotation));
                candidates.add(mcsreasymode$newSmithCandidate(structureManager, element, box.minX, box.minY, box.maxZ + gap, rotation));
                candidates.add(mcsreasymode$newSmithCandidate(structureManager, element, box.minX, box.minY, box.minZ - 12 - gap, rotation));
            }
        }

        final int centerX = (villageBox.minX + villageBox.maxX) / 2;
        final int centerZ = (villageBox.minZ + villageBox.maxZ) / 2;
        candidates.sort(Comparator.comparingInt(piece -> mcsreasymode$distanceTo(piece.getBoundingBox(), centerX, centerZ)));

        for (VillageGenerator.Piece candidate : candidates) {
            if (StructurePiece.getOverlappingPiece(pieces, candidate.getBoundingBox()) == null) {
                return candidate;
            }
        }

        return candidates.isEmpty()
                ? mcsreasymode$newSmithCandidate(structureManager, element, centerX + gap, villageBox.minY, centerZ + gap, BlockRotation.NONE)
                : candidates.get(0);
    }

    private static VillageGenerator.Piece mcsreasymode$newSmithCandidate(StructureManager structureManager, StructurePoolElement element, int x, int y, int z, BlockRotation rotation) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockBox box = element.getBoundingBox(structureManager, pos, rotation);
        return new VillageGenerator.Piece(structureManager, element, pos, element.getGroundLevelDelta(), rotation, box);
    }

    private static McsreasymodeVillageLavaPoolPiece mcsreasymode$createLavaPoolPiece(StructurePiece smith, List<StructurePiece> pieces) {
        BlockBox box = smith.getBoundingBox();
        int[][] candidates = new int[][]{
                {box.maxX + 3, box.minZ},
                {box.minX - McsreasymodeVillageLavaPoolPiece.FOOTPRINT_X - 3, box.minZ},
                {box.minX, box.maxZ + 3},
                {box.minX, box.minZ - McsreasymodeVillageLavaPoolPiece.FOOTPRINT_Z - 3}
        };

        for (int[] candidate : candidates) {
            McsreasymodeVillageLavaPoolPiece pool = new McsreasymodeVillageLavaPoolPiece(candidate[0], candidate[1]);
            if (StructurePiece.getOverlappingPiece(pieces, pool.getBoundingBox()) == null) {
                return pool;
            }
        }
        return new McsreasymodeVillageLavaPoolPiece(box.maxX + 3, box.minZ);
    }

    private static int mcsreasymode$distanceTo(BlockBox box, int x, int z) {
        int centerX = (box.minX + box.maxX) / 2;
        int centerZ = (box.minZ + box.maxZ) / 2;
        return Math.abs(centerX - x) + Math.abs(centerZ - z);
    }

    private static VillageVariant mcsreasymode$getVariant(StructurePoolFeatureConfig config, List<StructurePiece> pieces) {
        VillageVariant variant = VillageVariant.fromPath(config.startPool.getPath().toLowerCase());
        if (variant != VillageVariant.PLAINS) {
            return variant;
        }

        for (StructurePiece piece : pieces) {
            if (piece instanceof PoolStructurePiece) {
                variant = VillageVariant.fromPath(((PoolStructurePiece) piece).getPoolElement().toString().toLowerCase());
                if (variant != VillageVariant.PLAINS) {
                    return variant;
                }
            }
        }
        return variant;
    }

    private enum VillageVariant {
        DESERT("desert", "village/desert/houses/desert_weaponsmith_1"),
        PLAINS("plains", "village/plains/houses/plains_weaponsmith_1"),
        SAVANNA("savanna", "village/savanna/houses/savanna_weaponsmith_1"),
        SNOWY("snowy", "village/snowy/houses/snowy_weapon_smith_1"),
        TAIGA("taiga", "village/taiga/houses/taiga_weaponsmith_1");

        private final String logName;
        private final String template;

        VillageVariant(String logName, String template) {
            this.logName = logName;
            this.template = template;
        }

        private static VillageVariant fromPath(String path) {
            if (path.contains("desert")) {
                return DESERT;
            }
            if (path.contains("savanna")) {
                return SAVANNA;
            }
            if (path.contains("snowy")) {
                return SNOWY;
            }
            if (path.contains("taiga")) {
                return TAIGA;
            }
            return PLAINS;
        }
    }
}
