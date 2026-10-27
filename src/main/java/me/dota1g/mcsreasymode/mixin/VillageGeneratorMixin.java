package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeVillageLavaPoolPiece;
import me.dota1g.mcsreasymode.McsreasymodeVillageLootPiece;
import me.dota1g.mcsreasymode.VillageLavaPoolPlanner;
import me.dota1g.mcsreasymode.VillageSmithSiteSelector;
import me.dota1g.mcsreasymode.worldgen.VillageVariant;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.VillageGenerator;
import net.minecraft.structure.pool.LegacySinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(VillageGenerator.class)
public abstract class VillageGeneratorMixin {
    @Inject(method = "addPieces", at = @At("TAIL"))
    private static void mcsreasymode$standardizeVillage(ChunkGenerator chunkGenerator, StructureManager structureManager, BlockPos pos, List<StructurePiece> pieces, ChunkRandom random, StructurePoolFeatureConfig config, CallbackInfo ci) {
        if (!Mcsreasymode.isVillageStandardizationEnabled() || pieces.isEmpty()) {
            return;
        }

        BlockBox villageBox = mcsreasymode$getVillageBox(pieces);
        if (villageBox == null) {
            return;
        }

        VillageVariant variant = mcsreasymode$getVariant(config, pieces);
        if (!mcsreasymode$hasSmith(pieces)) {
            VillageGenerator.Piece smith = mcsreasymode$createSmithPiece(chunkGenerator, structureManager, villageBox, pieces, variant);
            if (smith != null) {
                pieces.add(smith);
                Mcsreasymode.debug("Village standardized: added vanilla " + variant.logName + " smith template because no smith generated.");
            }
        }

        List<BlockBox> occupied = new ArrayList<>();
        for (StructurePiece piece : pieces) {
            occupied.add(piece.getBoundingBox());
        }
        List<VillageLavaPoolPlanner.Site> sites = VillageLavaPoolPlanner.select(villageBox, occupied, random.nextLong(),
                new VillageLavaPoolPlanner.SurfaceProbe() {
                    @Override
                    public int surfaceY(int x, int z) {
                        return chunkGenerator.getHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG);
                    }

                    @Override
                    public int oceanFloorY(int x, int z) {
                        return chunkGenerator.getHeight(x, z, Heightmap.Type.OCEAN_FLOOR_WG);
                    }

                    @Override
                    public boolean vanillaSite(int originX, int originZ, int bottomY, long lakeSeed) {
                        return McsreasymodeVillageLavaPoolPiece.isVanillaSite(chunkGenerator, originX, originZ, bottomY, lakeSeed);
                    }
                });
        for (VillageLavaPoolPlanner.Site site : sites) {
            pieces.add(new McsreasymodeVillageLavaPoolPiece(site.x, site.z, site.bottomY, site.seed, site.needsSupport));
        }
        McsreasymodeVillageLootPiece loot = McsreasymodeVillageLootPiece.create(structureManager, pieces,
                variant, random.nextLong(), Mcsreasymode.isRankedIronGolemDropsEnabled());
        if (loot == null || !loot.hasSmithChest()) {
            // Some vanilla smith buildings have no chest. Supply a chest-bearing
            // smith so the village-wide adjustment has its intended destination.
            VillageGenerator.Piece supplySmith = mcsreasymode$createSmithPiece(chunkGenerator, structureManager, villageBox, pieces, variant);
            if (supplySmith != null) {
                pieces.add(supplySmith);
                loot = McsreasymodeVillageLootPiece.create(structureManager, pieces, variant,
                        random.nextLong(), Mcsreasymode.isRankedIronGolemDropsEnabled());
            }
        }
        if (loot != null) pieces.add(loot);
        Mcsreasymode.debug("Village standardized: placed " + sites.size() + " artificial lava pools near village.");
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

    private static VillageGenerator.Piece mcsreasymode$createSmithPiece(ChunkGenerator chunkGenerator, StructureManager structureManager, BlockBox villageBox, List<StructurePiece> pieces, VillageVariant variant) {
        StructurePoolElement element = new LegacySinglePoolElement(variant.template, java.util.Collections.emptyList()).setProjection(StructurePool.Projection.RIGID);
        List<BlockBox> occupied = new ArrayList<>();
        for (StructurePiece piece : pieces) occupied.add(piece.getBoundingBox());
        VillageSmithSiteSelector.SurfaceProbe terrain = new VillageSmithSiteSelector.SurfaceProbe() {
            @Override public int surfaceY(int x, int z) { return chunkGenerator.getHeightOnGround(x, z, Heightmap.Type.WORLD_SURFACE_WG); }
            @Override public int oceanFloorY(int x, int z) { return chunkGenerator.getHeightOnGround(x, z, Heightmap.Type.OCEAN_FLOOR_WG); }
        };
        VillageGenerator.Piece best = null;
        int bestScore = Integer.MAX_VALUE;
        for (BlockRotation rotation : BlockRotation.values()) {
            BlockBox unplaced = element.getBoundingBox(structureManager, BlockPos.ORIGIN, rotation);
            VillageSmithSiteSelector.Site site = VillageSmithSiteSelector.select(villageBox, occupied,
                    unplaced.maxX - unplaced.minX + 1, unplaced.maxZ - unplaced.minZ + 1, terrain);
            if (site == null || site.score >= bestScore) continue;
            // Vanilla's start piece places minY + groundLevelDelta at the terrain top.
            BlockPos origin = new BlockPos(site.x - unplaced.minX,
                    site.groundY - element.getGroundLevelDelta() - unplaced.minY,
                    site.z - unplaced.minZ);
            BlockBox box = element.getBoundingBox(structureManager, origin, rotation);
            best = new VillageGenerator.Piece(structureManager, element, origin, element.getGroundLevelDelta(), rotation, box);
            bestScore = site.score;
        }
        return best;
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
