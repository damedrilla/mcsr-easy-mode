package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeVillageLavaPoolPiece;
import me.dota1g.mcsreasymode.worldgen.VillageVariant;
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
        pieces.add(mcsreasymode$createLavaPoolPiece(villageBox, pieces, random));
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

    private static McsreasymodeVillageLavaPoolPiece mcsreasymode$createLavaPoolPiece(BlockBox villageBox, List<StructurePiece> pieces, ChunkRandom random) {
        long lakeSeed = random.nextLong();
        List<int[]> candidates = new ArrayList<>();
        int gap = 4;

        for (int i = 0; i < 8; i++) {
            int x = mcsreasymode$randomCoordinate(random, villageBox.minX, villageBox.maxX - McsreasymodeVillageLavaPoolPiece.FOOTPRINT_X + 1);
            int z = mcsreasymode$randomCoordinate(random, villageBox.minZ, villageBox.maxZ - McsreasymodeVillageLavaPoolPiece.FOOTPRINT_Z + 1);
            int extraGap = random.nextInt(12);

            candidates.add(new int[]{x, villageBox.minZ - McsreasymodeVillageLavaPoolPiece.FOOTPRINT_Z - gap - extraGap});
            candidates.add(new int[]{x, villageBox.maxZ + gap + extraGap});
            candidates.add(new int[]{villageBox.minX - McsreasymodeVillageLavaPoolPiece.FOOTPRINT_X - gap - extraGap, z});
            candidates.add(new int[]{villageBox.maxX + gap + extraGap, z});
        }
        Collections.shuffle(candidates, random);

        for (int[] candidate : candidates) {
            McsreasymodeVillageLavaPoolPiece pool = new McsreasymodeVillageLavaPoolPiece(candidate[0], candidate[1], lakeSeed);
            if (StructurePiece.getOverlappingPiece(pieces, pool.getBoundingBox()) == null) {
                return pool;
            }
        }
        return new McsreasymodeVillageLavaPoolPiece(villageBox.maxX + gap, villageBox.minZ, lakeSeed);
    }

    private static int mcsreasymode$randomCoordinate(ChunkRandom random, int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
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
}
