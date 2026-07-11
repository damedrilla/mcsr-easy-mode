package me.dota1g.mcsreasymode;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Random;

public class McsreasymodeVillageBlacksmithPiece extends StructurePiece {
    public static final int FOOTPRINT_X = 22;
    public static final int FOOTPRINT_Z = 16;
    private static final String[] LAVA_POOL_SHAPE = new String[]{
            "  SSSSS  ",
            " SLLLLS ",
            "SLLLLLLS",
            "SLLLLLLS",
            " SLLLLS ",
            "  SSSS  "
    };

    private final int originX;
    private final int originZ;
    private final Variant variant;

    public McsreasymodeVillageBlacksmithPiece(int originX, int originZ, Variant variant) {
        super(McsreasymodeStructurePieces.VILLAGE_BLACKSMITH, 0);
        this.originX = originX;
        this.originZ = originZ;
        this.variant = variant;
        this.boundingBox = new BlockBox(originX, 0, originZ, originX + FOOTPRINT_X - 1, 255, originZ + FOOTPRINT_Z - 1);
    }

    public McsreasymodeVillageBlacksmithPiece(CompoundTag tag) {
        super(McsreasymodeStructurePieces.VILLAGE_BLACKSMITH, tag);
        this.originX = tag.getInt("OriginX");
        this.originZ = tag.getInt("OriginZ");
        this.variant = Variant.fromName(tag.getString("Variant"));
        this.boundingBox = new BlockBox(this.originX, 0, this.originZ, this.originX + FOOTPRINT_X - 1, 255, this.originZ + FOOTPRINT_Z - 1);
    }

    @Override
    protected void toNbt(CompoundTag tag) {
        tag.putInt("OriginX", this.originX);
        tag.putInt("OriginZ", this.originZ);
        tag.putString("Variant", this.variant.name());
    }

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        int baseY = this.getAverageSurfaceY(world, 1, 1, 8, 6);
        this.clearFootprint(world, chunkBox, baseY);
        if (this.variant == Variant.DESERT) {
            this.placeDesertSmith(world, chunkBox, random, baseY);
        } else {
            this.placeGabledSmith(world, chunkBox, random, baseY);
        }
        this.placeLavaPool(world, chunkBox);
        Mcsreasymode.debugRateLimited("village_standardization_generate", "Village standardization generated artificial " + this.variant.logName + " blacksmith and nearby lava pool.", 1000L);
        return true;
    }

    private void clearFootprint(ServerWorldAccess world, BlockBox chunkBox, int baseY) {
        for (int x = 0; x <= 9; x++) {
            for (int z = 0; z <= 7; z++) {
                for (int y = -4; y <= -1; y++) {
                    this.set(world, chunkBox, x, baseY + y, z, this.variant.foundation);
                }
                for (int y = 0; y <= 9; y++) {
                    this.set(world, chunkBox, x, baseY + y, z, Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    private void placeGabledSmith(ServerWorldAccess world, BlockBox chunkBox, Random random, int baseY) {
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 6; z++) {
                this.set(world, chunkBox, x, baseY - 1, z, this.variant.foundation);
                this.set(world, chunkBox, x, baseY, z, this.variant.floor);
            }
        }

        for (int x = 0; x <= 8; x++) {
            for (int y = 1; y <= 3; y++) {
                this.set(world, chunkBox, x, baseY + y, 0, this.variant.stoneWall);
                this.set(world, chunkBox, x, baseY + y, 6, this.variant.woodWall);
            }
        }
        for (int z = 0; z <= 6; z++) {
            for (int y = 1; y <= 3; y++) {
                this.set(world, chunkBox, 0, baseY + y, z, this.variant.stoneWall);
                this.set(world, chunkBox, 8, baseY + y, z, this.variant.woodWall);
            }
        }

        this.set(world, chunkBox, 4, baseY + 1, 0, Blocks.AIR.getDefaultState());
        this.set(world, chunkBox, 4, baseY + 2, 0, Blocks.AIR.getDefaultState());
        this.set(world, chunkBox, 2, baseY + 2, 0, Blocks.GLASS_PANE.getDefaultState());
        this.set(world, chunkBox, 6, baseY + 2, 6, Blocks.GLASS_PANE.getDefaultState());

        for (int y = 1; y <= 3; y++) {
            this.set(world, chunkBox, 1, baseY + y, 1, this.variant.log);
            this.set(world, chunkBox, 7, baseY + y, 1, this.variant.log);
            this.set(world, chunkBox, 1, baseY + y, 5, this.variant.log);
            this.set(world, chunkBox, 7, baseY + y, 5, this.variant.log);
        }

        for (int x = 0; x <= 9; x++) {
            for (int z = 0; z <= 7; z++) {
                int roofY = baseY + 4 + Math.min(Math.min(z, 7 - z), 3);
                this.set(world, chunkBox, x, roofY, z, this.variant.roof);
                if (this.variant == Variant.SNOWY && (z == 2 || z == 5)) {
                    this.set(world, chunkBox, x, roofY + 1, z, Blocks.SNOW.getDefaultState());
                }
            }
            this.set(world, chunkBox, x, baseY + 7, 3, this.variant.roof);
            this.set(world, chunkBox, x, baseY + 7, 4, this.variant.roof);
        }

        this.placeForgeAndChest(world, chunkBox, random, baseY, Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH));
    }

    private void placeDesertSmith(ServerWorldAccess world, BlockBox chunkBox, Random random, int baseY) {
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 6; z++) {
                this.set(world, chunkBox, x, baseY - 1, z, this.variant.foundation);
                this.set(world, chunkBox, x, baseY, z, this.variant.floor);
            }
        }

        for (int x = 0; x <= 8; x++) {
            for (int y = 1; y <= 4; y++) {
                this.set(world, chunkBox, x, baseY + y, 0, this.variant.stoneWall);
                this.set(world, chunkBox, x, baseY + y, 6, this.variant.stoneWall);
            }
        }
        for (int z = 0; z <= 6; z++) {
            for (int y = 1; y <= 4; y++) {
                this.set(world, chunkBox, 0, baseY + y, z, this.variant.stoneWall);
                this.set(world, chunkBox, 8, baseY + y, z, this.variant.stoneWall);
            }
        }

        this.set(world, chunkBox, 4, baseY + 1, 0, Blocks.AIR.getDefaultState());
        this.set(world, chunkBox, 4, baseY + 2, 0, Blocks.AIR.getDefaultState());
        this.set(world, chunkBox, 2, baseY + 2, 0, Blocks.IRON_BARS.getDefaultState());
        this.set(world, chunkBox, 3, baseY + 2, 0, Blocks.IRON_BARS.getDefaultState());
        this.set(world, chunkBox, 6, baseY + 2, 6, Blocks.GLASS_PANE.getDefaultState());

        for (int y = 1; y <= 5; y++) {
            this.set(world, chunkBox, 1, baseY + y, 1, this.variant.trim);
            this.set(world, chunkBox, 7, baseY + y, 1, this.variant.trim);
            this.set(world, chunkBox, 1, baseY + y, 5, this.variant.trim);
            this.set(world, chunkBox, 7, baseY + y, 5, this.variant.trim);
        }
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 6; z++) {
                this.set(world, chunkBox, x, baseY + 5, z, this.variant.roof);
            }
        }
        this.set(world, chunkBox, 2, baseY + 6, 2, this.variant.roof);
        this.set(world, chunkBox, 6, baseY + 6, 2, this.variant.roof);
        this.set(world, chunkBox, 2, baseY + 6, 5, this.variant.roof);
        this.set(world, chunkBox, 6, baseY + 6, 5, this.variant.roof);

        this.placeForgeAndChest(world, chunkBox, random, baseY, Blocks.SANDSTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH));
    }

    private void placeForgeAndChest(ServerWorldAccess world, BlockBox chunkBox, Random random, int baseY, BlockState step) {
        this.set(world, chunkBox, 2, baseY + 1, 2, Blocks.LAVA.getDefaultState());
        this.set(world, chunkBox, 2, baseY + 1, 3, Blocks.LAVA.getDefaultState());
        this.set(world, chunkBox, 3, baseY + 1, 2, Blocks.LAVA.getDefaultState());
        this.set(world, chunkBox, 3, baseY + 1, 3, Blocks.LAVA.getDefaultState());
        this.set(world, chunkBox, 2, baseY + 2, 2, Blocks.IRON_BARS.getDefaultState());
        this.set(world, chunkBox, 2, baseY + 2, 3, Blocks.IRON_BARS.getDefaultState());
        this.set(world, chunkBox, 3, baseY + 2, 2, Blocks.IRON_BARS.getDefaultState());
        this.set(world, chunkBox, 3, baseY + 2, 3, Blocks.IRON_BARS.getDefaultState());
        this.set(world, chunkBox, 6, baseY + 1, 2, Blocks.FURNACE.getDefaultState());
        this.set(world, chunkBox, 6, baseY + 1, 4, Blocks.CRAFTING_TABLE.getDefaultState());

        BlockPos chestPos = this.pos(6, baseY + 1, 5);
        if (chunkBox.contains(chestPos)) {
            world.setBlockState(chestPos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.NORTH), 2);
            LootableContainerBlockEntity.setLootTable(world, random, chestPos, LootTables.VILLAGE_WEAPONSMITH_CHEST);
        }
        this.set(world, chunkBox, 4, baseY, 0, step);
    }

    private void placeLavaPool(ServerWorldAccess world, BlockBox chunkBox) {
        int poolX = 12;
        int poolZ = 4;
        int baseY = this.getAverageSurfaceY(world, poolX, poolZ, poolX + 8, poolZ + LAVA_POOL_SHAPE.length - 1) - 1;
        for (int z = 0; z < LAVA_POOL_SHAPE.length; z++) {
            String row = LAVA_POOL_SHAPE[z];
            for (int x = 0; x < row.length(); x++) {
                char marker = row.charAt(x);
                if (marker == ' ') {
                    continue;
                }

                BlockState edge = ((x + z) % 3 == 0 ? Blocks.DIRT : Blocks.STONE).getDefaultState();
                this.set(world, chunkBox, poolX + x, baseY - 1, poolZ + z, Blocks.STONE.getDefaultState());
                this.set(world, chunkBox, poolX + x, baseY, poolZ + z, marker == 'L' ? Blocks.LAVA.getDefaultState() : edge);
                this.set(world, chunkBox, poolX + x, baseY + 1, poolZ + z, Blocks.AIR.getDefaultState());
                this.set(world, chunkBox, poolX + x, baseY + 2, poolZ + z, Blocks.AIR.getDefaultState());
            }
        }
    }

    private int getAverageSurfaceY(ServerWorldAccess world, int minX, int minZ, int maxX, int maxZ) {
        int total = 0;
        int samples = 0;
        for (int x = minX; x <= maxX; x += Math.max(1, (maxX - minX) / 2)) {
            for (int z = minZ; z <= maxZ; z += Math.max(1, (maxZ - minZ) / 2)) {
                total += world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, this.originX + x, this.originZ + z);
                samples++;
            }
        }
        return samples == 0 ? 64 : total / samples;
    }

    private void set(ServerWorldAccess world, BlockBox chunkBox, int localX, int y, int localZ, BlockState state) {
        BlockPos pos = this.pos(localX, y, localZ);
        if (chunkBox.contains(pos)) {
            world.setBlockState(pos, state, 2);
        }
    }

    private BlockPos pos(int localX, int y, int localZ) {
        return new BlockPos(this.originX + localX, y, this.originZ + localZ);
    }

    public enum Variant {
        DESERT("desert", Blocks.SANDSTONE.getDefaultState(), Blocks.SMOOTH_SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), Blocks.CUT_SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), Blocks.SMOOTH_SANDSTONE.getDefaultState()),
        PLAINS("plains", Blocks.COBBLESTONE.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), Blocks.STRIPPED_OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState()),
        SAVANNA("savanna", Blocks.COBBLESTONE.getDefaultState(), Blocks.ACACIA_PLANKS.getDefaultState(), Blocks.STONE_BRICKS.getDefaultState(), Blocks.STRIPPED_ACACIA_WOOD.getDefaultState(), Blocks.ACACIA_LOG.getDefaultState(), Blocks.ACACIA_LOG.getDefaultState(), Blocks.ACACIA_PLANKS.getDefaultState()),
        SNOWY("snowy", Blocks.COBBLESTONE.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_LOG.getDefaultState(), Blocks.SPRUCE_LOG.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState()),
        TAIGA("taiga", Blocks.COBBLESTONE.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_LOG.getDefaultState(), Blocks.SPRUCE_LOG.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState());

        public final String logName;
        private final BlockState foundation;
        private final BlockState floor;
        private final BlockState stoneWall;
        private final BlockState woodWall;
        private final BlockState trim;
        private final BlockState log;
        private final BlockState roof;

        Variant(String logName, BlockState foundation, BlockState floor, BlockState stoneWall, BlockState woodWall, BlockState trim, BlockState log, BlockState roof) {
            this.logName = logName;
            this.foundation = foundation;
            this.floor = floor;
            this.stoneWall = stoneWall;
            this.woodWall = woodWall;
            this.trim = trim;
            this.log = log;
            this.roof = roof;
        }

        public static Variant fromPath(String path) {
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

        private static Variant fromName(String name) {
            for (Variant variant : values()) {
                if (variant.name().equals(name)) {
                    return variant;
                }
            }
            return PLAINS;
        }
    }
}
