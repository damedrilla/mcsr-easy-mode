package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeConfig;
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
    private static final Identifier MCSREASYMODE_BRIDGE_ORIGIN = new Identifier("bastion/bridge/start");
    private static final int MCSREASYMODE_TARGET_ADJACENT_FULL_STABLE_RAMPARTS = 2;
    private static final int MCSREASYMODE_TARGET_BRIDGE_FULL_RAMPARTS = 1;
    private static final int MCSREASYMODE_RAMPART_ATTEMPTS = 64;

    @Inject(method = "addPieces", at = @At("HEAD"), cancellable = true)
    private static void mcsreasymode$rerollBastionRamparts(ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos pos, List<StructurePiece> pieces, ChunkRandom random, BastionRemnantFeatureConfig config, CallbackInfo ci) {
        BastionRemnantGenerator.init();
        StructurePoolFeatureConfig selectedConfig = config.getRandom(random);

        boolean isHoglinStable = MCSREASYMODE_HOGLIN_STABLE_ORIGIN.equals(selectedConfig.startPool);
        boolean isBridge = MCSREASYMODE_BRIDGE_ORIGIN.equals(selectedConfig.startPool);

        if (isHoglinStable && Mcsreasymode.isRankedHoglinStableRampartsEnabled()) {
            mcsreasymode$rerollHoglinStable(chunkGenerator, structureManager, pos, pieces, random, selectedConfig, ci);
            return;
        }

        if (isBridge && Mcsreasymode.isRankedBridgeRampartsEnabled()) {
            mcsreasymode$rerollBridge(chunkGenerator, structureManager, pos, pieces, random, selectedConfig, ci);
            return;
        }

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
    }

    private static void mcsreasymode$rerollHoglinStable(ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos pos, List<StructurePiece> pieces, ChunkRandom random, StructurePoolFeatureConfig selectedConfig, CallbackInfo ci) {
        McsreasymodeConfig.HoglinStableRampartMode mode = Mcsreasymode.CONFIG.hoglinStableRampartMode;

        List<StructurePiece> bestPieces = null;
        int bestScore = -1;
        int bestRampartCount = 0;
        boolean bestHasAdjacentPair = false;
        int bestSideWallCount = 0;

        for (int attempt = 1; attempt <= MCSREASYMODE_RAMPART_ATTEMPTS; attempt++) {
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

            List<PoolStructurePiece> fullRamparts = mcsreasymode$getRampartsByName(attemptPieces, "bastion/hoglin_stable/ramparts/ramparts_1");
            int rampartCount = fullRamparts.size();
            boolean hasAdjacentPair = mcsreasymode$hasAdjacentFullHeightPair(fullRamparts);
            int sideWallCount = mcsreasymode$getRampartsByName(attemptPieces, "bastion/hoglin_stable/walls/side_wall_1").size();

            boolean winAdjacentPair = rampartCount >= MCSREASYMODE_TARGET_ADJACENT_FULL_STABLE_RAMPARTS && hasAdjacentPair;
            boolean winRampartPlusGoodGap = rampartCount >= 1 && sideWallCount >= 1;

            boolean accept;
            int score;
            switch (mode) {
                case DOUBLE_TRIPLE:
                    accept = winAdjacentPair;
                    score = (hasAdjacentPair ? 100 : 0) + rampartCount;
                    break;
                case TRIPLE_TO_GAP:
                    accept = winRampartPlusGoodGap;
                    score = (sideWallCount > 0 ? 100 : 0) + rampartCount;
                    break;
                default: // EITHER
                    accept = winAdjacentPair || winRampartPlusGoodGap;
                    score = (hasAdjacentPair ? 100 : 0) + rampartCount + (sideWallCount > 0 ? 50 : 0);
                    break;
            }

            if (score > bestScore) {
                bestScore = score;
                bestRampartCount = rampartCount;
                bestHasAdjacentPair = hasAdjacentPair;
                bestSideWallCount = sideWallCount;
                bestPieces = attemptPieces;
            }

            if (accept) {
                pieces.addAll(attemptPieces);
                Mcsreasymode.debug("Hoglin stable standardized (" + mode + "): " + (winAdjacentPair ? "adjacent full-height ramparts" : "full-height rampart + good gap")
                        + " after " + attempt + " attempt(s); ramparts: " + rampartCount + ", adjacent pair: " + hasAdjacentPair + ", side walls: " + sideWallCount + ".");
                ci.cancel();
                return;
            }
        }

        if (bestPieces != null) {
            pieces.addAll(bestPieces);
            Mcsreasymode.debug("Hoglin stable standardization fallback (" + mode + "): best assembly had " + bestRampartCount + " rampart(s), adjacent pair: " + bestHasAdjacentPair + ", side walls: " + bestSideWallCount + ", after " + MCSREASYMODE_RAMPART_ATTEMPTS + " attempts.");
        }
        ci.cancel();
    }
    private static void mcsreasymode$rerollBridge(ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos pos, List<StructurePiece> pieces, ChunkRandom random, StructurePoolFeatureConfig selectedConfig, CallbackInfo ci) {
        List<StructurePiece> bestPieces = null;
        int bestRampartCount = -1;
        for (int attempt = 1; attempt <= MCSREASYMODE_RAMPART_ATTEMPTS; attempt++) {
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

            int rampartCount = mcsreasymode$getRampartsByName(attemptPieces, "bastion/bridge/ramparts/rampart_1").size();
            if (rampartCount > bestRampartCount) {
                bestRampartCount = rampartCount;
                bestPieces = attemptPieces;
            }
            if (rampartCount >= MCSREASYMODE_TARGET_BRIDGE_FULL_RAMPARTS) {
                pieces.addAll(attemptPieces);
                Mcsreasymode.debug("Bridge standardized: selected assembly with full-height rampart after " + attempt + " attempt(s); full-height ramparts present: " + rampartCount + ".");
                ci.cancel();
                return;
            }
        }

        if (bestPieces != null) {
            pieces.addAll(bestPieces);
            Mcsreasymode.debug("Bridge standardization fallback: best assembly had " + bestRampartCount + " full-height rampart(s) after " + MCSREASYMODE_RAMPART_ATTEMPTS + " attempts.");
        }
        ci.cancel();
    }

    private static List<PoolStructurePiece> mcsreasymode$getRampartsByName(List<StructurePiece> pieces, String nameFragment) {
        List<PoolStructurePiece> ramparts = new ArrayList<>();
        for (StructurePiece piece : pieces) {
            if (piece instanceof PoolStructurePiece) {
                PoolStructurePiece poolPiece = (PoolStructurePiece) piece;
                String elementName = poolPiece.getPoolElement().toString();
                if (elementName.contains(nameFragment)) {
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