package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.structure.BastionRemnantGenerator;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.BastionRemnantFeatureConfig;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(BastionRemnantGenerator.class)
public abstract class BastionRemnantGeneratorMixin {
    private static final Identifier MCSREASYMODE_HOGLIN_STABLE_ORIGIN = new Identifier("bastion/hoglin_stable/origin");
    private static final int MCSREASYMODE_TARGET_ADJACENT_FULL_STABLE_RAMPARTS = 2;
    private static final int MCSREASYMODE_STABLE_RAMPART_ATTEMPTS = 64;

    @Inject(method = "addPieces", at = @At("HEAD"), cancellable = true)
    private static void mcsreasymode$rerollHoglinStableRamparts(ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos pos, List<StructurePiece> pieces, ChunkRandom random, BastionRemnantFeatureConfig config, CallbackInfo ci) {
        if (!Mcsreasymode.isRankedRngEnabled()) {
            return;
        }

        BastionRemnantGenerator.init();
        StructurePoolFeatureConfig selectedConfig = config.getRandom(random);
        if (!MCSREASYMODE_HOGLIN_STABLE_ORIGIN.equals(selectedConfig.startPool)) {
            StructurePoolBasedGenerator.addPieces(
                    selectedConfig.startPool,
                    selectedConfig.size,
                    BastionRemnantGenerator.Piece::new,
                    chunkGenerator,
                    structureManager,
                    pos,
                    pieces,
                    random,
                    false,
                    false
            );
            ci.cancel();
            return;
        }

        List<StructurePiece> bestPieces = null;
        int bestScore = -1;
        int bestRampartCount = 0;
        boolean bestHasAdjacentPair = false;
        for (int attempt = 1; attempt <= MCSREASYMODE_STABLE_RAMPART_ATTEMPTS; attempt++) {
            List<StructurePiece> attemptPieces = new ArrayList<>();
            Random attemptRandom = new Random(random.nextLong());
            StructurePoolBasedGenerator.addPieces(
                    selectedConfig.startPool,
                    selectedConfig.size,
                    BastionRemnantGenerator.Piece::new,
                    chunkGenerator,
                    structureManager,
                    pos,
                    attemptPieces,
                    attemptRandom,
                    false,
                    false
            );

            List<PoolStructurePiece> fullRamparts = mcsreasymode$getFullHeightStableRamparts(attemptPieces);
            int rampartCount = fullRamparts.size();
            boolean hasAdjacentPair = mcsreasymode$hasAdjacentFullHeightPair(fullRamparts);
            int score = (hasAdjacentPair ? 100 : 0) + rampartCount;
            if (score > bestScore) {
                bestScore = score;
                bestRampartCount = rampartCount;
                bestHasAdjacentPair = hasAdjacentPair;
                bestPieces = attemptPieces;
            }
            if (rampartCount >= MCSREASYMODE_TARGET_ADJACENT_FULL_STABLE_RAMPARTS && hasAdjacentPair) {
                pieces.addAll(attemptPieces);
                Mcsreasymode.debug("Hoglin stable standardized: selected assembly with adjacent full-height ramparts after " + attempt + " attempt(s); full-height ramparts present: " + rampartCount + ".");
                ci.cancel();
                return;
            }
        }

        if (bestPieces != null) {
            pieces.addAll(bestPieces);
            Mcsreasymode.debug("Hoglin stable standardization fallback: best assembly had " + bestRampartCount + " full-height rampart(s), adjacent pair: " + bestHasAdjacentPair + ", after " + MCSREASYMODE_STABLE_RAMPART_ATTEMPTS + " attempts.");
        }
        ci.cancel();
    }

    private static List<PoolStructurePiece> mcsreasymode$getFullHeightStableRamparts(List<StructurePiece> pieces) {
        List<PoolStructurePiece> ramparts = new ArrayList<>();
        for (StructurePiece piece : pieces) {
            if (piece instanceof PoolStructurePiece) {
                PoolStructurePiece poolPiece = (PoolStructurePiece) piece;
                String elementName = poolPiece.getPoolElement().toString();
                if (elementName.contains("bastion/hoglin_stable/ramparts/ramparts_1")) {
                    ramparts.add(poolPiece);
                }
            }
        }
        return ramparts;
    }

    private static boolean mcsreasymode$hasAdjacentFullHeightPair(List<PoolStructurePiece> ramparts) {
        for (int first = 0; first < ramparts.size(); first++) {
            for (int second = first + 1; second < ramparts.size(); second++) {
                if (mcsreasymode$areHorizontallyAdjacent(ramparts.get(first), ramparts.get(second))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean mcsreasymode$areHorizontallyAdjacent(StructurePiece first, StructurePiece second) {
        net.minecraft.util.math.BlockBox firstBox = first.getBoundingBox();
        net.minecraft.util.math.BlockBox secondBox = second.getBoundingBox();
        boolean yOverlaps = firstBox.minY <= secondBox.maxY && secondBox.minY <= firstBox.maxY;
        boolean adjacentX = firstBox.maxX + 1 >= secondBox.minX && secondBox.maxX + 1 >= firstBox.minX;
        boolean adjacentZ = firstBox.maxZ + 1 >= secondBox.minZ && secondBox.maxZ + 1 >= firstBox.minZ;
        boolean meaningfulZOverlap = Math.min(firstBox.maxZ, secondBox.maxZ) - Math.max(firstBox.minZ, secondBox.minZ) >= 8;
        boolean meaningfulXOverlap = Math.min(firstBox.maxX, secondBox.maxX) - Math.max(firstBox.minX, secondBox.minX) >= 8;
        return yOverlaps && ((adjacentX && meaningfulZOverlap) || (adjacentZ && meaningfulXOverlap));
    }
}
