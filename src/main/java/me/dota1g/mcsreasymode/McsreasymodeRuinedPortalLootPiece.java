package me.dota1g.mcsreasymode;

import me.dota1g.mcsreasymode.mixin.LootableContainerLootTableAccessor;
import me.dota1g.mcsreasymode.mixin.RuinedPortalStructurePieceAccessor;
import me.dota1g.mcsreasymode.mixin.SimpleStructurePieceAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.RuinedPortalStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Random;

/** Saved portal-specific chest plan, placed after its vanilla structure piece. */
public final class McsreasymodeRuinedPortalLootPiece extends StructurePiece {
    private static final Identifier VANILLA_TABLE = new Identifier("minecraft", "chests/ruined_portal");

    private final BlockPos chestPos;
    private final long planSeed;
    private final int missingFrameObsidian;
    private final boolean bucketRoute;
    private final boolean animalFoodRoute;
    private SimpleInventory plannedInventory;

    private McsreasymodeRuinedPortalLootPiece(BlockPos chestPos, long planSeed,
                                               int missingFrameObsidian, boolean bucketRoute, boolean animalFoodRoute) {
        super(McsreasymodeStructurePieces.RUINED_PORTAL_LOOT, 0);
        this.chestPos = chestPos;
        this.planSeed = planSeed;
        this.missingFrameObsidian = missingFrameObsidian;
        this.bucketRoute = bucketRoute;
        this.animalFoodRoute = animalFoodRoute;
        this.boundingBox = new BlockBox(chestPos, chestPos);
    }

    public McsreasymodeRuinedPortalLootPiece(CompoundTag tag) {
        super(McsreasymodeStructurePieces.RUINED_PORTAL_LOOT, tag);
        this.chestPos = BlockPos.fromLong(tag.getLong("ChestPos"));
        this.planSeed = tag.getLong("PlanSeed");
        this.missingFrameObsidian = tag.getInt("MissingFrameObsidian");
        this.bucketRoute = tag.getBoolean("BucketRoute");
        this.animalFoodRoute = tag.getBoolean("AnimalFoodRoute");
        this.boundingBox = new BlockBox(this.chestPos, this.chestPos);
        if (tag.contains("PlannedInventory", 9)) {
            this.plannedInventory = new SimpleInventory(27);
            ListTag stacks = tag.getList("PlannedInventory", 10);
            for (int index = 0; index < stacks.size(); index++) {
                CompoundTag stackTag = stacks.getCompound(index);
                int slot = stackTag.getInt("Slot");
                if (slot >= 0 && slot < this.plannedInventory.size()) {
                    this.plannedInventory.setStack(slot, ItemStack.fromTag(stackTag));
                }
            }
        }
    }

    public static McsreasymodeRuinedPortalLootPiece create(RuinedPortalStructurePiece portal, long seed, boolean animalsEnabled) {
        SimpleStructurePieceAccessor simple = (SimpleStructurePieceAccessor) portal;
        RuinedPortalStructurePieceAccessor ruined = (RuinedPortalStructurePieceAccessor) portal;
        Structure template = simple.mcsreasymode$getStructure();
        StructurePlacementData placement = simple.mcsreasymode$getPlacementData();
        BlockPos pos = simple.mcsreasymode$getPos();
        for (Structure.StructureBlockInfo info : template.getInfosForBlock(pos, placement, Blocks.CHEST)) {
            if (info.tag == null || !VANILLA_TABLE.toString().equals(info.tag.getString("LootTable"))) continue;
            RuinedPortalFramePlanner.Result frame = RuinedPortalFramePlanner.analyze(template, pos, placement,
                    ruined.mcsreasymode$getVerticalPlacement(), ruined.mcsreasymode$getProperties().cold);
            // Every surviving template lava source can support one cast block
            // when a water bucket supplies the water. The remaining gaps use
            // chest obsidian. No template lava means an obsidian-only route.
            boolean bucket = frame.hasLavaSource && new Random(seed).nextInt(5) == 0;
            boolean animalFood = animalsEnabled && animalFoodRoute(seed);
            return new McsreasymodeRuinedPortalLootPiece(info.pos, seed, frame.missingObsidian, bucket, animalFood);
        }
        return null;
    }

    static boolean animalFoodRoute(long seed) {
        long mixed = seed + 0x7F2D4A18B9C30E65L;
        mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
        return ((mixed ^ (mixed >>> 31)) & 1L) != 0L;
    }

    public boolean isAnimalFoodRoute() {
        return this.animalFoodRoute;
    }

    @Override
    protected synchronized void toNbt(CompoundTag tag) {
        tag.putLong("ChestPos", this.chestPos.asLong());
        tag.putLong("PlanSeed", this.planSeed);
        tag.putInt("MissingFrameObsidian", this.missingFrameObsidian);
        tag.putBoolean("BucketRoute", this.bucketRoute);
        tag.putBoolean("AnimalFoodRoute", this.animalFoodRoute);
        if (this.plannedInventory != null) {
            ListTag stacks = new ListTag();
            for (int slot = 0; slot < this.plannedInventory.size(); slot++) {
                ItemStack stack = this.plannedInventory.getStack(slot);
                if (stack.isEmpty()) continue;
                CompoundTag stackTag = stack.toTag(new CompoundTag());
                stackTag.putInt("Slot", slot);
                stacks.add(stackTag);
            }
            tag.put("PlannedInventory", stacks);
        }
    }

    @Override
    public boolean intersectsChunk(ChunkPos chunkPos, int offset) {
        return false; // Metadata must not alter the terrain around the portal.
    }

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator,
                            Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        if (!chunkBox.contains(this.chestPos)) return true;
        BlockEntity blockEntity = world.getBlockEntity(this.chestPos);
        if (!(blockEntity instanceof LootableContainerBlockEntity)) return true;
        LootableContainerBlockEntity chest = (LootableContainerBlockEntity) blockEntity;
        if (!VANILLA_TABLE.equals(((LootableContainerLootTableAccessor) chest).mcsreasymode$getLootTableId())) return true;
        SimpleInventory planned = this.getOrCreatePlan((ServerWorld) world.getWorld());
        chest.setLootTable(null, 0L);
        for (int slot = 0; slot < Math.min(chest.size(), planned.size()); slot++) {
            ItemStack stack = planned.getStack(slot);
            if (!stack.isEmpty()) chest.setStack(slot, stack.copy());
        }
        chest.markDirty();
        return true;
    }

    private synchronized SimpleInventory getOrCreatePlan(ServerWorld world) {
        if (this.plannedInventory != null) return this.plannedInventory;
        SimpleInventory inventory = new SimpleInventory(27);
        LootTable table = world.getServer().getLootManager().getTable(VANILLA_TABLE);
        LootContext context = new LootContext.Builder(world)
                .parameter(LootContextParameters.POSITION, this.chestPos)
                .random(this.planSeed == 0L ? 1L : this.planSeed)
                .build(LootContextTypes.CHEST);
        table.supplyInventory(inventory, context);
        RuinedPortalResourceRules.apply(inventory, this.missingFrameObsidian, this.bucketRoute, this.animalFoodRoute);
        this.plannedInventory = inventory;
        return inventory;
    }
}
