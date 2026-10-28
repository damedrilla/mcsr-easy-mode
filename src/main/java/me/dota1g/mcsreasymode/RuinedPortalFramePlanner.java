package me.dota1g.mcsreasymode;

import net.minecraft.block.Blocks;
import net.minecraft.structure.RuinedPortalStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Predicts the processed template blocks without requiring neighboring chunks to generate first. */
public final class RuinedPortalFramePlanner {
    public static final class Result {
        public final int missingObsidian;
        public final boolean hasLavaSource;

        private Result(int missingObsidian, boolean hasLavaSource) {
            this.missingObsidian = missingObsidian;
            this.hasLavaSource = hasLavaSource;
        }
    }

    private RuinedPortalFramePlanner() {
    }

    public static Result analyze(Structure template, BlockPos pos, StructurePlacementData placement,
                                 RuinedPortalStructurePiece.VerticalPlacement verticalPlacement, boolean cold) {
        Set<BlockPos> intact = new HashSet<>();
        Set<BlockPos> crying = new HashSet<>();
        for (Structure.StructureBlockInfo info : template.getInfosForBlock(pos, placement, Blocks.OBSIDIAN)) {
            // BlockAgeStructureProcessor makes this exact decision with a fresh
            // position-hash Random for each block.
            (new Random(MathHelper.hashCode(info.pos)).nextFloat() < 0.15F ? crying : intact).add(info.pos);
        }
        for (Structure.StructureBlockInfo info : template.getInfosForBlock(pos, placement, Blocks.CRYING_OBSIDIAN)) {
            crying.add(info.pos);
        }

        boolean lava = false;
        if (!cold && verticalPlacement != RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR) {
            for (Structure.StructureBlockInfo info : template.getInfosForBlock(pos, placement, Blocks.LAVA)) {
                // RuleStructureProcessor replaces 20% of warm template lava
                // with magma using the same position-hash seed.
                if (new Random(MathHelper.hashCode(info.pos)).nextFloat() >= 0.2F) {
                    lava = true;
                    break;
                }
            }
        }
        return new Result(missingForMinimumFrame(intact, crying), lava);
    }

    static int missingForMinimumFrame(Collection<BlockPos> intactBlocks, Collection<BlockPos> cryingBlocks) {
        if (intactBlocks.isEmpty()) return 10;
        Set<Long> intact = new HashSet<>();
        Set<Long> crying = new HashSet<>();
        for (BlockPos pos : intactBlocks) intact.add(pos.asLong());
        for (BlockPos pos : cryingBlocks) crying.add(pos.asLong());
        int best = 10;
        // Every candidate is anchored by an existing frame block. Try both
        // vertical portal planes and all ten legal non-corner frame slots.
        for (BlockPos anchor : intactBlocks) {
            for (int axis = 0; axis < 2; axis++) {
                for (int width = 0; width < 4; width++) {
                    for (int height = 0; height < 5; height++) {
                        if (!isRequiredFrame(width, height)) continue;
                        int base = (axis == 0 ? anchor.getX() : anchor.getZ()) - width;
                        int baseY = anchor.getY() - height;
                        int fixed = axis == 0 ? anchor.getZ() : anchor.getX();
                        int missing = candidateMissing(intact, crying, axis, base, baseY, fixed);
                        best = Math.min(best, missing);
                    }
                }
            }
        }
        return best;
    }

    private static int candidateMissing(Set<Long> intact, Set<Long> crying,
                                        int axis, int base, int baseY, int fixed) {
        int missing = 0;
        for (int width = 0; width < 4; width++) {
            for (int height = 0; height < 5; height++) {
                BlockPos pos = axis == 0 ? new BlockPos(base + width, baseY + height, fixed)
                        : new BlockPos(fixed, baseY + height, base + width);
                long key = pos.asLong();
                if (isRequiredFrame(width, height)) {
                    if (crying.contains(key)) return 10;
                    if (!intact.contains(key)) missing++;
                } else if (width > 0 && width < 3 && height > 0 && height < 4) {
                    // Crying obsidian and obsidian inside the opening require
                    // diamond mining before this rectangle can be lit.
                    if (crying.contains(key) || intact.contains(key)) return 10;
                }
            }
        }
        return missing;
    }

    private static boolean isRequiredFrame(int width, int height) {
        return ((width == 0 || width == 3) && height >= 1 && height <= 3)
                || ((height == 0 || height == 4) && (width == 1 || width == 2));
    }
}
