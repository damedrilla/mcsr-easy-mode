package me.dota1g.mcsreasymode;

import me.dota1g.mcsreasymode.mixin.LootableContainerLootTableAccessor;
import me.dota1g.mcsreasymode.mixin.SinglePoolElementInvoker;
import me.dota1g.mcsreasymode.worldgen.VillageVariant;
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
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/** Saved village manifest and deterministic, chunk-order-independent chest filler. */
public final class McsreasymodeVillageLootPiece extends StructurePiece {
    private static final class ChestSpec {
        private final BlockPos pos;
        private final Identifier table;

        private ChestSpec(BlockPos pos, Identifier table) {
            this.pos = pos;
            this.table = table;
        }
    }

    private final VillageVariant variant;
    private final long planSeed;
    private final boolean hasHay;
    private final int guaranteedGolemIron;
    private final List<ChestSpec> chests;
    private List<SimpleInventory> plannedInventories;

    private McsreasymodeVillageLootPiece(VillageVariant variant, long planSeed, boolean hasHay,
                                        int guaranteedGolemIron, List<ChestSpec> chests) {
        super(McsreasymodeStructurePieces.VILLAGE_LOOT, 0);
        this.variant = variant;
        this.planSeed = planSeed;
        this.hasHay = hasHay;
        this.guaranteedGolemIron = guaranteedGolemIron;
        this.chests = chests;
        this.boundingBox = bounds(chests);
    }

    public McsreasymodeVillageLootPiece(CompoundTag tag) {
        super(McsreasymodeStructurePieces.VILLAGE_LOOT, tag);
        VillageVariant loadedVariant;
        try {
            loadedVariant = VillageVariant.valueOf(tag.getString("VillageVariant"));
        } catch (IllegalArgumentException exception) {
            loadedVariant = VillageVariant.PLAINS;
        }
        this.variant = loadedVariant;
        this.planSeed = tag.getLong("PlanSeed");
        this.hasHay = tag.getBoolean("HasHay");
        this.guaranteedGolemIron = tag.getInt("GolemIron");
        this.chests = new ArrayList<>();
        ListTag list = tag.getList("Chests", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag chest = list.getCompound(i);
            this.chests.add(new ChestSpec(BlockPos.fromLong(chest.getLong("Pos")), new Identifier(chest.getString("Table"))));
        }
        this.chests.sort(Comparator.comparingLong(chest -> chest.pos.asLong()));
        this.boundingBox = bounds(this.chests);
        ListTag savedPlan = tag.getList("PlannedInventories", 9);
        if (savedPlan.size() == this.chests.size()) {
            this.plannedInventories = new ArrayList<>();
            for (int index = 0; index < savedPlan.size(); index++) {
                SimpleInventory inventory = new SimpleInventory(27);
                ListTag stacks = savedPlan.getList(index);
                for (int slotIndex = 0; slotIndex < stacks.size(); slotIndex++) {
                    CompoundTag stackTag = stacks.getCompound(slotIndex);
                    int slot = stackTag.getInt("Slot");
                    if (slot >= 0 && slot < inventory.size()) {
                        inventory.setStack(slot, ItemStack.fromTag(stackTag));
                    }
                }
                this.plannedInventories.add(inventory);
            }
        }
    }

    public static McsreasymodeVillageLootPiece create(StructureManager manager, List<StructurePiece> pieces,
                                                       VillageVariant variant, long seed, boolean rankedGolemDrops) {
        Map<Long, ChestSpec> found = new TreeMap<>();
        boolean hay = false;
        for (StructurePiece piece : pieces) {
            if (!(piece instanceof PoolStructurePiece)) continue;
            PoolStructurePiece poolPiece = (PoolStructurePiece) piece;
            if (!(poolPiece.getPoolElement() instanceof SinglePoolElement)) continue;
            Structure template = ((SinglePoolElementInvoker) poolPiece.getPoolElement()).mcsreasymode$getStructure(manager);
            StructurePlacementData placement = new StructurePlacementData().setRotation(poolPiece.getRotation());
            for (Structure.StructureBlockInfo info : template.getInfosForBlock(poolPiece.getPos(), placement, Blocks.CHEST)) {
                collectChest(found, info);
            }
            for (Structure.StructureBlockInfo info : template.getInfosForBlock(poolPiece.getPos(), placement, Blocks.BARREL)) {
                collectChest(found, info);
            }
            if (!template.getInfosForBlock(poolPiece.getPos(), placement, Blocks.HAY_BLOCK).isEmpty()) hay = true;
        }
        if (found.isEmpty()) return null;
        return new McsreasymodeVillageLootPiece(variant, seed, hay, rankedGolemDrops ? 4 : 3,
                new ArrayList<>(found.values()));
    }

    public boolean hasSmithChest() {
        for (ChestSpec chest : this.chests) {
            if (isSmithTable(chest.table)) return true;
        }
        return false;
    }

    private static boolean isSmithTable(Identifier table) {
        String path = table.getPath();
        return path.endsWith("village_weaponsmith") || path.endsWith("village_toolsmith")
                || path.endsWith("village_armorer");
    }

    private static void collectChest(Map<Long, ChestSpec> found, Structure.StructureBlockInfo info) {
        if (info.tag == null || !info.tag.contains("LootTable")) return;
        Identifier table = new Identifier(info.tag.getString("LootTable"));
        if (!table.getPath().startsWith("chests/village/")) return;
        found.put(info.pos.asLong(), new ChestSpec(info.pos, table));
    }

    private static BlockBox bounds(List<ChestSpec> chests) {
        BlockPos first = chests.get(0).pos;
        BlockBox box = new BlockBox(first, first);
        for (ChestSpec chest : chests) box.encompass(new BlockBox(chest.pos, chest.pos));
        return box;
    }

    @Override
    protected synchronized void toNbt(CompoundTag tag) {
        tag.putString("VillageVariant", this.variant.name());
        tag.putLong("PlanSeed", this.planSeed);
        tag.putBoolean("HasHay", this.hasHay);
        tag.putInt("GolemIron", this.guaranteedGolemIron);
        ListTag list = new ListTag();
        for (ChestSpec chest : this.chests) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", chest.pos.asLong());
            entry.putString("Table", chest.table.toString());
            list.add(entry);
        }
        tag.put("Chests", list);
        if (this.plannedInventories != null) {
            ListTag inventories = new ListTag();
            for (SimpleInventory inventory : this.plannedInventories) {
                ListTag stacks = new ListTag();
                for (int slot = 0; slot < inventory.size(); slot++) {
                    ItemStack stack = inventory.getStack(slot);
                    if (stack.isEmpty()) continue;
                    CompoundTag stackTag = stack.toTag(new CompoundTag());
                    stackTag.putInt("Slot", slot);
                    stacks.add(stackTag);
                }
                inventories.add(stacks);
            }
            tag.put("PlannedInventories", inventories);
        }
    }

    @Override
    public boolean intersectsChunk(ChunkPos chunkPos, int offset) {
        return false; // Metadata must not create a structure terrain beard.
    }

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator,
                            Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        ServerWorld serverWorld = (ServerWorld) world.getWorld();
        boolean pending = false;
        for (ChestSpec chest : this.chests) {
            if (!chunkBox.contains(chest.pos)) continue;
            BlockEntity blockEntity = world.getBlockEntity(chest.pos);
            if (blockEntity instanceof LootableContainerBlockEntity
                    && ((LootableContainerLootTableAccessor) blockEntity).mcsreasymode$getLootTableId() != null) {
                pending = true;
                break;
            }
        }
        if (!pending) return true;

        List<SimpleInventory> inventories = this.getOrCreatePlan(serverWorld);

        for (int index = 0; index < this.chests.size(); index++) {
            ChestSpec chest = this.chests.get(index);
            if (!chunkBox.contains(chest.pos)) continue;
            BlockEntity blockEntity = world.getBlockEntity(chest.pos);
            if (!(blockEntity instanceof LootableContainerBlockEntity)) continue;
            LootableContainerBlockEntity container = (LootableContainerBlockEntity) blockEntity;
            if (((LootableContainerLootTableAccessor) container).mcsreasymode$getLootTableId() == null) continue;
            container.setLootTable(null, 0L);
            SimpleInventory planned = inventories.get(index);
            for (int slot = 0; slot < Math.min(container.size(), planned.size()); slot++) {
                ItemStack stack = planned.getStack(slot);
                if (!stack.isEmpty()) container.setStack(slot, stack.copy());
            }
            container.markDirty();
        }
        return true;
    }

    private synchronized List<SimpleInventory> getOrCreatePlan(ServerWorld serverWorld) {
        if (this.plannedInventories != null) return this.plannedInventories;
        List<SimpleInventory> inventories = new ArrayList<>();
        int preferred = 0;
        for (int index = 0; index < this.chests.size(); index++) {
            ChestSpec chest = this.chests.get(index);
            if (isSmithTable(chest.table)) preferred = index;
            SimpleInventory inventory = new SimpleInventory(27);
            LootTable lootTable = serverWorld.getServer().getLootManager().getTable(chest.table);
            long seed = this.planSeed ^ chest.pos.asLong() * 0x9E3779B97F4A7C15L ^ chest.table.hashCode();
            LootContext context = new LootContext.Builder(serverWorld)
                    .parameter(LootContextParameters.POSITION, chest.pos)
                    .random(seed == 0L ? 1L : seed)
                    .build(LootContextTypes.CHEST);
            lootTable.supplyInventory(inventory, context);
            inventories.add(inventory);
        }
        VillageResourceRules.apply(this.variant, this.hasHay, this.guaranteedGolemIron, inventories, preferred);
        this.plannedInventories = inventories;
        return inventories;
    }
}
